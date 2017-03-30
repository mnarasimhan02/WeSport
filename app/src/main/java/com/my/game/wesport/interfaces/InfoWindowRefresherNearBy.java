package com.my.game.wesport.interfaces;

import android.net.Uri;
import android.util.Log;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by admin on 28/03/2017.
 */

public class InfoWindowRefresherNearBy implements RequestListener <Uri, GlideDrawable>{
    Marker markerToRefresh;

    public InfoWindowRefresherNearBy(Marker marker) {
        this.markerToRefresh = marker;
    }

    @Override
    public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {
        Log.e(getClass().getSimpleName(), "Error loading park image!");
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
        if (markerToRefresh != null && markerToRefresh.isInfoWindowShown()) {
            markerToRefresh.hideInfoWindow();
            markerToRefresh.showInfoWindow();
        }
        return false;}
}
