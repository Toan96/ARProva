package com.example.antonio.arprova;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import static com.example.antonio.arprova.CameraPreview.getCameraInstance;

public class MainActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private LocationManager locationManager;
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

        //gps
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        tvGpsValues = findViewById(R.id.tvGpsValues);

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

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        //TODO: da sistemare, sicuramente poco efficiente.
        ((ViewGroup) tvGpsValues.getParent()).removeView(tvGpsValues);
        preview.addView(tvGpsValues);
        takeLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPreview.releaseCamera();              // release the camera immediately on pause event
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    //metodi per gps
    public void takeLocationUpdates() {
        if (!checkLocation())
            return;

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            //in questo modo se > di marshmallow e utente rifiuta non si verificano problemi.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
            }
            locationManager.requestLocationUpdates(provider, 2 * 60 * 1000, 10, locationListener.get());
            Log.d("gps", provider);
        }
    }

    //metodi per verificare la presenza della geolocalizzazione e abilitarla in caso negativo.
    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.dialogLocation_Title)
                .setMessage(R.string.dialogLocation_Message)
                .setPositiveButton(R.string.dialogLocation_Positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton(R.string.dialogLocation_Negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    //variabile: listener per gps updates
    private final ThreadLocal<LocationListener> locationListener = new ThreadLocal<LocationListener>() {
        @Override
        protected LocationListener initialValue() {
            return new LocationListener() {
                public void onLocationChanged(Location location) {
                    final double longitude = location.getLongitude();
                    final double latitude = location.getLatitude();
                    final double altitude = location.getAltitude();
                    final String values = "Alt: " + altitude + System.getProperty("line.separator") +
                            "Lat: " + latitude + System.getProperty("line.separator") + "Lon: " + longitude;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvGpsValues.setText(values);
                            //do nothing else yet.
                        }
                    });
                    Log.d("gpsValues", values);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };
        }
    };
}
