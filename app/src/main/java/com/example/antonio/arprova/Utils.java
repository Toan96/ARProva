package com.example.antonio.arprova;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class Utils {

    //to take android m runtime permissions.
    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 0;

    //to take margin in pixels.
    public static int dpToPixels(Context context, int dpValue) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * d); // margin in pixels
    }

    //to take android m runtime permissions.
    @TargetApi(Build.VERSION_CODES.M)
    public static void permissionsCheck(final Activity activity) {
        Log.d("permissionsCheck: ", "need to allow permissions");
        List<String> permissionsNeeded = new ArrayList<>();

        final List<String> permissionsList = new ArrayList<>();
        // Add permission check for any permission that is not NORMAL_PERMISSIONS
        if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_FINE_LOCATION, activity))
            permissionsNeeded.add(activity.getString(R.string.explain_permission_fine_location));
        if (!addPermission(permissionsList, android.Manifest.permission.ACCESS_COARSE_LOCATION, activity))
            permissionsNeeded.add(activity.getString(R.string.explain_permission_coarse_location));
        if (!addPermission(permissionsList, Manifest.permission.CAMERA, activity))
            permissionsNeeded.add(activity.getString(R.string.explain_permission_camera));

        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {
                // Need Rationale
                String message = activity.getString(R.string.permission_grant_message) + permissionsNeeded.get(0);
                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + "\n" + permissionsNeeded.get(i);
                showMessageOKCancel(activity, message,
                        new DialogInterface.OnClickListener() {
                            @TargetApi(Build.VERSION_CODES.M)
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //direttamente activity non activity compat perchè ci dovrebbe arrivare solo api 23.
                                activity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                Log.d("permissionsCheck: ", "return in for");
                return;
            }
            activity.requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            Log.d("permissionsCheck: ", "return out of if");
        }
    }

    //to take android m runtime permissions.
    private static void showMessageOKCancel(Activity activity, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.dialog_permission_allow, okListener)
                .setNegativeButton(R.string.dialog_permission_refused, null)
                .create()
                .show();
    }

    //to take android m runtime permissions.
    //check if permission needs explaination.
    @TargetApi(Build.VERSION_CODES.M)
    private static boolean addPermission(List<String> permissionsList, String permission, Activity activity) {
        //direttamente activity non activity compat perchè ci dovrebbe arrivare solo api 23.
        if (activity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            //direttamente activity non activity compat perchè ci dovrebbe arrivare solo api 23.
            if (!activity.shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }


/*
    /**
     * Requests the {@link android.Manifest.permission#} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
/*    public static void requestPermission(final Context context, final String permission, final int requestCode, final Activity activity) {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
 /*           //TODO messaggio di spiegazione
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with a button to request the missing permission.
            Toast.makeText(context, R.string.errorePermessi,
                    Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                }
            }, 5000);
*//*
        } else {
            Toast.makeText(context, R.string.errorePermessi,
                    Toast.LENGTH_SHORT).show();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
                }
            }, 5000);
        }
    }
    */
}
