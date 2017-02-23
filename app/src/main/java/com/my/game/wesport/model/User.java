package com.my.game.wesport.model;

import android.util.Log;

import com.google.firebase.database.Exclude;

public class User {

    private String displayName;
    private String email;
    private String connection;
    private int avatarId;
    private long createdAt;

    private String mRecipientId;

    public User() {
    }

    public User(String displayName, String email, String connection, int avatarId, long createdAt) {
        this.displayName = displayName;
        this.email = email;
        this.connection = connection;
        this.avatarId = avatarId;
        this.createdAt = createdAt;
    }


    public String createUniqueChatRef(long createdAtCurrentUser, String currentUserEmail){
        String uniqueChatRef="";
        Log.d("mCurrentUserEmail", String.valueOf(createdAtCurrentUser));
        Log.d("mCurrentUserCreatedAt", String.valueOf(currentUserEmail));
        Log.d("getCreatedAt", String.valueOf(getCreatedAt()));
        if(createdAtCurrentUser > getCreatedAt()){
            uniqueChatRef = cleanEmailAddress(currentUserEmail)+"-"+cleanEmailAddress(getUserEmail());
            Log.d("uniqueChatRef",uniqueChatRef);
        }else {

            uniqueChatRef=cleanEmailAddress(getUserEmail())+"-"+cleanEmailAddress(currentUserEmail);
            Log.d("uniqueChatRef2",uniqueChatRef);
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

    public int getAvatarId() {
        return avatarId;
    }

    @Exclude
    public String getRecipientId() {
        return mRecipientId;
    }

    public void setRecipientId(String recipientId) {
        this.mRecipientId = recipientId;
    }
}
