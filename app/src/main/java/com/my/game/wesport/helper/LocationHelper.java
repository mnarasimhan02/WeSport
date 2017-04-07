package com.my.game.wesport.helper;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.model.LatLng;
import com.my.game.wesport.App;

import java.util.Locale;


public class LocationHelper {
    public static String getDistance(double currentLat, double currentLon, String lat, String lon) {
        double distance = 0;
        Location mCurrentLocation = new Location("mCurrentLocation");
        mCurrentLocation.setLatitude(currentLat);
        mCurrentLocation.setLongitude(currentLon);
        Location newLocation = new Location("newlocation");
        if (lat != null || lon != null) {
            newLocation.setLatitude(Double.parseDouble(lat));
            newLocation.setLongitude(Double.parseDouble(lon));
            distance = (mCurrentLocation.distanceTo(newLocation) / 1609.344);
            return String.format(Locale.US, "%.0f", distance);
        }
        return String.format(Locale.US, "%.0f", distance);
    }

    public static LatLng getLocationFromPref() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.getInstance().getApplicationContext());
        LatLng latLng = new LatLng(Double.parseDouble(prefs.getString("latitude", "0.0")),
                Double.parseDouble(prefs.getString("longtitude", "0.0"))
        );
        return latLng;
    }
}
