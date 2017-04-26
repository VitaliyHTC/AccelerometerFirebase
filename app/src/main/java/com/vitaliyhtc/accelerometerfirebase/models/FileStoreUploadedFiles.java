package com.vitaliyhtc.accelerometerfirebase.models;

import java.util.ArrayList;
import java.util.List;

public class FileStoreUploadedFiles {

    private List<FileInfoOnStorage> uploadedFilesInfo;

    public FileStoreUploadedFiles() {
        uploadedFilesInfo = new ArrayList<>();
    }

    public FileStoreUploadedFiles(List<FileInfoOnStorage> uploadedFilesInfo) {
        this.uploadedFilesInfo = uploadedFilesInfo;
    }

    public List<FileInfoOnStorage> getUploadedFilesInfo() {
        return uploadedFilesInfo;
    }

    public void setUploadedFilesInfo(List<FileInfoOnStorage> uploadedFilesInfo) {
        this.uploadedFilesInfo = uploadedFilesInfo;
    }
}
