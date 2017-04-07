package com.vitaliyhtc.accelerometerfirebase;

public abstract class Config {

    public static final String TAG_SERVICE_BROADCAST_RECEIVER = "MainServiceBroadcastReceiver";
    public static final String TAG_ACTIVITY_BROADCAST_RECEIVER = "MainActivityBroadcastReceiver";

    public static final String TAG_SERVICE_RUNNING_STATUS = "MainServiceRunningStatus";

    public static final String FIREBASE_DB_PATH_USERS = "users";
    public static final String FIREBASE_DB_PATH_SESSIONS_ITEM = "sessionsItem";



    private Config() {
        throw new AssertionError();
    }
}
