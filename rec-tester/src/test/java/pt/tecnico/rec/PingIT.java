package pt.tecnico.rec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import pt.tecnico.rec.grpc.*;

public class PingIT extends BaseIT {
    
	@BeforeEach
	public void setUp() {
	}

    @Test
	public void pingOKTest() {
		PingRequest request = PingRequest.newBuilder().build();
		String response = frontend.ping(request);
		assertEquals("\nRec 1: up", response);
	}

    @AfterEach
	public void clear() {
	}
}