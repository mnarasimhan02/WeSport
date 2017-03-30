package com.my.game.wesport.model;

import com.google.firebase.database.DataSnapshot;

/**
 * Created by sabeeh on 28-Mar-17.
 */

public class DataSnapWithFlag {
    private DataSnapshot dataSnapshot;
    private boolean flag;


    public DataSnapWithFlag(DataSnapshot dataSnapshot, boolean flag) {
        this.dataSnapshot = dataSnapshot;
        this.flag = flag;
    }

    public DataSnapshot getDataSnapshot() {
        return dataSnapshot;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }
}
