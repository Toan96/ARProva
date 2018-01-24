package com.example.antonio.arprova;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class Utils {

    //to take margin in pixels.
    public static int dpToPixels(Context context, int dpValue) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * d); // margin in pixels
    }

    /**
     * Requests the {@link android.Manifest.permission#} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    public static void requestPermission(final Context context, final String permission, final int requestCode, final Activity activity) {
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
*/
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
}
