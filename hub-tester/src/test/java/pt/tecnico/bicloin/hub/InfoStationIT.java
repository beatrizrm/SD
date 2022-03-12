package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static io.grpc.Status.NOT_FOUND;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.*;
import com.google.type.Money;
import com.google.type.LatLng;


public class InfoStationIT extends BaseIT {
    

	//test for this station Sto. Amaro Oeiras,stao,38.6867,-9.3124,30,20,3

	String abrev = "stao";
	String badAbrev = "staoo";
	String name = "Sto. Amaro Oeiras";
	LatLng stationCoords = LatLng.newBuilder().setLatitude(38.6867).setLongitude(-9.3124).build();
	int dockCapacity = 30;
	Money prize = Money.newBuilder().setCurrencyCode("BIC").setUnits(3).build();
	int bikes = 20;
	int pickups = 0;
	int deliveries = 0;
	InfoStationRequest request;
	InfoStationResponse response;

    @Test
	public void InfoStationOKTest() {
		//checks if every field matches the values from the csv file
		request = InfoStationRequest.newBuilder().setAbrev(abrev).build();
		response = frontend.infoStation(request);
		
		assertEquals(name, response.getName());
		assertEquals(stationCoords, response.getStationCoords());
		assertEquals(dockCapacity, response.getDockCapacity());
		assertEquals(prize, response.getPrize());
		assertEquals(bikes, response.getAvailableBikes());
		assertEquals(pickups, response.getPickups());
		assertEquals(deliveries, response.getDeliveries());
	}

	@Test
	public void InfoStationNotFoundTest() {
		request = InfoStationRequest.newBuilder().setAbrev(badAbrev).build();
		assertEquals(NOT_FOUND.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request)).getStatus().getCode());
		assertEquals("Estação não encontrada", assertThrows(StatusRuntimeException.class, () -> frontend.infoStation(request)).getStatus().getDescription());
	}
}
