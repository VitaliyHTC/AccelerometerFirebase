package com.vitaliyhtc.accelerometerfirebase.models;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class FileInfoOnDevice {

    private static final String[] PROJECTION = {
            MediaStore.MediaColumns.DATA,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE
    };

    private String pathToTheFile;
    private long size;
    private String fileName;
    private String mimeType;

    public FileInfoOnDevice() {
    }

    public FileInfoOnDevice(String pathToTheFile, long size, String fileName, String mimeType) {
        this.pathToTheFile = pathToTheFile;
        this.size = size;
        this.fileName = fileName;
        this.mimeType = mimeType;
    }

    public static FileInfoOnDevice getFileInfo(Context ctxt, Uri uri) {
        FileInfoOnDevice fileInfoOnDevice = new FileInfoOnDevice();
        Cursor cursor = ctxt.getContentResolver().query(uri, PROJECTION, null, null, null);
        if (cursor == null) {
            //fileInfoOnDevice.setPathToTheFile(uri.getPath());
            return null;
        } else {
            cursor.moveToFirst();
            fileInfoOnDevice.setFromCursor(cursor);
            cursor.close();
        }
        return fileInfoOnDevice;
    }

    public String getPathToTheFile() {
        return pathToTheFile;
    }

    public void setPathToTheFile(String pathToTheFile) {
        this.pathToTheFile = pathToTheFile;
        setFileNameFromPath(pathToTheFile);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFromCursor(Cursor cursor) {
        setPathToTheFile(cursor.getString(cursor.getColumnIndexOrThrow(PROJECTION[0])));
        setSize(cursor.getLong(cursor.getColumnIndexOrThrow(PROJECTION[1])));
        setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(PROJECTION[2])));
    }

    private void setFileNameFromPath(String path) {
        if (path.contains("/")) {
            fileName = path.substring(path.lastIndexOf("/") + 1);
        } else {
            fileName = path;
        }
    }
}
