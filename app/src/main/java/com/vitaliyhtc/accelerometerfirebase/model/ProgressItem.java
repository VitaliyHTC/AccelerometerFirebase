package com.vitaliyhtc.accelerometerfirebase.model;

public class ProgressItem {

    private String name;
    private long bytesTransfered;
    private long totalByteCount;

    public ProgressItem() {
    }

    public ProgressItem(String name, long bytesTransfered, long totalByteCount) {
        this.name = name;
        this.bytesTransfered = bytesTransfered;
        this.totalByteCount = totalByteCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBytesTransfered() {
        return bytesTransfered;
    }

    public void setBytesTransfered(long bytesTransfered) {
        this.bytesTransfered = bytesTransfered;
    }

    public long getTotalByteCount() {
        return totalByteCount;
    }

    public void setTotalByteCount(long totalByteCount) {
        this.totalByteCount = totalByteCount;
    }
}
