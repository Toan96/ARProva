package com.example.antonio.arprova;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import java.util.Locale;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class Utils {

    protected static final int MY_PERMISSIONS_REQUEST_ACCESS_CAMERA = 123;
    protected static final int MY_PERMISSIONS_REQUEST_ACCESS_LOC = 321;
    protected static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};

    //to take margin in pixels.
    public static int dpToPixels(Context context, int dpValue) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * d); // margin in pixels
    }

    //to take formatted string for gps values
    public static String formattedValues(Location location) {
        final double longitude = location.getLongitude();
        final double latitude = location.getLatitude();
        final double altitude = location.getAltitude();
        final double bearing = location.getBearing();
        final double accuracy = location.getAccuracy();

        final String values = "Alt: " + String.format(Locale.getDefault(), "%.1f", altitude) + " m" + System.getProperty("line.separator") +
                "Lat: " + String.format(Locale.getDefault(), "%.6f", latitude) + System.getProperty("line.separator") +
                "Lon: " + String.format(Locale.getDefault(), "%.6f", longitude);

        Log.d("gps values changed", values + " Bear: " + String.format("%.2f", bearing) + " Accu: " + String.format("%.2f", accuracy));

        return values;
    }

    //to verify multiple location permissions.
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
