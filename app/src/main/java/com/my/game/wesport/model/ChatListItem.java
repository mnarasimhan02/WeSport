package com.my.game.wesport.model;


public class ChatListItem {
    private String userUid;
    private UserModel user;
    private int counter;

    public ChatListItem(String userUid, UserModel user, int counter) {
        this.userUid = userUid;
        this.user = user;
        this.counter = counter;
    }

    public UserModel getUser() {
        return user;
    }

    public int getCounter() {
        return counter;
    }

    public String getUserUid() {
        return userUid;
    }
}
