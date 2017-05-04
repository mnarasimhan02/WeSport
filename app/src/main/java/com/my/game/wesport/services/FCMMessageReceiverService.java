package com.my.game.wesport.services;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.GroupActivity;
import com.my.game.wesport.activity.InvitesActivity;
import com.my.game.wesport.activity.MainActivity;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.model.NotificationModel;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import java.util.Random;

public class FCMMessageReceiverService extends FirebaseMessagingService {

    private String TAG = FCMMessageReceiverService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived: ");
//        sendNotification(remoteMessage.getNotification().getTitle());
        handleMessage(remoteMessage);
    }

    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(messageBody)
                .setAutoCancel(false)
                .setSound(defaultSoundUri);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(1, notificationBuilder.build());
    }


    private void handleMessage(RemoteMessage remoteMessage) {
        Log.d(TAG, "handleMessage: ");
        final RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null && !TextUtils.isEmpty(notification.getTitle())) {
            Log.d(TAG, "Message Notification Body: " + notification.getBody());
            if (remoteMessage.getData().size() > 0 && remoteMessage.getData().containsKey(NotificationHelper.EXTRA_MESSAGE)) {
                final String jsonMessage = remoteMessage.getData().get(NotificationHelper.EXTRA_MESSAGE);
                final NotificationModel notificationModel = NotificationHelper.parse(jsonMessage);
                if (notificationModel == null) {
                    return;
                }

                FirebaseHelper.getUserRef().child(notificationModel.getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        UserModel publicProfile = dataSnapshot.getValue(UserModel.class);
                        if (publicProfile != null) {
                            int notifyType = notificationModel.getType();
                            if (notifyType == NotificationHelper.TYPE_INVITATION || notifyType == NotificationHelper.TYPE_EVENT) {
                                sendNotification(notification, publicProfile, notificationModel);
                            } else if (notifyType == NotificationHelper.TYPE_CHAT && (TextUtils.isEmpty(ChatActivity.activeUserUid) || !ChatActivity.activeUserUid.equals(dataSnapshot.getKey()))) {
                                sendNotification(notification, publicProfile, notificationModel);
                            } else if (notifyType == NotificationHelper.TYPE_GROUP_CHAT && (TextUtils.isEmpty(GroupActivity.activeGroupGameKey) || !GroupActivity.activeGroupGameKey.equals(notificationModel.getGameKey()) || GroupActivity.activePageIndex != GroupActivity.PAGE_GROUP_CHAT)) {
                                sendNotification(notification, publicProfile, notificationModel);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }

    private void sendNotification(RemoteMessage.Notification notification, UserModel profile, NotificationModel notificationModel) {
        Intent intent = new Intent(this, MainActivity.class);
        if (notificationModel.getType() == NotificationHelper.TYPE_EVENT) {
            intent = GroupActivity.newIntent(this, notificationModel.getGameKey(), notificationModel.getGameAuthorKey(), GroupActivity.PAGE_EVENTS);
        } else if (notificationModel.getType() == NotificationHelper.TYPE_INVITATION) {
            intent = new Intent(this, InvitesActivity.class);
        } else if (notificationModel.getType() == NotificationHelper.TYPE_CHAT) {
            intent = ChatActivity.newIntent(this, profile, notificationModel.getSenderId());
        } else if (notificationModel.getType() == NotificationHelper.TYPE_GROUP_CHAT) {
            intent = GroupActivity.newIntent(this, notificationModel.getGameKey(), notificationModel.getGameAuthorKey(), GroupActivity.PAGE_GROUP_CHAT);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_ONE_SHOT);

//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        Uri defaultSoundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notification);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLights(0xffffffff, 300, 100)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.InboxStyle().addLine(notification.getBody()))
                .setGroup("test_group")
                .setGroupSummary(true);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
