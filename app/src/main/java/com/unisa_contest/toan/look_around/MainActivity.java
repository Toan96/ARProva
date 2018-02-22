package com.unisa_contest.toan.look_around;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
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
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.unisa_contest.toan.look_around.my_location.MyGPSLocation;
import com.unisa_contest.toan.look_around.places.PlaceDrawerASync;

import java.util.ArrayList;

import static com.unisa_contest.toan.look_around.CameraPreview.getCameraInstance;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class MainActivity extends AppCompatActivity implements UpdateUICallback, MapFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    float[] filteredValues = new float[5];//null create problems //need to compare for lowPassFilter
    private Runnable runnable;
    private Handler handler;
    private FrameLayout preview, overlay, mapContainer;
    private CoordinatorLayout coordinatorLayout;
    private RelativeLayout filterLayout;
    private LinearLayout linL;
    private MapFragment mapFragment;
    private CameraPreview mPreview;
    private MyGPSLocation myGPSLocation;
    private TextView tvGpsValues, tvBearing, tvDistance;
    private VerticalSeekBar seekZoom;
    private AsyncTask async;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;
    private boolean cameraGranted = false, locationGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set no title and no notifyBar before set layout
        setContentView(R.layout.activity_main);

        //need resources in utils class for bitmap
        Utils.res = getResources();

        if (null != savedInstanceState) {
            if (null != savedInstanceState.getParcelableArrayList("Places"))
                Utils.places = savedInstanceState.getParcelableArrayList("Places");
            Utils.FRIEND_MODE = savedInstanceState.getBoolean("FriendMode", false);
        }

        preview = findViewById(R.id.camera_preview);
        overlay = findViewById(R.id.overlay);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        tvGpsValues = findViewById(R.id.tvGpsValues);
        tvBearing = findViewById(R.id.tvBearing);
        tvDistance = findViewById(R.id.tvDistance);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                updateBearing(event);
                //Utils.setCurrentBearing need to do in updateBearing method
                //chiedere di ricalibrare il sensore se necessario
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
                    mapFragment.setZoomLevel(progress);
                    float visibleDistance = mapFragment.getMapRadius();
                    updateDistance(visibleDistance);
                    Utils.visibleDistance = visibleDistance;
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
                cameraGranted = true;
            }
        } else {
            Log.d(TAG, "API 22 or less: not need to check runtime permission for camera");
            cameraGranted = true;
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
                locationGranted = true;
            }
        } else {
            Log.d(TAG, "API 22 or less: not need to check runtime permissions for location");
            locationGranted = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("onResume: ", "entering..");
        showLayout();
        showLocation();
        if (cameraGranted && locationGranted) {
            initSensors();
            checkInternetConnection();
            //start AsyncTask for drawing AR places //no need executeOnExecutor
            async = new PlaceDrawerASync().execute(getApplicationContext(), overlay);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause: ", "entering..");
        sensorManager.unregisterListener(sensorEventListener);
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        //need to do before onSaveInstanceState
        if (null != mapContainer) {
            if (Utils.BIG_MAP)
                showMap(); //to avoid compass on small map
            getSupportFragmentManager().beginTransaction().remove(mapFragment).commit();
        }
        if (null != mPreview) {
            mPreview.releaseCamera(); // release the camera immediately on pause event
            preview.removeView(mPreview);
        }
        if (null != async)
            async.cancel(true);
        if (null != myGPSLocation) {
            myGPSLocation.stopSearchForPlaces();
            myGPSLocation.dismissDialog();
        }
        if (null != linL) {
            linL.setVisibility(View.GONE);
            ((ImageButton) findViewById(R.id.search)).setImageResource(R.drawable.search);
        }
    }

    @Override
    protected void onStop() {
        //in onStop supports notifications overlay
        super.onStop();
        Log.d("onStop: ", "entering..");
        cameraGranted = false;
        locationGranted = false;
        if (null != myGPSLocation)
            myGPSLocation.stopUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != myGPSLocation)
            myGPSLocation.removeHandler();
        if (Utils.FRIEND_MODE)
            Utils.FRIEND_MODE = false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if ((null != Utils.places) && (Utils.places.size() > 0))
            outState.putParcelableArrayList("Places", Utils.places);
        if (Utils.FRIEND_MODE)
            outState.putBoolean("FriendMode", Utils.FRIEND_MODE);
    }

    @Override
    public void updateGpsTv(String values) {
        tvGpsValues.setText(values);
    }

    @Override
    public void updateSeekZoom(int zoom) {
        seekZoom.setProgressAndThumb(zoom);
    }

    @Override
    public void updateDistance(float mapRadius) {
        tvDistance.setText(Utils.formatDistance(mapRadius));
    }

    @Override
    public void showMap() {
        if (null != mapFragment) {
            Log.d(TAG, "show map");
            //se mappa piccola
            if (mapContainer.getWidth() == Utils.dpToPixels(this, Utils.SMALL_MAP_DIMEN)) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mapContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            Utils.dpToPixels(this, Utils.BIG_MAP_DIMEN_PORTRAIT)));
                } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    mapContainer.setLayoutParams(new LinearLayout.LayoutParams(Utils.dpToPixels(this, Utils.BIG_MAP_DIMEN_LAND),
                            Utils.dpToPixels(this, Utils.BIG_MAP_DIMEN_LAND))); //no match_parent
                }
                tvBearing.setVisibility(View.INVISIBLE);
                Utils.BIG_MAP = true;
                filterLayout.setVisibility(View.GONE);
                //fermo async AR
                async.cancel(true);
            } else {
                //se mappa grande
                mapContainer.setLayoutParams(new LinearLayout.LayoutParams(Utils.dpToPixels(this, Utils.SMALL_MAP_DIMEN),
                        Utils.dpToPixels(this, Utils.SMALL_MAP_DIMEN)));
                tvBearing.setVisibility(View.VISIBLE);
                Utils.BIG_MAP = false;
                filterLayout.setVisibility(View.VISIBLE);
                //riparte async AR
                async = new PlaceDrawerASync().execute(getApplicationContext(), overlay);
            }
            mapFragment.switchCompassOnMap();

            //update distance
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (null != mapFragment) {
                        updateDistance(mapFragment.getMapRadius());
                        Utils.visibleDistance = mapFragment.getMapRadius();
                    }
                }
            };
            handler.postDelayed(runnable, 1000);
        }
    }

    private void showLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!cameraGranted) return;
        }
        // Create an instance of Camera
        Camera mCamera = getCameraInstance(getApplicationContext());
        // Create the Preview view and set it as the content of this Activity.
        mPreview = new CameraPreview(this, mCamera);
        preview.addView(mPreview);
        //layout container need to stay up. bring to up other subViews.
        overlay.bringToFront();// per sicurezza
        coordinatorLayout.bringToFront();
        seekZoom.bringToFront();
        tvBearing.bringToFront();
        tvDistance.bringToFront();
        tvDistance.setVisibility(View.VISIBLE);
        tvBearing.setVisibility(View.VISIBLE);
        seekZoom.setVisibility(View.VISIBLE);
        seekZoom.setProgressAndThumb(0);
        mapContainer = findViewById(R.id.mapContainer);
        mapContainer.setVisibility(View.VISIBLE);
        mapFragment = MapFragment.newInstance();
        filterLayout = findViewById(R.id.filterLayout);
        if (Utils.FRIEND_MODE) {
            findViewById(R.id.returnToDefault).setVisibility(View.VISIBLE);
            filterLayout.setVisibility(View.GONE);
        } else {
            linL = findViewById(R.id.filterLine);
            linL.removeAllViews();
            createFilterButton();
            filterLayout.bringToFront();
        }
        getSupportFragmentManager().beginTransaction().add(R.id.mapContainer, mapFragment).commitAllowingStateLoss();//non cambiare.
    }

    //search for location, if not lastKnown try to get location from map after some time
    //for map to obtain location (google is faster than me).
    private void showLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!locationGranted || !cameraGranted) return;
        }
        myGPSLocation = new MyGPSLocation(this, mapFragment);
        checkExistingPosition();
        tvGpsValues.setVisibility(View.VISIBLE);
        myGPSLocation.takeLocationUpdates();
    }

    public void checkExistingPosition() {
        Location lastKnown = myGPSLocation.getBestLastKnownLocation();
        if (null != lastKnown) {
            myGPSLocation.startIntentService(lastKnown);
            if ((null != Utils.places) && (0 == Utils.places.size()))
                myGPSLocation.startSearchForPlaces(lastKnown);
            Log.d("gps: ", "used lastKnownLocation");
            tvGpsValues.setText(Utils.formattedValues(lastKnown));
            Utils.myLocation = lastKnown;
            mapFragment.setCamera(lastKnown);
            updateSeekZoom(MapFragment.MAX_ZOOM_SEEK);
            MyGPSLocation.first = false;
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    if (null != mapFragment)
                        updateDistance(mapFragment.getMapRadius()); //non cambiare, la mappa in updateSeekZoom potrebbe non essere pronta
                }
            };
            handler.postDelayed(runnable, 3000);
        } else {
            tvGpsValues.setText(R.string.tvGpsValuesHint);
            Log.d("gps: ", "no last known position");
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    Log.d("post delayed: ", "search for map location..");
                    Location l = mapFragment.getMapLocation();
                    if (null != l) {
                        updateGpsTv(Utils.formattedValues(l));
                        Log.d("gps: ", "used location from map.getMyLocation");
                        myGPSLocation.startIntentService(l);
                        if ((null != Utils.places) && (0 == Utils.places.size()))
                            myGPSLocation.startSearchForPlaces(l);
                        Utils.myLocation = l;
                        mapFragment.setCamera(l);
                        updateSeekZoom(MapFragment.MAX_ZOOM_SEEK);
                        //updateDistance(mapFragment.getMapRadius());
                        MyGPSLocation.first = false;
                    } else {
                        handler.postDelayed(runnable, 3000);
                    }
                }
            };
            handler.postDelayed(runnable, 8000);
        }
    }

    private void initSensors() {
        if (null != sensorManager) {
            Sensor rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR); //include gyro
            if (null != rotationVectorSensor) {
                Log.i(TAG, "rotationVector sensor available. (TYPE_ROTATION_VECTOR)");
                sensorManager.registerListener(sensorEventListener,
                        rotationVectorSensor, SensorManager.SENSOR_DELAY_UI);
                return;
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    Sensor geoRotVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR); //without gyro
                    if (null != geoRotVectorSensor) {
                        Log.i(TAG, "geoRotVector sensor available. (TYPE_GEOMAGNETIC_ROTATION_VECTOR)");
                        sensorManager.registerListener(sensorEventListener,
                                geoRotVectorSensor, SensorManager.SENSOR_DELAY_UI);
                        return;
                    }
                }
            }
            Log.i(TAG, "Deprecated use: (TYPE_ORIENTATION)");
            sensorManager.registerListener(sensorEventListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                    SensorManager.SENSOR_DELAY_GAME);
        }
    }

    private void checkInternetConnection() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        //noinspection ConstantConditions
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if (!isConnected) {
            Toast.makeText(getApplicationContext(), R.string.checkInternet, Toast.LENGTH_SHORT).show();
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    checkInternetConnection();
                }
            };
            handler.postDelayed(runnable, 12000);
        }
    }

    private void updateBearing(SensorEvent event) {
        //todo solo 180 con rotation vector
        //todo aggiungere condizione flat screen?
        //todo true nord
        if (Sensor.TYPE_ROTATION_VECTOR == event.sensor.getType() ||
                (Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR == event.sensor.getType())) {
            float[] rotationMatrix = new float[16];
            if (Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR == event.sensor.getType()) {
                filteredValues = Utils.lowPass(event.values, filteredValues);
            } else {
                filteredValues = event.values; //with gyro can avoid to filter, it is enough stable
            }
            SensorManager.getRotationMatrixFromVector(
                    rotationMatrix, filteredValues);
//            Log.d("Getting rot matrix: ", "DONE");
            float[] remappedRotationMatrix = new float[16];
            //need to remap (x, z)
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_X, SensorManager.AXIS_Z,
                    remappedRotationMatrix);
            // Calculate Orientation
            float orientation[] = new float[3];
            SensorManager.getOrientation(remappedRotationMatrix,
                    orientation);
            //need to convert in degrees (getOrientation returns radians)
            for (int i = 0; i < 3; i++) {
                orientation[i] = (float) (Math.toDegrees(orientation[i]));
            }
            //for look through, add the rotation state
            // look through has different angles depending on rotation state
            switch (getWindowManager().getDefaultDisplay()
                    .getRotation()) {
                case Surface.ROTATION_0:
                    break;
                case Surface.ROTATION_90: {
                    orientation[2] += 90;
                    break;
                }
                case Surface.ROTATION_180: {
                    orientation[2] += 180;
                    break;
                }
                case Surface.ROTATION_270: {
                    orientation[2] += 270;
                    break;
                }
            }
//            Log.d("rotVector:", "orientation values: " + orientation[0] + ", " + orientation[1] + ", " + orientation[2]);

            Utils.usedSensor = "rotationVector";
            //qui uso yaw or azimuth
            Utils.currentBearing = Utils.normalizeBearing(orientation[0]);
            //qui uso pitch
            Utils.currentInclination = orientation[1];
            //qui uso roll
            Utils.currentRoll = orientation[2];
            //aggiorno view
            tvBearing.setText(Utils.formatBearing(orientation[0]));
            mapFragment.updateCameraBearing(orientation[0]);

        } else {
            //for old devices
            //only orientation
            float bearing = 0;
//          Log.d("event values: ", event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
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
            Utils.currentBearing = bearing;
        }
    }

    //used to restart activity and return to classic use
    public void returnToDefault(View view) {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        //noinspection ConstantConditions  //forse non tutti necessari
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        Utils.FRIEND_MODE = false;
        Utils.places = new ArrayList<>();
        finish();
        startActivity(i);
    }

    public void showHelp(View v) {
        Toast.makeText(getApplicationContext(), "Coming soon..", Toast.LENGTH_SHORT).show();
    }

    public void showPlacesFilters(View v) {
        if (View.VISIBLE == linL.getVisibility()) { //filtri visibili, spariscono
            linL.setVisibility(View.GONE);
            ((ImageButton) v).setImageResource(R.drawable.search);
        } else { //filtri invisibili, vengono mostrati
            linL.setVisibility(View.VISIBLE);
            ((ImageButton) v).setImageResource(R.drawable.close);
        }
    }

    private void createFilterButton() {
        for (int i = 0; i < Utils.placesFilter.length; i++) {
            ImageButton ib = new ImageButton(this);
            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 10, 0);
            ib.setLayoutParams(lp);
            ib.setTag(Utils.placesFilter[i]);
            if (ib.getTag().toString().equals(Utils.PLACES_TO_SEARCH)) {
                ib.setColorFilter(Color.WHITE);
            }
            ib.setImageResource(Utils.getImageId(this, ib.getTag().toString()));
            ib.setBackgroundColor(Color.TRANSPARENT);
            ib.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (Utils.PLACES_TO_SEARCH.equals(view.getTag().toString()))
                        return; //se uguale inutile ricominciare la ricerca
                    ViewGroup v = ((ViewGroup) view.getParent());
                    for (int i = 0; i < v.getChildCount(); i++) {
                        View child = v.getChildAt(i);
                        if (child.getTag().toString().equals(Utils.PLACES_TO_SEARCH))
                            ((ImageButton) child).setColorFilter(Color.parseColor("#cecece"));
                    }
                    ((ImageButton) view).setColorFilter(Color.WHITE);
                    Utils.PLACES_TO_SEARCH = view.getTag().toString();
                    Log.d("click on filter: ", Utils.PLACES_TO_SEARCH);
                    myGPSLocation.stopSearchForPlaces();
                    ConnectivityManager cm =
                            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    //noinspection ConstantConditions
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    boolean isConnected = activeNetwork != null &&
                            activeNetwork.isConnectedOrConnecting();
                    if (isConnected && (null != Utils.myLocation)) {
                        myGPSLocation.startSearchForPlaces(Utils.myLocation);
                        Log.d(TAG, "now searching for filtered places..");
                    }
                }
            });
            linL.addView(ib);
        }
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
                cameraGranted = true;
                errCamera.setVisibility(View.GONE);
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }
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
                    handler.postDelayed(runnable, 2000);
                }
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.");
                coordinatorLayout.bringToFront();
                errCamera.setVisibility(View.VISIBLE);
                errCamera.bringToFront();
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
                locationGranted = true;
                tvGpsValues.setText(R.string.tvGpsValuesHint);
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }
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
