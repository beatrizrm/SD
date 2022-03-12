package pt.tecnico.bicloin.app.responses;

import pt.tecnico.bicloin.hub.grpc.InfoStationResponse;

public class ScanResponse {
    private String abrev;
    private InfoStationResponse stationInfo;
    private double distance;

    public ScanResponse(String abrev, InfoStationResponse stationInfo, double distance) {
        this.abrev = abrev;
        this.stationInfo = stationInfo;
        this.distance = distance;
    }

    public String getAbrev() {
        return abrev;
    }

    public InfoStationResponse getStationInfo() {
        return stationInfo;
    }

    public double getDistance() {
        return distance;
    }

    public void setAbrev(String abrev) {
        this.abrev = abrev;
    }

    public void setStationInfo(InfoStationResponse stationInfo) {
        this.stationInfo = stationInfo;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
