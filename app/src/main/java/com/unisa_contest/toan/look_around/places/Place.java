package com.unisa_contest.toan.look_around.places;

import android.graphics.Color;
import android.location.Location;

import java.io.Serializable;
import java.util.Random;

/**
 * Created by Antonio on 01/02/2018.
 * .
 */

public class Place implements Serializable {

    private String nome, indirizzo;
    private Location locationData;
    private int color;

    Place(String nome, double lat, double lng, String indirizzo) {
        this.nome = nome;
        this.indirizzo = indirizzo;
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

    String getIndirizzo() {
        return indirizzo;
    }

    Location getLocationData() {
        return locationData;
    }

    double getLatitude() {
        return this.locationData.getLatitude();
    }

    double getLongitude() {
        return this.locationData.getLongitude();
    }

    public int getColor() {
        return color;
    }

    @Override
    public String toString() {
        return "Place{" +
                "nome='" + nome + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                '}';
    }
}
