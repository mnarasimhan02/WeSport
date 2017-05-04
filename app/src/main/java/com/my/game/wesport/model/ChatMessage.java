package com.my.game.wesport.model;

import com.google.firebase.database.Exclude;

@SuppressWarnings("unused")
public class ChatMessage {
    public static final int TYPE_MESSAGE = 0;
    public static final int TYPE_ATTACHMENT = 1;
    public static final String STATUS_SENDING = "sending";
    public static final String STATUS_SENT = "sending";
    public static final String STATUS_FAIL = "fail";

    private String message;
    private String recipient;
    private String sender;
    private String status;
    private String photoUrl;
    private int type = TYPE_MESSAGE;
    private boolean seen = true;

    public ChatMessage() {
    }

    public ChatMessage(String message, String sender, String recipient, String photoUrl) {
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
        this.photoUrl = photoUrl;
    }


    public String getMessage() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSender() {
        return sender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Exclude
    public boolean isAttachment() {
        return type == TYPE_ATTACHMENT;
    }
}