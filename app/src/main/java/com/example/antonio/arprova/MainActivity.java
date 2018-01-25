package com.example.antonio.arprova;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.antonio.arprova.myLocation.MyGPSLocation;

import java.util.HashMap;
import java.util.Map;

import static com.example.antonio.arprova.CameraPreview.getCameraInstance;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class MainActivity extends Activity implements UpdateUICallback {

    private static String TAG = "MainActivity";
    private Camera mCamera;
    private CameraPreview mPreview;
    private MyGPSLocation myGPSLocation;
    private FrameLayout preview;
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
            Log.d("permission needed: ", "API: " + Build.VERSION.SDK_INT);
            Utils.permissionsCheck(this);
        }
        createLayout();
        Location lastKnown = myGPSLocation.getBestLastKnownLocation();
        if (lastKnown != null) {
            myGPSLocation.startIntentService(lastKnown);
            String values = "Alt: " + lastKnown.getAltitude() + " m" + System.getProperty("line.separator") +
                    "Lat: " + lastKnown.getLatitude() + System.getProperty("line.separator") + "Lon: " + lastKnown.getLongitude();
            Log.d("gps: ", "used lastKnownLocation");
            tvGpsValues.setText(values);

        } else {
            tvGpsValues.setText(R.string.tvGpsValuesHint);
            Log.d("gps: ", "no last known position");
        }
        Log.d(TAG, "updated tvGpsValues");
        myGPSLocation.takeLocationUpdates();

        Log.d(TAG, "numThreads: " + Thread.activeCount());
    }

    @Override
    public void updateGpsTv(String values) {
        tvGpsValues.setText(values);
    }

    private void createLayout() {

        //TODO attenzione permessi camera
        // Create an instance of Camera
        mCamera = getCameraInstance(getApplicationContext());

        myGPSLocation = new MyGPSLocation(this, this);

        // Create the Preview view and set it as the content of this Activity.
        mPreview = new CameraPreview(this, mCamera);
        preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        //add views on camera preview.

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        int margin = Utils.dpToPixels(getApplicationContext(), 10);
        layoutParams.setMargins(margin, margin, margin, margin);
        layoutParams.gravity = Gravity.BOTTOM | Gravity.START;
        tvGpsValues = new TextView(this);
        tvGpsValues.setLayoutParams(layoutParams);
        tvGpsValues.setTextColor(Color.parseColor("#cecece"));
        //tvGpsValues.setBackgroundColor(Color.parseColor("#BF7c7c7c"));
        preview.addView(tvGpsValues);

        //add map fragment
        FrameLayout mapContainer = new FrameLayout(this);
        mapContainer.setId(R.id.mapContainer);
        FrameLayout.LayoutParams layoutParamsMap = new FrameLayout.LayoutParams(Utils.dpToPixels(getApplicationContext(),
                100), Utils.dpToPixels(getApplicationContext(), 100));
        layoutParamsMap.setMargins(margin, margin, margin, margin);
        layoutParamsMap.gravity = Gravity.BOTTOM | Gravity.END;
        mapContainer.setLayoutParams(layoutParamsMap);
        //mapContainer.setBackgroundColor(Color.GREEN);
        preview.addView(mapContainer);
        //TODO map
        //getFragmentManager().beginTransaction().add(R.id.mapContainer, com.google.android.gms.maps.MapFragment.newInstance()).commit();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "on request permissions result called");
        switch (requestCode) {
            case Utils.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(android.Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(android.Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for permissions
                if (perms.get(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                        perms.get(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        && perms.get(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    createLayout();
                } else {
                    // Permissions Denied
                    Toast.makeText(this, R.string.permission_denied_message, Toast.LENGTH_SHORT).show();
                    //TODO other
                }
            }
            break;
            default:
                this.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }












/*
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        Log.d(TAG, "onRequestPermissionsResult");
        if (requestCode == MyGPSLocation.MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOC) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myGPSLocation.takeLocationUpdates();
            } else {
                // TODO other
                // Permission request was denied.
                finish();
            }
        }

        if (requestCode == MyGPSLocation.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOC) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                myGPSLocation.takeLocationUpdates();
            } else {
                // TODO other
                // Permission request was denied.
                finish();
            }
        }
    }
    */
}
