package com.my.game.wesport.login;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.my.game.wesport.FireChatHelper.ChatHelper;
import com.my.game.wesport.IntroActivity;
import com.my.game.wesport.MainActivity;
import com.my.game.wesport.R;
import com.my.game.wesport.model.User;

import java.util.Date;

import butterknife.ButterKnife;

@SuppressWarnings("ALL")
public class SigninActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;

    //private MessageAdapter mMessageAdapter;
    private ProgressBar mProgressBar;

    private String mUsername, loginUser;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseStorage mFirebaseStorage;
    private DatabaseReference mMessagesDatabaseReference;
    private DatabaseReference mDatabase;
    private StorageReference mChatPhotosStorageReference;
    private View mLayout;
    private boolean mActivity=false;

    private static final String TAG = SigninActivity.class.getSimpleName();
    //@BindView(R.id.edit_text_email_login)
    EditText mUserEmail;
    //@BindView(R.id.edit_text_password_log_in) EditText mUserPassWord;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = findViewById(android.R.id.content);

        //  Declare a new thread to do a preference check. Library to invoke Intro slides
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  Initialize SharedPreferences
                SharedPreferences getPrefs = PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext());

                //  Create a new boolean and preference and set it to true
                boolean isFirstStart = getPrefs.getBoolean("firstStart", true);

                //  If the activity has never started before...
                if (isFirstStart) {

                    //  Launch app intro
                    Intent i = new Intent(SigninActivity.this, IntroActivity.class);
                    startActivity(i);

                    //  Make a new preferences editor
                    SharedPreferences.Editor e = getPrefs.edit();

                    //  Edit preference to make it false because we don't want this to run again
                    e.putBoolean("firstStart", false);

                    //  Apply changes
                    e.apply();
                }
                else {
                    //  Launch Mainactivity
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                   // mActivity=true;

                }
            }
        });
        // Start the thread
        t.start();
        setContentView(R.layout.activity_main);

        //hideActionBar();
        bindButterKnife();
        setAuthInstance();

    }

    private void hideActionBar() {
        this.getActionBar().hide();
    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    private void setAuthInstance() {
        mUsername = ANONYMOUS;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        // Initialize Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();

        // Initialize references to views
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        // Initialize progress bar
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mMessagesDatabaseReference= FirebaseDatabase.getInstance().getReference();


        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    if (user.getDisplayName() != null) {
                        loginUser = onSignedInInitialize(user.getDisplayName());
                        onAuthSuccess(firebaseAuth.getCurrentUser());
                        setUserOnline();
                    } else {
//                        Log.d("user displayname",user.getDisplayName());
                        loginUser = onSignedInInitialize(getString(R.string.email_user));
                        onAuthSuccess(firebaseAuth.getCurrentUser());
                        setUserOnline();
                    }

                    // User is signed in
                } else {
                    // User is signed out
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setProviders(
                                            AuthUI.EMAIL_PROVIDER,
                                            AuthUI.GOOGLE_PROVIDER)
                                    .setTheme(R.style.GreenTheme)
                                    .setIsSmartLockEnabled(false)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }

    private void setUserOnline() {
        if(mFirebaseAuth.getCurrentUser()!=null ) {
            String userId = mFirebaseAuth.getCurrentUser().getUid();
            FirebaseDatabase.getInstance()
                    .getReference().
                    child("users").
                    child(userId).
                    child("connection").
                    setValue(com.my.game.wesport.adapter.UsersChatAdapter.ONLINE);
        }
    }

    private void onAuthSuccess(FirebaseUser user) {
        createNewUser(user.getUid());
    }

    private void createNewUser(String userId){
        User user = buildNewUser();
        mMessagesDatabaseReference.child("users").child(userId).setValue(user);
    }

    private User buildNewUser() {
        Log.d("loginUser",loginUser);
        Log.d("getUserEmail",getUserEmail());
        Log.d("Status",com.my.game.wesport.adapter.UsersChatAdapter.ONLINE);
        Log.d("Avatar", String.valueOf(ChatHelper.generateRandomAvatarForUser()));
        Log.d("datetime", String.valueOf(new Date().getTime()));
        Log.d("getUserPhotoUri", String.valueOf(getUserPhotoUri()));

        return new User(
                loginUser,
                getUserEmail(),
                com.my.game.wesport.adapter.UsersChatAdapter.ONLINE,
                getUserPhotoUri(),
                new Date().getTime()
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Snackbar.make(mLayout, getString(R.string.signin_string),
                    Snackbar.LENGTH_SHORT).show();
            // Sign-in succeeded, set up the UI
            Snackbar.make(mLayout, getString(R.string.signin_string),
                    Snackbar.LENGTH_SHORT).show();
            //if (!mActivity) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
           // }
        } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Snackbar.make(mLayout, getString(R.string.signin_cancel),
                        Snackbar.LENGTH_SHORT).show();
                finish();
            }
        }

    private String getUserEmail() {
        return mFirebaseAuth.getCurrentUser().getEmail();
    }

    private Uri getUserPhotoUri() {
        return mFirebaseAuth.getCurrentUser().getPhotoUrl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private String onSignedInInitialize(String username) {
        if (username != null) {
            mUsername = username;
            // attachDatabaseReadListener();
        } else {
            mUsername = getString(R.string.email_sign);
        }
        return username;
    }
}