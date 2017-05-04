package com.my.game.wesport.model;

import com.google.firebase.database.Exclude;

public class GroupChatModel {

    private String message;
    private String senderName;
    private String senderId;

    public GroupChatModel() {
    }

    public GroupChatModel(String message, String senderName, String senderId) {
        this.message = message;
        this.senderName = senderName;
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getSenderId() {
        return senderId;
    }
}