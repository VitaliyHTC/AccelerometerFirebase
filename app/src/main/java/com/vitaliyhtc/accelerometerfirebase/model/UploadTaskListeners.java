package com.vitaliyhtc.accelerometerfirebase.model;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;

public class UploadTaskListeners {

    private OnFailureListener onFailureListener;
    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener;
    private OnPausedListener<UploadTask.TaskSnapshot> onPausedListener;
    private OnProgressListener<UploadTask.TaskSnapshot> onProgressListener;

    public UploadTaskListeners() {
    }

    public UploadTaskListeners(
            OnFailureListener onFailureListener,
            OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener,
            OnPausedListener<UploadTask.TaskSnapshot> onPausedListener,
            OnProgressListener<UploadTask.TaskSnapshot> onProgressListener) {
        this.onFailureListener = onFailureListener;
        this.onSuccessListener = onSuccessListener;
        this.onPausedListener = onPausedListener;
        this.onProgressListener = onProgressListener;
    }

    public OnFailureListener getOnFailureListener() {
        return onFailureListener;
    }

    public void setOnFailureListener(OnFailureListener onFailureListener) {
        this.onFailureListener = onFailureListener;
    }

    public OnSuccessListener<UploadTask.TaskSnapshot> getOnSuccessListener() {
        return onSuccessListener;
    }

    public void setOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
    }

    public OnPausedListener<UploadTask.TaskSnapshot> getOnPausedListener() {
        return onPausedListener;
    }

    public void setOnPausedListener(OnPausedListener<UploadTask.TaskSnapshot> onPausedListener) {
        this.onPausedListener = onPausedListener;
    }

    public OnProgressListener<UploadTask.TaskSnapshot> getOnProgressListener() {
        return onProgressListener;
    }

    public void setOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> onProgressListener) {
        this.onProgressListener = onProgressListener;
    }
}
