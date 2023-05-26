package org.example;

public class Status {
    private long station_id;
    private long s_no;
    private String battery_status;
    private long status_timestamp;
    private Weather weather;

    public Status() {
    }

    public Status(long station_id, long s_no, String battery_status, long status_timestamp, Weather weather) {
        this.station_id = station_id;
        this.s_no = s_no;
        this.battery_status = battery_status;
        this.status_timestamp = status_timestamp;
        this.weather = weather;
    }

    public long getStation_id() {
        return this.station_id;
    }

    public void setStation_id(long station_id) {
        this.station_id = station_id;
    }

    public long getS_no() {
        return this.s_no;
    }

    public void setS_no(long s_no) {
        this.s_no = s_no;
    }

    public String getBattery_status() {
        return this.battery_status;
    }

    public void setBattery_status(String battery_status) {
        this.battery_status = battery_status;
    }

    public long getStatus_timestamp() {
        return this.status_timestamp;
    }

    public void setStatus_timestamp(long status_timestamp) {
        this.status_timestamp = status_timestamp;
    }

    public Weather getWeather() {
        return this.weather;
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public Status station_id(long station_id) {
        setStation_id(station_id);
        return this;
    }

    public Status s_no(long s_no) {
        setS_no(s_no);
        return this;
    }

    public Status battery_status(String battery_status) {
        setBattery_status(battery_status);
        return this;
    }

    public Status status_timestamp(long status_timestamp) {
        setStatus_timestamp(status_timestamp);
        return this;
    }

    public Status weather(Weather weather) {
        setWeather(weather);
        return this;
    }

    @Override
    public String toString() {
        return "{" +
            "\"station_id\":" + getStation_id() + "," +
            "\"s_no\":" + getS_no() + "," +
            "\"battery_status\":\"" + getBattery_status() + "\"," +
            "\"status_timestamp\":" + getStatus_timestamp() + "," +
            "\"weather\":" + getWeather().toString() +
            "}";
    }

}
