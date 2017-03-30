package com.my.game.wesport;

import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.Marker;

class InfoWindowRefresher implements RequestListener<String, GlideDrawable> {
    private final Marker markerToRefresh;


    public InfoWindowRefresher(Marker marker) {
        this.markerToRefresh = marker;
    }


    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
        Log.e(getClass().getSimpleName(), "Error loading park image!");
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        if (markerToRefresh != null && markerToRefresh.isInfoWindowShown()) {
            markerToRefresh.hideInfoWindow();
            markerToRefresh.showInfoWindow();
        }
        return false;
    }
}
