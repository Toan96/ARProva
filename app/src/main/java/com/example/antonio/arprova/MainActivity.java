package com.example.antonio.arprova;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.antonio.arprova.myLocation.MyGPSLocation;

import java.util.Locale;

import static com.example.antonio.arprova.CameraPreview.getCameraInstance;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class MainActivity extends Activity implements UpdateUICallback {

    private static String TAG = "MainActivity";
    private FrameLayout preview;
    private CameraPreview mPreview;
    private MyGPSLocation myGPSLocation;
    private TextView tvGpsValues;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set no title and no notifybar before set layout
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myGPSLocation.removeHandler();
    }

    @Override
    protected void onPause() {
        super.onPause();
        myGPSLocation.stopUpdates();
        mPreview.releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume: ", "entering..");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "API 23+: need check permission for camera");
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "API 23+: requesting permission for camera");
                requestCameraPermission();
            }
        } else {
            Log.d(TAG, "API 22 or less: not need to check runtime permission for camera");
            showLayout();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "API 23+: need check permissions for location");
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "API 23+: requesting permissions for location");
                requestLocationPermissions();
            }
        } else {
            Log.d(TAG, "API 22 or less: not need to check runtime permissions for location");
            showLocation();
        }

        Log.d(TAG, "numThreads: " + Thread.activeCount());
    }


    @Override
    public void updateGpsTv(String values) {
        tvGpsValues.setText(values);
    }

    private void showLocation() {
        myGPSLocation = new MyGPSLocation(this, this);
        Location lastKnown = myGPSLocation.getBestLastKnownLocation();
        if (lastKnown != null) {
            myGPSLocation.startIntentService(lastKnown);
            String values = "Alt: " + String.format(Locale.getDefault(), "%.1f", lastKnown.getAltitude()) + " m" + System.getProperty("line.separator") +
                    "Lat: " + String.format(Locale.getDefault(), "%.4f", lastKnown.getLatitude()) + System.getProperty("line.separator") +
                    "Lon: " + String.format(Locale.getDefault(), "%.4f", lastKnown.getLongitude());
            Log.d("gps: ", "used lastKnownLocation");
            tvGpsValues.setText(values);

        } else {
            tvGpsValues.setText(R.string.tvGpsValuesHint);
            Log.d("gps: ", "no last known position");
        }
        Log.d(TAG, "updated tvGpsValues");
        myGPSLocation.takeLocationUpdates();
    }

    private void showLayout() {
        // Create an instance of Camera
        Camera mCamera = getCameraInstance(getApplicationContext());

        // Create the Preview view and set it as the content of this Activity.
        mPreview = new CameraPreview(this, mCamera);
        preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        //add views on camera preview. layout container need to stay up.
        LinearLayout layoutContainer = findViewById(R.id.layoutContainer);
        layoutContainer.bringToFront();
        tvGpsValues = findViewById(R.id.tvGpsValues);
        tvGpsValues.bringToFront();
        //add map fragment
        FrameLayout mapContainer = findViewById(R.id.mapContainer);
        mapContainer.bringToFront();
        //TODO map
        //getFragmentManager().beginTransaction().add(R.id.mapContainer, com.google.android.gms.maps.MapFragment.newInstance()).commit();
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a message will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @TargetApi(23)
    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG, "Displaying camera permission rationale to provide additional context.");
            //per forza preview se non c'è la camera non c'è mPreview.
            Snackbar.make(preview, R.string.explain_permission_camera,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.dialog_permission_allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_ACCESS_CAMERA);
                        }
                    })
                    .show();
        } else {
            // Camera permission has not been granted yet. Request it directly.
            requestPermissions(new String[]{Manifest.permission.CAMERA}, Utils.MY_PERMISSIONS_REQUEST_ACCESS_CAMERA);
        }
    }

    /**
     * Requests the location permissions.
     * If the permission has been denied previously, a message will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    @TargetApi(23)
    private void requestLocationPermissions() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)
                || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Log.i(TAG, "Displaying location permission rationale to provide additional context.");

            // Display a SnackBar with an explanation and a button to trigger the request.
            View v = preview; //preview se non c'è camera.
            if (null != mPreview) {
                v = mPreview; //preview se c'è camera.
            }
            Snackbar.make(v, R.string.explain_permission_fine_location,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.dialog_permission_allow, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(Utils.PERMISSIONS_LOCATION, Utils.MY_PERMISSIONS_REQUEST_ACCESS_LOC);
                        }
                    })
                    .show();
        } else {
            // Location permissions have not been granted yet. Request them directly.
            requestPermissions(Utils.PERMISSIONS_LOCATION, Utils.MY_PERMISSIONS_REQUEST_ACCESS_LOC);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == Utils.MY_PERMISSIONS_REQUEST_ACCESS_CAMERA) {
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.");
            TextView errCamera = findViewById(R.id.tvErrorePermessiCamera);
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.");
                //errCamera.setVisibility(View.GONE);
                showLayout();
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                errCamera.setVisibility(View.VISIBLE);
                errCamera.bringToFront();
            }
        } else if (requestCode == Utils.MY_PERMISSIONS_REQUEST_ACCESS_LOC) {
            Log.i(TAG, "Received response for location permissions request.");

            // We have requested multiple permissions for location, so all of them need to be
            // checked.
            if (Utils.verifyPermissions(grantResults)) {
                // All required permissions have been granted, display contacts fragment.
                showLocation();
            } else {
                Log.i(TAG, "Contacts permissions were NOT granted.");
                updateGpsTv(getString(R.string.errorePermessi) + "GPS");
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
