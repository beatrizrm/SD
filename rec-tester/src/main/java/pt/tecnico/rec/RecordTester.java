package pt.tecnico.rec;

import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import io.grpc.StatusRuntimeException;


public class RecordTester {
	
	public static void main(String[] args) {
		System.out.println(RecordTester.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", RecordTester.class.getName());
			return;
		}

        final String zooHost = args[0];
		final int zooPort = Integer.parseInt(args[1]);
		final int cid = Integer.parseInt(args[2]);

        RecordFrontend frontend;
		try {
			frontend = new RecordFrontend(cid, zooHost, zooPort, 1000);
		} catch (ZKNamingException e) {
			System.out.println("Error connecting to record: " + e.getMessage());
			return;
		}
        try {
            System.out.println(frontend.ping(PingRequest.newBuilder().build()));
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
		    e.getStatus().getDescription());
        }
        frontend.close();
    }
		
}
