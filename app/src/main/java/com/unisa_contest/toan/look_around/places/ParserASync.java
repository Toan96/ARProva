package com.unisa_contest.toan.look_around.places;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.unisa_contest.toan.look_around.Utils;

import org.json.JSONObject;

import java.util.ArrayList;

import static android.os.SystemClock.sleep;

/**
 * Created by Antonio on 09/02/2018.
 * .
 */

public class ParserASync extends AsyncTask<String, Integer, ArrayList<Place>> {

    // Invoked by execute() method of this object
    @Override
    protected ArrayList<Place> doInBackground(String... jsonData) {

        JSONObject jObject;
        ArrayList<Place> places = null;
        Place_JSON placeJson = new Place_JSON();

        try {
            jObject = new JSONObject(jsonData[0]);

            places = placeJson.parse(jObject);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        }
        return places;
    }

    // Executed after the complete execution of doInBackground() method
    @Override
    protected void onPostExecute(ArrayList<Place> list) {

        Log.d("Map", "list size: " + list.size());
        //waiting for map ready
        while (null == Utils.map) {
            sleep(2000);
        }
        Utils.map.clear(); //Clears all the existing markers;

        Utils.places = list;//used in drawerAsync

        //use setTag on marker to link with a place (data are in json)
        for (Place p : list) {
            Log.d("MapFragment: ", "adding markers.. " + p.getLatitude() + ", " + p.getLongitude());
            Utils.map.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getLatitude(), p.getLongitude()))
                    .title(p.getNome())
                    .icon(BitmapDescriptorFactory.fromBitmap(Utils.changeBitmapColor(p.getColor())))
            );
        }
    }
}
