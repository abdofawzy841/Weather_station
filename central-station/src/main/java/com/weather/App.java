package com.weather;

import java.io.File;

import com.weather.bitcask.Bitcask;

public class App {
    private static int numOfStations;
    public static void main(String[] args) {
        numOfStations = 10;
        createFolders();
        System.out.println("Application Started");
        Consumer statusConsumer = new Consumer("weather-topic");
        BWriter[] writers = new BWriter[numOfStations + 1];
        for(int i=1; i<=numOfStations; i++){
            writers[i] = new BWriter(i);
        }
        Bitcask bitcask = new Bitcask("bitcaskDB", 30);
        while(true){
            for(Status status: statusConsumer.poll()){
                writers[(int)status.getStation_id()].write(status);
                bitcask.addRecord(status.getStation_id(), status.toString());
            }
        }
    }
    private static void createFolders(){
        for(int i=1; i<=numOfStations; i++){
            File folder = new File("../data/station_" + i);
            System.out.print("Creating folder for station: " + i + " ... ");
            if (!folder.exists()) {
                if (folder.mkdir())
                    System.out.println(" created");
                else
                    System.out.println("Failed to create it!");
            }
            else
                System.out.println(" already exists");
        }
    }
}
