package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import io.grpc.StatusRuntimeException;

public class HubTester {
    public static void main(String[] args) {
		System.out.println(HubTester.class.getSimpleName());

		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 2) {
			System.out.println("Argument(s) missing!");
			System.out.printf("Usage: java %s host port%n", HubTester.class.getName());
			return;
		}

        final String host = args[0];
		final int port = Integer.parseInt(args[1]);

		HubFrontend frontend;
        try {
			frontend = new HubFrontend(host, port, 3000);
		} catch (ZKNamingException e) {
			System.out.println("Error connecting to hub: " + e.getMessage());
			return;
		}

        try {
			System.out.println(frontend.ping(PingRequest.newBuilder().build()).getOutputText());
        } catch (StatusRuntimeException e) {
            System.out.println("Caught exception with description: " +
		    e.getStatus().getDescription());
        }
        frontend.close();
    }
}