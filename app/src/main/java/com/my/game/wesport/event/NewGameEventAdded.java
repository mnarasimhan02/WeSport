package com.my.game.wesport.event;

import com.my.game.wesport.model.EventModel;

/**
 * Created by sabeeh on 01-Apr-17.
 */

public class NewGameEventAdded {
    private String gameKey;
    private String gameAuthorId;
    private EventModel eventModel;

    public NewGameEventAdded(String gameKey, String gameAuthorId, EventModel eventModel) {
        this.gameAuthorId = gameAuthorId;
        this.eventModel = eventModel;
        this.gameKey = gameKey;
    }

    public EventModel getEventModel() {
        return eventModel;
    }

    public String getGameKey() {
        return gameKey;
    }

    public String getGameAuthorId() {
        return gameAuthorId;
    }
}
