package com.vitaliyhtc.accelerometerfirebase.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.vitaliyhtc.accelerometerfirebase.services.AccelerometerDataLoggerService;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

public class OnAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctxt, Intent intent) {
        if (Utils.isNetworkAvailable(ctxt)) {
            ctxt.startService(new Intent(ctxt, AccelerometerDataLoggerService.class));
        }
    }
}
