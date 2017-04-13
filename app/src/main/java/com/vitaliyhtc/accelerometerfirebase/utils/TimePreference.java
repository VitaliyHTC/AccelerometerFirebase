package com.vitaliyhtc.accelerometerfirebase.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import com.vitaliyhtc.accelerometerfirebase.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TimePreference extends DialogPreference {
    private static final int GET_HOUR_OF_DAY = 0x01;
    private static final int GET_MINUTES = 0x02;

    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker picker = null;

    public static int getHour(String time) {
        return parse24hTimeString(time, GET_HOUR_OF_DAY);
    }

    public static int getMinute(String time) {
        return parse24hTimeString(time, GET_MINUTES);
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText(ctxt.getResources().getString(R.string.pref_app_time_pref_set));
        setNegativeButtonText(ctxt.getResources().getString(R.string.pref_app_time_pref_cancel));
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());
        picker.setIs24HourView(true);
        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);

            if (callChangeListener(time)) {
                persistString(time);
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = null;

        if (restoreValue) {
            if (defaultValue == null) {
                time = getPersistedString("00:00");
            } else {
                time = getPersistedString(defaultValue.toString());
            }
        } else {
            time = defaultValue.toString();
        }

        lastHour = getHour(time);
        lastMinute = getMinute(time);
    }


    private static int parse24hTimeString(String value, int whatToReturn) {
        int hourOfDay = 0;
        int minute = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date = sdf.parse(value);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.setTime(date);
            hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (whatToReturn == GET_HOUR_OF_DAY) {
            return hourOfDay;
        } else if (whatToReturn == GET_MINUTES) {
            return minute;
        } else {
            return 0;
        }
    }
}
