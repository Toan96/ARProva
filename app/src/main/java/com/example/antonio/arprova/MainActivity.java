package com.example.antonio.arprova;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import static com.example.antonio.arprova.CameraPreview.getCameraInstance;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class MainActivity extends Activity implements UpdateUICallback {

    //TODO delete numChiamate
    static int numChiamate = 0;
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

        //lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        myGPSLocation = new MyGPSLocation(this, this);

        // Create an instance of Camera
        mCamera = getCameraInstance(getApplicationContext());

        //TODO: forse inutile
        // get Camera parameters
        Camera.Parameters params = mCamera.getParameters();

        List<String> focusModes = params.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            // Autofocus mode is supported
            // set the focus mode
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            // set Camera parameters
            mCamera.setParameters(params);
        }
        //fine forse inutile


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
        //getFragmentManager().beginTransaction().add(R.id.mapContainer, com.google.android.gms.maps.MapFragment.newInstance()).commit();
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
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Utils.requestPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION, MyGPSLocation.MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOC, this);
            }
        }
        //TODO riabilita
        Location lastKnown = null;// = myGPSLocation.getLocationManager().getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (lastKnown != null) {
            String values = "Alt: " + lastKnown.getAltitude() + " m" + System.getProperty("line.separator") +
                    "Lat: " + lastKnown.getLatitude() + System.getProperty("line.separator") + "Lon: " + lastKnown.getLongitude();
            Log.d("gps: ", "used lastKnownLocation");

            tvGpsValues.setText(values);
        } else {
            tvGpsValues.setText(R.string.tvGpsValuesHint);
            Log.d(TAG, "no last known position");
        }
        Log.d(TAG, "updated tvGpsValues");
        Log.d(TAG, "numThreads: " + Thread.activeCount());
        myGPSLocation.takeLocationUpdates();
    }

    @Override
    public void updateGpsTv(String values) {
        final String value = values;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvGpsValues.setText(value + " " + numChiamate++);
            }
        });
    }


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
}
