package pt.tecnico.bicloin.hub;

import pt.tecnico.rec.RecordFrontend;
import pt.tecnico.rec.grpc.*;
import pt.ulisboa.tecnico.sdis.zk.ZKNamingException;
import pt.tecnico.bicloin.hub.exceptions.*;
import pt.tecnico.bicloin.hub.returntypes.StationInfo;

import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;

import com.google.type.LatLng;
import com.google.type.Money;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import io.grpc.StatusRuntimeException;

public class HubBIC {
    HashMap<String, UserObj> users = new HashMap<String, UserObj>();
    HashMap<String, StationObj> stations = new HashMap<String, StationObj>();
    RecordFrontend frontend;

    static Integer numReads = 0;
    static Integer numWrites = 0;
    static ArrayList<Long> durationReads = new ArrayList<Long>();
    static ArrayList<Long> durationWrites = new ArrayList<Long>();

    public static Double standardDeviation (ArrayList<Long> duration) {
        Double mean = duration.stream().mapToDouble(d -> d).average().orElse(0.0);
        Double temp = 0.0;
        
        for (int i = 0; i < duration.size(); i++)   {
            Long val = duration.get(i);
            Double squrDiffToMean = Math.pow(val - mean, 2);
            temp += squrDiffToMean;
        }
        
        double meanOfDiffs = (double) temp / (double) (duration.size());

        return Math.sqrt(meanOfDiffs);
    }

    public static String performanceData() {
        Double avgReadTime = durationReads.stream().mapToDouble(d -> d).average().orElse(0.0);
        Double avgWriteTime = durationWrites.stream().mapToDouble(d -> d).average().orElse(0.0);
        String output = "Reads: " + numReads + "\nWrites: " + numWrites +
                        "\nAvg. read time: " + avgReadTime + "\nAvg. write time: " + avgWriteTime +
                        "\nRead time standard deviation: " + standardDeviation(durationReads) +
                        "\nWrite time standard deviation: " + standardDeviation(durationWrites);
        return output;
    }


    public HubBIC(int cid, String host, int port, int timeout) throws ErrorConnectingException {
        try {
            frontend = new RecordFrontend(cid, host, port, timeout);
        } catch (ZKNamingException e) {
            throw new ErrorConnectingException(e.getMessage());
        }
    }

    class UserObj {
        /* user class to hold all the immutable info about users */
        private String name;
        private String phoneNumber;

        public UserObj(String name, String phoneNumber) {
            this.name = name;
            this.phoneNumber = phoneNumber;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    class StationObj {
        /* station class to hold all the immutable info about stations */
        private String name;
        private int docks;
        private Money prize;
        private LatLng coordinates;

        public StationObj(String name, int docks, Money prize, LatLng coordinates) {
            this.name = name;
            this.docks = docks;
            this.prize = prize;
            this.coordinates = coordinates;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDocks() {
            return docks;
        }

        public void setDocks(int docks) {
            this.docks = docks;
        }

        public Money getPrize() {
            return prize;
        }

        public void setPrize(Money prize) {
            this.prize = prize;
        }

        public LatLng getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(LatLng coordinates) {
            this.coordinates = coordinates;
        }
    }

    public String readFromRec(String id) {
        String output = "";

        numReads++;
        long startTime = System.nanoTime();

        output = frontend.read(ReadRequest.newBuilder().setId(id).build()).getValue();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        durationReads.add(duration);

        return output;
    }

    public void writeToRec(String id, String value) {
        numWrites++;
        long startTime = System.nanoTime();

        frontend.write(WriteRequest.newBuilder().setId(id).setValue(value).build());

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        durationWrites.add(duration);
    }

    public Money moneyBuild(String currencyCode, int amount) {
        /* builds money type from currency code and amount */
        return Money.newBuilder().setCurrencyCode(currencyCode).setUnits(amount).build();
    }

    public String sysStatus() {
        System.out.println("SYS_STATUS");
        System.out.println("  Request received: sys_status");

        String output = "", recResponse;
        
        /* adds its own answer */
        System.out.println("\tCalling hub method: ping");
        output += "\nHub: " + ping();
        
        /* tries to ping rec servers */
        try {
            System.out.println("\tPinging rec server...");
            recResponse = frontend.ping(PingRequest.newBuilder().build());
            output += recResponse;
            System.out.println("\tReceived ping response.");
        } catch(StatusRuntimeException e) {
            output += "\nRec: down";
            System.out.println("\tCouldn't connect to rec server.");
        }

        System.out.println("  Sending response: sys_status");
        System.out.println("------");

        return output;
    }

    public String ping() {
        System.out.println("PING");
        System.out.println("  Request received: ping");
        System.out.println("  Sending response: ping");
        System.out.println("------");

		return "up";
    }

    public Money balance(String username) {
        System.out.println("BALANCE");
        System.out.println("  Request received: balance");
        
        System.out.println("\tReading balance from rec...");
        int currentBalance = Integer.parseInt(readFromRec(username + "_balance"));
        System.out.println("\tReceived balance response from rec: " + currentBalance);
        Money moneyBalance = moneyBuild("BIC", currentBalance);

        System.out.println("  Sending response: balance");
        System.out.println("------");
        
        return moneyBalance;
    }

    public void topUp(String username, Money amount, String phoneNumber) throws WrongPhoneNumberException, AmountNotInEurosException, UserNullException, ValueOutOfRangeTopUpException {
        System.out.println("TOP_UP");
        System.out.println("  Request received: top_up");
        
        if (users.get(username) == null) { 
            throw new UserNullException(username);
        } 

        synchronized(users.get(username)) {
            /* thrown exceptions */
            
            if (!phoneNumber.equals(users.get(username).getPhoneNumber())) { 
                throw new WrongPhoneNumberException(username, phoneNumber);
            } 
            
            if (!amount.getCurrencyCode().equals("EUR")) {
                throw new AmountNotInEurosException();
            }
            
            if (amount.getUnits() > 20 || amount.getUnits() < 1) {
                throw new ValueOutOfRangeTopUpException(amount.getUnits());
            }

            /* add amount to current balance */
            System.out.println("\tCalling hub method: balance");
            String bicAmount = String.valueOf(amount.getUnits()*10 + balance(username).getUnits());
            System.out.println("\tWriting new balance to rec...");
            writeToRec(username + "_balance", bicAmount);
            System.out.println("\tFinished writing to rec.");

            System.out.println("  Sending response: top_up");
            System.out.println("------");
        }
    }

    public String bikeUp(String username, LatLng location, String abrev) throws NoMoneyException, AlreadyRentingException, TooFarException, NoBikeAvailableException, UserNullException {
        System.out.println("BIKE_UP");
        System.out.println("  Request received: bike_up");

        if (users.get(username) == null) { 
            throw new UserNullException(username);
        } 
        synchronized(stations.get(abrev)) {
            synchronized(users.get(username)) {
                /* thrown exceptions */
                if (Boolean.parseBoolean(readFromRec(username + "_usingBike"))) {
                    throw new AlreadyRentingException(username);
                }
                if (balance(username).getUnits() < 10) {
                    throw new NoMoneyException(username);
                }
                if (calculateDistanceM(location, stations.get(abrev).getCoordinates()) >= 200) {
                    throw new TooFarException(username, stations.get(abrev).getName());
                }
                if (Integer.parseInt(readFromRec(abrev + "_bikes")) >= getStation(abrev).getDocks()) {
                    throw new NoBikeAvailableException(stations.get(abrev).getName());
                }
                
                /* subtract balance */
                System.out.println("\tCalling hub method: balance");
                String newBalance = String.valueOf(-10 + balance(username).getUnits());
                System.out.println("\tWriting new data to rec...");
                writeToRec(username + "_balance", newBalance);

                /* set usingBike as true */
                writeToRec(username + "_usingBike", "true");

                /* increment used bikes */
                int bikesUsed = usedBikes(abrev);
                writeToRec(abrev + "_bikes", String.valueOf(bikesUsed+1));
                
                /* increment pickups */
                int pickups = bikePickups(abrev);
                writeToRec(abrev + "_pickups", String.valueOf(pickups+1));
                System.out.println("\tFinished writing to rec.");
                
                System.out.println("  Sending response: bike_up");
                System.out.println("------");
                
                return "OK";
            }
        }
    }

    public String bikeDown(String username, LatLng location, String abrev) throws NotRentingException, TooFarException, NoDockAvailableException, UserNullException {
        System.out.println("BIKE_DOWN");
        System.out.println("  Request received: bike_down");
        if (users.get(username) == null) { 
            throw new UserNullException(username);
        } 
        synchronized(stations.get(abrev)) {
            synchronized(users.get(username)) {
                /* thrown exceptions */
                if (!Boolean.parseBoolean(readFromRec(username + "_usingBike"))) {
                    throw new NotRentingException(username);
                }
                if (calculateDistanceM(location, stations.get(abrev).getCoordinates()) >= 200) {
                    throw new TooFarException(username, stations.get(abrev).getName());
                }
                if (usedBikes(abrev) <= 0) {
                    throw new NoDockAvailableException(getStation(abrev).getName());
                }
                
                /* add prize */
                System.out.println("\tCalling hub method: balance");
                String newBalance = String.valueOf(getStation(abrev).getPrize().getUnits() + balance(username).getUnits());
                System.out.println("\tWriting new data to rec...");
                writeToRec(username + "_balance", newBalance);

                /* set usingBike as false */
                writeToRec(username + "_usingBike", "false");

                /* decrement used bikes */
                int bikesUsed = usedBikes(abrev);
                writeToRec(abrev + "_bikes", String.valueOf(bikesUsed-1));        
                
                /* increment deliveries */
                int deliveries = bikeDeliveries(abrev);
                writeToRec(abrev + "_deliveries", String.valueOf(deliveries+1));
                System.out.println("\tFinished writing to rec.");

                System.out.println("  Sending response: bike_down");
                System.out.println("------");

                return "OK";
            }
        }
    }

    public StationObj getStation(String abrev) {
        return stations.get(abrev);
    }
    
    public int usedBikes(String abrev) {
        /* returns amount of bikes that aren't at the station */
        System.out.println("\tReading used bikes from rec...");
        return Integer.parseInt(readFromRec(abrev + "_bikes"));
    }

    public int availableBikes(String abrev) {
        /* returns amount of bikes that are available at the station */
        return getStation(abrev).getDocks() - usedBikes(abrev);
    }

    public int bikeDeliveries(String abrev) {
        /* returns amount of bikes delivered at that station */
        System.out.println("\tReading bike deliveries from rec...");
        return Integer.parseInt(readFromRec(abrev + "_deliveries"));
    }

    public int bikePickups(String abrev) {
        /* returns amount of bikes picked up from that station */
        System.out.println("\tReading bike pickups from rec...");
        return Integer.parseInt(readFromRec(abrev + "_pickups"));
    }

    public StationInfo infoStation(String abrev) throws StationDoesNotExistException {
        System.out.println("INFO_STATION");
        System.out.println("  Request received: info_station");

        StationObj station = stations.get(abrev);
        if (station == null) {
            throw new StationDoesNotExistException(abrev);
        }
        StationInfo info = new StationInfo(station.getName(), station.getCoordinates(), station.getDocks(), station.getPrize(),
            availableBikes(abrev), bikePickups(abrev), bikeDeliveries(abrev));

        System.out.println("  Sending response: info_station");
        System.out.println("------");

        return info;
    }
   
    public List<String> locateStation(LatLng userCoords, int stationsToShow) {
        /* create a list to calculate stations distances */
        List<Pair<String, Double>> distances = new ArrayList<>(stations.size());
        
        /* calculate every distance */
        for (Map.Entry<String, StationObj> entry : stations.entrySet()) {
            Double distance = calculateDistanceM(userCoords, entry.getValue().getCoordinates());
            distances.add(new ImmutablePair<>(entry.getKey(), distance));
        }

        /* sort it by distance (ascending) */
        Collections.sort(distances, Comparator.comparing(d -> d.getRight()));

        /* only show available stations, even if input is bigger than the number of stations */
        int listSize = Math.min(stationsToShow, distances.size());

        /* create a new list with the abreviations of the n closest stations */
        List<String> abrevs = new ArrayList<String>(listSize);
        for (int i = 0; i < listSize; i++) {
            abrevs.add(distances.get(i).getLeft());
        }
        
        return abrevs;
    }

    public void initData(String dataFileUsers, String dataFileStations) throws FileNotFoundException, WrongFormatCSVException {
        System.out.println("Initializing data from CSV files...");

        /* resets rec */
        System.out.println("\tResetting rec data...");
        writeToRec("reset", "");

        System.out.println("\tWriting users data...");
        /* opens a scanner from csv users file */
        File dataFile;
        dataFile = new File("../csv/" + dataFileUsers);
        Scanner scanner = new Scanner(dataFile);
        
        String line;
        String[] tokens;
        String username, name, phoneNumber;

        /* scans user file line by line, splitting by "," */
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            tokens = line.strip().split(",");
            
            /* if a line is missing data */
            if (tokens.length != 3) {
                scanner.close();
                throw new WrongFormatCSVException();
            }

            username = tokens[0];
            name = tokens[1];
            phoneNumber = tokens[2];

            /* thrown exceptions (bad data) */
            if (username.length() < 3 || username.length() > 10) {
                scanner.close();
                throw new WrongFormatCSVException("O identificador do utilizador deve ter entre 3 e 10 caracteres.");
            }
            else if (name.length() < 3 || name.length() > 30) {
                scanner.close();
                throw new WrongFormatCSVException("O nome do utilizador deve ter entre 3 e 30 caracteres.");
            }
            else if (phoneNumber.length() > 16 || !phoneNumber.matches("\\+[0-9]+")) {
                scanner.close();
                throw new WrongFormatCSVException("O número de telemóvel deve começar com \"+\" seguido do código de país, e deve conter apenas dígitos.");
            }
            
            /* create user objects from data */
            users.put(username, new UserObj(name, phoneNumber));
            writeToRec(username + "_usingBike", "false");
        }

        /* close users file scanner */
        scanner.close();

        System.out.println("\tWriting stations data...");
        /* open new scanner from stations file */
        dataFile = new File("../csv/" + dataFileStations);
        scanner = new Scanner(dataFile);

        String abrev;
        int docks, bikes;
        Money prize;
        LatLng coords;
        double lat, lng;
        
        /* scans stations file line by line, splitting by "," */
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            tokens = line.strip().split(",");
            
            /* if a line is missing data */
            if (tokens.length != 7) {
                scanner.close();
                throw new WrongFormatCSVException();
            }
            
            name = tokens[0];
            abrev = tokens[1];

            /* thrown exceptions (bad data) */
            try {
                lat = Double.valueOf(tokens[2]);
            } catch (IllegalArgumentException e) {
                scanner.close();
                throw new WrongFormatCSVException("Argumento com tipo incorreto: latitude devia ser um número decimal." );
            }
            try {
                lng = Double.valueOf(tokens[3]);
            } catch (IllegalArgumentException e) {
                scanner.close();
                throw new WrongFormatCSVException("Argumento com tipo incorreto: longitude devia ser um número decimal." );
            }
                coords = LatLng.newBuilder().setLatitude(lat).setLongitude(lng).build();
            try {
                docks = Integer.valueOf(tokens[4]);
            } catch (IllegalArgumentException e) {
                scanner.close();
                throw new WrongFormatCSVException("Argumento com tipo incorreto: o número de docas devia ser um número inteiro." );
            }
            try {
                bikes = Integer.valueOf(tokens[5]);
            } catch (IllegalArgumentException e) {
                scanner.close();
                throw new WrongFormatCSVException("Argumento com tipo incorreto: o número de bicicletas devia ser um número inteiro." );
            }
            try {
                prize = Money.newBuilder().setUnits(Integer.parseInt(tokens[6])).setCurrencyCode("BIC").build();
            } catch (IllegalArgumentException e) {
                scanner.close();
                throw new WrongFormatCSVException("Argumento com tipo incorreto: o prémio devia ser um número inteiro." );
            }
            
            
            if (abrev.length() != 4) {
                scanner.close();
                throw new WrongFormatCSVException("O identificador da estação deve ter exatamente 4 caracteres.");
            }
            else if (lat < -90 || lat > 90) {
                scanner.close();
                throw new WrongFormatCSVException("A latitude deve ser um valor entre -90 e 90.");
            }
            else if (lng < -180 || lng > 180) {
                scanner.close();
                throw new WrongFormatCSVException("A longitude deve ser um valor entre -180 e 180.");
            }
            else if (docks < 0) {
                scanner.close();
                throw new WrongFormatCSVException("O número de docas deve ser sempre positivo.");
            }
            else if (bikes < 0) {
                scanner.close();
                throw new WrongFormatCSVException("O número de bicicletas disponíveis deve ser sempre positivo.");
            }
            else if (prize.getUnits() < 0) {
                scanner.close();
                throw new WrongFormatCSVException("O pŕemio deve ser sempre positivo.");
            }
            
            /* create station objects from data */
            stations.put(abrev, new StationObj(name, docks, prize, coords));
            writeToRec(abrev + "_bikes", String.valueOf(docks-bikes));

        }
        durationWrites.clear();
        numWrites = 0;
        /* close stations file scanner */
        scanner.close();
        System.out.println(" Finished initializing data.");
        System.out.println("------");
    }

    private double calculateDistanceM(LatLng loc1, LatLng loc2) {
        /* haversine formula */
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