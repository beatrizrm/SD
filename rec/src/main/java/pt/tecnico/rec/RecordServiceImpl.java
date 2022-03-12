package pt.tecnico.rec;

import pt.tecnico.rec.grpc.*;

import io.grpc.stub.StreamObserver;


public class RecordServiceImpl extends RecordServiceGrpc.RecordServiceImplBase {

	private RecordBIC rec = new RecordBIC();

    @Override
	public void read(ReadRequest request, StreamObserver<ReadResponse> responseObserver) {
		String valueRead = rec.read(request.getId());
		RecTag tag = rec.getTag();
		ReadResponse response = ReadResponse.newBuilder()
			.setValue(valueRead)
			.setTag(Tag.newBuilder().setSeq(tag.getSeq()).setCid(tag.getCid()))
			.build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

    @Override
	public void write(WriteRequest request, StreamObserver<WriteResponse> responseObserver) {
		RecTag tag = new RecTag(request.getTag().getSeq(), request.getTag().getCid());
		String result = rec.write(request.getId(), request.getValue(), tag);
		WriteResponse response = WriteResponse.newBuilder().
		setResponse(result).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		PingResponse response = PingResponse.newBuilder().
		setOutputText(rec.ping()).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}
}