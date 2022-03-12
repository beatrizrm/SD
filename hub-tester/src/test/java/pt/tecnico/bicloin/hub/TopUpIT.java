package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.*;
import com.google.type.Money;


public class TopUpIT extends BaseIT {
    
	String user = "alice";
	String phoneNumber = "+35191102030";
	Money amount = Money.newBuilder().setCurrencyCode("EUR").setUnits(15).build();
	Money amountOutOfRange = Money.newBuilder().setCurrencyCode("EUR").setUnits(21).build();
	Money amountInUSD = Money.newBuilder().setCurrencyCode("USD").setUnits(15).build();

    @Test
	public void TopUpOKTest() {
		//checks if amount after topup is the same as the balance before plus the topup amount
		BalanceRequest balanceRequest = BalanceRequest.newBuilder().setUser(user).build();
		BalanceResponse balanceResponse = frontend.balance(balanceRequest);
        Money balanceBefore = balanceResponse.getBalance();

		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUser(user).setAmount(amount).setPhoneNumber(phoneNumber).build();
		TopUpResponse topUpResponse = frontend.topUp(topUpRequest);

		assertEquals(Money.newBuilder().setCurrencyCode("BIC").setUnits(balanceBefore.getUnits() + amount.getUnits()*10).build(), topUpResponse.getBalance());
	}
    
	@Test
	public void TopUpWrongPhoneNumberTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUser(user).setAmount(amount).setPhoneNumber(phoneNumber + "1").build();
		assertEquals(Status.INVALID_ARGUMENT.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getCode());
		assertEquals("Número de telemóvel errado", assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getDescription());
	}
	
	@Test
	public void TopUpUserNullTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUser(user + "a").setAmount(amount).setPhoneNumber(phoneNumber).build();
		assertEquals(Status.NOT_FOUND.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getCode());
		assertEquals("Utilizador não existe", assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getDescription());
	}
	
	@Test
	public void TopUpValueOutOfRangeTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUser(user).setAmount(amountOutOfRange).setPhoneNumber(phoneNumber).build();
		assertEquals(Status.OUT_OF_RANGE.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getCode());
		assertEquals("O montante deve estar entre 1 e 20", assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getDescription());
	}
	
	@Test
	public void TopUpNotInEurosTest() {
		TopUpRequest topUpRequest = TopUpRequest.newBuilder().setUser(user).setAmount(amountInUSD).setPhoneNumber(phoneNumber).build();
		assertEquals(Status.INVALID_ARGUMENT.getCode(), assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getCode());
		assertEquals("O montante deve ser em euros", assertThrows(StatusRuntimeException.class, () -> frontend.topUp(topUpRequest)).getStatus().getDescription());
	}
}
