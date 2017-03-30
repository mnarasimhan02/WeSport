package com.my.game.wesport.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.my.game.wesport.App;
import com.my.game.wesport.FireChatHelper.ExtraIntent;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.MessageChatAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.model.ChatMessage;
import com.my.game.wesport.model.UserModel;

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

    public static Intent newIntent(Context context, UserModel userModel) {
        String chatRef = userModel.createUniqueChatRef(App.getInstance().getUserModel().getCreatedAt(), App.getInstance().getUserModel().getEmail());
        Log.d(TAG, "newIntent: " + chatRef);
        Intent chatIntent = new Intent(context, ChatActivity.class);
        chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, FirebaseHelper.getCurrentUser().getUid());
        chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, userModel.getRecipientId());
        chatIntent.putExtra(ExtraIntent.EXTRA_CHAT_REF, chatRef);
        chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_USERNAME, userModel.getDisplayName());

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

        setChatRecyclerView();

        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setDisplayShowCustomEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
            supportActionBar.setCustomView(R.layout.chat_username);
            supportActionBar.setTitle(
                    Html.fromHtml("<font color=\"white\">" + mUsername + " - "
                            + "</font>"));
            TextView label = (TextView) supportActionBar.getCustomView().findViewById(R.id.username);
            label.setText(mUsername);
        }
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setUsersId() {
        mRecipientId = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_ID);
        mCurrentUserId = getIntent().getStringExtra(ExtraIntent.EXTRA_CURRENT_USER_ID);
        mUsername = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_USERNAME);
        Log.d(TAG, "setUsersId: " + mUsername);
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
        messageChatListener = FirebaseHelper.getCurrentUserConversationRef().child(mRecipientId).limitToFirst(20).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
                try {
                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(newMessage.getSender().equals(mCurrentUserId)) {
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.SENDER);
                    } else {
                        if (!newMessage.isSeen()) {
                            dataSnapshot.child("seen").getRef().setValue(true);
                        }
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.RECIPIENT);
                    }
                    messageChatAdapter.refillAdapter(newMessage);
                    mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount() - 1);
                } catch (Exception e) {
                    Log.d(TAG, "onChildAdded: " + e.getLocalizedMessage());
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
        messageChatAdapter.cleanUp();
    }

    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(@SuppressWarnings("UnusedParameters") View sendButton) {
        String senderMessage = mUserMessageChatText.getText().toString().trim();
        if (!senderMessage.isEmpty()) {
            ChatMessage newMessage = new ChatMessage(senderMessage, mCurrentUserId, mRecipientId, null);
            FirebaseHelper.addConversation(newMessage);
            NotificationHelper.sendMessageByTopic(mRecipientId, App.getInstance().getUserModel().getDisplayName(), senderMessage, "", mCurrentUserId);
            mUserMessageChatText.setText("");
        }
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
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activeUserUid = "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        activeUserUid = mRecipientId;
    }
}