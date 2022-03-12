package pt.tecnico.bicloin.hub;

import pt.tecnico.bicloin.hub.HubBIC.StationObj;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.tecnico.bicloin.hub.returntypes.StationInfo;
import pt.tecnico.bicloin.hub.exceptions.*;

import io.grpc.stub.StreamObserver;

import java.io.FileNotFoundException;
import java.util.List;

import io.grpc.Context;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

public class HubServiceImpl extends HubServiceGrpc.HubServiceImplBase {

	private HubBIC hub;
	String dataFileUsers;
	String dataFileStations;
	boolean initRec;

	public HubServiceImpl(int instance, String zooAddr, int zooPort, int timeout, String dataFileUsers, String dataFileStations, boolean initRec) throws ErrorConnectingException, FileNotFoundException, WrongFormatCSVException {
		hub = new HubBIC(instance, zooAddr, zooPort, timeout);
		this.dataFileUsers = dataFileUsers;
		this.dataFileStations = dataFileStations;
		this.initRec = initRec;

		if (initRec) {
			writeDataToRec();
		}
	}

	public void writeDataToRec() throws FileNotFoundException, WrongFormatCSVException, StatusRuntimeException {
		hub.initData(dataFileUsers, dataFileStations);
	}


    @Override
	public void balance(BalanceRequest request, StreamObserver<BalanceResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		try {
			BalanceResponse response = BalanceResponse.newBuilder().
			setBalance(hub.balance(request.getUser())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (StatusRuntimeException e) {
			responseObserver.onError(e.getStatus().asRuntimeException());
		}
	}

	@Override
	public void topUp(TopUpRequest request, StreamObserver<TopUpResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		try {
			hub.topUp(request.getUser(), request.getAmount(), request.getPhoneNumber());
			TopUpResponse response = TopUpResponse.newBuilder().setBalance(hub.balance(request.getUser())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
			
		}
		catch (WrongPhoneNumberException e) {
			responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Número de telemóvel errado").asRuntimeException());
			printError(e.getMessage());
		}
		catch (UserNullException e) {
			responseObserver.onError(Status.NOT_FOUND.withDescription("Utilizador não existe").asRuntimeException());
			printError(e.getMessage());
		}
		catch (AmountNotInEurosException e) {
			responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("O montante deve ser em euros").asRuntimeException());
			printError(e.getMessage());
		}
		catch (ValueOutOfRangeTopUpException e) {
			responseObserver.onError(Status.OUT_OF_RANGE.withDescription("O montante deve estar entre 1 e 20").asRuntimeException());
			printError(e.getMessage());
		}
		catch (StatusRuntimeException e) {
			responseObserver.onError(e.getStatus().asRuntimeException());
		}
	}

    @Override
	public void infoStation(InfoStationRequest request, StreamObserver<InfoStationResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		try {
			StationInfo info = hub.infoStation(request.getAbrev());
			InfoStationResponse response = InfoStationResponse.newBuilder()
					.setName(info.getName())
					.setStationCoords(info.getCoordinates())
					.setDockCapacity(info.getDocks())
					.setPrize(info.getPrize())
					.setAvailableBikes(info.getAvailableBikes())
					.setPickups(info.getPickups())
					.setDeliveries(info.getDeliveries())
					.build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		} catch (StationDoesNotExistException e) {
			responseObserver.onError(Status.NOT_FOUND.withDescription("Estação não encontrada").asRuntimeException());
			printError(e.getMessage());
		} catch (StatusRuntimeException e) {
			responseObserver.onError(e.getStatus().asRuntimeException());
		}
	}

	@Override
	public void locateStation(LocateStationRequest request, StreamObserver<LocateStationResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		List<String> abrevs = hub.locateStation(request.getUserCoords(), request.getKStations());
		LocateStationResponse response = LocateStationResponse.newBuilder().addAllAbrev(abrevs).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

	@Override
	public void bikeUp(BikeUpRequest request, StreamObserver<BikeUpResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		try {
			BikeUpResponse response = BikeUpResponse.newBuilder().setResponse(hub.bikeUp(request.getUser(), request.getUserCoords(), request.getAbrev())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (AlreadyRentingException e) {
			responseObserver.onError(Status.PERMISSION_DENIED.withDescription("O utilizador já se encontra numa bicicleta").asRuntimeException());
			printError(e.getMessage());
		}
		catch (NoMoneyException e) {
			responseObserver.onError(Status.PERMISSION_DENIED.withDescription("O utilizador não tem saldo suficiente").asRuntimeException());
			printError(e.getMessage());
		}
		catch (UserNullException e) {
			responseObserver.onError(Status.NOT_FOUND.withDescription("Utilizador não existe").asRuntimeException());
			printError(e.getMessage());
		}
		catch (TooFarException e) {
			responseObserver.onError(Status.PERMISSION_DENIED.withDescription("O utilizador está muito longe").asRuntimeException());
			printError(e.getMessage());
		}
		catch (NoBikeAvailableException e) {
			responseObserver.onError(Status.FAILED_PRECONDITION.withDescription("Não há bicicletas disponíveis").asRuntimeException());
			printError(e.getMessage());
		}
		catch (StatusRuntimeException e) {
			responseObserver.onError(e.getStatus().asRuntimeException());
		}
	}

    @Override
	public void bikeDown(BikeDownRequest request, StreamObserver<BikeDownResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		try {
			BikeDownResponse response = BikeDownResponse.newBuilder().setResponse(hub.bikeDown(request.getUser(), request.getUserCoords(), request.getAbrev())).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}
		catch (NotRentingException e) {
			responseObserver.onError(Status.PERMISSION_DENIED.withDescription("O utilizador não se encontra numa bicicleta").asRuntimeException());
			printError(e.getMessage());
		}
		catch (UserNullException e) {
			responseObserver.onError(Status.NOT_FOUND.withDescription("Utilizador não existe").asRuntimeException());
			printError(e.getMessage());
		}
		catch (TooFarException e) {
			responseObserver.onError(Status.PERMISSION_DENIED.withDescription("O utilizador está muito longe").asRuntimeException());
			printError(e.getMessage());
		}
		catch (NoDockAvailableException e) {
			responseObserver.onError(Status.FAILED_PRECONDITION.withDescription("Não há docas disponíveis").asRuntimeException());
			printError(e.getMessage());
		}
		catch (StatusRuntimeException e) {
			responseObserver.onError(e.getStatus().asRuntimeException());
		}
	}

	@Override
	public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		PingResponse response = PingResponse.newBuilder().
		setOutputText(hub.ping()).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();
	}

    @Override
	public void sysStatus(SysStatusRequest request, StreamObserver<SysStatusResponse> responseObserver) {
		if (Context.current().isCancelled()) {
			responseObserver.onError(Status.CANCELLED.withDescription("Cancelado pelo cliente").asRuntimeException());
			return;
		}
		SysStatusResponse response = SysStatusResponse.newBuilder().
		setOutputText(hub.sysStatus()).build();
		responseObserver.onNext(response);
		responseObserver.onCompleted();	
	}

	private void printError(String details) { System.out.println("ERRO " + details); }

} 