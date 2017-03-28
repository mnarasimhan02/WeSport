package com.my.game.wesport.helper;

import android.location.Location;

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
            distance = ((mCurrentLocation.distanceTo(newLocation) / 1000) / 1.6);
            return String.format(Locale.US, "%.0f", distance);
        }
        return String.format(Locale.US, "%.0f", distance);
    }
}
