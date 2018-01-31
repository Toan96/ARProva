package com.example.antonio.arprova;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.antonio.arprova.my_location.MyGPSLocation;

import static com.example.antonio.arprova.CameraPreview.getCameraInstance;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class MainActivity extends AppCompatActivity implements UpdateUICallback, MapFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    static Location lastKnown = null;
    private Runnable runnable;
    private Handler handler;
    private FrameLayout preview;
    private CoordinatorLayout coordinatorLayout;
    private FrameLayout mapContainer;
    private MapFragment mapFragment;
    private CameraPreview mPreview;
    private MyGPSLocation myGPSLocation;
    private TextView tvGpsValues;
    private TextView tvBearing;
    private TextView tvDistance;
    private VerticalSeekBar seekZoom;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
/*
    private Sensor mSensorAccel;
    private Sensor mSensorMagneticField;
    private float compass_last_measured_bearing = 0;
*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set no title and no notifybar before set layout
        setContentView(R.layout.activity_main);

        preview = findViewById(R.id.camera_preview);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        tvGpsValues = findViewById(R.id.tvGpsValues);
        tvBearing = findViewById(R.id.tvBearing);
        tvDistance = findViewById(R.id.tvDistance);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                updateBearing(event);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        seekZoom = findViewById(R.id.seekBarZoom);
        // perform seek bar change listener event used for getting the progress value
        seekZoom.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (null != mapFragment) {
                    mapFragment.SetZoomLevel(progress);
                    updateDistance(mapFragment.getMapRadius());
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    //used onStart to avoid onResume loop on asking permissions
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("onStart: ", "entering..");
        //show layout, if necessary check camera permission.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "API 23+: need check permission for camera");
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "API 23+: requesting permission for camera");
                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d("post delayed: ", "camera permission");
                        requestCameraPermission();
                    }
                };
                handler.postDelayed(runnable, 1000);
            } else {
                showLayout();
            }
        } else {
            Log.d(TAG, "API 22 or less: not need to check runtime permission for camera");
            showLayout();
        }
        //show location, if necessary check location permissions.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "API 23+: need check permissions for location");
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "API 23+: requesting permissions for location");
                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d("post delayed: ", "location permissions");
                        requestLocationPermissions();
                    }
                };
                handler.postDelayed(runnable, 3000);
            } else {
                showLocation();
            }
        } else {
            Log.d(TAG, "API 22 or less: not need to check runtime permissions for location");
            showLocation();
        }
        //check num of active threads
        Log.d(TAG, "numThreads: " + Thread.activeCount());
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
        //need to do before onSaveInstanceState
        if (null != mapContainer) {
            getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
    }

    @Override
    protected void onStop() {
        //in onStop supports notifications overlay
        super.onStop();
        if (null != myGPSLocation)
            myGPSLocation.stopUpdates();
        if (null != mPreview)
            mPreview.releaseCamera(); // release the camera immediately on pause event
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != myGPSLocation)
            myGPSLocation.removeHandler();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    public void updateGpsTv(String values) {
        tvGpsValues.setText(values);
    }

    @Override
    public void updateSeekZoom(int zoom) {
        seekZoom.setProgressAndThumb(zoom);
    }

    @SuppressLint("SetTextI18n")
    public void updateDistance(float mapRadius) {
        int distance = (int) mapRadius;
        //noinspection StatementWithEmptyBody
        if (distance == 0) {
            //maybe do nothing, use ~ by static layout
        } else if (distance > 1000) {
            distance /= 1000;
            tvDistance.setText("~" + distance + " km");
        } else {
            tvDistance.setText("~" + ((distance + 5) / 10) * 10 + " m"); //round to 10th
        }
    }

    private void showLocation() {
        myGPSLocation = new MyGPSLocation(this);
        lastKnown = myGPSLocation.getBestLastKnownLocation();
        if (lastKnown != null) {
            myGPSLocation.startIntentService(lastKnown);
            Log.d("gps: ", "used lastKnownLocation");
            tvGpsValues.setText(Utils.formattedValues(lastKnown));
        } else {
            tvGpsValues.setText(R.string.tvGpsValuesHint);
            Log.d("gps: ", "no last known position");
        }
        Log.d(TAG, "updated tvGpsValues");
        tvGpsValues.setVisibility(View.VISIBLE);
        myGPSLocation.takeLocationUpdates();
    }

    private void showLayout() {
        // Create an instance of Camera
        Camera mCamera = getCameraInstance(getApplicationContext());
        // Create the Preview view and set it as the content of this Activity.
        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);
        //layout container need to stay up. bring to up other subViews.
        coordinatorLayout.bringToFront();
        seekZoom.bringToFront();
        tvBearing.bringToFront();
        tvDistance.bringToFront();
        tvDistance.setVisibility(View.VISIBLE);
        tvBearing.setVisibility(View.VISIBLE);
        seekZoom.setVisibility(View.VISIBLE);
        seekZoom.setProgressAndThumb(0);

        //add map fragment
        mapContainer = findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.VISIBLE);
/*TODO        mapContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showMap();
            }
        });
*/
        mapFragment = MapFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.mapContainer, mapFragment).commit();
    }

    //TODO zoom map on click
    private void showMap() {
        Log.d(TAG, "show map");
        if (mapContainer.getWidth() == Utils.dpToPixels(this, 100)) {
            mapContainer.setLayoutParams(new FrameLayout.LayoutParams(Utils.dpToPixels(this, 400),
                    Utils.dpToPixels(this, 500)));
            mapContainer.bringToFront();
        } else {
            mapContainer.setLayoutParams(new FrameLayout.LayoutParams(Utils.dpToPixels(this, 100),
                    Utils.dpToPixels(this, 100)));
        }
    }

    private void initSensors() {
        if (sensorManager != null) {
/*            mSensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorMagneticField = sensorManager
                    .getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            if ((mSensorAccel != null) && (mSensorMagneticField != null)) {
            /* Initialize the gravity sensor */
/*                if (mSensorAccel != null) {
                    Log.i(TAG, "Accel sensor available. (TYPE_ACCELEROMETER)");
                    sensorManager.registerListener(sensorEventListener,
                            mSensorAccel, SensorManager.SENSOR_DELAY_UI);
                } else {
                    Log.i(TAG, "Accel sensor unavailable. (TYPE_ACCELEROMETER)");
                }

            /* Initialize the magnetic field sensor */
/*               if (mSensorMagneticField != null) {
                    Log.i(TAG, "Magnetic field sensor available. (TYPE_MAGNETIC_FIELD)");
                    sensorManager.registerListener(sensorEventListener,
                            mSensorMagneticField, SensorManager.SENSOR_DELAY_UI);
                } else {
                    Log.i(TAG,
                            "Magnetic field sensor unavailable. (TYPE_MAGNETIC_FIELD)");
                }
            } else {
*/
            Log.i(TAG,
                    "Deprecated use: (TYPE_ORIENTATION)");
            //noinspection deprecation
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_UI);
//           }
        }
    }

    private void updateBearing(SensorEvent event) {
/*
        float[] mAccel = null, mMagnetic = null;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mAccel = event.values.clone();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagnetic = event.values.clone();
        }

        if ((mAccel != null) && (mMagnetic != null)) {

            /* Create rotation Matrix */
        //need inclination matrix in future.
/*            float[] rotationMatrix = new float[9];
            if (SensorManager.getRotationMatrix(rotationMatrix, null,
                    mAccel, mMagnetic)) {

                /* Compensate device orientation */
        // http://android-developers.blogspot.de/2010/09/one-screen-turn-deserves-another.html
/*                float[] remappedRotationMatrix = new float[9];
                switch (getWindowManager().getDefaultDisplay()
                        .getRotation()) {
                    case Surface.ROTATION_0:
                        SensorManager.remapCoordinateSystem(rotationMatrix,
                                SensorManager.AXIS_X, SensorManager.AXIS_Y,
                                remappedRotationMatrix);
                        break;
                    case Surface.ROTATION_90:
                        //noinspection SuspiciousNameCombination
                        SensorManager.remapCoordinateSystem(rotationMatrix,
                                SensorManager.AXIS_Y,
                                SensorManager.AXIS_MINUS_X,
                                remappedRotationMatrix);
                        break;
                    case Surface.ROTATION_180:
                        SensorManager.remapCoordinateSystem(rotationMatrix,
                                SensorManager.AXIS_MINUS_X,
                                SensorManager.AXIS_MINUS_Y,
                                remappedRotationMatrix);
                        break;
                    case Surface.ROTATION_270:
                        //noinspection SuspiciousNameCombination
                        SensorManager.remapCoordinateSystem(rotationMatrix,
                                SensorManager.AXIS_MINUS_Y,
                                SensorManager.AXIS_X, remappedRotationMatrix);
                        break;
                }

                /* Calculate Orientation */
/*                float results[] = new float[3];
                SensorManager.getOrientation(remappedRotationMatrix,
                        results);

                /* Get measured value */
/*               float current_measured_bearing = (float) (results[0] * 180 / Math.PI);
                if (current_measured_bearing < 0) {
                    current_measured_bearing += 360;
                }

                /* Smooth values using a 'Low Pass Filter' */
/*               current_measured_bearing = current_measured_bearing
                        + Utils.SMOOTHING_FACTOR_COMPASS
                        * (current_measured_bearing - compass_last_measured_bearing);

                /*
                 * Update variables for next use (Required for Low Pass
                 * Filter)
                 */
//               compass_last_measured_bearing = current_measured_bearing;

                /* Update normal output */
//              tvBearing.setText(Utils.formatBearing(current_measured_bearing));

                /*
                Update map camera rotation.
                */
//               mapFragment.updateCameraBearing(current_measured_bearing);
//           }
//       } else {
        //for old devices
        float bearing = 0;
        //Log.d("event values: ", event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        //for screen adjustment
        switch (getWindowManager().getDefaultDisplay()
                .getRotation()) {
            case Surface.ROTATION_0:
                bearing = event.values[0];
                break;
            case Surface.ROTATION_90:
                bearing = event.values[0] + 90;
                break;
            case Surface.ROTATION_180:
                bearing = event.values[0] + 180;
                break;
            case Surface.ROTATION_270:
                bearing = event.values[0] + 270;
                break;
        }
        tvBearing.setText(Utils.formatBearing(bearing));
        mapFragment.updateCameraBearing(bearing);
//       }
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
            Snackbar.make(coordinatorLayout, R.string.explain_permission_camera,
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
            Snackbar.make(coordinatorLayout, R.string.explain_permission_fine_location,
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
    @TargetApi(23)
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
                showLayout();
                errCamera.setVisibility(View.INVISIBLE);
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "API 23+: requesting permissions for location");
                    //requestLocationPermissions();
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d("post delayed: ", "location permissions");
                            requestLocationPermissions();
                        }
                    };
                    handler.postDelayed(runnable, 2000);
                }
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                coordinatorLayout.bringToFront();
                errCamera.setVisibility(View.VISIBLE);

                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        requestCameraPermission();
                    }
                };
                handler.postDelayed(runnable, 2000);
            }
        } else if (requestCode == Utils.MY_PERMISSIONS_REQUEST_ACCESS_LOC) {
            Log.i(TAG, "Received response for location permissions request.");
            // requested multiple permissions for location, so all of them need to be
            // checked.
            if (Utils.verifyPermissions(grantResults)) {
                // All required permissions have been granted.
                showLocation();
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "API 23+: requesting permission for camera");
                    //requestCameraPermission();
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            Log.d("post delayed: ", "camera permission");
                            requestCameraPermission();
                        }
                    };
                    handler.postDelayed(runnable, 2000);
                }
            } else {
                Log.i(TAG, "Location permissions were NOT granted.");
                updateGpsTv(getString(R.string.errorePermessiGPS));
                tvGpsValues.setVisibility(View.VISIBLE);

                handler = new Handler();
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        requestLocationPermissions();
                    }
                };
                handler.postDelayed(runnable, 2000);
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
