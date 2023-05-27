package org.example;

import java.util.Random;

import com.google.gson.Gson;

public class MessageGenerator {
  long stationID;
  long sequenceNumber;
  Random batteryStatusRandom = new Random();
  Random weatherRandom = new Random();
  
  public MessageGenerator(long stationID, long startingSequenceNumber) {
    this.stationID = stationID;
    this.sequenceNumber = startingSequenceNumber;
  }

  public String getNextMessage () {
    String batterStatus;
    int batteryPercentage = batteryStatusRandom.nextInt(101);
    if (batteryPercentage < 30) {
      batterStatus = "low";
    } else if (batteryPercentage < 70) {
      batterStatus = "medium";
    } else {
      batterStatus = "high";
    }
    Status status = new Status(stationID, sequenceNumber, batterStatus, System.currentTimeMillis(), generateRandomWeather());
    sequenceNumber++;
    return status.toString();
  }

  private Weather generateRandomWeather() {
    Weather weather = new Weather();
    weather.setHumidity(weatherRandom.nextInt(100) + 1);
    weather.setTemperature(weatherRandom.nextInt(150 - (-150) + 1) + (-150));
    weather.setWind_speed(weatherRandom.nextInt(100) + 1);
    return weather;
  }
}
