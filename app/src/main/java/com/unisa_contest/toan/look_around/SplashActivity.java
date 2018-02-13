package com.unisa_contest.toan.look_around;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.unisa_contest.toan.look_around.places.Place;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //noinspection ConstantConditions
        if (getIntent().getAction().equals(Intent.ACTION_VIEW)) { //avvio da location link
            Utils.FIND_FRIEND_MODE = true;
            String data = getIntent().getDataString();
            //noinspection ConstantConditions
            String[] latLng = data.substring(data.lastIndexOf("loc:") + 4).split(",");
            Utils.places.add(new Place(getString(R.string.customPlace), Double.parseDouble(latLng[0]),
                    Double.parseDouble(latLng[1]), "customAddress")); //todo search address with service
        }
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
