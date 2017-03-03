package com.my.game.wesport.model;

import android.net.Uri;

import com.google.firebase.database.Exclude;

public class User {

    private String displayName;
    private String email;
    private String connection;
    //private int avatarId;
    private Uri photoUrl;
    private long createdAt;

    private String mRecipientId;

    public User() {
    }

    public User(String displayName, String email, String connection,  Uri photoUrl, long createdAt) {
        this.displayName = displayName;
        this.email = email;
        this.connection = connection;
        this.createdAt = createdAt;
        this.photoUrl = photoUrl;
    }


    public String createUniqueChatRef(long createdAtCurrentUser, String currentUserEmail){
        String uniqueChatRef="";
        if(createdAtCurrentUser > getCreatedAt()){
            uniqueChatRef = cleanEmailAddress(currentUserEmail)+"-"+cleanEmailAddress(getUserEmail());
        }else {
            uniqueChatRef=cleanEmailAddress(getUserEmail())+"-"+cleanEmailAddress(currentUserEmail);
        }
        return uniqueChatRef;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    private String cleanEmailAddress(String email){
        //replace dot with comma since firebase does not allow dot
        return email.replace(".","-");
    }

    private String getUserEmail() {
        //Log.e("user email  ", userEmail);
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getConnection() {
        return connection;
    }

    public Uri getAvatarId() {
        return photoUrl;
    }

    @Exclude
    public String getRecipientId() {
        return mRecipientId;
    }

    public void setRecipientId(String recipientId) {
        this.mRecipientId = recipientId;
    }
}
