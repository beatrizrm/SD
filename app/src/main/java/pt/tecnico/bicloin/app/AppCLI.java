package pt.tecnico.bicloin.app;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import com.google.type.LatLng;
import com.google.type.Money;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.bicloin.app.exceptions.ValueOutOfRangeException;
import pt.tecnico.bicloin.app.exceptions.NoSuchTagException;
import pt.tecnico.bicloin.app.responses.ScanResponse;
import pt.tecnico.bicloin.hub.grpc.BalanceResponse;
import pt.tecnico.bicloin.hub.grpc.InfoStationResponse;
import pt.tecnico.bicloin.hub.grpc.SysStatusResponse;
import pt.tecnico.bicloin.hub.grpc.TopUpResponse;

public class AppCLI {
	private App app;

    public AppCLI(App app) {
		this.app = app;
    }

	public void startCLI() {
		try (Scanner scanner = new Scanner(System.in)) {
			String input, command;
			String[] tokens;
			if (System.console() != null) { System.out.printf("> "); }

			while (scanner.hasNextLine()) {
				input = scanner.nextLine();
				tokens = input.strip().split("\\s+");
				command = tokens[0];

				switch(command) {
					case "balance":
						cmdBalance(tokens);
						break;
					case "top-up":
						cmdTopUp(tokens);
						break;
					case "tag":
						cmdTag(tokens);
						break;
					case "move":
						cmdMove(tokens);
						break;
					case "at":
						cmdAt(tokens);
						break;
					case "scan":
						cmdScan(tokens);
						break;
					case "info":
						cmdInfo(tokens);
						break;
					case "bike-up":
						cmdBikeUp(tokens);
						break;
					case "bike-down":
						cmdBikeDown(tokens);
						break;
					case "zzz":
						cmdZzz(tokens);
						break;
					case "ping":
						cmdPing(tokens);
						break;
					case "sys_status":
						cmdSysStatus(tokens);
						break;
					case "help":
						printHelp();
						break;
					case "quit":
					case "q":
						cmdQuit(tokens);
						return;
					default:
						if (!(command.startsWith("#") || command.equals(""))) { printInvalidCmd(); }
				}
				if (System.console() != null) { System.out.printf("> "); }
			}
		}
	}
    
	private void cmdBalance(String[]tokens) {
		if (tokens.length != 1) {
			printInvalidCmd();
			return;
		}
		try {
			BalanceResponse response = app.balance();
			System.out.println(app.getUserInfo().getId() + " " + response.getBalance().getUnits() + " " + response.getBalance().getCurrencyCode());
		} catch(StatusRuntimeException e) {
			printError(e);
		}
    }

    private void cmdTopUp(String[] tokens) {
		if (tokens.length != 2) {
			printInvalidCmd();
			return;
		}
		try {
			int amount = Integer.valueOf(tokens[1]);
			TopUpResponse response = app.topUp(amount);
			System.out.println(app.getUserInfo().getId() + " " + response.getBalance().getUnits() + " " + response.getBalance().getCurrencyCode());
		} catch (NumberFormatException e) {
			printInvalidCmd();
		} catch (ValueOutOfRangeException e) {
			printError(e.getMessage());
		} catch(StatusRuntimeException e) {
			printError(e);
		}
    }

    private void cmdTag(String[] tokens) {
		if (tokens.length != 4) {
			printInvalidCmd();
			return;
		}
		try {
			double latitude = Double.valueOf(tokens[1]);
			double longitude = Double.valueOf(tokens[2]);
			String tagName = tokens[3];
			app.tag(latitude, longitude, tagName);
			printSuccess();
		} catch (NumberFormatException e) {
			printInvalidCmd();
		} catch (ValueOutOfRangeException e) {
			printError(e.getMessage());
		}
    }

	private void cmdMove(String[] tokens) {
		// move <tag>
		if (tokens.length == 2) {
			try {
				String tagName = tokens[1];
				LatLng newLocation = app.move(tagName);
				System.out.println(app.getUserInfo().getId() + " em " + getMapUrl(newLocation));
			} catch (NoSuchTagException e) {
				printError(e.getMessage());
			}
		}
		// move <latitude> <longitude>
		else if (tokens.length == 3) {
			try {
				double latitude = Double.valueOf(tokens[1]);
				double longitude = Double.valueOf(tokens[2]);
				LatLng newLocation = app.move(latitude, longitude);
				System.out.println(app.getUserInfo().getId() + " em " + getMapUrl(newLocation));
			} catch (NumberFormatException e) {
				printInvalidCmd();
			}
		}
		else {
			printInvalidCmd();
		}
    }

    private void cmdAt(String[] tokens) {
		if (tokens.length != 1) {
			printInvalidCmd();
			return;
		}
		LatLng location = app.at();
		System.out.println(app.getUserInfo().getId() + " em " + getMapUrl(location));
    }

    private void cmdScan(String[] tokens) {
		if (tokens.length != 2) {
			printInvalidCmd();
			return;
		}
		try {
			Integer numStations = Integer.valueOf(tokens[1]);
			List<ScanResponse> responses = app.scan(numStations);
			for (ScanResponse response : responses) {
				String abrev = response.getAbrev();
				LatLng coords = response.getStationInfo().getStationCoords();
				int dockCapacity = response.getStationInfo().getDockCapacity();
				Money prize = response.getStationInfo().getPrize();
				int numBikes = response.getStationInfo().getAvailableBikes();
				long distance = Math.round(response.getDistance());
				System.out.printf(String.format(Locale.US, 
					"%s, lat %.4f, %.4f long, %d docas, %d %s prémio, %d bicicletas, a %d metros%n",
					abrev, coords.getLatitude(), coords.getLongitude(), dockCapacity, prize.getUnits(), prize.getCurrencyCode(), numBikes, distance)); 
			}
		} catch (NumberFormatException e) {
			printInvalidCmd();
		} catch(StatusRuntimeException e) {
			printError(e);
		}
    }

    private void cmdInfo(String[] tokens) {
		if (tokens.length != 2) {
			printInvalidCmd();
			return;
		}
		try {
			String abrev = tokens[1];
			InfoStationResponse response = app.info(abrev);
			String name = response.getName();
			LatLng coords = response.getStationCoords();
			int dockCapacity = response.getDockCapacity();
			Money prize = response.getPrize();
			int numBikes = response.getAvailableBikes();
			int pickups = response.getPickups();
			int deliveries = response.getDeliveries();
			System.out.printf(String.format(Locale.US, 
				"%s, lat %.4f, %.4f long, %d docas, %d %s prémio, %d bicicletas, %d levantamentos, %d devoluções, %s%n",
				name, coords.getLatitude(), coords.getLongitude(), dockCapacity, prize.getUnits(), prize.getCurrencyCode(), numBikes, pickups, deliveries, getMapUrl(coords)));
		} catch(StatusRuntimeException e) {
			printError(e);
		}
    }

    private void cmdBikeUp(String[] tokens) {
		if (tokens.length != 2) {
			printInvalidCmd();
			return;
		}
		String abrev = tokens[1];
		try {
			app.bikeUp(abrev);
			printSuccess();
		} catch(StatusRuntimeException e) {
			printError(e);
		}
    }

    private void cmdBikeDown(String[] tokens) {
		if (tokens.length != 2) {
			printInvalidCmd();
			return;
		}
		String abrev = tokens[1];
		try {
			app.bikeDown(abrev);
			printSuccess();
		} catch(StatusRuntimeException e) {
			printError(e);
		}
    }

	private void cmdZzz(String[] tokens) {
		if (tokens.length != 2) {
			printInvalidCmd();
			return;
		}
		try {
			Integer millisec = Integer.valueOf(tokens[1]);
			Thread.sleep(millisec);
		} catch (IllegalArgumentException e) {
			printInvalidCmd();
		} catch (InterruptedException e) {
			printError(e.getMessage());
		}
	}

	private void cmdPing(String[] tokens) {
		if (tokens.length != 1) {
			printInvalidCmd();
			return;
		}
		try {
			System.out.println("Estado do servidor: " + app.ping().getOutputText());
		} catch(StatusRuntimeException e) {
			printError(e);
		}
	}

	private void cmdSysStatus(String[] tokens) {
		if (tokens.length != 1) {
			printInvalidCmd();
			return;
		}
		try {
			SysStatusResponse response = app.sysStatus();
			System.out.println("Estado do sistema: " + response.getOutputText());
		} catch(StatusRuntimeException e) {
			printError(e);
		}
	}

	private void cmdQuit(String[] tokens) {
		if (tokens.length != 1) {
			printInvalidCmd();
			return;
		}
		app.close();
	}

	private String getMapUrl(LatLng coords) {
		return String.format(Locale.US, "https://www.google.com/maps/place/%.4f,%.4f", coords.getLatitude(), coords.getLongitude());
	}

	private void printSuccess() { System.out.println("OK"); }

	private void printError(StatusRuntimeException e) {
		if (e.getStatus().getCode() == Status.DEADLINE_EXCEEDED.getCode()) {
			printError("O servidor excedeu o tempo de resposta");
		}
		else if (e.getStatus().getCode() == Status.CANCELLED.getCode()) {
			printError("O servidor cancelou o pedido");
		}
		else if (e.getStatus().getCode() == Status.UNAVAILABLE.getCode()) {
			printError("Não foi possível contactar o servidor");
		}
		else {
			printError(e.getStatus().getDescription());
		}
	}

	private void printError(String details) {
		if (details != null) {
			System.out.println("ERRO " + details); 
		}
		else {
			System.out.println("ERRO"); 
		}
	}

	private void printInvalidCmd() {
		System.out.println("Comando inválido. Escreva \"help\" para ver uma lista dos comandos disponíveis.");
	}
	
	private void printHelp() {
		String balance = String.format("%-45s%s", "balance", "Mostra o saldo de bicloins%n");
		String topUp = String.format("%-45s%s%s", "top-up <valor>", "Carrega <valor> euros em bicloins.",
			" <valor>: número inteiro entre 1 e 20.%n");
		String tag = String.format("%-45s%s", "tag <latitude> <longitude> <nome>", "Cria uma nova tag.%n");
		String move = String.format("%-45s%s", "move {<nome_tag>|<latitude> <longitude>}", "Desloca para o local especificado.%n");
		String at = String.format("%-45s%s", "at", "Apresenta a localização atual.%n");
		String scan = String.format("%-45s%s", "scan <n>", "Lista as <n> estações mais próximas.%n");
		String info = String.format("%-45s%s", "info <id>", "Lista a informação da estação com identificador <id>.%n");
		String bikeUp = String.format("%-45s%s", "bike-up <id_estação>", "Levanta uma bicicleta da estação indicada.%n");
		String bikeDown = String.format("%-45s%s", "bike-down <id_estação>", "Devolve uma bicicleta na estação indicada.%n");
		String zzz = String.format("%-45s%s%s", "zzz <milisegundos>", "Pausa o processamento pela duração indicada.",
			" <milisegundos>: número inteiro.%n");
		String ping = String.format("%-45s%s", "ping", "Verifica o estado do servidor.%n");
		String sysStatus = String.format("%-45s%s", "sys_status", "Verifica o estado de todo o sistema.%n");
		String quit = String.format("%-45s%s", "{quit|q}", "Encerra a aplicação.%n");
		String help = String.format("%-45s%s", "help", "Mostra esta mensagem.%n");
		System.out.printf("Utilização:%n" + balance + topUp + tag + move + at + scan + info + bikeUp + bikeDown +
			zzz + ping + sysStatus + quit + help);
	}
}
