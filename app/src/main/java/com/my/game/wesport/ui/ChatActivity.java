package com.my.game.wesport.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.my.game.wesport.App;
import com.my.game.wesport.FireChatHelper.ExtraIntent;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.FullscreenImageActivity;
import com.my.game.wesport.activity.OtherUserProfileActivity;
import com.my.game.wesport.adapter.MessageChatAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.model.ChatMessage;
import com.my.game.wesport.model.UserModel;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ChatActivity extends AppCompatActivity implements MessageChatAdapter.ChatListener {
    private String mUsername;
    private String mUserImage;

    private static final String TAG = ChatActivity.class.getSimpleName();

    @BindView(R.id.recycler_view_chat)
    RecyclerView mChatRecyclerView;
    @BindView(R.id.edit_text_message)
    EditText mUserMessageChatText;

    private String mRecipientId;
    private String mCurrentUserId;
    private MessageChatAdapter messageChatAdapter;
    private ChildEventListener messageChatListener;

    public static String activeUserUid;
    private Query userChatListQuery;
    private Uri mCropImageUri;

    public static Intent newIntent(Context context, UserModel userModel, String mRecipientId) {
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(ExtraIntent.EXTRA_USER_ID, FirebaseHelper.getCurrentUser().getUid());
        chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, mRecipientId);
        chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_USERNAME, userModel.getDisplayName());
        chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_PICTURE, userModel.getPhotoUri());
        return chatIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        bindButterKnife();
        setUsersId();

        if (TextUtils.isEmpty(mRecipientId) || TextUtils.isEmpty(mUsername)) {
            Toast.makeText(this, "Invalid user id or username!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Intent intent = getIntent();
        String msg = intent.getExtras().getString("msg");
        mUserMessageChatText.setText(msg);

        userChatListQuery = FirebaseHelper.getCurrentUserConversationRef().child(mRecipientId);

        setChatRecyclerView();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_chat_activity);
        TextView label = (TextView) findViewById(R.id.username);
        ImageView userImage = (ImageView) findViewById(R.id.user_profile_image_chat_activity);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            final Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        label.setText(mUsername);

        Glide.with(this)
                .load(mUserImage)
                .error(R.drawable.profile)
                .into(userImage);

        /*if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowCustomEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setCustomView(R.layout.chat_username);
            supportActionBar.setTitle(mUsername);
        }*/

        messageChatListener = userChatListQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
                try {
                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                    messageChatAdapter.add(dataSnapshot);
                    mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount() - 1);
                    if (!newMessage.getSender().equals(mCurrentUserId) && !newMessage.isSeen()) {
                        dataSnapshot.child("seen").getRef().setValue(true);
                    }
                } catch (Exception e) {
                    Log.d(TAG, "onChildAdded: " + e.getLocalizedMessage());
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                messageChatAdapter.update(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        AdView mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("8D55278B12588486D7D396079CB75B6B")
                .build();
        mAdView.loadAd(adRequest);

        mUserMessageChatText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (messageChatAdapter.getItemCount() > 0) {
                            mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount() - 1);
                        }
                    }
                }, 700);
            }
        });
        mUserMessageChatText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (messageChatAdapter.getItemCount() > 0) {
                            mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount() - 1);
                        }
                    }
                }, 700);
            }
        });
    }

    //Handling Toolbar image click
    public void onActionBarImageCLick(View view) {
        startActivity(OtherUserProfileActivity.newIntent(this, mRecipientId, null, null));

    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setUsersId() {
        mRecipientId = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_ID);
        mCurrentUserId = getIntent().getStringExtra(ExtraIntent.EXTRA_USER_ID);
        mUsername = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_USERNAME);
        mUserImage = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_PICTURE);
        Log.d(TAG, "setUsersId: " + mUsername);
    }

    private void setChatRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        mChatRecyclerView.setLayoutManager(layoutManager);
        mChatRecyclerView.setHasFixedSize(true);
        messageChatAdapter = new MessageChatAdapter(new ArrayList<DataSnapshot>());
        mChatRecyclerView.setAdapter(messageChatAdapter);
        messageChatAdapter.setChatListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(@SuppressWarnings("UnusedParameters") View sendButton) {
        try {
            String senderMessage = mUserMessageChatText.getText().toString().trim();
            if (!senderMessage.isEmpty()) {
                ChatMessage newMessage = new ChatMessage(senderMessage, mCurrentUserId, mRecipientId, null);
                FirebaseHelper.addConversation(newMessage);
                NotificationHelper.sendMessageByTopic(mRecipientId, App.getInstance().getUserModel().getDisplayName(), senderMessage, "", NotificationHelper.getChatMessage(mCurrentUserId));
                mUserMessageChatText.setText("");
            }
        } catch (Exception e) {
            Log.d(TAG, "btnSendMsgListener: " + e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activeUserUid = "";
        if (messageChatListener != null && userChatListQuery != null) {
            userChatListQuery.removeEventListener(messageChatListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeUserUid = mRecipientId;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void onAttachmentClick(View view) {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(this);
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, R.string.error_permission_not_granted, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            mCropImageUri = CropImage.getPickImageResultUri(this, data);
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, mCropImageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                /*String realPath = null;
                try {
                    realPath = getRealPathFromURI(mCropImageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(realPath)) {
                    realPath = mCropImageUri.toString();
                }*/

                ChatMessage chatMessage = new ChatMessage("", mCurrentUserId, mRecipientId, null);
                chatMessage.setType(ChatMessage.TYPE_ATTACHMENT);
                chatMessage.setStatus(ChatMessage.STATUS_SENDING);

                FirebaseHelper.addAttachmentConversation(chatMessage, new FirebaseHelper.AttachmentMessageListener() {
                    @Override
                    public void onMessageAdded(final DatabaseReference senderRef, final DatabaseReference recipientRef) {
                        FirebaseHelper.uploadChatImage(mCurrentUserId, senderRef.getKey(), mCropImageUri, new FirebaseHelper.FileUploadListener() {
                            @Override
                            public void imageUploaded(Uri fileUri) {
                                senderRef.child("message").setValue(fileUri.toString());
                                recipientRef.child("message").setValue(fileUri.toString());
                            }
                        });
                    }
                });
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) throws Exception {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @Override
    public void onMessageClick(int position, DataSnapshot chatDataSnapshot) {
        ChatMessage chatMessage = chatDataSnapshot.getValue(ChatMessage.class);
        if (chatMessage != null && chatMessage.getType() == ChatMessage.TYPE_ATTACHMENT && !TextUtils.isEmpty(chatMessage.getMessage())) {
            ArrayList<String> images = new ArrayList<>();
            images.add(chatMessage.getMessage());
            startActivity(FullscreenImageActivity.newIntent(this, images, 0));
        }
    }
}