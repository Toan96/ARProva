package com.unisa_contest.toan.look_around;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.unisa_contest.toan.look_around.places.Place;

/**
 * Created by Antonio on 19/01/2018.
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {

    public static final int MAX_ZOOM_SEEK = 5; //necessary in another package
    static final int DEFAULT_ZOOM = 11;
    static final int MAX_ZOOM = 16;
    static final int MIN_ZOOM = 11;
    static boolean first = true;
    static GoogleMap map;
    MapView mapView;
    private OnFragmentInteractionListener mListener;

    public MapFragment() {// Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MapFragment.
     */
    public static MapFragment newInstance() {
/*        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
*/
        return new MapFragment();//fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_map, container, false);

        //noinspection deprecation
        int statusCode = com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.getActivity());
        Log.d("MapFragment: ", "Connection Result = " + statusCode);

        // Gets the MapView from the XML layout and creates it
        mapView = v.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        try {
            MapsInitializer.initialize(this.getActivity());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else if (this instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) this;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap map) {
        MapFragment.map = map;
        Utils.map = map;
        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(41.89, 12.51), DEFAULT_ZOOM);
        map.animateCamera(cameraUpdate);
        map.setMinZoomPreference(MIN_ZOOM);
        map.setMaxZoomPreference(MAX_ZOOM);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.d("OnMapReady: ", "Not Android M, set my location enabled");
            map.setMyLocationEnabled(true);
        } else {
            if (this.getContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    this.getContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                map.setMyLocationEnabled(true);
                Log.d("Location permission: ", "granted");
            }
        }
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(false);
        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        setCamera(Utils.myLocation);  //in questo modo se c'è myLocation verra anche centrato sulla mappa (altrimenti mi porta sul mare)
        mListener.updateDistance(getMapRadius()); //forse non necessario
        if (null != Utils.myLocation) {
            setZoomLevel(MAX_ZOOM_SEEK); //serve non toccare per sicurezza e per evitare solo "~ km" in tvDistance
            mListener.updateSeekZoom(MAX_ZOOM_SEEK);
        }
        //use setTag on marker to link with a place
        if (Utils.places.size() > 0)
            for (Place p : Utils.places) {
                Log.d("MapFragment: ", "adding markers.. " + p.getLatitude() + ", " + p.getLongitude());
                map.addMarker(new MarkerOptions()
                        .position(new LatLng(p.getLatitude(), p.getLongitude()))
                        .title(p.getNome())
                        .icon(BitmapDescriptorFactory.fromBitmap(Utils.changeBitmapColor(p.getColor())))
                );
            }

        map.setOnMapClickListener(this);
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d("MapFragment: ", "Click on map");
                if (!Utils.BIG_MAP)
                    mListener.showMap();
                else marker.showInfoWindow();
                return true;
            }
        });
//      map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (null != map) {
            Log.d("MapFragment: ", "Click on map");
            mListener.showMap();
        }
    }

    @SuppressWarnings("all")
    public void setCamera(Location location) {
        if (null != map) {
            CameraUpdate cameraUpdate;
            if (null != location && first) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAX_ZOOM);
                first = false;
            } else if ((null != location) && !first) {
                cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
            } else {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(41.89, 12.49), DEFAULT_ZOOM); //Roma, Colosseo
            }
            map.moveCamera(cameraUpdate); //oppure animate ma crea maggiori problemi a causa della durata

            if (!map.isMyLocationEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (null != getContext()) {
                        if (getContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                getContext().checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            return;
                    } else return;
                }
                map.setMyLocationEnabled(true);
            }
        }
    }

    public void setZoomLevel(int zoomLevel) {
        if (null != map) {
            Location location;
            try {
                //first time can't access map location
                //noinspection deprecation
                location = map.getMyLocation();
            } catch (RuntimeException e) {
                location = Utils.myLocation;
            }
            if (null != location) {
                Log.d("MapFragment: ", "SetZoomLevel with location");
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                        location.getLongitude()), zoomLevel + MIN_ZOOM));
            } else {
                Log.d("MapFragment: ", "SetZoomLevel without location");
                map.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel + MIN_ZOOM));
            }
            Log.d("Zoom Level changed: ", "now on map is " + String.valueOf(map.getCameraPosition().zoom));
        }
    }

    public void updateCameraBearing(float bearing) {
        if (null == map) return;
        CameraPosition camPos = CameraPosition
                .builder(
                        map.getCameraPosition() // current Camera
                )
                .bearing(bearing)
                .build();
        map.moveCamera(CameraUpdateFactory.newCameraPosition(camPos));
    }

    public float getMapRadius() {
        if (null != map) {
            VisibleRegion region = map.getProjection().getVisibleRegion();
            //calcolo la latitudine del centro e la longitudine dell'angolo in basso a sinistra,
            // combinandoli ottengo raggio.
            double centerLat = region.latLngBounds.getCenter().latitude;
            double leftLong = region.latLngBounds.southwest.longitude;
            Location middleLeftCornerLocation = new Location("corner");
            middleLeftCornerLocation.setLatitude(centerLat);
            middleLeftCornerLocation.setLongitude(leftLong);
            Location center = new Location("center");
            center.setLatitude(region.latLngBounds.getCenter().latitude);
            center.setLongitude(region.latLngBounds.getCenter().longitude);
            return center.distanceTo(middleLeftCornerLocation); //return distance between middleLeftCorner and center
        }
        return 0;
    }

    public Location getMapLocation() {
        try {
            //noinspection deprecation
            return map.getMyLocation();
        } catch (RuntimeException e) {
            Log.d("MapFragment: ", "getMapLocation Exception raised");
            return null;
        }
    }

    public void switchCompassOnMap() {
        if (null != map) {
            if (map.getUiSettings().isCompassEnabled()) {
                map.getUiSettings().setCompassEnabled(false);
            } else {
                map.getUiSettings().setCompassEnabled(true);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {

        void updateSeekZoom(int zoom);

        void updateDistance(float mapRadius);

        void showMap();
    }
}
