package com.my.game.wesport.model;


public class UserListItem {
    private String userUid;
    private UserModel user;
    private int counter;

    public UserListItem(String userUid, UserModel user, int counter) {
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

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public void setUser(UserModel user) {
        this.user = user;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }
}
