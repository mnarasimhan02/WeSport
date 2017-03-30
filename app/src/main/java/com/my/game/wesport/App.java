package com.my.game.wesport;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;
import com.my.game.wesport.model.GridImages;
import com.my.game.wesport.model.UserModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class App extends MultiDexApplication {
    private String TAG = App.class.getSimpleName();
    private static App instance;

    private UserModel userModel;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static App getInstance() {
        return instance;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
