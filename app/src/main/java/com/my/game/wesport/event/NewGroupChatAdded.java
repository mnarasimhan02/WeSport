package com.my.game.wesport.event;

import com.my.game.wesport.model.EventModel;
import com.my.game.wesport.model.GroupChatModel;

/**
 * Created by sabeeh on 01-Apr-17.
 */

public class NewGroupChatAdded {
    private String gameKey;
    private String gameAuthorId;
    private GroupChatModel groupChatModel;

    public NewGroupChatAdded(String gameKey, String gameAuthorId, GroupChatModel groupChatModel) {
        this.gameAuthorId = gameAuthorId;
        this.groupChatModel = groupChatModel;
        this.gameKey = gameKey;
    }

    public GroupChatModel getGroupChatModel() {
        return groupChatModel;
    }

    public String getGameKey() {
        return gameKey;
    }

    public String getGameAuthorId() {
        return gameAuthorId;
    }
}
