package com.my.game.wesport.model;

import com.google.firebase.database.Exclude;

public class User {

    private String displayName;
    private String email;
    private String connection;
    //private int avatarId;
    private String photoUri;
    private long createdAt;
    private int nonAvatarId;
    private String mRecipientId;

    public User() {
    }

    public User(String displayName, String email, String connection,  String photoUri, long createdAt, int nonAvatarId) {
        this.displayName = displayName;
        this.email = email;
        this.connection = connection;
        this.photoUri = photoUri;
        this.createdAt = createdAt;
        this.nonAvatarId = nonAvatarId;
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


    private String cleanEmailAddress(String email){
        //replace dot with comma since firebase does not allow dot
        return email.replace(".","-");
    }

    private String getUserEmail() {return email;}

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getConnection() {
        return connection;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getNonAvatarId() {return nonAvatarId;}

    @Exclude
    public String getRecipientId() {
        return mRecipientId;
    }
    public void setRecipientId(String recipientId) {this.mRecipientId = recipientId;}
}
