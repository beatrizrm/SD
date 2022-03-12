package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.*;

public class PingIT extends BaseIT {
    
    @Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().build();
		PingResponse response = frontend.ping(request);
		assertEquals("up", response.getOutputText());
	}

}
