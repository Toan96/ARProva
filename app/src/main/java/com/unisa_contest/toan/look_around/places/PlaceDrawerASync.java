package com.unisa_contest.toan.look_around.places;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.unisa_contest.toan.look_around.Utils;

import java.util.ArrayList;

import static android.os.SystemClock.sleep;

/**
 * Created by Antonio on 04/02/2018.
 * .
 */

public class PlaceDrawerASync extends AsyncTask<Object, Object, Object> {

    private int width, height;

    @Override
    protected void onPreExecute() {
        Log.d("ASync: ", "started");
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d("ASync: ", "doInBackground");
        while (Utils.myLocation == null) {
            if (isCancelled()) return null; //exit when onPause called
            Log.d("ASync: ", "waiting for location...");
            sleep(3000);
        }
//todo inutile eliminare probabilmente
        while (0 == Utils.places.size()) {
            if (isCancelled()) return null; //exit when onPause called
            Log.d("ASync: ", "waiting for places...");
            sleep(3000);
        }

        Context context = (Context) objects[0];
        ViewGroup view = (ViewGroup) objects[1];
        ArrayList<PlaceTag> toDraw = new ArrayList<>();
        //take screen size
        //noinspection ConstantConditions
        width = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        //noinspection ConstantConditions
        height = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
        float distanceTo, bearingTo, bearTo;
        boolean isLastZero = false; //to avoid polling and continuous logging

        while (true) {
            if (isCancelled()) return null; //exit when onPause called
            for (Place p : Utils.places) {
                distanceTo = Utils.myLocation.distanceTo(p.getLocationData());
                //se la distanza di place > di raggio della mappa allora non mostrare, vai avanti
                if (distanceTo > Utils.visibleDistance || distanceTo < 0)
                    continue;
                bearTo = Utils.myLocation.bearingTo(p.getLocationData());
                bearingTo = Utils.normalizeBearing(bearTo); //altrimenti non effettua il calcolo corretto
                //se orientamento di place troppo distante dal mio allora non mostrare
                if ((bearingTo > (Utils.currentBearing + Utils.BEARING_OFFSET)) ||
                        (bearingTo < (Utils.currentBearing - Utils.BEARING_OFFSET)))
                    continue;
                toDraw.add(new PlaceTag(context, p, calculateX(bearingTo), calculateY(distanceTo), Utils.formatDistance(distanceTo)));
            }
            if (toDraw.size() > 0) {
                publishProgress(view, toDraw.clone());//todo maybe better solution instead of clone, gc is called often
                toDraw.clear();
                isLastZero = false;
            } else {
                if (!isLastZero) {
                    publishProgress(view, "zero");
                    isLastZero = true;
                }
            }
        }
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        ((ViewGroup) values[0]).removeAllViews();
        if (values[1].equals("zero")) {
            Log.d("ASync update: ", "No places to draw");
        } else {
            Log.d("ASync update: ", "places to draw: " + ((ArrayList) values[1]).size());
            // noinspection unchecked
            for (PlaceTag pt : ((ArrayList<PlaceTag>) values[1]))
                ((ViewGroup) values[0]).addView(pt);            //concurrentModificationException solved but used clone
        }
    }

    @Override
    protected void onPostExecute(Object result) {
        Log.d("ASync: ", "onPostExecute " + result);
    }

    private int calculateY(float distanceTo) { //todo aggiungere altri livelli probabilmente
        int inclination = 0;
        if ((null != Utils.usedSensor) && (Utils.usedSensor.equals("rotationVector"))) {
            inclination = (int) (((height * -Utils.currentInclination) / (Utils.INCLINATION_OFFSET * 2)));
            //inclination = inclination + (width / 2);
        }
        if (distanceTo < 250)
            return height - (height / 5) + inclination;
        else if (distanceTo < 800)
            return height - (height / 3) + inclination;
        else return height / 2 + inclination;
    }

    private int calculateX(float bearingTo) {
        float currOffset;
        if (Utils.currentBearing > bearingTo) {                      //caso 1
            if (Utils.currentBearing > (bearingTo + 180)) {          //caso 1.1 positivo
                currOffset = ((bearingTo - Utils.currentBearing) + 360); //devo disegnare a destra della meta dello schermo
            } else {//(Utils.currentBearing <= (bearingTo + 180))    //caso 1.2 negativo
                currOffset = bearingTo - Utils.currentBearing;           //devo disegnare a sinistra della meta dello schermo
            }
        } else {//Utils.currentBearing <= bearingTo)                 //caso 2
            if ((Utils.currentBearing + 180) < bearingTo) {          //caso 2.1 negativo
                currOffset = ((bearingTo - Utils.currentBearing) - 360); //devo disegnare a sinistra della meta dello schermo
            } else {//((Utils.currentBearing + 180) <= bearingTo)    //caso 2.2 positivo
                currOffset = bearingTo - Utils.currentBearing;           //devo disegnare a destra della meta dello schermo
            }
        }
        currOffset = (((width * currOffset) / (Utils.BEARING_OFFSET * 2))); //todo inversamente proporzionale alla distanza
//        Log.d("Async back: ", "calc x: " + currOffset);
        return (int) currOffset + (width / 2);
    }
}
