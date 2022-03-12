package pt.tecnico.bicloin.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.type.LatLng;
import com.google.type.Money;

import pt.tecnico.bicloin.app.exceptions.ValueOutOfRangeException;
import pt.tecnico.bicloin.app.exceptions.NoSuchTagException;
import pt.tecnico.bicloin.app.responses.ScanResponse;
import pt.tecnico.bicloin.hub.HubFrontend;
import pt.tecnico.bicloin.hub.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;

public class App {
    private HubFrontend hub;
    private UserInfo user;
    private HashMap<String, LatLng> tags;

    class UserInfo {
        private String id;
        private String phoneNumber;
        private LatLng location;

        private UserInfo(String id, String phoneNumber, double lat, double lng) {
            this.id = id;
            this.phoneNumber = phoneNumber;
            this.location = LatLng.newBuilder().setLatitude(lat).setLongitude(lng).build();
        }
        public String getId() { return id; }
        public String getPhoneNumber() { return phoneNumber; }
        public LatLng getLocation() { return location; }
        private void setLocation(LatLng location) { this.location = location; }
    }

    public App(String host, int port, int timeout, String userId, String phoneNumber, double lat, double lng) throws ZKNamingException {
        this.hub = new HubFrontend(host, port, timeout);
        this.user = new UserInfo(userId, phoneNumber, lat, lng);
        this.tags = new HashMap<String, LatLng>();
    }

    public void close() {
        hub.close();
    }

    public BalanceResponse balance() {
        BalanceResponse response = hub.balance(BalanceRequest.newBuilder().setUser(user.getId()).build());
        return response;
    }

    public TopUpResponse topUp(int amount) throws ValueOutOfRangeException {
        if (amount < 1 || amount > 20) {
            throw new ValueOutOfRangeException("Carregamento", 1, 20);
        }
		Money money = Money.newBuilder().setUnits(amount).setCurrencyCode("EUR").build();
        TopUpResponse response = hub.topUp(TopUpRequest.newBuilder()
                                            .setUser(user.getId())
                                            .setPhoneNumber(user.getPhoneNumber())
                                            .setAmount(money)
                                            .build());
        return response;
    }

    public void tag(double lat, double lng, String tagName) throws ValueOutOfRangeException {
        if (lat < -90 || lat > 90) {
            throw new ValueOutOfRangeException("Latitude", -90, 90);
        }
        else if (lng < -180 || lng > 180) {
            throw new ValueOutOfRangeException("Longitude", -180, 180);
        }
        LatLng coordinates = LatLng.newBuilder().setLatitude(lat).setLongitude(lng).build();
        addTag(tagName, coordinates);
    }

    public LatLng move(String tagName) throws NoSuchTagException {
        LatLng newLocation = tags.get(tagName);
        if (newLocation == null) {
            throw new NoSuchTagException(tagName);
        }
        user.setLocation(newLocation);
        return newLocation;
    }

    public LatLng move(double lat, double lng) {
        LatLng newLocation = LatLng.newBuilder().setLatitude(lat).setLongitude(lng).build();
        user.setLocation(newLocation);
        return newLocation;
    }

    public LatLng at() {
        return user.getLocation();
    }

    public List<ScanResponse> scan(int k) { 
        List<String> abrevs = hub.locateStation(LocateStationRequest.newBuilder()
                                                .setUserCoords(user.getLocation())
                                                .setKStations(k)
                                                .build())
                                                .getAbrevList();
        List<ScanResponse> responses = new ArrayList<ScanResponse>();
        for (String abrev : abrevs) {
            InfoStationResponse response = hub.infoStation(InfoStationRequest.newBuilder().setAbrev(abrev).build());
            double distance = calculateDistanceM(user.getLocation(), response.getStationCoords());
            responses.add(new ScanResponse(abrev, response, distance));
        }
        return responses;
    }

    public InfoStationResponse info(String abrev) {
        InfoStationResponse response = hub.infoStation(InfoStationRequest.newBuilder().setAbrev(abrev).build());
        return response;
    }

    public BikeUpResponse bikeUp(String abrev) {
        BikeUpResponse response = hub.bikeUp(BikeUpRequest.newBuilder()
                                                .setUser(user.getId())
                                                .setUserCoords(user.getLocation())
                                                .setAbrev(abrev)
                                                .build());
        return response;
    }

    public BikeDownResponse bikeDown(String abrev) {
        BikeDownResponse response = hub.bikeDown(BikeDownRequest.newBuilder()
                                            .setUser(user.getId())
                                            .setUserCoords(user.getLocation())
                                            .setAbrev(abrev)
                                            .build());
        return response;
    }

    public PingResponse ping() {
        PingResponse response = hub.ping(PingRequest.newBuilder().build());
        return response;
    }

    public SysStatusResponse sysStatus() {
        SysStatusResponse response = hub.sysStatus(SysStatusRequest.newBuilder().build());
        return response;
    }

    public UserInfo getUserInfo() { 
        return user;
    }

    private void addTag(String tagName, LatLng coordinates) {
        tags.put(tagName, coordinates);
    }

    private double calculateDistanceM(LatLng loc1, LatLng loc2) {
        double earthRadius = 6371;
        double lat1 = Math.toRadians(loc1.getLatitude());
        double lat2 = Math.toRadians(loc2.getLatitude());
        double lng1 = Math.toRadians(loc1.getLongitude());
        double lng2 = Math.toRadians(loc2.getLongitude());

        double hav1 = Math.pow(Math.sin((lat2-lat1)/2), 2);
        double hav2 = Math.pow(Math.sin((lng2-lng1)/2), 2);

        return 2 * earthRadius * Math.sqrt(hav1 + Math.cos(lat1)*Math.cos(lat2)*hav2) * 1000;
    }
}
