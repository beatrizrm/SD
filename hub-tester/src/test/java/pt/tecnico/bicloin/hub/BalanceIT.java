package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.*;
import com.google.type.Money;


public class BalanceIT extends BaseIT {
    
	String user = "alice";
	String phoneNumber = "+35191102030";
	BalanceRequest request;
	BalanceResponse response;
	Money balanceBefore;
	Money amount = Money.newBuilder().setCurrencyCode("EUR").setUnits(15).build();

    @BeforeEach
	public void setUp() {
		//saves the balance before each test
		request = BalanceRequest.newBuilder().setUser(user).build();
		response = frontend.balance(request);
        balanceBefore = response.getBalance();
	}

    @Test
	public void BalanceOKTest() {
		//checks for correct structure of Money in balance response
		assertEquals(Money.newBuilder().setCurrencyCode("BIC").setUnits(balanceBefore.getUnits()).build(), response.getBalance());
	}
    
	@Test
	public void BalanceAfterTopUpOKTest() {
		//checks if balance after topUp is the same as the balance before the topUp plus the amount
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUser(user).setAmount(amount).setPhoneNumber(phoneNumber).build();
		TopUpResponse topUpResponse = frontend.topUp(topUpRequest);
		assertEquals(Money.newBuilder().setCurrencyCode("BIC").setUnits(balanceBefore.getUnits()+amount.getUnits()*10).build(), topUpResponse.getBalance());
	
	}
}
