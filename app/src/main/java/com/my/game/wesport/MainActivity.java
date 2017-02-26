package com.my.game.wesport;

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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.my.game.wesport.ui.MyGames;

import static android.support.design.widget.Snackbar.make;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final int PERMISSION_MULTIPLE= 1;

    private String lat;
    private String lon;
    private String[] gridViewString;
    private GoogleApiClient mGoogleApiClient;
    private View mLayout;
    private String chosenGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridViewString = getResources().getStringArray(R.array.games_array);
        mLayout = findViewById(android.R.id.content);

        int[] gridViewImageId = {
                R.drawable.basketball, R.drawable.cricket, R.drawable.football, R.drawable.tennis,
                R.drawable.frisbee, R.drawable.pingpong, R.drawable.soccer, R.drawable.volleyball
        };
        CustomGridViewActivity adapterViewAndroid = new CustomGridViewActivity(MainActivity.this, gridViewString, gridViewImageId);
        GridView androidGridView = (GridView) findViewById(R.id.grid_view_image_text);
        androidGridView.setAdapter(adapterViewAndroid);
        androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int i, long id) {
                make(mLayout, getString(R.string.chosen_game) + " "+gridViewString[+i],
                        Snackbar.LENGTH_LONG).show();
                chosenGame = gridViewString[+i];
                SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = chGame.edit();
                editor.putString("chosenGame", chosenGame).apply();
                if (isLocationEnabled(getApplicationContext())) {
                    Intent intent = new Intent(getApplicationContext(), MyGames.class);
                    startActivity(intent);
                }
                else {
                    make(mLayout, getString(R.string.loc_not_enable),
                            Snackbar.LENGTH_LONG).show();
                    startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                }
            }
        });
        //setup GoogleApiclient
        buildGoogleApiClient();
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            //noinspection deprecation
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_MULTIPLE ) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for location permission.
            // Check if the only required permission has been granted

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted, preview can be displayed
                Snackbar.make(mLayout, R.string.permision_available_location,
                        Snackbar.LENGTH_LONG).show();
                startLocationServices();
            } else {
                Snackbar.make(mLayout, R.string.close_app,Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar,
                                            int event) {
                        if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                            MainActivity.this.finish();
                        }
                    }
                }).show();
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
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission.ACCESS_COARSE_LOCATION) +
                (ContextCompat.checkSelfPermission(getApplicationContext(), permission.WRITE_CALENDAR))
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (MainActivity.this, permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (MainActivity.this, permission.WRITE_CALENDAR)) {
                make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_LONG).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, permission.WRITE_CALENDAR},
                                        PERMISSION_MULTIPLE);
                            }
                        }).show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, permission.WRITE_CALENDAR},
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
            if (mLastLocation==null){
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
        storeprefs(lat, lon);
    }

    private void storeprefs(String lat, String lon) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("latitude", lat).apply();
        editor.putString("longtitude", lon).apply();
    }
}