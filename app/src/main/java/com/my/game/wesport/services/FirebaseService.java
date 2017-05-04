package com.my.game.wesport.services;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.App;
import com.my.game.wesport.event.NewGameEventAdded;
import com.my.game.wesport.event.NewGroupChatAdded;
import com.my.game.wesport.event.ProfileImageUpdateEvent;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.NotificationHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by sabeeh on 30-Mar-17.
 */

public class FirebaseService {
    private String TAG = FirebaseService.class.getSimpleName();

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewEventAdded(final NewGameEventAdded event) {
        FirebaseHelper.getGameUsersRef(event.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        don't send notification to event owner
                        if (!event.getGameAuthorId().equals(snapshot.getKey())) {
                            NotificationHelper.sendMessageByTopic(snapshot.getKey(), "New event added in game", event.getEventModel().getTitle(), "", NotificationHelper.getEventMessage(event.getEventModel().getAuthor(), event.getGameKey(), event.getGameAuthorId()));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewGroupChatAdded(final NewGroupChatAdded event) {
        if (!event.getGameAuthorId().equals(FirebaseHelper.getCurrentUserId())){
            NotificationHelper.sendMessageByTopic(event.getGameAuthorId(), App.getInstance().getUserModel().getDisplayName(), event.getGroupChatModel().getMessage(), "", NotificationHelper.getGroupChat(event.getGameKey(), event.getGameAuthorId(), FirebaseHelper.getCurrentUserId()));
        }
        FirebaseHelper.getGameUsersRef(event.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userKey = snapshot.getKey();
                    Log.d(TAG, "onDataChange: " + userKey);
                    if (!userKey.equals(FirebaseHelper.getCurrentUserId())){
                        NotificationHelper.sendMessageByTopic(userKey, App.getInstance().getUserModel().getDisplayName(), event.getGroupChatModel().getMessage(), "", NotificationHelper.getGroupChat(event.getGameKey(), event.getGameAuthorId(), FirebaseHelper.getCurrentUserId()));
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onProfileImageUpdate(ProfileImageUpdateEvent event) {
        FirebaseHelper.uploadPicture(event.getImageUri(), event.getImageType());
    }
}
