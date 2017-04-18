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

        if (totalByteCount != that.totalByteCount) return false;
        if (filename != null ? !filename.equals(that.filename) : that.filename != null)
            return false;
        if (filepath != null ? !filepath.equals(that.filepath) : that.filepath != null)
            return false;
        if (downloadUrl != null ? !downloadUrl.equals(that.downloadUrl) : that.downloadUrl != null)
            return false;
        return mimeType != null ? mimeType.equals(that.mimeType) : that.mimeType == null;

    }

    @Override
    public int hashCode() {
        int result = filename != null ? filename.hashCode() : 0;
        result = 31 * result + (filepath != null ? filepath.hashCode() : 0);
        result = 31 * result + (downloadUrl != null ? downloadUrl.hashCode() : 0);
        result = 31 * result + (int) (totalByteCount ^ (totalByteCount >>> 32));
        result = 31 * result + (mimeType != null ? mimeType.hashCode() : 0);
        return result;
    }
}
