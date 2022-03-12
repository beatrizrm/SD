package pt.tecnico.bicloin.hub;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pt.tecnico.bicloin.hub.grpc.*;

public class SysStatusIT extends BaseIT {

    @Test
	public void sysStatusOKTest() {
		SysStatusRequest request = SysStatusRequest.newBuilder().build();
		SysStatusResponse response = frontend.sysStatus(request);
		assertEquals("\nHub: up\nRec 1: up", response.getOutputText());
	}
}
