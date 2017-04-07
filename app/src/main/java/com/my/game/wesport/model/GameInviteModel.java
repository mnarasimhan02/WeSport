package com.my.game.wesport.model;

import android.text.TextUtils;

import com.google.firebase.database.Exclude;

/**
 * Created by sabeeh on 01-Apr-17.
 */

public class GameInviteModel {
    private String gameKey;
    private String authorName;
    private String authorUid;
    private String status = "";

    public GameInviteModel() {
    }

    public GameInviteModel(String gameKey, String authorName, String authorUid) {
        this.gameKey = gameKey;
        this.authorName = authorName;
        this.authorUid = authorUid;
    }

    public String getGameKey() {
        return gameKey;
    }

    public void setGameKey(String gameKey) {
        this.gameKey = gameKey;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Exclude
    @Override
    public String toString() {
        return "gameKey: " + gameKey + ", authoranme: " + authorName + ", authorUid: " + authorUid + ", status: " + status;
    }

    @Exclude
    public boolean isRejected() {
        return !TextUtils.isEmpty(status) && status.equals("rejected");
    }

    @Exclude
    public boolean isAccepted() {
        return !TextUtils.isEmpty(status) && status.equals("accepted");
    }
}
