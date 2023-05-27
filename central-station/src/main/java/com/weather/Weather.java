package com.weather;

import java.io.Serializable;

public class Weather implements Serializable {
    private int humidity;
    private int temperature;
    private int wind_speed;

    public Weather() {
    }

    public Weather(int humidity, int temperature, int wind_speed) {
        this.humidity = humidity;
        this.temperature = temperature;
        this.wind_speed = wind_speed;
    }

    public int getHumidity() {
        return this.humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public int getTemperature() {
        return this.temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getWind_speed() {
        return this.wind_speed;
    }

    public void setWind_speed(int wind_speed) {
        this.wind_speed = wind_speed;
    }

    @Override
    public String toString() {
        return "{" +
                "\"humidity\":" + humidity +
                ", \"temperature\":" + temperature +
                ", \"wind_speed\":" + wind_speed +
                '}';
    }
}
