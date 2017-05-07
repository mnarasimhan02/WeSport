package com.my.game.wesport.activity;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
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
import com.my.game.wesport.adapter.HomeGamesFragment;
import com.my.game.wesport.adapter.UsersChatListAdapter;
import com.my.game.wesport.event.EventLocationUpdated;
import com.my.game.wesport.event.ProfileUpdatedLocalEvent;
import com.my.game.wesport.fragment.MapsFragment;
import com.my.game.wesport.fragment.UserChatListFragment;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.NotificationHelper;
import com.my.game.wesport.helper.PermissionHelper;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.GameInviteModel;
import com.my.game.wesport.model.NotificationModel;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {
    private static final int REQUEST_INVITE = 23;
    private final int PERMISSION_MULTIPLE = 1;
    public static final String EXTRA_IS_FIRST_TIME_REGISTER = "first_time_register";

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private TextView txtName;
    private TextView emailText;
    private Toolbar toolbar;
    private ImageView imgNavHeaderCover, imgProfile;
    private TextView invites;
    private ImageView inviteImage;

    private String lat;
    private String lon;
    //private String[] gridViewString;

    private GoogleApiClient mGoogleApiClient;
    private View mLayout;
    private View navHeader;
    private String chosenGame;
    private String TAG = MainActivity.class.getSimpleName();

    private static String EXTRA_KEY_GAME = "key_game";
    private static String EXTRA_PLACE_ID = "place_id";

    boolean deleteAllGames = false;
    String placeId;
    String gameKey;

    boolean isNewUser = false;

    // index to identify current nav menu item
    public static int navItemIndex = 0;
    private boolean shouldLoadHomeFragOnBackPress = true;

    EventBus eventBus = EventBus.getDefault();
    //private NearbyUserFragment nearbyUserFragment = NearbyUserFragment.newInstance();
    private HomeGamesFragment homeGamesFragment;
    private MapsFragment mapsFragment = MapsFragment.newInstance();
    private UserChatListFragment userChatListFragment;
    private ViewPager mViewPager;

    boolean showMapToast = true;

    public static Intent newIntent(Context context, String gameKey, String placeId) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_KEY_GAME, gameKey);
        intent.putExtra(EXTRA_PLACE_ID, placeId);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (FirebaseHelper.getCurrentUser() == null) {
            startActivity(new Intent(this, SigninActivity.class));
            finish();
            return;
        }

        if (getIntent() != null && getIntent().hasExtra(EXTRA_IS_FIRST_TIME_REGISTER)) {
            isNewUser = getIntent().getBooleanExtra(EXTRA_IS_FIRST_TIME_REGISTER, false);
        }

        if (isNewUser) {
            startActivity(new Intent(this, UserProfileActivity.class));
        }

        FirebaseHelper.syncUserConnectionStatus();
        //Notification
        NotificationHelper.subscribe();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        gameKey = getIntent().getStringExtra(EXTRA_KEY_GAME);

        //Setting up Navigation drawer and Navigation view
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        invites = (TextView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_invites));
        inviteImage = (ImageView) MenuItemCompat.getActionView(navigationView.getMenu().findItem(R.id.nav_invite_friends));
        inviteImage.setImageResource(R.drawable.ic_small_launcher);

        //Navigation view headerg
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.nav_drawer_name);
        imgNavHeaderCover = (ImageView) navHeader.findViewById(R.id.img_header_cover);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);
        emailText = (TextView) navHeader.findViewById(R.id.nav_drawer_email);

        // initializing navigation menu
        setUpNavigationView();

        //gridViewString = getResources().getStringArray(R.array.games_array);
        mLayout = findViewById(android.R.id.content);

        //setting tabs and view pager
        MainActivity.SectionsPagerAdapter mSectionsPagerAdapter = new MainActivity.SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

       /* HomeGridAdapter adapterViewAndroid = new HomeGridAdapter(MainActivity.this, gridViewString, GameHelper.getImages());
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
        });*/
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
        parseNotifyMessageFromBundle(getIntent());

        homeGamesFragment = HomeGamesFragment.newInstance(placeId, HomeGamesFragment.TYPE_USER_GAMES, HomeGamesFragment.TYPE_NEARBY_GAMES);
        userChatListFragment = UserChatListFragment.newInstance(false, null, null);
        //mapsFragment = new MapsFragment();

        FirebaseHelper.getInvitesRef(FirebaseHelper.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int pendingInvitesCounter = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GameInviteModel inviteModel = snapshot.getValue(GameInviteModel.class);
                    if (inviteModel != null && TextUtils.isEmpty(inviteModel.getStatus())) {
                        Log.d(TAG, "onDataChange: " + inviteModel);
                        pendingInvitesCounter++;
                    }
                }
                initializeCountDrawer(String.valueOf(pendingInvitesCounter));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initializeCountDrawer(String count) {
        //Gravity property aligns the text
        if (invites != null) {
            invites.setGravity(Gravity.CENTER_VERTICAL);
            invites.setTypeface(null, Typeface.BOLD);
            invites.setTextColor(getResources().getColor(R.color.colorAccent));
            invites.setText(count);
        }
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
                        navItemIndex = 4;
                        mViewPager.setCurrentItem(1, true);
                        drawer.closeDrawers();
//                        startActivity(TeamsActivity.newIntent(MainActivity.this));
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

    /*@SuppressWarnings("UnusedAssignment")
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
    }*/

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_MULTIPLE) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for location permission.
            // Check if the only required permission has been granted

            boolean isGranted = true;
            for (int grantResult : grantResults) {
                isGranted = isGranted && grantResult == PackageManager.PERMISSION_GRANTED;
            }

            if (isGranted) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionHelper.check(MainActivity.this, new String[]{
                permission.ACCESS_COARSE_LOCATION,
                permission.WRITE_CALENDAR,
                permission.WRITE_EXTERNAL_STORAGE,
                permission.ACCESS_COARSE_LOCATION,
                permission.WRITE_CALENDAR
        }, PERMISSION_MULTIPLE)) {
            Snackbar.make(mLayout, R.string.permissions_not_granted,
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
            startLocationServices();
        }
    }


    private void startLocationServices() {
        Location mLastLocation = null;
        try {
            LocationRequest mLocationRequest = LocationRequest.create();
            if (!PermissionHelper.check(this, new String[]{permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION}, PERMISSION_MULTIPLE)) {
                return;
            }
            if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            Log.d(TAG, "startLocationServices: " + mLastLocation);

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(10000); // Update location every 10 sec
            mLocationRequest.setFastestInterval(1000);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        } catch (Exception exception) {
            exception.printStackTrace();
            Log.d(TAG, "startLocationServices: " + exception.getMessage());
        }

        if (mLastLocation != null) {
            lat = String.valueOf(mLastLocation.getLatitude());
            lon = String.valueOf(mLastLocation.getLongitude());
            storeprefs(lat, lon);
            updateLocationtoFirebase(lat, lon);
            EventBus.getDefault().post(new EventLocationUpdated(mLastLocation));
        }
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
        Log.d(TAG, "onConnectionFailed: " + connectionResult.getErrorMessage());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: ");
        // New location has now been determined
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());

        Log.d(TAG, "onLocationChanged: lat: " + lat + ", long: " + lon);

        GeoFire geoFire = new GeoFire(FirebaseHelper.getUserLocationRef());
        geoFire.setLocation(FirebaseHelper.getCurrentUser().getUid(), new GeoLocation(location.getLatitude(), location.getLongitude()));

        storeprefs(lat, lon);
        updateLocationtoFirebase(lat, lon);
        EventBus.getDefault().post(new EventLocationUpdated(location));
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
            Log.d(TAG, "parseNotifyMessageFromBundle: " + jsonMessage);
            intent.removeExtra(NotificationHelper.EXTRA_MESSAGE);
            final NotificationModel notificationModel = NotificationHelper.parse(jsonMessage);
            FirebaseHelper.getUserRef().child(notificationModel.getSenderId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
//                    dialogHelper.dismiss();
                    if (dataSnapshot != null) {
                        UserModel profileModel = dataSnapshot.getValue(UserModel.class);
                        if (notificationModel.getType() == NotificationHelper.TYPE_EVENT) {
                            startActivity(GroupActivity.newIntent(MainActivity.this, notificationModel.getGameKey(), notificationModel.getGameAuthorKey()));
                        } else if (notificationModel.getType() == NotificationHelper.TYPE_INVITATION) {
                            startActivity(new Intent(MainActivity.this, InvitesActivity.class));
                        } else if (notificationModel.getType() == NotificationHelper.TYPE_CHAT) {
                            startActivity(ChatActivity.newIntent(MainActivity.this, profileModel, dataSnapshot.getKey()));
                        } else if (notificationModel.getType() == NotificationHelper.TYPE_GROUP_CHAT) {
                            startActivity(GroupActivity.newIntent(MainActivity.this, notificationModel.getGameKey(), notificationModel.getGameAuthorKey()));
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return mapsFragment;
                case 1:
                    return homeGamesFragment;
                case 2:
                    return userChatListFragment;
            }
            // invalidateOptionsMenu();
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Maps";
                case 1:
                    return "Games";
                case 2:
                    return "Chat";
            }
            return null;
        }
    }

    public void switchPage(int pos) {
        if (mViewPager != null) {
            mViewPager.setCurrentItem(pos, true);
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

    public boolean isShowMapToast() {
        return showMapToast;
    }

    public void setShowMapToast(boolean showMapToast) {
        this.showMapToast = showMapToast;
    }
}