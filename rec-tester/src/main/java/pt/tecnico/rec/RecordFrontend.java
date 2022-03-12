package pt.tecnico.rec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;



public class RecordFrontend implements AutoCloseable {
	private final int cid;
	private final String basePath = "/grpc/bicloin/rec";
	private final ZKNaming zkNaming;
	private final int timeoutMs;
	private int numReplicas;
	private float readQuorum;
	private float writeQuorum;
	private List<Replica> replicas;

	public RecordFrontend(int cid, String zooHost, int zooPort, int timeout) throws ZKNamingException {
		// Set client id
		this.cid = cid;

		zkNaming = new ZKNaming(zooHost, String.valueOf(zooPort));
		Collection<ZKRecord> records = zkNaming.listRecords(basePath);
		numReplicas = records.size();
		replicas = new ArrayList<>(numReplicas);
		
		
		if (numReplicas < 3) {
			readQuorum = (float) numReplicas / 2;
			writeQuorum = (float) numReplicas / 2;
		}
		else {
			readQuorum = (float) numReplicas / 2 - 1;
			writeQuorum = (float) numReplicas / 2 + 1;
		}

		// Create stubs
		for (ZKRecord record : records) {
			String target = record.getURI();
			System.out.println("Found replica at " + target);
			String path = record.getPath();
			replicas.add(new Replica(target, path));
		}

		// Set message deadline
		timeoutMs = timeout;
	}

	public String ping(PingRequest request) {
		Object lock = new Object();

		synchronized(lock) {
			ResponseCollector<PingResponse> collector = new ResponseCollector<>();

			// Ping all replicas
			for (Replica replica : replicas) {
				pingReplica(replica, request, 2, collector, lock);
			}
			// Wait for responses from all replicas
			while (collector.getNumResponses() < numReplicas) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			collector.sortByInstance();
			String output = "";
			for (Response<PingResponse> response  : collector.getResponses()) {
				output += "\nRec " + response.getInstance() + ": " + response.getContent().getOutputText();
			}
			
			return output;
		}
	}

	public ReadResponse read(ReadRequest request) {
		Object lock = new Object();
		
		synchronized(lock) {
			ResponseCollector<ReadResponse> collector = new ResponseCollector<>();

			// Read from all replicas
			Context ctx = Context.current().fork();
			ctx.run(() -> {
				for (Replica replica : replicas) {
					readReplica(replica, request, 2, collector, lock);
				}
			});
			// Wait for responses from a quorum
			while (collector.getNumResponses() < readQuorum) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			return collector.getQuorumResponse();
		}
	}

	public WriteResponse write(WriteRequest baseRequest) {
		// Add metadata to request
		WriteRequest request = baseRequest.toBuilder().setTag(calcNewTag()).build();

		Object lock = new Object();
		
		synchronized(lock) {
			ResponseCollector<WriteResponse> collector = new ResponseCollector<>();

			// Write to all replicas
			Context ctx = Context.current().fork();
			ctx.run(() -> {
				for (Replica replica : replicas) {
					writeReplica(replica, request, 2, collector, lock);
				}
			});

			// Wait for responses from all replicas
			while (collector.getNumResponses() < writeQuorum) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			// All responses are the same ("OK"), so we can just return the first one
			return collector.getResponses().get(0).getContent();
		}
	}

	private void pingReplica(Replica replica, PingRequest request, int tries, ResponseCollector<PingResponse> collector, Object lock) {
		if (tries == 0) {	
			System.out.println("\t\tReplica " + replica.getInstance() + " exhausted number of retries.");
			collector.addResponse(replica.getInstance(), PingResponse.newBuilder().setOutputText("down").build());
			return;	
		}
		System.out.println("\t\tPinging replica " + replica.getInstance() + " at " + replica.getTarget() + "...");
		replica.getStub().withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).ping(request, new StreamObserver<PingResponse>() {
			@Override
			public void onNext(PingResponse r) {
				System.out.println("\t\tReceived ping response from replica " + replica.getInstance() + ": " + r.getOutputText());
				collector.addResponse(replica.getInstance(), r);
			}
			@Override
			public void onError(Throwable throwable) {
				System.out.println("\t\tReceived ping error from replica " + replica.getInstance() + ": " + Status.fromThrowable(throwable).getCode());
				// Check if replica's address changed, update it and retry
				updateReplicaAddress(replica);
				System.out.println("\t\tRetrying...");
				pingReplica(replica, request, tries-1, collector, lock);
				synchronized(lock) {
					lock.notifyAll();
				}
			}
			@Override
			public void onCompleted() {
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		});
	}

	private void readReplica(Replica replica, ReadRequest request, int tries, ResponseCollector<ReadResponse> collector, Object lock) {
		if (tries == 0) {	
			System.out.println("\t\tReplica " + replica.getInstance() + " exhausted number of retries.");
			return;	
		}
		System.out.println("\t\tReading from replica " + replica.getInstance() + " at " + replica.getTarget() + "...");
		replica.getStub().withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).read(request, new StreamObserver<ReadResponse>() {
			@Override
			public void onNext(ReadResponse r) {
				if (r.getValue().equals("")) {
					System.out.println("\t\tReceived tag from replica " + replica.getInstance()
						+ ": <" + r.getTag().getSeq() + ", " + r.getTag().getCid() + ">");
				}
				else {
					System.out.println("\t\tReceived read response from replica " + replica.getInstance() + ": " + r.getValue()
					+ " (tag <" + r.getTag().getSeq() + ", " + r.getTag().getCid() + ">)");
				}
				collector.addResponse(replica.getInstance(), r, r.getTag().getSeq(), r.getTag().getCid());
			}
			@Override
			public void onError(Throwable throwable) {
				System.out.println("\t\tReceived read error from replica " + replica.getInstance() + ": " + Status.fromThrowable(throwable).getCode());
				// Check if replica's address changed, update it and retry
				updateReplicaAddress(replica);
				System.out.println("\t\tRetrying...");
				readReplica(replica, request, tries-1, collector, lock);
				synchronized(lock) {
					lock.notifyAll();
				}
			}
			@Override
			public void onCompleted() {
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		});
	}

	private void writeReplica(Replica replica, WriteRequest request, int tries, ResponseCollector<WriteResponse> collector, Object lock) {
		if (tries == 0) {	
			System.out.println("\t\tReplica " + replica.getInstance() + " exhausted number of retries.");
			return;	
		}
		System.out.println("\t\tWriting to replica " + replica.getInstance() + " at " + replica.getTarget()
			+ " (tag <" + request.getTag().getSeq() + ", " + request.getTag().getCid() + ">)...");
		replica.getStub().withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).write(request, new StreamObserver<WriteResponse>(){
			@Override
			public void onNext(WriteResponse r) {
				System.out.println("\t\tReceived write ack from replica " + replica.getInstance() + ". ");
				collector.addResponse(replica.getInstance(), r);
			}
			@Override
			public void onError(Throwable throwable) {
				System.out.println("\t\tReceived write error from replica " + replica.getInstance() + ": " + Status.fromThrowable(throwable).getCode());
				// Check if replica's address changed, update it and retry
				updateReplicaAddress(replica);
				System.out.println("\t\tRetrying...");
				writeReplica(replica, request, tries-1, collector, lock);
				synchronized(lock) { 
					lock.notifyAll();
				}
			}
			@Override
			public void onCompleted() {
				synchronized(lock) {
					lock.notifyAll();
				}
			}
		});
	}

	private Tag calcNewTag() {
		// Read max tag from rec
		ReadRequest request = ReadRequest.newBuilder().setId("tag").build();
		ReadResponse response = read(request);

		// Return new tag
		int seq = response.getTag().getSeq() + 1;
		return Tag.newBuilder().setCid(cid).setSeq(seq).build();
	}

	private void updateReplicaAddress(Replica replica) {
		// Check if replica's address changed
		try {
			ZKRecord record = zkNaming.lookup(basePath + "/" + replica.getInstance());
			if (!replica.getTarget().equals(record.getURI())) {
				// Change target and retry
				replica.changeTarget(record.getURI());
				System.out.println("\t\tReplica " + replica.getInstance() + " at new target " + replica.getTarget());
			}
		} catch (ZKNamingException e) {
			// Replica is down, don't do anything
		}
	}

	@Override
	public final void close() {
		for (Replica replica : replicas) {
			replica.closeChannel();
		}
	}
}