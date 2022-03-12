package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.*;
import com.google.type.Money;
import com.google.type.LatLng;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;



public class BikeUpIT extends BaseIT {
    
    BikeUpRequest request;
    BikeUpResponse response;
    BikeDownRequest bikeDownRequest;
    BikeDownResponse bikeDownResponse;
    Money amount = Money.newBuilder().setCurrencyCode("EUR").setUnits(5).build();
    String username = "alice";
    String noMoneyUsername = "bruno";
    String phoneNumber = "+35191102030";
    LatLng userCoords = LatLng.newBuilder().setLatitude(38.7372).setLongitude(-9.3023).build();
    LatLng userFarCoords = LatLng.newBuilder().setLatitude(38).setLongitude(-9).build();
    LatLng cateCoords = LatLng.newBuilder().setLatitude(38.7097).setLongitude(-9.1336).build();
    String abrev = "istt";
    
    @Test
	public void BikeUpOKTest() {
        //check if bikeUp is working and decrementing the money - needs topUp first and bikeDown after
        frontend.topUp(TopUpRequest.newBuilder().setUser(username).setAmount(amount).setPhoneNumber(phoneNumber).build());
        
        BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUser(username).build();
		BalanceResponse balanceResponse = frontend.balance(balanceRequest);
        Money balanceBefore = balanceResponse.getBalance();

		request = BikeUpRequest.newBuilder().setUser(username).setUserCoords(userCoords).setAbrev(abrev).build();
		response = frontend.bikeUp(request);
        
        Money balanceAfter = frontend.balance(balanceRequest).getBalance();
		
        assertEquals("OK", response.getResponse());
		assertEquals(Money.newBuilder().setCurrencyCode("BIC").setUnits(balanceBefore.getUnits() - 10).build(), balanceAfter);
        
        bikeDownRequest = BikeDownRequest.newBuilder().setUser(username).setUserCoords(userCoords).setAbrev(abrev).build();
        bikeDownResponse = frontend.bikeDown(bikeDownRequest);
	}
    
    @Test
    public void BikeUpNoMoneyTest() {
        request = BikeUpRequest.newBuilder().setAbrev(abrev).setUser(noMoneyUsername).setUserCoords(userCoords).build();
        
        assertEquals(Status.PERMISSION_DENIED.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
        assertEquals("O utilizador não tem saldo suficiente", assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getDescription());
    } 

    @Test
    public void BikeUpAlreadyRentingTest() {
        request = BikeUpRequest.newBuilder().setAbrev(abrev).setUser(username).setUserCoords(userCoords).build();
        frontend.bikeUp(request);
        
        assertEquals(Status.PERMISSION_DENIED.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
        assertEquals("O utilizador já se encontra numa bicicleta", assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getDescription());
        
        bikeDownRequest = BikeDownRequest.newBuilder().setUser(username).setUserCoords(userCoords).setAbrev(abrev).build();
        bikeDownResponse = frontend.bikeDown(bikeDownRequest);
    } 
    
    @Test
    public void BikeUpTooFarTest() {
        request = BikeUpRequest.newBuilder().setAbrev(abrev).setUser(username).setUserCoords(userFarCoords).build();
        
        assertEquals(Status.PERMISSION_DENIED.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
        assertEquals("O utilizador está muito longe", assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getDescription());
    } 
    
    @Test
    public void BikeUpNoBikeAvailableTest() {
        //needs to have money because of exception priority
        frontend.topUp(TopUpRequest.newBuilder().setUser(username).setAmount(amount).setPhoneNumber(phoneNumber).build());
        
        request = BikeUpRequest.newBuilder().setAbrev("cate").setUser(username).setUserCoords(cateCoords).build();
        
        assertEquals(Status.FAILED_PRECONDITION.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getCode());
        assertEquals("Não há bicicletas disponíveis", assertThrows(StatusRuntimeException.class, () -> frontend.bikeUp(request)).getStatus().getDescription());
    } 
}
