package com.example.antonio.arprova;

import android.graphics.Color;
import android.location.Location;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by Antonio on 01/02/2018.
 * .
 */

public class Place implements Serializable {

    private String nome;
    private Location locationData;
    private int color;

    Place(String nome, double lat, double lng) {
        this.nome = nome;
        this.locationData = new Location("mockProvider");
        locationData.setLatitude(lat);
        locationData.setLongitude(lng);
        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        this.color = Color.rgb(r, g, b);
    }

    String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    Location getLocationData() {
        return locationData;
    }

    public void setLocationData(Location locationData) {
        this.locationData = locationData;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    double getLatitude() {
        return this.locationData.getLatitude();
    }

    double getLongitude() {
        return this.locationData.getLongitude();
    }
}
