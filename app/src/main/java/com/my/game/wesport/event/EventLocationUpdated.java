package com.my.game.wesport.event;

import android.location.Location;

/**
 * Created by admin on 26/04/2017.
 */

public class EventLocationUpdated {
    private Location location;
    public EventLocationUpdated(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }
}
