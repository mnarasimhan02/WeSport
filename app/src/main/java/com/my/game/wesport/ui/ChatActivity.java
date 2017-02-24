package com.my.game.wesport.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.my.game.wesport.FireChatHelper.ExtraIntent;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.MessageChatAdapter;
import com.my.game.wesport.model.ChatMessage;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.my.game.wesport.login.SigninActivity.RC_SIGN_IN;

public class ChatActivity extends AppCompatActivity {
    private static final String ANONYMOUS = "anonymous";
    private static final int RC_PHOTO_PICKER = 2;
    private String mUsername;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private StorageReference mChatPhotosStorageReference;
    private DatabaseReference mUserRefDatabase;


    private static final String TAG = com.my.game.wesport.ui.ChatActivity.class.getSimpleName();

    @BindView(R.id.recycler_view_chat) RecyclerView mChatRecyclerView;
    @BindView(R.id.edit_text_message) EditText mUserMessageChatText;


    private String mRecipientId;
    private String mCurrentUserId;
    private MessageChatAdapter messageChatAdapter;
    private DatabaseReference messageChatDatabase;
    private ChildEventListener messageChatListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        bindButterKnife();
        setDatabaseInstance();
        setUsersId();
        setChatRecyclerView();
        // Initialize Firebase components
        FirebaseDatabase mFirebaseDatabase = FirebaseDatabase.getInstance();
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");

        ImageButton mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);

        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });

        FirebaseAuth.AuthStateListener mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                }
            }
        };
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }
    private void setDatabaseInstance() {
        String chatRef = getIntent().getStringExtra(ExtraIntent.EXTRA_CHAT_REF);
        Log.d ("chatRef", String.valueOf(chatRef));
        if (chatRef!=null) {
            messageChatDatabase = FirebaseDatabase.getInstance().getReference().child(chatRef);
            Log.d ("setDatabaseInstance", String.valueOf(messageChatDatabase));
        }
    }

    private void setUsersId() {
        mRecipientId = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_ID);
        mCurrentUserId = getIntent().getStringExtra(ExtraIntent.EXTRA_CURRENT_USER_ID);
    }

    private void setChatRecyclerView() {
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setHasFixedSize(true);
        messageChatAdapter = new MessageChatAdapter(new ArrayList<ChatMessage>());
        mChatRecyclerView.setAdapter(messageChatAdapter);
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.d ("onStart", String.valueOf(messageChatDatabase));
        messageChatListener = messageChatDatabase.limitToFirst(20).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
                    try{
                    if (dataSnapshot.exists()) {
                        ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                        if (newMessage.getSender().equals(mCurrentUserId)) {
                            newMessage.setRecipientOrSenderStatus(MessageChatAdapter.SENDER);
                        } else {
                            newMessage.setRecipientOrSenderStatus(MessageChatAdapter.RECIPIENT);
                        }
                        messageChatAdapter.refillAdapter(newMessage);
                        mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount() - 1);
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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
        }

    @Override
    protected void onStop() {
        super.onStop();
        if(messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();
    }

    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(@SuppressWarnings("UnusedParameters") View sendButton){
        String senderMessage = mUserMessageChatText.getText().toString().trim();
        if(!senderMessage.isEmpty() && messageChatDatabase!=null){
            ChatMessage newMessage = new ChatMessage(senderMessage,mCurrentUserId,mRecipientId,null);
            messageChatDatabase.push().setValue(newMessage);
            mUserMessageChatText.setText("");
        }
    }

    private void onSignedInInitialize(String username) {
        mUsername = username;
        //  attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        //mMessageAdapter.clear();
        //detachDatabaseReadListener();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, getString(R.string.signin_string), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, getString(R.string.signin_cancel), Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at chat_photos/<FILENAME>
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // When the image has successfully uploaded, we get its download URL
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            // Set the download URL to the message box, so that the user can send it to the database
                            assert downloadUrl != null;
                            ChatMessage newMessage = new ChatMessage(null,mCurrentUserId,mRecipientId,downloadUrl.toString());
                            mMessagesDatabaseReference.push().setValue(newMessage);
                        }
                    });
        }
    }


}
