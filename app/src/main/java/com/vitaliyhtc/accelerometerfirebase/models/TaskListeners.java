package com.vitaliyhtc.accelerometerfirebase.models;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;

public class TaskListeners<T> {

    private OnFailureListener onFailureListener;
    private OnSuccessListener<T> onSuccessListener;
    private OnPausedListener<T> onPausedListener;
    private OnProgressListener<T> onProgressListener;

    public TaskListeners() {
    }

    public TaskListeners(
            OnFailureListener onFailureListener,
            OnSuccessListener<T> onSuccessListener,
            OnPausedListener<T> onPausedListener,
            OnProgressListener<T> onProgressListener) {
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

    public OnSuccessListener<T> getOnSuccessListener() {
        return onSuccessListener;
    }

    public void setOnSuccessListener(OnSuccessListener<T> onSuccessListener) {
        this.onSuccessListener = onSuccessListener;
    }

    public OnPausedListener<T> getOnPausedListener() {
        return onPausedListener;
    }

    public void setOnPausedListener(OnPausedListener<T> onPausedListener) {
        this.onPausedListener = onPausedListener;
    }

    public OnProgressListener<T> getOnProgressListener() {
        return onProgressListener;
    }

    public void setOnProgressListener(OnProgressListener<T> onProgressListener) {
        this.onProgressListener = onProgressListener;
    }
}
