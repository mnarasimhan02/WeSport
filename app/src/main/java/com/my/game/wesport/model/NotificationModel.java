package com.my.game.wesport.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by sabeeh on 01-Apr-17.
 */

public class NotificationModel {
    @SerializedName("type")
    @Expose
    private
    int type;

    @SerializedName("user_uid")
    @Expose
    private
    String potentialUserId;

    @SerializedName("game_key")
    @Expose
    private
    String gameKey;

    @SerializedName("game_author_key")
    @Expose
    private
    String gameAuthorKey;

    public String getPotentialUserId() {
        return potentialUserId;
    }

    public void setPotentialUserId(String potentialUserId) {
        this.potentialUserId = potentialUserId;
    }

    public String getGameKey() {
        return gameKey;
    }

    public void setGameKey(String gameKey) {
        this.gameKey = gameKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getGameAuthorKey() {
        return gameAuthorKey;
    }

    public void setGameAuthorKey(String gameAuthorKey) {
        this.gameAuthorKey = gameAuthorKey;
    }
}
