package com.vitaliyhtc.accelerometerfirebase.model;

import java.util.ArrayList;
import java.util.List;

public class SessionItem {
    private int interval;
    private long startTime;
    private long stopTime;
    private Device deviceInfo;
    private List<AccelerometerData> coordinates = new ArrayList<>();



    public SessionItem() {}

    public SessionItem(int interval, long startTime, long stopTime, Device deviceInfo, List<AccelerometerData> coordinates) {
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

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public Device getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(Device deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public List<AccelerometerData> getCoordinates() {
        return coordinates;
    }

    public void addCoordinate(AccelerometerData coordinate){
        coordinates.add(coordinate);
    }

    public void setCoordinates(List<AccelerometerData> coordinates) {
        this.coordinates = coordinates;
    }
}
