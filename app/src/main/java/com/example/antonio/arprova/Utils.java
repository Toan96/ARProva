package com.example.antonio.arprova;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

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
