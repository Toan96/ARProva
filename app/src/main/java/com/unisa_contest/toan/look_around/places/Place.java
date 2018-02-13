package com.unisa_contest.toan.look_around.places;

import android.graphics.Color;
import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Random;

/**
 * Created by Antonio on 01/02/2018.
 * .
 */

public class Place implements Parcelable {

    public static final Creator<Place> CREATOR = new Creator<Place>() {
        @Override
        public Place createFromParcel(Parcel in) {
            return new Place(in);
        }

        @Override
        public Place[] newArray(int size) {
            return new Place[size];
        }
    };
    private String nome, indirizzo;
    private Location locationData;
    private int color;

    public Place(String nome, double lat, double lng, String indirizzo) {
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

    private Place(Parcel in) {
        nome = in.readString();
        indirizzo = in.readString();
        locationData = in.readParcelable(Location.class.getClassLoader());
        color = in.readInt();
    }

    public String getNome() {
        return nome;
    }

    String getIndirizzo() {
        return indirizzo;
    }

    Location getLocationData() {
        return locationData;
    }

    public double getLatitude() {
        return this.locationData.getLatitude();
    }

    public double getLongitude() {
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nome);
        dest.writeString(indirizzo);
        dest.writeParcelable(locationData, flags);
        dest.writeInt(color);
    }
}
