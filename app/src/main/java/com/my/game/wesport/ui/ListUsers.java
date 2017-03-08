package com.my.game.wesport.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.appinvite.AppInviteReferral;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.UsersChatAdapter;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;

public class ListUsers extends Fragment implements
        GoogleApiClient.OnConnectionFailedListener {


    private static final int REQUEST_INVITE = 0;
    private static String TAG =  ListUsers.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;


    @BindView(R.id.progress_bar_users) ProgressBar mProgressBarForUsers;
    @BindView(R.id.recycler_view_users) RecyclerView mUsersRecyclerView;

    private String mCurrentUserUid;
    private List<String> mUsersKeyList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mUserRefDatabase;
    private ChildEventListener mChildEventListener;
    private UsersChatAdapter mUsersChatAdapter;
    private String loginUser;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.activity_list_users, container, false);
        ButterKnife.bind(this, rootView);
        mUsersRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_users);
        setAuthInstance();
        setUsersDatabase();
        setUserRecyclerView();
        setUsersKeyList();
        setAuthListener();
        setHasOptionsMenu(true);


        // Create an auto-managed GoogleApiClient with access to App Invites.
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(AppInvite.API)
                .enableAutoManage(getActivity(), this)
                .build();
        boolean autoLaunchDeepLink = true;
        AppInvite.AppInviteApi.getInvitation(mGoogleApiClient, getActivity(), autoLaunchDeepLink)
                .setResultCallback(
                        new ResultCallback<AppInviteInvitationResult>() {
                            @Override
                            public void onResult(AppInviteInvitationResult result) {
                                Log.d(TAG, "getInvitation:onResult:" + result.getStatus());
                                if (result.getStatus().isSuccess()) {
                                    // Extract information from the intent
                                    Intent intent = result.getInvitationIntent();
                                    String deepLink = AppInviteReferral.getDeepLink(intent);
                                    String invitationId = AppInviteReferral.getInvitationId(intent);

                                }
                            }
                        });

        // Setup FAB to open EditorActivity
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
    public void onConnectionFailed(ConnectionResult connectionResult) {
        showMessage(getString(R.string.google_play_services_error));
    }

    private void setAuthInstance() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUsersDatabase() {
        mUserRefDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    }
    private void setUserRecyclerView() {
        //to be updated
        mUsersChatAdapter = new UsersChatAdapter(getActivity(), new ArrayList<User>());
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mUsersRecyclerView.setHasFixedSize(true);
        mUsersRecyclerView.setAdapter(mUsersChatAdapter);
    }

    private void setUsersKeyList() {
        mUsersKeyList = new ArrayList<String>();
    }

    private void setAuthListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {

            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                hideProgressBarForUsers();
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    if (user.getDisplayName() != null) {
                        loginUser = onSignedInInitialize(user.getDisplayName());
                        setUserData(user);
                        queryAllUsers();

                    } else {
                        loginUser = onSignedInInitialize(getString(R.string.email_user));
                        setUserData(user);
                        queryAllUsers();
                    }
                    // User is signed in
                } else {
                    // User is signed out
                    goToLogin();
                }
            }
        };
    }

    private void setUserData(FirebaseUser user) {
        mCurrentUserUid = user.getUid();
    }

    private void queryAllUsers() {
        mChildEventListener = getChildEventListener();
        mUserRefDatabase.limitToFirst(50).addChildEventListener(mChildEventListener);
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
        showProgressBarForUsers();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        clearCurrentUsers();
        if (mChildEventListener != null) {
            mUserRefDatabase.removeEventListener(mChildEventListener);
        }

        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void clearCurrentUsers() {
        mUsersChatAdapter.clear();
        mUsersKeyList.clear();
    }

    private void logout() {
        showProgressBarForUsers();
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
        if(mAuth.getCurrentUser()!=null ) {
            String userId = mAuth.getCurrentUser().getUid();
            mUserRefDatabase.child(userId).child("connection").setValue(UsersChatAdapter.OFFLINE);
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
        if(item.getItemId()==R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showProgressBarForUsers(){
        mProgressBarForUsers.setVisibility(View.VISIBLE);
    }

    private void hideProgressBarForUsers(){
        if(mProgressBarForUsers.getVisibility()==View.VISIBLE) {
            mProgressBarForUsers.setVisibility(View.GONE);
        }
    }

    private ChildEventListener getChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    if (dataSnapshot.exists()) {
                        String userUid = dataSnapshot.getKey();
                        if (dataSnapshot.getKey().equals(mCurrentUserUid)) {
                            User currentUser = dataSnapshot.getValue(User.class);
                            mUsersChatAdapter.setCurrentUserInfo(userUid, currentUser.getEmail(),
                                    currentUser.getCreatedAt(), currentUser.getPhotoUri(),
                                    currentUser.getLatitude(), currentUser.getLongitude(),
                                            currentUser.getDistance());
                            Log.d("getCreatedAt", String.valueOf(currentUser.getCreatedAt()));
                            Log.d("mUsersKeyList email", String.valueOf(currentUser.getEmail()));
                            Log.d("getNonAvatarId", String.valueOf(currentUser.getNonAvatarId()));
                            Log.d("getLatreceiver", String.valueOf(currentUser.getLatitude()));
                            Log.d("getLonreceiver", String.valueOf(currentUser.getLongitude()));
                            Log.d("distanceto", currentUser.getDistance());

                        } else {
                            User recipient = dataSnapshot.getValue(User.class);
                            recipient.setRecipientId(userUid);;
                            mUsersKeyList.add(userUid);
                            mUsersChatAdapter.refill(recipient);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private List<User> compareDistance(List<User> mUsers, final double mLat, final double mLon) {
                Comparator<User> distance = new Comparator<User>() {
                    @Override
                    public int compare(User o, User o2) {
                        float[] result1 = new float[3];
                        Float distance1 = null;
                        if (!o.getLatitude().equals("") || !o.getLongitude().equals("")) {
                            android.location.Location.distanceBetween(mLat, mLon,
                                    o.getLatitude() != null ? Double.parseDouble(o.getLatitude()) : 0,
                                    o.getLongitude() != null ? Double.parseDouble(o.getLongitude()) : 0, result1);
                            distance1 = result1[0];
                        }
                        float[] result2 = new float[3];
                        Float distance2 = null;
                        if (!o2.getLatitude().equals("") || !o2.getLongitude().equals("")) {
                            android.location.Location.distanceBetween(mLat, mLon,
                                    o2.getLatitude() != null ? Double.parseDouble(o2.getLatitude()) : 0,
                                    o2.getLongitude() != null ? Double.parseDouble(o2.getLongitude()) : 0, result2);
                            distance2 = result2[0];
                            //Log.d("distance1.compareTo", String.valueOf(distance1.compareTo(distance2)));
                        }
                        return distance1.compareTo(distance2);
                    }
                };
                Collections.sort(mUsers, distance);
                return mUsers;
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                try {
                    if(dataSnapshot.exists()) {
                    String userUid = dataSnapshot.getKey();
                    if(!userUid.equals(mCurrentUserUid)) {
                        User user = dataSnapshot.getValue(User.class);
                        int index = mUsersKeyList.indexOf(userUid);
                        if(index > -1) {
                            mUsersChatAdapter.changeUser(index, user);
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
    private String onSignedInInitialize(String username) {
        String mUsername;
        if (username != null) {
            mUsername = username;
            // attachDatabaseReadListener();
        } else {
            mUsername = getString(R.string.email_sign);
        }
        return username;
    }


    /**
     * User has clicked the 'Invite' button, launch the invitation UI with the proper
     * title, message, and deep link
     */
    private void onInviteClicked() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse("https://w2mkk.app.goo.gl/b2OT"))
                .setCustomImage(Uri.parse("android.resource://"+getActivity().getPackageName()+"/mipmap/ic_launcher"))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
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

