package com.my.game.wesport.model;

import com.google.firebase.database.Exclude;

@SuppressWarnings("unused")
public class ChatMessage {

    private String message;
    private String recipient;
    private String sender;
    private String photoUrl;
    private int mRecipientOrSenderStatus;
    private boolean seen = true;

    public ChatMessage() {
    }

    public ChatMessage(String message, String sender, String recipient, String photoUrl) {
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
        this.photoUrl = photoUrl;
    }

    public void setRecipientOrSenderStatus(int recipientOrSenderStatus) {
        this.mRecipientOrSenderStatus = recipientOrSenderStatus;
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

    @Exclude
    public int getRecipientOrSenderStatus() {
        return mRecipientOrSenderStatus;
    }
}