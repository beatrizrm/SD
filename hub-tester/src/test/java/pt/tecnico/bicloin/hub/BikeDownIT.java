package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.*;
import com.google.type.Money;
import com.google.type.LatLng;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;



public class BikeDownIT extends BaseIT {
    
    BikeUpRequest bikeUpRequest;
    BikeDownRequest request;
    BikeDownResponse response;
    Money amount = Money.newBuilder().setCurrencyCode("EUR").setUnits(5).build();
    String username = "alice";
    String phoneNumber = "+35191102030";
    LatLng userCoords = LatLng.newBuilder().setLatitude(38.7372).setLongitude(-9.3023).build();
    LatLng userFarCoords = LatLng.newBuilder().setLatitude(38).setLongitude(-9).build();
    LatLng gulbCoords = LatLng.newBuilder().setLatitude(38.7376).setLongitude(-9.1545).build();
    String abrev = "istt";
    int prize = 4;

    
    @Test
	public void BikeDownOKTest() {
        //check if normal bike down works - need to topup and bikeup first
        frontend.topUp(TopUpRequest.newBuilder().setUser(username).setAmount(amount).setPhoneNumber(phoneNumber).build());
		
        bikeUpRequest = BikeUpRequest.newBuilder().setUser(username).setUserCoords(userCoords).setAbrev(abrev).build();
		frontend.bikeUp(bikeUpRequest);
        
        BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUser(username).build();
		BalanceResponse balanceResponse = frontend.balance(balanceRequest);
        Money balanceBefore = balanceResponse.getBalance();
        
        request = BikeDownRequest.newBuilder().setUser(username).setUserCoords(userCoords).setAbrev(abrev).build();
        response = frontend.bikeDown(request);

        Money balanceAfter = frontend.balance(balanceRequest).getBalance();
		
        assertEquals("OK", response.getResponse());
		assertEquals(Money.newBuilder().setCurrencyCode("BIC").setUnits(balanceBefore.getUnits() + prize).build(), balanceAfter);
	}
    

    @Test
    public void BikeDownNotRentingTest() {
        request = BikeDownRequest.newBuilder().setAbrev(abrev).setUser(username).setUserCoords(userCoords).build();
        
        assertEquals(Status.PERMISSION_DENIED.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());
        assertEquals("O utilizador não se encontra numa bicicleta", assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getDescription());
    } 
    
    @Test
    public void BikeDownTooFarTest() {
        frontend.topUp(TopUpRequest.newBuilder().setUser(username).setAmount(amount).setPhoneNumber(phoneNumber).build());
        bikeUpRequest = BikeUpRequest.newBuilder().setUser(username).setUserCoords(userCoords).setAbrev(abrev).build();
		frontend.bikeUp(bikeUpRequest);

        request = BikeDownRequest.newBuilder().setAbrev(abrev).setUser(username).setUserCoords(userFarCoords).build();
        
        assertEquals(Status.PERMISSION_DENIED.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());
        assertEquals("O utilizador está muito longe", assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getDescription());
        
        request = BikeDownRequest.newBuilder().setAbrev(abrev).setUser(username).setUserCoords(userCoords).build();
        frontend.bikeDown(request);
    } 
    
    @Test
    public void BikeDownNoDockAvailableTest() {
        frontend.topUp(TopUpRequest.newBuilder().setUser(username).setAmount(amount).setPhoneNumber(phoneNumber).build());
        bikeUpRequest = BikeUpRequest.newBuilder().setUser(username).setUserCoords(userCoords).setAbrev(abrev).build();
		frontend.bikeUp(bikeUpRequest);

        request = BikeDownRequest.newBuilder().setAbrev("gulb").setUser(username).setUserCoords(gulbCoords).build();
        
        assertEquals(Status.FAILED_PRECONDITION.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getCode());
        assertEquals("Não há docas disponíveis", assertThrows(StatusRuntimeException.class, () -> frontend.bikeDown(request)).getStatus().getDescription());
        
        request = BikeDownRequest.newBuilder().setAbrev(abrev).setUser(username).setUserCoords(userCoords).build();
        frontend.bikeDown(request);
    } 
}
