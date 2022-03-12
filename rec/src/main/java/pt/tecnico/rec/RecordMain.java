package pt.tecnico.rec;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import io.grpc.BindableService;

import java.io.IOException;

public class RecordMain {

	private static String zookeeperAddress;
	private static String portZookeeper;
	private static String address;
	private static String port;
	private static String path;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(RecordMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 4) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zookeeper_address zookeeper_port address port path %n", RecordMain.class.getName());
			return;
		}

		zookeeperAddress = args[0];
		portZookeeper = String.valueOf(args[1]);
		address = args[2];
		port = String.valueOf(args[3]);
		path = String.valueOf(args[4]);

		ZKNaming zkNaming = null;
		try {
			zkNaming = new ZKNaming(zookeeperAddress, portZookeeper);
			zkNaming.rebind(path, address, port);

			final BindableService impl = new RecordServiceImpl();

			// Create a new server to listen on port.
			Server server = ServerBuilder.forPort(Integer.valueOf(port)).addService(impl).build();
			// Start the server.
			server.start();
			// Server threads are running in the background.
			System.out.println("Server started");

			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();
		} catch (ZKNamingException e) {
			System.out.println("Erro a registar no serviço de nomes: " + e.getMessage());
		} finally  {
			try {
				if (zkNaming != null) {
					// remove
					zkNaming.unbind(path, address, port);
				} 
			} catch (ZKNamingException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
}
