package com.unisa_contest.toan.look_around;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.unisa_contest.toan.look_around.places.Place;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        Log.d("action", "" + getIntent().getAction());
        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) { //avvio da location link
            Utils.FRIEND_MODE = true;
            String data = getIntent().getDataString();
            //noinspection ConstantConditions
            String[] latLng = data.substring(data.lastIndexOf("loc:") + 4).split(",");
            boolean toAdd = true;
            for (Place p : Utils.places) {
                if (("" + p.getLatitude()).equals(latLng[0]) && ("" + p.getLongitude()).equals(latLng[1])) {
                    toAdd = false;
                    break;
                }
            }
            if (toAdd) {
                Utils.places.add(new Place(getString(R.string.customPlace).concat(" " + (Utils.places.size() + 1)), Double.parseDouble(latLng[0]),
                        Double.parseDouble(latLng[1]), "Finding address..."));
            }
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
