package com.my.game.wesport.services;

import com.my.game.wesport.helper.FirebaseHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by sabeeh on 30-Mar-17.
 */

public class FirebaseService {
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void startSyncing(){

    }
}
