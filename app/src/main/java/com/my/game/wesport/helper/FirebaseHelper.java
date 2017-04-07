package com.my.game.wesport.helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.UserProfileActivity;
import com.my.game.wesport.model.ChatMessage;
import com.my.game.wesport.model.GameInviteModel;
import com.my.game.wesport.model.UserListItem;
import com.my.game.wesport.model.UserModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

/**
 * Created by sabeeh on 18-Mar-17.
 */

public class FirebaseHelper {
    private static String TAG = FirebaseHelper.class.getSimpleName();

    public static void setUserConnectionStatus(String userId, String status) {
        FirebaseDatabase.getInstance()
                .getReference().
                child("users").
                child(userId).
                child("connection").
                setValue(status);
    }

    public static boolean uploadPicture(Uri fileUri, final int type) {
        String userUid = getCurrentUser().getUid();
        StorageReference photoRef;
        if (type == UserProfileActivity.EDIT_AVATAR) {
            photoRef = FirebaseStorage.getInstance().getReference().child(userUid).child("profile.jpg");
        } else {
            photoRef = FirebaseStorage.getInstance().getReference().child(userUid).child("cover.jpg");
        }

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(App.getInstance().getContentResolver(), fileUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "uploadThumbImage: " + e.getLocalizedMessage());
            return false;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] data1 = baos.toByteArray();


//        photoRef.getName().equals(photoRef.getName());
        UploadTask uploadTask = photoRef.putBytes(data1);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri subDownloadUrl = taskSnapshot.getDownloadUrl();
                String childName = "photoUri";
                if (type == UserProfileActivity.EDIT_COVER_IMAGE) {
                    childName = "coverUri";
                }
                FirebaseDatabase.getInstance().getReference().child("users")
                        .child(getCurrentUser().getUid())
                        .child(childName).setValue(subDownloadUrl.toString());
            }
        });

        return true;
    }

    public static void addConversation(ChatMessage message) {
        message.setSeen(true);
        getCurrentUserConversationRef().child(message.getRecipient()).push().setValue(message);
        message.setSeen(false);
        getConversationRef().child(message.getRecipient()).child(getCurrentUser().getUid()).push().setValue(message);
    }

    public static void getChatListItem(final String userUid, final UserModel recipient, final ChatListItemListener listener) {
        FirebaseHelper.getCurrentUserConversationRef().child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int unreadCounter = 0;
                FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
                for (DataSnapshot chatSnapshot : dataSnapshot.getChildren()) {
                    ChatMessage message = chatSnapshot.getValue(ChatMessage.class);
                    if (!message.getSender().equals(currentUser.getUid()) && !message.isSeen()) {
                        unreadCounter++;
                    }
                }
                UserListItem chatListItem = new UserListItem(userUid, recipient, unreadCounter);
                listener.onGetChatListItem(chatListItem);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static String uploadGameImage(final String gameKey, Uri imageUri, final FileUploadListener listener) {
        StorageReference photoRef;
        String fileName = String.valueOf(new Date().getTime()) + ".jpg";
        photoRef = FirebaseStorage.getInstance().getReference().child("gameGrids").child(gameKey).child(fileName);

        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(App.getInstance().getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, "uploadThumbImage: " + e.getLocalizedMessage());
            return fileName;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] data1 = baos.toByteArray();


//        photoRef.getName().equals(photoRef.getName());
        UploadTask uploadTask = photoRef.putBytes(data1);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "onFailure: " + exception.getMessage());
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri subDownloadUrl = taskSnapshot.getDownloadUrl();
                if (listener != null) {
                    listener.imageUploaded(subDownloadUrl);
                }
            }
        });

        return fileName;
    }

    public static void removeFromGame(String gameKey, String potentialUserId) {
        getGameUsersRef(gameKey).child(potentialUserId).removeValue();
        getInvitesRef(potentialUserId).child(gameKey).removeValue();
    }

    public interface ChatListItemListener {
        void onGetChatListItem(UserListItem chatListItem);
    }

    public static DatabaseReference getCurrentUserRef() {
        return FirebaseDatabase.getInstance().getReference().child("users").child(getCurrentUser().getUid());
    }

    public static FirebaseUser getCurrentUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    public static String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static DatabaseReference getUserRef() {
        return FirebaseDatabase.getInstance().getReference().child("users");
    }

    public static DatabaseReference getLocationRef() {
        return FirebaseDatabase.getInstance().getReference().child("locations");
    }

    public static DatabaseReference getConversationRef() {
        return FirebaseDatabase.getInstance().getReference().child("conversations");
    }

    public static DatabaseReference getCurrentUserConversationRef() {
        return FirebaseDatabase.getInstance().getReference().child("conversations").child(getCurrentUser().getUid());
    }

    public static DatabaseReference getGamesRef(String userUid) {
        return FirebaseDatabase.getInstance().getReference().child("games").child(userUid);
    }

    public static DatabaseReference getGameUsersRef(String gameKey) {
        return FirebaseDatabase.getInstance().getReference().child("game_users").child(gameKey);
    }

    public static DatabaseReference getGamesRef() {
        return FirebaseDatabase.getInstance().getReference().child("games");
    }

    public static DatabaseReference getInvitesRef(String userUid) {
        return FirebaseDatabase.getInstance().getReference().child("invites_users").child(userUid);
    }


    public static DatabaseReference getEventRef(String gameKey) {
        return FirebaseDatabase.getInstance().getReference().child("events").child(gameKey);
    }

    public static DatabaseReference getGameImagesRef(String gameKey) {
        return FirebaseDatabase.getInstance().getReference().child("gameImages").child(gameKey);
    }

    public static void acceptGameInvitation(String gameKey) {
        String uid = FirebaseHelper.getCurrentUser().getUid();
        getGameUsersRef(gameKey).child(uid).setValue(true);
        getInvitesRef(uid).child(gameKey).child("status").setValue("accepted");
        getInvitesRefByGame(gameKey).child(uid).removeValue();

    }

    public static void rejectGameInvitation(String gameKey) {
        String uid = FirebaseHelper.getCurrentUser().getUid();
        getInvitesRef(uid).child(gameKey).child("status").setValue("rejected");
        getInvitesRefByGame(gameKey).child(uid).setValue(false);
    }

    public static void inviteUserInGame(String potentialUserUid, GameInviteModel gameInviteModel) {
//        store invitation by user reference
//        user/game
        getInvitesRef(potentialUserUid).child(gameInviteModel.getGameKey()).setValue(gameInviteModel);
        //        store invitation by game reference
        getInvitesRefByGame(gameInviteModel.getGameKey()).child(potentialUserUid).setValue(false);
    }

    public static DatabaseReference getInvitesRefByGame(String gameKey) {
        return FirebaseDatabase.getInstance().getReference().child("invites_games").child(gameKey);
    }

    public static void inviteFriends(Activity activity, int requestCode) {
        Intent intent = new AppInviteInvitation.IntentBuilder(activity.getString(R.string.invitation_title))
                .setMessage(activity.getString(R.string.invitation_message))
                .setDeepLink(Uri.parse("https://w2mkk.app.goo.gl/b2OT"))
                .setCustomImage(Uri.parse("android.resource://" + activity.getPackageName() + "/mipmap/ic_launcher"))
                .setCallToActionText(activity.getString(R.string.invitation_cta))
                .build();
        activity.startActivityForResult(intent, requestCode);
    }


    public interface FileUploadListener {
        void imageUploaded(Uri fileUri);
    }
}
