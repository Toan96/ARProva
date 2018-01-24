package com.example.antonio.arprova;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class MyGPSLocation {

    protected static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOC = 123;
    protected static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOC = 321;
    private static final long MIN_TIME = 5 * 1000;
    private static final long MIN_DISTANCE = 5;
    private static String TAG = "MyGPSLocation";
    private Context context;
    private LocationManager locationManager;
    private Runnable runnable;
    private Handler handler = null;
    private UpdateUICallback updateUICallback = null;
    private AlertDialog.Builder dialog = null;

    //listener per gps updates
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d("gps: ", "location changed");
            final double longitude = location.getLongitude();
            final double latitude = location.getLatitude();
            final double altitude = location.getAltitude();
            final double bearing = location.getBearing();
            final double accuracy = location.getAccuracy();

            final String values = "Alt: " + altitude + " m" + System.getProperty("line.separator") +
                    "Lat: " + latitude + System.getProperty("line.separator") + "Lon: " + longitude;

            updateUICallback.updateGpsTv(values);
            Log.d("gps values changed", values + " Bear: " + bearing + " Accu: " + accuracy);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d("gps: ", "status changed");
            takeLocationUpdates();
        }

        @Override
        public void onProviderEnabled(String s) {
            Log.d("gps: ", "enabled");
            takeLocationUpdates();
        }

        @Override
        public void onProviderDisabled(String s) {
            Log.d("gps: ", "disabled");
            String string = context.getString(R.string.tvGpsValuesHint);
            updateUICallback.updateGpsTv(string);
            Log.d(TAG, "updated tvGpsValues");
            //TODO check se ora toast rispetta tema
            Toast.makeText(context.getApplicationContext(), R.string.dialogLocation_Title, Toast.LENGTH_SHORT).show();
        }
    };

    public MyGPSLocation(Context context, UpdateUICallback uiCallback) {
        this.context = context;
        this.updateUICallback = uiCallback;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    //TODO se abilito a mano dopo chiudi prima volta non funziona..listener non creato
    //TODO se funziona handler alert evitabile, dopo 7 secondi richiama funzione trova abilitato e va(si perde solo un o di tempo)
    //metodi per gps
    public void takeLocationUpdates() {
        if (!checkLocation())
            return;
        Log.d(TAG, "takeLocationUpdates");
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_HIGH);//TODO check..prima era fine
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);//TODO check..prima era Medium
        String provider = locationManager.getBestProvider(criteria, true);
        if (provider != null) {
            //in questo modo se > di marshmallow e utente rifiuta non si verificano problemi.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Log.d(TAG + " permission", "need android m runtime permissions");
                if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Utils.requestPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOC, (Activity) context);
                }

                if (context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Utils.requestPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION, MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOC, (Activity) context);
                }
            }
        }
        //forse qui veniva chiamato troppe volte inutilmente(impossibile vedere). removeupdates per evitare (spero).
        locationManager.removeUpdates(locationListener);
        locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, locationListener);
        Log.d("Best location provider", provider);
    }

    //metodi per verificare la presenza della geolocalizzazione e abilitarla in caso negativo.
    private boolean checkLocation() {
        if (!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    //TODO da rivedere magari migliorare
    private void showAlert() {
        if (null == dialog) {
            dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.dialogLocation_Title)
                    .setMessage(R.string.dialogLocation_Message)
                    .setPositiveButton(R.string.dialogLocation_Positive, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            context.startActivity(myIntent);
                        }
                    });
        }
        dialog.setNegativeButton(R.string.dialogLocation_Negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                if (null == handler) {
                    handler = new Handler();
                    runnable = new Runnable() {
                        @Override
                        public void run() {
                            takeLocationUpdates();
                        }
                    };
                    handler.postDelayed(runnable, 7000);
                }
            }
        });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void removeHandler() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    public LocationManager getLocationManager() {
        return locationManager;
    }

    public void stopUpdates() {
        locationManager.removeUpdates(locationListener);
    }
}
