package com.my.game.wesport.activity;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.HomeGridAdapter;
import com.my.game.wesport.adapter.UsersChatListAdapter;
import com.my.game.wesport.event.ProfileUpdatedLocalEvent;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.GameHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.NotificationModel;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static android.support.design.widget.Snackbar.make;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_INVITE = 23;
    private final int PERMISSION_MULTIPLE = 1;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private TextView txtName;
    private TextView emailText;
    private Toolbar toolbar;
    private ImageView imgNavHeaderCover, imgProfile;

    private String lat;
    private String lon;
    private String[] gridViewString;

    private GoogleApiClient mGoogleApiClient;
    private View mLayout;
    private View navHeader;
    private String chosenGame;
    private String TAG = MainActivity.class.getSimpleName();

    // index to identify current nav menu item
    public static int navItemIndex = 0;
    private boolean shouldLoadHomeFragOnBackPress = true;

    EventBus eventBus = EventBus.getDefault();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationHelper.subscribe();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.nav_drawer_name);
        imgNavHeaderCover = (ImageView) navHeader.findViewById(R.id.img_header_cover);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);
        emailText = (TextView) navHeader.findViewById(R.id.nav_drawer_email);

        // initializing navigation menu
        setUpNavigationView();

        gridViewString = getResources().getStringArray(R.array.games_array);
        mLayout = findViewById(android.R.id.content);

        HomeGridAdapter adapterViewAndroid = new HomeGridAdapter(MainActivity.this, gridViewString, GameHelper.getImages());
        GridView androidGridView = (GridView) findViewById(R.id.grid_view_image_text);
        androidGridView.setAdapter(adapterViewAndroid);
        adapterViewAndroid.setListener(new HomeGridAdapter.HomeGridListener() {
            @Override
            public void onItemClick(int position) {
                try {
                    chosenGame = gridViewString[position];
                    SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = chGame.edit();
                    editor.putString("chosenGame", chosenGame).apply();
                    editor.putInt("chosenGame_pos", position).apply();
                    if (isLocationEnabled(getApplicationContext())) {
                        Log.d("MainActivity", "onItemClick: ");
                        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                        startActivity(intent);
                    } else {
                        Snackbar.make(mLayout, getString(R.string.loc_not_enable),
                                Snackbar.LENGTH_SHORT).show();
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //setup GoogleApiclient
        buildGoogleApiClient();

        loadNavHeader();

//        location udpated in profile
        /*FirebaseHelper.getCurrentUserRef().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (FirebaseHelper.getCurrentUser() != null) {
                    UserModel user = dataSnapshot.getValue(UserModel.class);
                    if (user != null) {
                        App.getInstance().setUserModel(user);
                        loadNavHeader();
                        eventBus.post(new UserProfileUpdatedEvent());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    private void loadNavHeader() {
        UserModel userModel = App.getInstance().getUserModel();
        if (userModel == null) {
            return;
        }
        // Loading name and email
        txtName.setText(userModel.getDisplayName());
        emailText.setText(userModel.getEmail());
        try {
            // Loading header cover/background image
            Glide.with(this).load(userModel.getCoverUri()).into(imgNavHeaderCover);

            // Loading profile image
            Glide.with(this).load(userModel.getPhotoUri()).error(R.drawable.profile).into(imgProfile);
        } catch (Exception e) {
            Log.d(TAG, "loadNavHeader: " + e.getLocalizedMessage());
        }
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.nav_profile:
                        navItemIndex = 0;
                        startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
                        break;
                    case R.id.nav_invite_friends:
                        navItemIndex = 1;
                        FirebaseHelper.inviteFriends(MainActivity.this, REQUEST_INVITE);
                        break;
                    case R.id.nav_invites:
                        navItemIndex = 2;
                        startActivity(new Intent(MainActivity.this, InvitesActivity.class));
                        break;
                    case R.id.nav_nearby_users:
                        navItemIndex = 3;
                        startActivity(new Intent(MainActivity.this, NearbyUserActivity.class));
                        break;
                    case R.id.nav_dashboard:
                        navItemIndex = 3;
                        startActivity(TeamsActivity.newIntent(MainActivity.this));
                        break;
                    case R.id.nav_log_out:
                        // launch new intent instead of loading fragment
                        signoutuser();
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);
                //loadHomeFragment();
                return true;
            }
        });
    }

    private void signoutuser() {
        //FirebaseAuth.getInstance().signOut();
        NotificationHelper.unSubscribeAndLogout();
        mGoogleApiClient.disconnect();
        FirebaseHelper.setUserConnectionStatus(FirebaseHelper.getCurrentUser().getUid(), UsersChatListAdapter.OFFLINE);
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        startActivity(new Intent(MainActivity.this, SigninActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                }
            } else {
                // Sending failed or it was canceled, show failure message to the user
                // [START_EXCLUDE]
                Toast.makeText(this, R.string.send_failed, Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                //loadHomeFragment();
                return;
            }
        }
        super.onBackPressed();
    }

    @SuppressWarnings("UnusedAssignment")
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            //noinspection deprecation
            locationProviders = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_MULTIPLE) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for location permission.
            // Check if the only required permission has been granted

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted, preview can be displayed
                Snackbar.make(mLayout, R.string.permision_available_location,
                        Snackbar.LENGTH_SHORT).show();
                startLocationServices();
            } else {
                Toast.makeText(this, R.string.close_app, Toast.LENGTH_SHORT).show();
                MainActivity.this.finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    protected void onStart() {
        // Connect the client.
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getApplicationContext(), permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(getApplicationContext(), permission.ACCESS_COARSE_LOCATION) +
                (ContextCompat.checkSelfPermission(getApplicationContext(), permission.WRITE_CALENDAR))
                != PackageManager.PERMISSION_GRANTED) {
            if (
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission.WRITE_EXTERNAL_STORAGE)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission.ACCESS_COARSE_LOCATION)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission.WRITE_CALENDAR)) {
                make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, permission.WRITE_CALENDAR, permission.WRITE_EXTERNAL_STORAGE},
                                        PERMISSION_MULTIPLE);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, permission.WRITE_CALENDAR, permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSION_MULTIPLE);
            }
        } else {
            startLocationServices();
        }
    }

    private void startLocationServices() {
        try {
            LocationRequest mLocationRequest = LocationRequest.create();
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            if (mLastLocation == null) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            } else {
                lat = String.valueOf(mLastLocation.getLatitude());
                lon = String.valueOf(mLastLocation.getLongitude());
            }
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000); // Update location every 10 mins
            mLocationRequest.setFastestInterval(1000);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (SecurityException exception) {
            exception.printStackTrace();
        }
        storeprefs(lat, lon);
        updateLocationtoFirebase(lat, lon);
    }

    @SuppressWarnings("UnusedAssignment")
    private void updateLocationtoFirebase(String lat, String lon) {
        FirebaseUser user = null;
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            float distance = 0;
            //Instiantiate  task to update lat , lon with actual values and distance as 0 for each user
            FirebaseHelper.getCurrentUserRef().child("latitude").setValue(lat);
            FirebaseHelper.getCurrentUserRef().child("longitude").setValue(lon);
            FirebaseHelper.getCurrentUserRef().child("distance").setValue(String.valueOf(distance));
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());

        GeoFire geoFire = new GeoFire(FirebaseHelper.getLocationRef());
        geoFire.setLocation(FirebaseHelper.getCurrentUser().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));

        storeprefs(lat, lon);
        updateLocationtoFirebase(lat, lon);
    }

    private void storeprefs(String lat, String lon) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("latitude", lat).apply();
        editor.putString("longtitude", lon).apply();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseNotifyMessageFromBundle(intent);
    }

    private void parseNotifyMessageFromBundle(Intent intent) {
        Log.d(TAG, "parseNotifyMessageFromBundle: ");
        // user clicked on notification

        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.containsKey(NotificationHelper.EXTRA_MESSAGE)) {
//            dialogHelper.showProgressDialog("Please wait!", "Loading chat!", true);
            final String jsonMessage = bundle.getString(NotificationHelper.EXTRA_MESSAGE);
            final NotificationModel notificationModel = NotificationHelper.parse(jsonMessage);
            FirebaseHelper.getUserRef().child(notificationModel.getPotentialUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    dialogHelper.dismiss();
                    if (dataSnapshot != null) {
                        UserModel profileModel = dataSnapshot.getValue(UserModel.class);
                        if (notificationModel.getType() == NotificationHelper.TYPE_EVENT) {
                            startActivity(GroupActivity.newIntent(MainActivity.this, notificationModel.getGameKey(), notificationModel.getGameAuthorKey()));
                        } else if (notificationModel.getType() == NotificationHelper.TYPE_INVITATION) {
                            startActivity(new Intent(MainActivity.this, InvitesActivity.class));
                        } else {
                            startActivity(ChatActivity.newIntent(MainActivity.this, profileModel, dataSnapshot.getKey()));
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
//                    dialogHelper.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_invite_friends) {
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocalProfileUpdated(ProfileUpdatedLocalEvent event) {
        if (event.getImageType() == UserProfileActivity.EDIT_AVATAR) {
            // Loading profile image
            Glide.with(this).load(event.getImageUri()).error(R.drawable.profile).into(imgProfile);
        } else if (event.getImageType() == UserProfileActivity.EDIT_COVER_IMAGE) {
            // Loading header cover/background image
            Glide.with(this).load(event.getImageUri()).into(imgNavHeaderCover);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (eventBus.isRegistered(this)) {
            eventBus.unregister(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}