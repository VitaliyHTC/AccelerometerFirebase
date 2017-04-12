package com.vitaliyhtc.accelerometerfirebase;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

public class SettingsActivity extends SettingsActivityAdapter {

    // TODO: 12.04.17 code style. Check everywhere.
    SharedPreferences prefs=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        if (getSupportActionBar1() != null) {
            getSupportActionBar1().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(onChange);
    }

    @Override
    public void onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(onChange);

        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    SharedPreferences.OnSharedPreferenceChangeListener onChange =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                public void onSharedPreferenceChanged(SharedPreferences prefs,
                                                      String key) {
                    if (getResources().getString(R.string.config_pref_key_enable_service_start_setting).equals(key)) {
                        boolean enabled=prefs.getBoolean(key, false);
                        int flag=(enabled ?
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
                        ComponentName component=new ComponentName(SettingsActivity.this, OnBootReceiver.class);

                        getPackageManager()
                                .setComponentEnabledSetting(component, flag, PackageManager.DONT_KILL_APP);

                        if (enabled) {
                            OnBootReceiver.setAlarm(SettingsActivity.this);
                        }
                        else {
                            OnBootReceiver.cancelAlarm(SettingsActivity.this);
                        }
                    }
                    else if (getResources().getString(R.string.config_pref_key_ServiceStartTime).equals(key)) {
                        OnBootReceiver.cancelAlarm(SettingsActivity.this);
                        OnBootReceiver.setAlarm(SettingsActivity.this);
                    }
                }
            };
}
