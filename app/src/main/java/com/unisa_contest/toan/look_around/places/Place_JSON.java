package com.unisa_contest.toan.look_around.places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Antonio on 09/02/2018.
 * .
 */

class Place_JSON {

    /**
     * Receives a JSONObject and returns a list
     */
    ArrayList<Place> parse(JSONObject jObject) {

        JSONArray jPlaces = null;
        try {
            /*
             * Retrieves all the elements in the 'places' array
             */
            jPlaces = jObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*
         * Invoking getPlaces with the array of json object
         * where each json object represent a place
         */
        return getPlaces(jPlaces);
    }

    private ArrayList<Place> getPlaces(JSONArray jPlaces) {
        int placesCount = jPlaces.length();
        ArrayList<Place> placesList = new ArrayList<>();
        Place place;
        // Taking each place, parses and adds to list object
        for (int i = 0; i < placesCount; i++) {
            try {
                /*
                 * Call getPlace with place JSON object to parse the place
                 */
                place = getPlace((JSONObject) jPlaces.get(i));
                placesList.add(place);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    /**
     * Parsing the Place JSON object
     */
    private Place getPlace(JSONObject jPlace) {

        Place place = null;
        String placeName = "-NA-";
        String vicinity = "-NA-"; //or formatted_address for complete address
        double latitude;
        double longitude;

        try {
            // Extracting Place name, if available
            if (!jPlace.isNull("name")) {
                placeName = jPlace.getString("name");
            }
            // Extracting Place Vicinity, if available
            if (!jPlace.isNull("vicinity")) {
                vicinity = jPlace.getString("vicinity");
            }
            latitude = jPlace.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
            longitude = jPlace.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
            //reference = jPlace.getString("reference");

            place = new Place(placeName, latitude, longitude, vicinity);

        } catch (JSONException e) {
            e.printStackTrace();
        }
//        Log.d("Place_JSON: ", "getPlace: " + place.toString());
        return place;
    }
}
