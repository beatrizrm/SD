package pt.tecnico.bicloin.hub;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.ulisboa.tecnico.sdis.zk.ZKRecord;


public class HubFrontend implements AutoCloseable {
    private final ManagedChannel channel;
	private final HubServiceGrpc.HubServiceBlockingStub stub;
	private final String basePath = "/grpc/bicloin/hub";
	private final int timeoutMs;

	public HubFrontend(String zooHost, int zooPort, int timeout) throws ZKNamingException {
		ZKNaming zkNaming = new ZKNaming(zooHost, String.valueOf(zooPort));

		ZKRecord hub = getHub(zkNaming, basePath);
		String target = hub.getURI();
		this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();

		// Create a blocking stub.
		stub = HubServiceGrpc.newBlockingStub(channel);

		// Set message deadline.
		timeoutMs = timeout;
	}

	public PingResponse ping(PingRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).ping(request);
	}

	public TopUpResponse topUp(TopUpRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).topUp(request);
	}

	public BalanceResponse balance(BalanceRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).balance(request);
	}
	public InfoStationResponse infoStation(InfoStationRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).infoStation(request);
	}
	public LocateStationResponse locateStation(LocateStationRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).locateStation(request);
	}
	public BikeUpResponse bikeUp(BikeUpRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).bikeUp(request);
	}
	public BikeDownResponse bikeDown(BikeDownRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).bikeDown(request);
	}
	public SysStatusResponse sysStatus(SysStatusRequest request) {
		return stub.withDeadlineAfter(timeoutMs, TimeUnit.MILLISECONDS).sysStatus(request);
	}

	@Override
	public final void close() {
		channel.shutdown();
	}

	private ZKRecord getHub(ZKNaming zkNaming, String path) throws ZKNamingException {
		Collection<ZKRecord> hubs = zkNaming.listRecords(path);
		// Returning the first hub since we only have one for now
		return hubs.iterator().next();
	}
}
