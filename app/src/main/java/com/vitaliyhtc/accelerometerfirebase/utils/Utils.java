package com.vitaliyhtc.accelerometerfirebase.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public abstract class Utils {

    private Utils() {
        throw new AssertionError();
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public static String getDate(long milliSeconds, String dateFormat) {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

    // http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
    // https://en.wikipedia.org/wiki/Kilobyte
    // modified.
    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
