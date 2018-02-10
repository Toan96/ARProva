package com.unisa_contest.toan.look_around.places;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Antonio on 09/02/2018.
 * .
 */

public class PlacesASync extends AsyncTask<String, Integer, String> {

    // Invoked by execute() method of this object
    @Override
    protected String doInBackground(String... url) {
        String data = null;
        try {
            data = downloadUrl(url[0]);
        } catch (Exception e) {
            Log.d("Background Task", e.toString());
        }
        return data;
    }

    // Executed after the complete execution of doInBackground() method
    @Override
    protected void onPostExecute(String result) {
        ParserASync parserASync = new ParserASync();
        // Start parsing the Google places in JSON format
        // Invokes the "doInBackground()" method of the class Parser.
        parserASync.execute(result);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder(); //before StringBuffer
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.d("PlacesAsync downloadURL", "Exception while downloading url" + e.toString());
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
//        Log.d("PlacesAsync downloadURL", data);
        return data;
    }
}
