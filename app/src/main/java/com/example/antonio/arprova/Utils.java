package com.example.antonio.arprova;

import android.Manifest;
import android.content.Context;
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

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Antonio on 19/01/2018.
 * .
 */

public class Utils {

    static final int MY_PERMISSIONS_REQUEST_ACCESS_CAMERA = 123;
    static final int MY_PERMISSIONS_REQUEST_ACCESS_LOC = 321;
    static final float SMOOTHING_FACTOR_COMPASS = 0.8f; //needs between 0, 1.
    static final int SMALL_MAP_DIMEN = 100;
    static final int BIG_MAP_DIMEN_PORTRAIT = 500;
    static final int BIG_MAP_DIMEN_LAND = 400;
    public static Location myLocation = null;
    static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION};
    static float BEARING_OFFSET = 28f; //degrees
    static float visibleDistance;
    static float currentBearing;
    static ArrayList<Place> mockPlaces = new ArrayList<Place>() {{
        add(new Place("Mensa Universitaria", 40.7729432, 14.7938988));
        add(new Place("Biblioteca Scientifica", 40.7724951, 14.7889083));
        add(new Place("Piazza del Sapere", 40.7705566, 14.7924083));
        add(new Place("Bar Saperi & Sapori", 40.7752657, 14.7883457));
    }};

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

    static String formatDistance(float baseDistance) {
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

    //todo da sistemare
    //to change color of a place marker
    static Bitmap changeBitmapColor(Resources res, int color) {
        int strokeWidth = 5;
        //cambio colore con filter
        Bitmap sourceBitmap = BitmapFactory.decodeResource(res,
                R.drawable.base_marker);
        Bitmap resultBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0,
                sourceBitmap.getWidth() - 1, sourceBitmap.getHeight() - 1);
        Paint p = new Paint();
        ColorFilter filter = new LightingColorFilter(color, 0);
        p.setColorFilter(filter);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(resultBitmap, 0, 0, p);
        //fine cambio colore con filter

        //inizio ad aggiungere stroke
        Bitmap resultWithStroke = Bitmap.createBitmap(sourceBitmap.getWidth() - 1 + strokeWidth,
                sourceBitmap.getHeight() - 1 + strokeWidth, Bitmap.Config.ARGB_8888);

        p = new Paint();
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.BLACK);
        p.setStrokeWidth(strokeWidth);
        p.setAntiAlias(true);
        canvas = new Canvas(resultWithStroke);
        canvas.drawCircle(canvas.getWidth() - strokeWidth / 2, canvas.getWidth() - strokeWidth / 2,
                canvas.getWidth() - strokeWidth / 2, p); //tutto sbagliato ma bello
//        canvas.drawCircle(((canvas.getWidth()) - (2 * strokeWidth)) / 2, ((canvas.getHeight()) - (2 * strokeWidth)) / 2,
//                ((canvas.getWidth()) - (2 * strokeWidth)) / 2, p);
        canvas.drawBitmap(resultBitmap, strokeWidth, strokeWidth, null);
        //fine aggiunta stroke
        //to free memory
        sourceBitmap.recycle();
        return resultWithStroke;
    }
}
