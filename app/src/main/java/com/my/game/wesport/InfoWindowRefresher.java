package com.my.game.wesport;

import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.squareup.picasso.Callback;

/**
 * Created by MaheshN on 2/20/17.
 */
public class InfoWindowRefresher implements Callback {
    private Marker markerToRefresh;


    public InfoWindowRefresher(Marker marker) {
        this.markerToRefresh = marker;
    }
    @Override
    public void onSuccess() {
       // markerToRefresh.showInfoWindow();
        if (markerToRefresh != null && markerToRefresh.isInfoWindowShown()) {
            markerToRefresh.hideInfoWindow();
            markerToRefresh.showInfoWindow();
        }
    }

    @Override
    public void onError() {
        Log.e(getClass().getSimpleName(), "Error loading park image!");

    }

}
