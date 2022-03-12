package pt.tecnico.bicloin.app;

import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class AppMain {
	private static App app;
	private static AppCLI cli;
	
	public static void main(String[] args) {
		System.out.println(AppMain.class.getSimpleName());

		if (args.length != 6) {
			System.out.println("Número de argumentos incorreto.");
			System.out.println("Utilização: app <zookeeper_address> <zookeeper_port> <user_id> <phone_number> <latitude> <longitude>");
			return;
		}

		String zooAddr;
		int zooPort;
		String userId;
		String phoneNumber;
		double latitude;
		double longitude;
		int timeout = 3000;

		try {
			zooAddr = args[0];
			zooPort = Integer.valueOf(args[1]);
			userId = args[2];
			phoneNumber = args[3];
			latitude = Double.valueOf(args[4]);
			longitude = Double.valueOf(args[5]);
		} catch (NumberFormatException e) {
			System.out.println("Tipo de argumento inválido: " + e.getMessage());
			return;
		}

		if (userId.length() < 3 || userId.length() > 10) {
			System.out.println("O identificador do utilizador deve ter entre 3 e 10 caracteres.");
			return;
		}
		if (phoneNumber.length() > 16 || !phoneNumber.matches("\\+[0-9]+")) {
			System.out.println("O número de telemóvel deve começar com \"+\" seguido do código de país, e deve conter apenas dígitos.");
			return;
		}

		try {
			app = new App(zooAddr, zooPort, timeout, userId, phoneNumber, latitude, longitude);
		} catch (ZKNamingException e) {
			System.out.println("Não foi possível contactar o servidor.");
			return;
		}
		
		cli = new AppCLI(app);
		cli.startCLI();
	}
}
