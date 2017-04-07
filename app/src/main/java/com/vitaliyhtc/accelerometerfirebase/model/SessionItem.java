package com.vitaliyhtc.accelerometerfirebase.model;

import java.util.ArrayList;
import java.util.List;

public class SessionItem {
    private int interval;
    private String startTime;
    private String stopTime;
    private String deviceInfo;
    private List<AccelerometerData> coordinates = new ArrayList<>();



    public SessionItem() {}

    public SessionItem(int interval, String startTime, String stopTime, String deviceInfo, List<AccelerometerData> coordinates) {
        this.interval = interval;
        this.startTime = startTime;
        this.stopTime = stopTime;
        this.deviceInfo = deviceInfo;
        this.coordinates = coordinates;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<AccelerometerData> getCoordinates() {
        return coordinates;
    }

    public void addCoordinate(AccelerometerData coordinate){
        coordinates.add(coordinate);
    }
    /*
    public void setCoordinates(List<AccelerometerData> coordinates) {
        this.coordinates = coordinates;
    }
    */
}
