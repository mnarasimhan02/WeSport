package com.my.game.wesport.model;

import com.google.firebase.database.Exclude;

@SuppressWarnings("unused")
public class ChatMessage {

    private String photoUrl;
    private String message;
    private String sender;
    private String recipient;
    private int mRecipientOrSenderStatus;

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

    public String getRecipient(){
        return recipient;
    }

    public String getSender(){
        return sender;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Exclude
    public int getRecipientOrSenderStatus() {
        return mRecipientOrSenderStatus;
    }
}