package com.vitaliyhtc.accelerometerfirebase.model;

public class FileInfoOnStorage {

    private String filename;
    private String filepath;
    private String downloadUrl;
    private long totalByteCount;
    private String mimeType;

    public FileInfoOnStorage() {
    }

    public FileInfoOnStorage(
            String filename,
            String filepath,
            String downloadUrl,
            long totalByteCount,
            String mimeType) {
        this.filename = filename;
        this.filepath = filepath;
        this.downloadUrl = downloadUrl;
        this.totalByteCount = totalByteCount;
        this.mimeType = mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public long getTotalByteCount() {
        return totalByteCount;
    }

    public void setTotalByteCount(long totalByteCount) {
        this.totalByteCount = totalByteCount;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FileInfoOnStorage)) return false;
        FileInfoOnStorage that = (FileInfoOnStorage) o;
        return filename.equals(that.filename);
    }

    @Override
    public int hashCode() {
        return filename.hashCode();
    }
}
