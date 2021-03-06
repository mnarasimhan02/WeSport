package com.my.game.wesport.model;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;


public class UserMarkerModel {
    DataSnapshot dataSnapshot;
    Marker marker;


    public UserMarkerModel(DataSnapshot dataSnapshot, Marker marker) {
        this.dataSnapshot = dataSnapshot;
        this.marker = marker;
    }

    public DataSnapshot getDataSnapshot() {
        return dataSnapshot;
    }

    public Marker getMarker() {
        return marker;
    }
}
