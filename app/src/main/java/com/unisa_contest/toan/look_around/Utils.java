package com.unisa_contest.toan.look_around;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.unisa_contest.toan.look_around.places.Place;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class Utils {

    //used for permissions
    static final int MY_PERMISSIONS_REQUEST_ACCESS_CAMERA = 123;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_LOC = 321;
    //used for map fragment
    static final int SMALL_MAP_DIMEN = 100;
    static final int BIG_MAP_DIMEN_PORTRAIT = 500;
    static final int BIG_MAP_DIMEN_LAND = 400;
    //used for AR
    private static final float ALPHA = 0.25f;//used for LowPass Filter, if ALPHA = 1 OR 0, no filter applies, less is smoother but also slower.
    //used to access map from PlacesASync
    public static GoogleMap map = null;
    public static Location myLocation = null;
    public static float BEARING_OFFSET = 28f; //28 degrees is default before getViewAngle
    public static float INCLINATION_OFFSET = 35f; //35 degrees is default before getViewAngle
    public static float visibleDistance;
    public static float currentBearing;
    public static float currentInclination;
    public static float currentRoll;
    public static String usedSensor;
    //mockPlaces
    public static ArrayList<Place> places = new ArrayList<>(); /*new ArrayList<Place>(); {{
        add(new Place("Mensa Universitaria", 40.7729432, 14.7938988, "mockAddress"));
        add(new Place("Biblioteca Scientifica", 40.7724951, 14.7889083, "mockAddress"));
        add(new Place("Piazza del Sapere", 40.7705566, 14.7924083, "mockAddress"));
        add(new Place("Bar Saperi & Sapori", 40.7752657, 14.7883457, "mockAddress"));
        add(new Place("Pizzeria Sant'Antonio", 40.3405952, 15.3346918, "mockAddress"));
    }};
*/
    //used for shared locations
    public static boolean FRIEND_MODE = false;
    //used for json query
    static String PLACES_TO_SEARCH = "point_of_interest"; // "restaurant"
    static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    static boolean BIG_MAP = false;
    //used to draw marker
    static Resources res;
    //places filters
    //per aggiungere filtri aggiungere qui e creare icona
    static String[] placesFilter = {"point_of_interest", "restaurant", "bar", "museum", "gas_station"};

    //to take margin in pixels.
    static int dpToPixels(Context context, int dpValue) {
        float d = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * d); // margin in pixels
    }

    //to verify multiple location permissions.
    static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if (grantResults.length < 1) {
            Log.d("grantResults length: ", "too short " + grantResults.length);
            return false;
        }
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                Log.d("grantResults: ", result + " NOT granted");
                return false;
            }
        }
        Log.d("grantResults:", "ALL permissions granted");
        return true;
    }

    //to take formatted string for gps values
    public static String formattedValues(Location location) {
        final double longitude = location.getLongitude();
        final double latitude = location.getLatitude();
        final double altitude = location.getAltitude();
        final double bearing = location.getBearing();
        final double accuracy = location.getAccuracy();

        final String values = "Alt: " + String.format(Locale.getDefault(), "%.1f", altitude) + " m" + System.getProperty("line.separator") +
                "Lat: " + String.format(Locale.getDefault(), "%.6f", latitude) + System.getProperty("line.separator") +
                "Lon: " + String.format(Locale.getDefault(), "%.6f", longitude);

        Log.d("gps values changed", values + " Bear: " + String.format("%.2f", bearing) + " Accu: " + String.format("%.2f", accuracy));
        return values;
    }

    static String formatBearing(float baseAzimuth) {

        if (baseAzimuth < 0)
            baseAzimuth += 360;
        //Set the field
        String bearingText;
        if (baseAzimuth > 360)
            baseAzimuth %= 360;

        if ((360 >= baseAzimuth && baseAzimuth >= 337.5) || (0 <= baseAzimuth && baseAzimuth <= 22.5))
            bearingText = "N";//N
        else if (baseAzimuth > 22.5 && baseAzimuth < 67.5) bearingText = "NE";
        else if (baseAzimuth >= 67.5 && baseAzimuth <= 112.5) bearingText = "E";
        else if (baseAzimuth > 112.5 && baseAzimuth < 157.5) bearingText = "SE";
        else if (baseAzimuth >= 157.5 && baseAzimuth <= 202.5) bearingText = "S";
        else if (baseAzimuth > 202.5 && baseAzimuth < 247.5) bearingText = "SW";
        else if (baseAzimuth >= 247.5 && baseAzimuth <= 292.5) bearingText = "W";
        else if (baseAzimuth > 292.5 && baseAzimuth < 337.5) bearingText = "NW";
        else bearingText = "?";

        //Log.i("Utils: format bearing", "rotation sensor azimuth " + baseAzimuth + ": " + bearingText);
        return (int) baseAzimuth + "° " + bearingText;
    }

    //used to apply a low pass filter to rotation vector data
    static float[] lowPass(float[] input, float[] output) {
        if (output == null) return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = (output[i] + ALPHA * (input[i] - output[i]));
        }
        return output;
    }

    public static float normalizeBearing(float bearingTo) {
        if (bearingTo > 360)
            bearingTo %= 360;
        if (bearingTo < 0)
            //non cambiare, dovrebbe funzionare perche bearingTo è negativo
            bearingTo = 360 + bearingTo;
        return bearingTo;
    }

    public static String formatDistance(float baseDistance) {
        int distance = (int) baseDistance;
        if (distance == 0) {
            //do nothing, return ~ by static layout
            return ("~ km");
        } else if (distance > 1000) {
            distance /= 1000;
            return ("~" + distance + " km");
        } else {
            return ("~" + ((distance + 5) / 10) * 10 + " m"); //round to 10th
        }
    }

    //to change color of a place marker
    public static Bitmap changeBitmapColor(int color) {
        int dim = 20; //dimension in pixel of a map marker;
        //cambio colore con filter
        Bitmap sourceBitmap = BitmapFactory.decodeResource(res,
                R.drawable.base_marker);
        Bitmap resultBitmap = Bitmap.createScaledBitmap(sourceBitmap, dim, dim, false);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 0);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
        //to free memory
        sourceBitmap.recycle();
        return resultBitmap;
    }

    //return URL for JSON places data
    public static String sbMethod(Activity main, Location location) {
        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" +
                location.getLatitude() + "," + location.getLongitude() +
                "&radius=2000" + //"&sensor=true" seems deprecated
                "&types=" + PLACES_TO_SEARCH +
                "&key=" + getMetadata(main);
    }

    //to avoid to write api key in code
    private static String getMetadata(Context context) {
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                return appInfo.metaData.getString("com.google.android.maps.v2.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
            // if we can’t find it in the manifest, just return null
        }
        return null;
    }

    //to check if color is dark or light
    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness < 0.5) {
            return false; // It's a light color
        } else {
            return true; // It's a dark color
        }
    }

    //to take filter image programmatically
    static int getImageId(Context context, String imageName) {
        return context.getResources().getIdentifier("drawable/" + imageName, null, context.getPackageName());
    }

}
