package com.my.game.wesport.ui;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.UsersChatListAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.ChatListItem;
import com.my.game.wesport.model.UserModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class UserChatListFragment extends Fragment implements GoogleApiClient.OnConnectionFailedListener {


    private static final int REQUEST_INVITE = 0;
    private static String TAG = UserChatListFragment.class.getSimpleName();


    @BindView(R.id.recycler_view_users)
    RecyclerView mUsersRecyclerView;

    private List<String> mUsersKeyList;

    private FirebaseAuth mAuth;
    private ChildEventListener mChildEventListener;
    private UsersChatListAdapter mUsersChatListAdapter;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_list_users, container, false);
        ButterKnife.bind(this, rootView);
        mUsersRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_users);
        setAuthInstance();
        setUserRecyclerView();
        setUsersKeyList();
        setAuthListener();
        setHasOptionsMenu(true);


        if (mGoogleApiClient == null) {
            // Create an auto-managed GoogleApiClient with access to App Invites.
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(AppInvite.API)
                    .enableAutoManage(getActivity(), this)
                    .build();
        }

        boolean autoLaunchDeepLink = true;
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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onInviteClicked();
            }
        });
        return rootView;
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
        mUsersChatListAdapter = new UsersChatListAdapter(getActivity(), new ArrayList<ChatListItem>());
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
        mChildEventListener = getChildEventListener();
        FirebaseHelper.getUserRef().limitToFirst(50).addChildEventListener(mChildEventListener);

//        listener to update chat counter when user chat updated
        FirebaseHelper.getCurrentUserConversationRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String recipientUserUid = dataSnapshot.getKey();
                ChatListItem item = mUsersChatListAdapter.getItem(recipientUserUid);
                if (item != null) {
                    UserModel userModel = item.getUser();
                    LatLng currentLatLng = getLocationFromPref();
                    if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, userModel.getLatitude(), userModel.getLongitude())) <= 50) {
                        FirebaseHelper.getChatListItem(recipientUserUid, userModel, new FirebaseHelper.ChatListItemListener() {
                            @Override
                            public void onGetChatListItem(ChatListItem chatListItem) {
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
    public void onStop() {
        super.onStop();
        clearCurrentUsers();
        if (mChildEventListener != null) {
            FirebaseHelper.getUserRef().removeEventListener(mChildEventListener);
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.list_signout, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                NotificationHelper.unsubscribeAndLogout();
                logout();
                return true;
            case R.id.action_invite:
                onInviteClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private LatLng getLocationFromPref() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        LatLng latLng = new LatLng(
                Double.parseDouble(prefs.getString("latitude", "0.0")),
                Double.parseDouble(prefs.getString("longtitude", "0.0"))
        );

        return latLng;
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (dataSnapshot.exists()) {
                        final String userUid = dataSnapshot.getKey();
                        if (dataSnapshot.getKey().equals(FirebaseHelper.getCurrentUser().getUid())) {
                            UserModel currentUserModel = dataSnapshot.getValue(UserModel.class);
                            mUsersChatListAdapter.setCurrentUserInfo(userUid, currentUserModel.getEmail(),
                                    currentUserModel.getCreatedAt(), currentUserModel.getPhotoUri(),
                                    currentUserModel.getLatitude(), currentUserModel.getLongitude(),
                                    currentUserModel.getDistance());
                        } else {
                            final UserModel recipient = dataSnapshot.getValue(UserModel.class);
                            recipient.setRecipientId(userUid);
                            LatLng currentLatLng = getLocationFromPref();
                            //Shows only those parks which are less then 50 miles
                            if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, recipient.getLatitude(), recipient.getLongitude())) <= 50) {
                                FirebaseHelper.getChatListItem(userUid, recipient, new FirebaseHelper.ChatListItemListener() {
                                    @Override
                                    public void onGetChatListItem(ChatListItem chatListItem) {
                                        mUsersKeyList.add(chatListItem.getUserUid());
                                        mUsersChatListAdapter.refill(chatListItem);
                                    }
                                });
                            }
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
                                        public void onGetChatListItem(ChatListItem chatListItem) {
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
}