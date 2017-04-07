package com.vitaliyhtc.accelerometerfirebase;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vitaliyhtc.accelerometerfirebase.Utils.TimePreference;
import com.vitaliyhtc.accelerometerfirebase.model.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.model.Device;
import com.vitaliyhtc.accelerometerfirebase.model.SessionItem;
import com.vitaliyhtc.accelerometerfirebase.model.User;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainService extends Service {

    private volatile boolean isRunning;


    private User mUser;
    private Device mDevice;
    private SessionItem mSessionItem;

    private ScheduledExecutorService mScheduleTaskExecutor;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private int mLoggingInterval;
    private boolean isEnabledSessionLengthSetting;
    private String mSessionLength;
    private long mSessionLengthInMillis;

    private long mStopTime;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("onStartCommand()", "start service");

        // do work
        performBroadcastReceiverRegistration();
        readSettings();
        initData();
        isRunning = true;
        initFirebase();
        if(isRunning){
            makeWork();
        }

        Log.e("onStartCommand()", "return START_NOT_STICKY");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        //release resources here.
    }

    private void performBroadcastReceiverRegistration(){
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isRunning = intent.getExtras().getBoolean(Config.TAG_SERVICE_RUNNING_STATUS, false);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Config.TAG_SERVICE_BROADCAST_RECEIVER));
    }



    private void readSettings(){
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Resources res = getResources();

        // Default values same as in res/xml/settings.xml
        mLoggingInterval = Integer.parseInt(prefs.getString(res.getString(R.string.config_pref_key_LoggingInterval), "1"));
        isEnabledSessionLengthSetting = prefs.getBoolean(res.getString(R.string.config_pref_key_enable_session_length_setting), true);
        mSessionLength = prefs.getString(res.getString(R.string.config_pref_key_SessionLength), "00:03");
        mSessionLengthInMillis = (TimePreference.getHour(mSessionLength)*60+TimePreference.getMinute(mSessionLength))*60*1000;
    }

    private void initData(){
        mUser = new User();
        mDevice = new Device();
        mSessionItem = new SessionItem();
    }

    private void initFirebase(){
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, nothing to do.
            isRunning = false;
        } else {
            mUser.setUserUid(mFirebaseUser.getUid());
            mUser.setUserName(mFirebaseUser.getDisplayName());
            mUser.setUserEmail(mFirebaseUser.getEmail());
            mUser.setUserPhotoUrl(mFirebaseUser.getPhotoUrl().toString());

            mDevice.setBoard(Build.BOARD);
            mDevice.setBootloader(Build.BOARD);
            mDevice.setBrand(Build.BRAND);
            mDevice.setDevice(Build.DEVICE);
            mDevice.setHardware(Build.HARDWARE);
            mDevice.setManufacturer(Build.MANUFACTURER);
            mDevice.setModel(Build.MODEL);
            mDevice.setProduct(Build.PRODUCT);
            mDevice.setSdkInt(Build.VERSION.SDK_INT);

            mSessionItem.setInterval(mLoggingInterval);
            long startTime = System.currentTimeMillis();
            mSessionItem.setStartTime(startTime);
            mStopTime = startTime + mSessionLengthInMillis;
            mSessionItem.setDeviceInfo(mDevice);
            // need to set coordinates in tread and stopTime when finish work.
        }
    }

    private void makeWork(){
        mScheduleTaskExecutor = Executors.newScheduledThreadPool(1);
        mScheduleTaskExecutor.schedule(new MainServiceRunnable(), 0, TimeUnit.SECONDS);
    }

    private class MainServiceRunnable implements Runnable, SensorEventListener {
        long mCurrentMillis;

        private DatabaseReference mDatabase;

        boolean isInitiated;
        private SensorManager mSensorManager;
        private Sensor mAccelerometerSensor;

        @Override
        public void run() {
            Log.e("MainServiceRunnable", "run()");



            Intent intent = new Intent(Config.TAG_ACTIVITY_BROADCAST_RECEIVER);
            intent.putExtra(Config.TAG_SERVICE_RUNNING_STATUS, true);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);

            initSensor();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            isInitiated = true;
        }

        private void initSensor(){
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

            /**
              * @param samplingPeriodUs The rate {@link android.hardware.SensorEvent sensor events} are
              *            delivered at. This is only a hint to the system. Events may be received faster or
              *            slower than the specified rate. Usually events are received faster.
             */
            mSensorManager.registerListener(this, mAccelerometerSensor, mLoggingInterval*1000*1000);
        }

        private void finalizeSensorWork(){
            mSensorManager.unregisterListener(this);
            mSessionItem.setStopTime(System.currentTimeMillis());
            postDataToFirebaseAndShutdown();



            Intent intent = new Intent(Config.TAG_ACTIVITY_BROADCAST_RECEIVER);
            intent.putExtra(Config.TAG_SERVICE_RUNNING_STATUS, false);
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

            mCurrentMillis = System.currentTimeMillis();
            if(isEnabledSessionLengthSetting && mCurrentMillis > mStopTime){
                isRunning = false;
            }

            if(isRunning){
                Sensor mySensor = sensorEvent.sensor;
                if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
                    mSessionItem.addCoordinate(new AccelerometerData(
                            System.currentTimeMillis(),
                            sensorEvent.values[0],
                            sensorEvent.values[1],
                            sensorEvent.values[2]
                    ));

                    Log.e("onSensorChanged", "new data: x="+sensorEvent.values[0]+"; y="
                            +sensorEvent.values[1]+"; z="+sensorEvent.values[2]+";");
                }
            }

            if(!isRunning){
                finalizeSensorWork();
                mScheduleTaskExecutor.shutdown();
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //do nothing.
        }

        private void postDataToFirebaseAndShutdown(){
            Log.e("MainServiceRunnable", "postDataToFirebaseAndShutdown");
            mDatabase.child(Config.FIREBASE_DB_PATH_USERS).child(mUser.getUserUid()).setValue(mUser);
            mDatabase.child(Config.FIREBASE_DB_PATH_SESSIONS_ITEM).child(mUser.getUserUid()).push().setValue(mSessionItem);
        }
    }

}
