package com.my.game.wesport.model;

/**
 * Created by sabeeh on 29-Oct-16.
 */

public class GridImageModel {
    private String remoteUrl = "";
    private String localPath = "";
    private String remoteFileName = "";
    private String firebaseKeyName = "";
    private boolean isSelected = false;

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getRemoteFileName() {
        return remoteFileName;
    }

    public void setRemoteFileName(String remoteFileName) {
        this.remoteFileName = remoteFileName;
    }

    public String getFirebaseKeyName() {
        return firebaseKeyName;
    }

    public void setFirebaseKeyName(String firebaseKeyName) {
        this.firebaseKeyName = firebaseKeyName;
    }
}
