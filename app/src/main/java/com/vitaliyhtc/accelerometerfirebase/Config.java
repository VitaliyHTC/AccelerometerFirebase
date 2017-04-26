package com.vitaliyhtc.accelerometerfirebase;

public abstract class Config {

    public static final String TAG_SERVICE_BROADCAST_RECEIVER = "MainServiceBroadcastReceiver";
    public static final String TAG_ACTIVITY_BROADCAST_RECEIVER = "MainActivityBroadcastReceiver";

    public static final String TAG_SERVICE_RUNNING_STATUS = "MainServiceRunningStatus";

    public static final String FIREBASE_DB_PATH_USERS = "users";
    public static final String FIREBASE_DB_PATH_FILES = "files";
    public static final String FIREBASE_DB_PATH_SESSIONS_ITEM = "sessionsItem";
    public static final String FIREBASE_DB_PATH_COORDINATES = "coordinates";

    public static final String DATE_TIME_FULL_FORMAT = "yy.MM.dd HH:mm:ss.SSS";
    public static final String TIME_FULL_FORMAT = "hh:mm:ss.SSS";

    public static final boolean IS_IN_DEBUG_STATE_MAIN_SERVICE = true;


    private Config() {
        throw new AssertionError();
    }

}
