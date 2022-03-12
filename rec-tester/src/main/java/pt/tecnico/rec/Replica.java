package pt.tecnico.rec;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.rec.grpc.RecordServiceGrpc;

public class Replica {
    private final int instance;
    private String target;
    private ManagedChannel channel;
    private RecordServiceGrpc.RecordServiceStub stub;

    public Replica(String target, String path) {
        this.target = target;
        this.instance = Integer.valueOf(path.substring(path.lastIndexOf("/") + 1));
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = RecordServiceGrpc.newStub(channel);
    }

    public int getInstance() {
        return instance;
    }

    public String getTarget() {
        return target;
    }

    public RecordServiceGrpc.RecordServiceStub getStub() {
        return stub;
    }

    public void closeChannel() {
        channel.shutdown();
    }

    public void changeTarget(String target) {
        closeChannel();
        this.target = target;
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
        this.stub = RecordServiceGrpc.newStub(channel);
    }
}