package com.my.game.wesport;

import android.support.multidex.MultiDexApplication;

import com.google.firebase.database.FirebaseDatabase;
import com.my.game.wesport.model.UserListItem;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.services.FirebaseService;

import org.greenrobot.eventbus.EventBus;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public class App extends MultiDexApplication {
    private String TAG = App.class.getSimpleName();
    private static App instance;

    private UserModel userModel;

    private UserListItem userListItem;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/Montserrat-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        EventBus.getDefault().register(new FirebaseService());
    }

    public static App getInstance() {
        return instance;
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public UserListItem getUserListItem() {
        return userListItem;
    }


    public void setUserModel(UserModel userModel) {
        this.userModel = userModel;
    }
}
