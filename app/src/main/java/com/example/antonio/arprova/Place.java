package com.example.antonio.arprova;

import android.location.Location;

/**
 * Created by Antonio on 01/02/2018.
 * .
 */

public class Place {

    private String nome;
    private Location locationData;

    public Place(String nome, double lat, double lng) {
        this.nome = nome;
        this.locationData = new Location("mockProvider");
        locationData.setLatitude(lat);
        locationData.setLongitude(lng);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Location getLocationData() {
        return locationData;
    }

    public void setLocationData(Location locationData) {
        this.locationData = locationData;
    }

    public double getLatitude() {
        return this.locationData.getLatitude();
    }

    public double getLongitude() {
        return this.locationData.getLongitude();
    }
}
