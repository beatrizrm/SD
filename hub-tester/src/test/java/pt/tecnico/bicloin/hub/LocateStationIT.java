package pt.tecnico.bicloin.hub;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.type.LatLng;

import pt.tecnico.bicloin.hub.grpc.*;

public class LocateStationIT extends BaseIT {
    
	LatLng userCoords = LatLng.newBuilder().setLatitude(38.7380).setLongitude(-9.3000).build();
	int k = 12;
	LocateStationRequest request;
	LocateStationResponse response;

    @Test
	public void LocateStationOKTest() {
		//check if the stations located are correct and in order
        request = LocateStationRequest.newBuilder()
        .setUserCoords(userCoords) 
        .setKStations(k)
        .build();
        List<String> abrevs = frontend.locateStation(request).getAbrevList();
		assertEquals("istt", abrevs.get(0));
		assertEquals("stao", abrevs.get(1));
		assertEquals("jero", abrevs.get(2));
		assertEquals("gulb", abrevs.get(3));
		assertEquals("cais", abrevs.get(4));
		assertEquals("ista", abrevs.get(5));
		assertEquals("prcm", abrevs.get(6));
		assertEquals("cate", abrevs.get(7));
		assertEquals("ocea", abrevs.get(8));
		
	}

}
