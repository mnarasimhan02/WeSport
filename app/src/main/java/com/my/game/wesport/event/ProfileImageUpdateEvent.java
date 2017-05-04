package com.my.game.wesport.event;

import android.net.Uri;

/**
 * Created by sabeeh on 05-Apr-17.
 */

public class ProfileImageUpdateEvent {
    Uri imageUri;
    int imageType;

    public ProfileImageUpdateEvent(Uri imageUri, int imageType) {
        this.imageUri = imageUri;
        this.imageType = imageType;
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public int getImageType() {
        return imageType;
    }
}
