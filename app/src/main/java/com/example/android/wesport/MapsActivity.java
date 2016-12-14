package com.example.android.wesport;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    SupportMapFragment mapFragment;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_places);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Double mLat = Double.parseDouble(preferences.getString("latitude",""));
        Double mLon = Double.parseDouble(preferences.getString("longtitude",""));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLat,mLon), 16));
        map.addMarker(new MarkerOptions().position(new LatLng(mLat,mLon)));
        //map.setMyLocationEnabled(true);
    }
}