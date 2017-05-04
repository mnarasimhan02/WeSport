package com.my.game.wesport.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.GroupChatAdapter;
import com.my.game.wesport.event.NewGroupChatAdded;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.GroupChatModel;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 */
public class GroupChatFragment extends Fragment {
    public static final String EXTRA_GAME_KEY = "game_key";
    public static final String EXTRA_GAME_AUTHOR = "game_author";
    @BindView(R.id.recycler_view_chat)
    RecyclerView mChatRecyclerView;
    @BindView(R.id.edit_text_message)
    EditText mUserMessageChatText;
    private GroupChatAdapter groupChatAdapter;
    String gameKey;
    String gameAuthor;
    private String TAG = GroupChatFragment.class.getSimpleName();

    public static GroupChatFragment newInstance(String gameKey, String gameAuthor) {

        Bundle args = new Bundle();
        args.putString(EXTRA_GAME_KEY, gameKey);
        args.putString(EXTRA_GAME_AUTHOR, gameAuthor);
        GroupChatFragment fragment = new GroupChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GroupChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_group_chat, container, false);
        ButterKnife.bind(this, view);


        gameKey = getArguments().getString(EXTRA_GAME_KEY);
        gameAuthor = getArguments().getString(EXTRA_GAME_AUTHOR);

        setChatRecyclerView();

        mUserMessageChatText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (groupChatAdapter.getItemCount() > 0) {
                            mChatRecyclerView.scrollToPosition(groupChatAdapter.getItemCount() - 1);
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
                        if (groupChatAdapter.getItemCount() > 0) {
                            mChatRecyclerView.scrollToPosition(groupChatAdapter.getItemCount() - 1);
                        }
                    }
                }, 700);
            }
        });

        return view;
    }

    private void setChatRecyclerView() {
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mChatRecyclerView.setHasFixedSize(true);
        groupChatAdapter = new GroupChatAdapter(new ArrayList<GroupChatModel>());
        mChatRecyclerView.setAdapter(groupChatAdapter);

        FirebaseHelper.getGameGroupChatRef(gameKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                groupChatAdapter.add(dataSnapshot.getValue(GroupChatModel.class));
                mChatRecyclerView.scrollToPosition(groupChatAdapter.getItemCount() - 1);
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

    @OnClick(R.id.btn_send_message)
    public void btnSendMsgListener(@SuppressWarnings("UnusedParameters") View sendButton) {
        final String senderMessage = mUserMessageChatText.getText().toString().trim();
        if (!senderMessage.isEmpty()) {
            GroupChatModel groupChatModel = new GroupChatModel(senderMessage, App.getInstance().getUserModel().getDisplayName(), FirebaseHelper.getCurrentUserId());
            FirebaseHelper.addGroupChat(gameKey, groupChatModel);
            mUserMessageChatText.setText("");

            EventBus.getDefault().post(new NewGroupChatAdded(gameKey, gameAuthor, groupChatModel));
        }
    }
}
