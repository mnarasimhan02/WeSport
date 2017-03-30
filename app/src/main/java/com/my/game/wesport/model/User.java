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
    private String latitude;
    private String longitude;
    private String distance;
    private String Bio;


    public User() {
    }

    @SuppressWarnings("SameParameterValue")
    public User(String displayName, String email, String connection, String photoUri, long createdAt,
                int nonAvatarId, String latitude, String longitude, String distance, String Bio) {
        this.displayName = displayName;
        this.email = email;
        this.connection = connection;
        this.photoUri = photoUri;
        this.createdAt = createdAt;
        this.nonAvatarId = nonAvatarId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.Bio = Bio;
    }

     @SuppressWarnings("UnusedAssignment")
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

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getDistance() {
        return distance;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getNonAvatarId() {return nonAvatarId;}

    public String getBio() {
        return Bio;
    }

    public void setBio(String bio) {
        Bio = bio;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    @Exclude
    public String getRecipientId() {
        return mRecipientId;
    }
    public void setRecipientId(String recipientId) {this.mRecipientId = recipientId;}
}
