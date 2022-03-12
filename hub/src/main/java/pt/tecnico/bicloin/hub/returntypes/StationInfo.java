package pt.tecnico.bicloin.hub.returntypes;

import com.google.type.LatLng;
import com.google.type.Money;

public class StationInfo {
    private String name;
    private LatLng coordinates;
    private int docks;
    private Money prize;
    private int availableBikes;
    private int pickups;
    private int deliveries;

    public StationInfo(String name, LatLng coordinates, int docks, Money prize, int availableBikes, int pickups, int deliveries) {
        this.name = name;
        this.coordinates = coordinates;
        this.docks = docks;
        this.prize = prize;
        this.availableBikes = availableBikes;
        this.pickups = pickups;
        this.deliveries = deliveries;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LatLng getCoordinates() {
        return this.coordinates;
    }

    public void setCoordinates(LatLng coordinates) {
        this.coordinates = coordinates;
    }

    public int getDocks() {
        return this.docks;
    }

    public void setDocks(int docks) {
        this.docks = docks;
    }

    public Money getPrize() {
        return this.prize;
    }

    public void setPrize(Money prize) {
        this.prize = prize;
    }

    public int getAvailableBikes() {
        return this.availableBikes;
    }

    public void setAvailableBikes(int availableBikes) {
        this.availableBikes = availableBikes;
    }

    public int getPickups() {
        return this.pickups;
    }

    public void setPickups(int pickups) {
        this.pickups = pickups;
    }

    public int getDeliveries() {
        return this.deliveries;
    }

    public void setDeliveries(int deliveries) {
        this.deliveries = deliveries;
    }
}
