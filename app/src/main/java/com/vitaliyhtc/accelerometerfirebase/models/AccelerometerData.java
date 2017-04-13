package com.vitaliyhtc.accelerometerfirebase.models;

public class AccelerometerData {
    private long timeStamp;
    private float x;
    private float y;
    private float z;


    public AccelerometerData() {
    }

    public AccelerometerData(long timeStamp, float x, float y, float z) {
        this.timeStamp = timeStamp;
        this.x = x;
        this.y = y;
        this.z = z;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }
}
