package com.my.game.wesport.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.OtherUserProfileActivity;
import com.my.game.wesport.adapter.SimpleUserListAdapter;
import com.my.game.wesport.adapter.UsersChatListAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.model.GameInviteModel;
import com.my.game.wesport.model.UserListItem;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class NearbyUsersListFragment extends Fragment implements UsersChatListAdapter.ChatListInterface {
    public static final String EXTRA_GAME_KEY = "game_key";
    RecyclerView mUsersRecyclerView;
    SimpleUserListAdapter simpleUserListAdapter;

    private static String TAG = UserChatListFragment.class.getSimpleName();

    private ChildEventListener mChildEventListener;
    private UsersChatListAdapter mUsersChatListAdapter;
    private boolean isFromGroup = false;

    private List<String> mUsersKeyList;
    List<String> gameInvitations = new ArrayList<>();
    String gameKey = "";
    private String gameAuthorId = "";

    public static NearbyUsersListFragment newInstance(String gameKey) {
        Bundle args = new Bundle();
        args.putString(EXTRA_GAME_KEY, gameKey);
        NearbyUsersListFragment fragment = new NearbyUsersListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_nearby_user_list, container, false);

        mUsersRecyclerView = (RecyclerView) rootView.findViewById(R.id.nearby_user_recycler_view);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersChatListAdapter = new UsersChatListAdapter(getActivity(), new ArrayList<UserListItem>(), this);

        gameKey = getArguments().getString(EXTRA_GAME_KEY);

        setupGameInvitations();
        setupUserSelectionList();
        loadDataInUserSelectionListAdapter();
        return rootView;
    }

    public void setupUserSelectionList() {
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersRecyclerView.setHasFixedSize(true);
        simpleUserListAdapter = new SimpleUserListAdapter(getActivity(), new SimpleUserListAdapter.SimpleUserListListener() {
            @Override
            public void onUserClick(int position, UserListItem userListItem) {
                FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
                UserModel userModel = App.getInstance().getUserModel();
                GameInviteModel gameInviteModel = new GameInviteModel(gameKey, userModel.getDisplayName(), currentUser.getUid());
                FirebaseHelper.inviteUserInGame(userListItem.getUserUid(), gameInviteModel);
                NotificationHelper.sendMessageByTopic(userListItem.getUserUid(), "Invitation to game", "by " + userModel.getDisplayName(), "", NotificationHelper.getInvitationMessage(currentUser.getUid()));
                Toast.makeText(getActivity(), "Invitation sent to " + userListItem.getUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        });
        mUsersRecyclerView.setAdapter(simpleUserListAdapter);
        loadDataInUserSelectionListAdapter();
    }

    private void setupGameInvitations() {
        FirebaseHelper.getInvitesRefByGame(gameKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                gameInvitations.add(dataSnapshot.getKey());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                gameInvitations.remove(dataSnapshot.getKey());

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadDataInUserSelectionListAdapter() {
        FirebaseHelper.getUserRef().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<UserListItem> users = new ArrayList<UserListItem>();
                if (dataSnapshot != null && dataSnapshot.getChildrenCount() > 0) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel != null && isUserValidForSelectionList(snapshot)) {
                            users.add(new UserListItem(snapshot.getKey(), userModel, 0));
                        }
                    }
                }
                simpleUserListAdapter.updateList(users);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean isUserValidForSelectionList(DataSnapshot snapshot) {
        UserModel userModel = snapshot.getValue(UserModel.class);
        LatLng location = LocationHelper.getLocationFromPref();
        Float distance = Float.parseFloat(LocationHelper.getDistance(location.latitude, location.longitude, userModel.getLatitude(), userModel.getLongitude()));
        return !snapshot.getKey().equals(FirebaseHelper.getCurrentUser().getUid()) &&
                !mUsersChatListAdapter.contains(snapshot.getKey()) &&
                !gameInvitations.contains(snapshot.getKey()) && distance <= 50;
    }

    @Override
    public void onUserItemClick(int position, UserListItem userListItem) {


    }

    @Override
    public void onUserItemLongClick(int position, UserListItem userListItem) {
    }

}
