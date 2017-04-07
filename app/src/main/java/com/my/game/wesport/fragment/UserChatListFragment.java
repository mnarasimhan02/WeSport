package com.my.game.wesport.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.InviteUserActivity;
import com.my.game.wesport.activity.OtherUserProfileActivity;
import com.my.game.wesport.adapter.UsersChatListAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.UserListItem;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class UserChatListFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener, UsersChatListAdapter.ChatListInterface {
    private static final int REQUEST_INVITE = 0;
    public static final String EXTRA_IS_GROUP = "is_group";
    public static final String EXTRA_GAME_KEY = "game_key";
    private static String TAG = UserChatListFragment.class.getSimpleName();
    private static String EXTRA_GAME_AUTHOR = "game_author";
//    SimpleUserListAdapter simpleUserListAdapter;


    @BindView(R.id.recycler_view_users)
    RecyclerView mUsersRecyclerView;

    private List<String> mUsersKeyList;

    private FirebaseAuth mAuth;
    private ChildEventListener mChildEventListener;
    private UsersChatListAdapter mUsersChatListAdapter;
    private GoogleApiClient mGoogleApiClient;
    private boolean isFromGroup = false;
    private String gameKey = "";
    private String gameAuthorId = "";

    private View emptyView;

    private Dialog userSelectionListDialog;

    List<String> gameInvitations = new ArrayList<>();
    private DatabaseReference databaseReference;

    public static UserChatListFragment newInstance(boolean isFromGroup, String gameKey, String gameAuthorId) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_GROUP, isFromGroup);
        args.putString(EXTRA_GAME_KEY, gameKey);
        args.putString(EXTRA_GAME_AUTHOR, gameAuthorId);
        UserChatListFragment fragment = new UserChatListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_list_users, container, false);
        ButterKnife.bind(this, rootView);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(EXTRA_IS_GROUP)
                && arguments.containsKey(EXTRA_GAME_KEY)) {

            isFromGroup = arguments.getBoolean(EXTRA_IS_GROUP);
            gameKey = arguments.getString(EXTRA_GAME_KEY);
            gameAuthorId = arguments.getString(EXTRA_GAME_AUTHOR);
        }

        mUsersRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_users);
        setAuthInstance();
        setUserRecyclerView();
        setUsersKeyList();
        setAuthListener();
        setHasOptionsMenu(true);

        emptyView = rootView.findViewById(R.id.empty_view_team_members);

        if (mGoogleApiClient == null) {
            // Create an auto-managed GoogleApiClient with access to App Invites.
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(AppInvite.API)
                    .enableAutoManage(getActivity(), this)
                    .build();
        }

        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, getActivity(), true)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(@NonNull AppInviteInvitationResult result) {
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);

                                }
                            }
                        });

        // Setup FAB to open GameEditActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        if (isFromGroup) {
            emptyView.setVisibility(View.VISIBLE);
            setupGameInvitations();
//            setupUserSelectionList();
            fab.setImageResource(R.drawable.ic_add_24dp);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO:  User Invite Activity
                    //showUserSelectionSheet();
                    startActivity(InviteUserActivity.newIntent(getActivity(), gameKey));
                }
            });
        } else {
            emptyView.setVisibility(View.GONE);
            fab.setImageResource(R.drawable.share);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onInviteClicked();
                }
            });
        }

        return rootView;
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        showMessage(getString(R.string.google_play_services_error));
    }

    private void setAuthInstance() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUserRecyclerView() {
        //to be updated
        mUsersChatListAdapter = new UsersChatListAdapter(getActivity(), new ArrayList<UserListItem>(), this);
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setAdapter(mUsersChatListAdapter);
    }

    private void setUsersKeyList() {
        mUsersKeyList = new ArrayList<String>();
    }

    private void setAuthListener() {
        UserModel user = App.getInstance().getUserModel();
        if (user != null) {
            queryAllUsers();
        } else {
            goToLogin();
        }
    }

    private void queryAllUsers() {
        if (isFromGroup) {
            mChildEventListener = getGameUserListChildEventListener();
            databaseReference = FirebaseHelper.getGameUsersRef(gameKey);
        } else {
            mChildEventListener = getUserListChildEventListener();
            databaseReference = FirebaseHelper.getUserRef().limitToFirst(50).getRef();
        }

        databaseReference.addChildEventListener(mChildEventListener);

//        listener to update chat counter when user chat updated
        FirebaseHelper.getCurrentUserConversationRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String recipientUserUid = dataSnapshot.getKey();
                UserListItem item = mUsersChatListAdapter.getItem(recipientUserUid);
                if (item != null) {
                    UserModel userModel = item.getUser();
                    LatLng currentLatLng = getLocationFromPref();
                    if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, userModel.getLatitude(), userModel.getLongitude())) <= 50) {
                        FirebaseHelper.getChatListItem(recipientUserUid, userModel, new FirebaseHelper.ChatListItemListener() {
                            @Override
                            public void onGetChatListItem(UserListItem chatListItem) {
                                mUsersChatListAdapter.updateUser(chatListItem);
                            }
                        });
                    } else {
                        mUsersChatListAdapter.removeUser(mUsersChatListAdapter.indexOf(recipientUserUid));
                    }
                }
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

    private void goToLogin() {
        Intent intent = new Intent(getActivity(), SigninActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // SigninActivity is a New Task
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK); // The old task when coming back to this activity should be cleared so we cannot come back to it.
        startActivity(intent);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clearCurrentUsers();
        if (mChildEventListener != null) {
            databaseReference.removeEventListener(mChildEventListener);
        }
    }

    private void clearCurrentUsers() {
        mUsersChatListAdapter.clear();
        mUsersKeyList.clear();
    }

    private void logout() {
        setUserOffline();
        mAuth.signOut();
        AuthUI.getInstance()
                .signOut(getActivity())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        NotificationHelper.unSubscribeAndLogout();
                        // user is now signed out
                        startActivity(new Intent(getActivity(), SigninActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().finish();
                    }
                });
    }

    private void setUserOffline() {
        if (mAuth.getCurrentUser() != null) {
            String userId = mAuth.getCurrentUser().getUid();
            FirebaseHelper.getUserRef().child(userId).child("connection").setValue(UsersChatListAdapter.OFFLINE);
        }
    }

    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.list_signout, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                NotificationHelper.unSubscribeAndLogout();
                logout();
                return true;
            case R.id.action_invite:
                onInviteClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    private LatLng getLocationFromPref() {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            LatLng latLng = new LatLng(
                    Double.parseDouble(prefs.getString("latitude", "0.0")),
                    Double.parseDouble(prefs.getString("longtitude", "0.0"))
            );

            return latLng;
        } catch (Exception e) {
            Log.d(TAG, "getLocationFromPref: " + e.getLocalizedMessage());
        }

        return new LatLng(0.0, 0.0);
    }

    private ChildEventListener getUserListChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {

                    final String userUid = dataSnapshot.getKey();
                    if (!dataSnapshot.getKey().equals(FirebaseHelper.getCurrentUser().getUid())) {
                        final UserModel recipient = dataSnapshot.getValue(UserModel.class);
                        LatLng currentLatLng = getLocationFromPref();
                        //Shows only those parks which are less then 50 miles
                        if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, recipient.getLatitude(), recipient.getLongitude())) <= 50) {
                            FirebaseHelper.getChatListItem(userUid, recipient, new FirebaseHelper.ChatListItemListener() {
                                @Override
                                public void onGetChatListItem(UserListItem chatListItem) {
                                    mUsersKeyList.add(chatListItem.getUserUid());
                                    mUsersChatListAdapter.refill(chatListItem);
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    if (dataSnapshot.exists()) {
                        final String userUid = dataSnapshot.getKey();
                        if (!userUid.equals(FirebaseHelper.getCurrentUser().getUid())) {
                            final UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            final int index = mUsersKeyList.indexOf(userUid);
                            if (index > -1) {
                                LatLng currentLatLng = getLocationFromPref();
                                if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, userModel.getLatitude(), userModel.getLongitude())) <= 50) {
                                    FirebaseHelper.getChatListItem(userUid, userModel, new FirebaseHelper.ChatListItemListener() {
                                        @Override
                                        public void onGetChatListItem(UserListItem chatListItem) {
                                            mUsersChatListAdapter.changeUser(index, chatListItem);
                                        }
                                    });
                                } else {
                                    mUsersChatListAdapter.removeUser(index);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
        };
    }

    private ChildEventListener getGameUserListChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    final String userUid = dataSnapshot.getKey();
//                    ignore game author and user him self
                    if (!userUid.equals(FirebaseHelper.getCurrentUser().getUid()) && !userUid.equals(gameAuthorId)) {
                        FirebaseHelper.getUserRef().child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final UserModel recipient = dataSnapshot.getValue(UserModel.class);
                                FirebaseHelper.getChatListItem(userUid, recipient, new FirebaseHelper.ChatListItemListener() {
                                    @Override
                                    public void onGetChatListItem(UserListItem chatListItem) {
                                        chatListItem.setCounter(0);
                                        emptyView.setVisibility(View.GONE);
                                        mUsersKeyList.add(chatListItem.getUserUid());
                                        mUsersChatListAdapter.refill(chatListItem);
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    if (dataSnapshot.exists()) {
                        final String userUid = dataSnapshot.getKey();
                        if (!userUid.equals(FirebaseHelper.getCurrentUser().getUid())) {
                            final UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            final int index = mUsersKeyList.indexOf(userUid);
                            if (index > -1) {
                                LatLng currentLatLng = getLocationFromPref();
                                if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, userModel.getLatitude(), userModel.getLongitude())) <= 50) {
                                    FirebaseHelper.getChatListItem(userUid, userModel, new FirebaseHelper.ChatListItemListener() {
                                        @Override
                                        public void onGetChatListItem(UserListItem chatListItem) {
                                            mUsersChatListAdapter.changeUser(index, chatListItem);
                                        }
                                    });
                                } else {
                                    mUsersChatListAdapter.removeUser(index);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mUsersChatListAdapter.removeUser(mUsersChatListAdapter.indexOf(dataSnapshot.getKey()));
                if (mUsersChatListAdapter.getItemCount() > 0) {
                    emptyView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    /**
     * UserModel has clicked the 'Invite' button, launch the invitation UI with the proper
     * title, message, and deep link
     */
    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse("https://w2mkk.app.goo.gl/b2OT"))
                .setCustomImage(Uri.parse("android.resource://" + getActivity().getPackageName() + "/mipmap/ic_launcher"))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                /*String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                }*/
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // [START_EXCLUDE]
                showMessage(getString(R.string.send_failed));
                // [END_EXCLUDE]
            }
        }
    }

    private void showMessage(String msg) {
        ViewGroup container = (ViewGroup) getActivity().findViewById(R.id.snackbar_layout);
        Snackbar.make(container, msg, Snackbar.LENGTH_SHORT).show();
    }

    /*public void setupUserSelectionList() {
        View settingsView = LayoutInflater.from(getContext()).inflate(R.layout.user_list_layout, null);
        settingsView.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userSelectionListDialog.dismiss();
            }
        });

        userSelectionListDialog = new Dialog(getActivity(), R.style.MaterialDialogSheetAnim);
        userSelectionListDialog.setContentView(settingsView); // your custom view.
        userSelectionListDialog.setCancelable(true);
        userSelectionListDialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        userSelectionListDialog.getWindow().setGravity(Gravity.TOP);

        RecyclerView recyclerView = (RecyclerView) settingsView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        simpleUserListAdapter = new SimpleUserListAdapter(getContext(), new SimpleUserListAdapter.SimpleUserListListener() {
            @Override
            public void onUserClick(int position, UserListItem userListItem) {
                FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
                UserModel userModel = App.getInstance().getUserModel();
                GameInviteModel gameInviteModel = new GameInviteModel(gameKey, userModel.getDisplayName(), currentUser.getUid());
                FirebaseHelper.inviteUserInGame(userListItem.getUserUid(), gameInviteModel);
                NotificationHelper.sendMessageByTopic(userListItem.getUserUid(), "Invitation to game", "by " + userModel.getDisplayName(), "", NotificationHelper.getInvitationMessage(currentUser.getUid()));
                Toast.makeText(getContext(), "Invitation sent to " + userListItem.getUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                userSelectionListDialog.dismiss();
            }
        });
        recyclerView.setAdapter(simpleUserListAdapter);
        loadDataInUserSelectionListAdapter();
    }*/

    /*private void loadDataInUserSelectionListAdapter() {
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
    }*/

    /*private boolean isUserValidForSelectionList(DataSnapshot snapshot) {
        return !snapshot.getKey().equals(FirebaseHelper.getCurrentUser().getUid()) &&
                !mUsersChatListAdapter.contains(snapshot.getKey()) &&
                !gameInvitations.contains(snapshot.getKey());
    }*/

    /*public void showUserSelectionSheet() {
        if (userSelectionListDialog != null) {
            loadDataInUserSelectionListAdapter();
            userSelectionListDialog.show();
        }
    }*/

    @Override
    public void onUserItemClick(int position, UserListItem userListItem) {
        if (isFromGroup) {
            startActivity(OtherUserProfileActivity.newIntent(getActivity(), userListItem.getUserUid(), gameKey));
        } else {
            try {
                startActivity(ChatActivity.newIntent(getActivity(), userListItem.getUser(), userListItem.getUserUid()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onUserItemLongClick(int position, UserListItem userListItem) {
        if (isFromGroup) {
            showDeleteConfirmationDialog(userListItem);
        }
    }

    private void showDeleteConfirmationDialog(final UserListItem userItem) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.delete_user_dialog_msg, userItem.getUser().getDisplayName()));
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeUserFromGameList(userItem.getUserUid());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void removeUserFromGameList(String potentialUserId) {
        FirebaseHelper.removeFromGame(gameKey, potentialUserId);
    }
}