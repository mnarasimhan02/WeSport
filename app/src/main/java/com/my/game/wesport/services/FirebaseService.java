package com.my.game.wesport.services;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.event.NewGameEventAdded;
import com.my.game.wesport.event.ProfileImageUpdateEvent;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.NotificationHelper;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by sabeeh on 30-Mar-17.
 */

public class FirebaseService {
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewEventAdded(final NewGameEventAdded event) {
        FirebaseHelper.getGameUsersRef(event.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        NotificationHelper.sendMessageByTopic(snapshot.getKey(), "New event added in game", event.getEventModel().getTitle(), "", NotificationHelper.getEventMessage(event.getEventModel().getAuthor(), event.getGameKey(), event.getGameAuthorId()));
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
