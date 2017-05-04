package com.my.game.wesport.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.people.v1.People;
import com.google.api.services.people.v1.model.Person;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.IntroActivity;
import com.my.game.wesport.activity.MainActivity;
import com.my.game.wesport.adapter.UsersChatListAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.UserModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SigninActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";
    public static final int RC_SIGN_IN = 1;
    public static final int RC_SIGN_GOOGLE = 2;
    boolean isNewUser = false;


    //private MessageAdapter mMessageAdapter;
    private String userBio;

    private UserModel userModel;
    // Firebase instance variables
    private DatabaseReference mMessagesDatabaseReference;

    private String mLatitude = null;
    private String mLongitude = null;
    private String mDistance = null;

    public static final String GOOGLE_CLIENT_ID = "1096682850687-il386lp4cmprppmidarfmgbosstc6btf.apps.googleusercontent.com";
    public static final String GOOGLE_CLIENT_SECRET = "Os41WW9ZVY2_41qRjoMXCGs3";

    private String profileAbout = "";
    private String profileCover = "";

    private static final String TAG = SigninActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

      /*  AdView mAdView = (AdView) findViewById(R.id.adView_signIn);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("8D55278B12588486D7D396079CB75B6B")
                .build();
        mAdView.loadAd(adRequest)*/;
        skipSplashScreen();

    }

   /* public void onSkipClick(View view) {
       // skipSplashScreen(view);
    }*/

    public void skipSplashScreen() {
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
            }
        });
        // Start the thread
        t.start();

        //hideActionBar();
        setAuthInstance();
    }

    private void setAuthInstance() {
        try {
            setupGoogleAdditionalDetailsLogin();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "setAuthInstance: " + e.getMessage());
        }

        mMessagesDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //enabling disk persistance
        if (FirebaseHelper.getCurrentUser() != null) {
            firebaseLoginSuccess();
        } else {
            loginWithFirebase();
        }
    }

    private void loginWithFirebase() {
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setProviders(getSelectedProviders())
                        .setTheme(R.style.GreenTheme)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
    }

    private List<AuthUI.IdpConfig> getSelectedProviders() {
        /*AuthUI.IdpConfig googleIdp = new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                .setPermissions(Arrays.asList(Scopes.EMAIL, Scopes.PROFILE, Scopes.PLUS_ME))
                .build();*/

        return Arrays.asList(
                new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()
        );
        /*ArrayList<String> selectedProviders = new ArrayList<>();
        selectedProviders.add(AuthUI.EMAIL_PROVIDER);
        selectedProviders.add(AuthUI.GOOGLE_PROVIDER);
        selectedProviders.add(AuthUI.FACEBOOK_PROVIDER);
        return selectedProviders.toArray(new String[selectedProviders.size()]);*/
    }

    private UserModel createNewUser(String userId) {
        isNewUser = true;
        UserModel userModel = buildNewUser();
        userModel.setBio(profileAbout);
        userModel.setCoverUri(profileCover);
        mMessagesDatabaseReference.child("users").child(userId).setValue(userModel);
        return userModel;
    }

    public UserModel buildNewUser() {
        return new UserModel(
                FirebaseHelper.getCurrentUser().getDisplayName(),
                getUserEmail(),
                UsersChatListAdapter.ONLINE,
                getUserPhotoUri(),
                new Date().getTime(),
                mLatitude,
                mLongitude,
                mDistance,
                userBio
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void firebaseLoginSuccess() {
        final FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        mMessagesDatabaseReference.child("users").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if user first time logged in then create its profile
                userModel = dataSnapshot.getValue(UserModel.class);
                if (userModel == null) {
                    userModel = createNewUser(currentUser.getUid());
                }
                App.getInstance().setUserModel(userModel);
                FirebaseHelper.setUserConnectionStatus(currentUser.getUid(), UsersChatListAdapter.ONLINE);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_IS_FIRST_TIME_REGISTER, isNewUser);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void startGoogleAdditionalRequest() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_GOOGLE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        if (resultCode == RESULT_OK) {
            if (requestCode == RC_SIGN_IN) {
                if (FirebaseAuth.getInstance().getCurrentUser().getProviders().get(0).equals("google.com")) {
                    startGoogleAdditionalRequest();
                } else {
                    firebaseLoginSuccess();
                }
            } else if (requestCode == RC_SIGN_GOOGLE) {
                googleAdditionalDetailsResult(data);
            }
        } else {
            if (requestCode == RC_SIGN_GOOGLE) {
                firebaseLoginSuccess();
            } else {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, getString(R.string.signin_cancel), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public String getUserEmail() {
        return FirebaseHelper.getCurrentUser().getEmail();
    }

    private String getUserPhotoUri() {
        return String.valueOf(FirebaseHelper.getCurrentUser().getPhotoUrl());
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

    public void googleAdditionalDetailsResult(Intent data) {
        Log.d(TAG, "googleAdditionalDetailsResult: " + data);
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        if (result != null && result.isSuccess()) {
            // Signed in successfully
            GoogleSignInAccount acct = result.getSignInAccount();
            // execute AsyncTask to get data from Google People API
            new GoogleAdditionalDetailsTask().execute(acct);
        } else {
            Log.d(TAG, "googleAdditionalDetailsResult: fail");
            firebaseLoginSuccess();
        }
    }

    public class GoogleAdditionalDetailsTask extends AsyncTask<GoogleSignInAccount, Void, Person> {

        @Override
        protected Person doInBackground(GoogleSignInAccount... googleSignInAccounts) {
            Person profile = null;
            try {
                HttpTransport httpTransport = new NetHttpTransport();
                JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

                //Redirect URL for web based applications.
                // Can be empty too.
                String redirectUrl = "urn:ietf:wg:oauth:2.0:oob";

                // Exchange auth code for access token
                GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                        httpTransport,
                        jsonFactory,
                        GOOGLE_CLIENT_ID,
                        GOOGLE_CLIENT_SECRET,
                        googleSignInAccounts[0].getServerAuthCode(),
                        redirectUrl
                ).execute();

                GoogleCredential credential = new GoogleCredential.Builder()
                        .setClientSecrets(GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET)
                        .setTransport(httpTransport)
                        .setJsonFactory(jsonFactory)
                        .build();

                credential.setFromTokenResponse(tokenResponse);

                People peopleService = new People.Builder(httpTransport, jsonFactory, credential)
                        .setApplicationName(App.getInstance().getString(R.string.app_name))
                        .build();

                // Get the user's profile
                profile = peopleService.people().get("people/me").execute();
            } catch (IOException e) {
                Log.d(TAG, "doInBackground: " + e.getMessage());
                e.printStackTrace();
            }
            return profile;
        }

        @Override
        protected void onPostExecute(Person person) {
            if (person != null) {
                if (person.getBiographies() != null && person.getBiographies().size() > 0) {
                    profileAbout = person.getBiographies().get(0).getValue();
                }
                if (person.getCoverPhotos() != null && person.getCoverPhotos().size() > 0) {
                    profileCover = person.getCoverPhotos().get(0).getUrl();
                }
                Log.d(TAG, String.format("googleOnComplete: about: %s, cover: %s", profileAbout, profileCover));
            }
            firebaseLoginSuccess();
        }
    }

    private void setupGoogleAdditionalDetailsLogin() throws Exception {
        // Configure sign-in to request the user's ID, email address, and basic profile. ID and
        // basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(GOOGLE_CLIENT_ID)
                .requestServerAuthCode(GOOGLE_CLIENT_ID)
                .requestScopes(new Scope("profile"))
                .build();

        // Build a GoogleApiClient with access to GoogleSignIn.API and the options above.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.d(TAG, "onConnectionFailed: ");
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}