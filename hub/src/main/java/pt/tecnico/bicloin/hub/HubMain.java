package pt.tecnico.bicloin.hub;

import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;

import java.io.FileNotFoundException;
import java.io.IOException;

import pt.tecnico.bicloin.hub.exceptions.ErrorConnectingException;
import pt.tecnico.bicloin.hub.exceptions.WrongFormatCSVException;
import pt.ulisboa.tecnico.sdis.zk.ZKNaming;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

import java.util.Scanner;



public class HubMain {

	private static String zookeeperAddress;
	private static int portZookeeper;
	private static String hubAddress;
	private static int portHub;
	private static String path;
	private static String dataFileUsers;
	private static String dataFileStations;
	private static boolean initRec;
	
	public static void main(String[] args) throws IOException, InterruptedException {
		System.out.println(HubMain.class.getSimpleName());
		
		// receive and print arguments
		System.out.printf("Received %d arguments%n", args.length);
		for (int i = 0; i < args.length; i++) {
			System.out.printf("arg[%d] = %s%n", i, args[i]);
		}

		// check arguments
		if (args.length < 7) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s zookeeper_address zookeeper_port hub_address hub_port path users_datafile stations_datafile init_rec (optional) %n", HubMain.class.getName());
			return;
		}

		try {
			zookeeperAddress = args[0];
			portZookeeper = Integer.valueOf(args[1]);
			hubAddress = args[2];
			portHub = Integer.valueOf(args[3]);
			path = args[4];
			dataFileUsers = args[5];
			dataFileStations = args[6];
		} catch (NumberFormatException e) {
			System.out.println("Tipo de argumento inválido: " + e.getMessage());
			return;
		}

		if (args.length > 7) {
			initRec = args[7].equals("initRec") ? true : false;
		}
		
		int timeout = 1000;
		ZKNaming zkNaming = null;
		int instance = Integer.valueOf(path.substring(path.lastIndexOf("/") + 1));

		try {
			zkNaming = new ZKNaming(zookeeperAddress, String.valueOf(portZookeeper));
			zkNaming.rebind(path, hubAddress, String.valueOf(portHub));
			
			final BindableService impl = new HubServiceImpl(instance, zookeeperAddress, portZookeeper, timeout, dataFileUsers, dataFileStations, initRec);
			
			// Create a new server to listen on port.
			Server server = ServerBuilder.forPort(portHub).addService(impl).build();
			
			// Start the server.
			server.start();
			System.out.println("Server started");
			
			Scanner scanner = new Scanner(System.in);
			String input;
			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				if (input.equals("performance"))
					System.out.println(HubBIC.performanceData());
			}			
			
			// Server threads are running in the background.
	
			// Do not exit the main thread. Wait until server is terminated.
			server.awaitTermination();
			scanner.close();
		} catch (ErrorConnectingException e) {
			System.out.println(e.getMessage());
		} catch (ZKNamingException e) {
			System.out.println("Erro a registar no serviço de nomes: " + e.getMessage());
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (WrongFormatCSVException e) {
			System.out.println(e.getMessage());
		} catch (StatusRuntimeException e) {
			System.out.println(e.getMessage() + " " + e.getCause().getMessage());
		}
		finally {
			try {
				if (zkNaming != null) {
					// remove
					zkNaming.unbind(path, hubAddress, String.valueOf(portHub));
				} 
			} catch (ZKNamingException e) {
				System.out.println(e.getMessage());
			}
		}
	}
	
}
