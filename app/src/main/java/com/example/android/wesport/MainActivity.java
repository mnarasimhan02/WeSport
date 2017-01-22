package com.example.android.wesport;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final String TAG = "LocationActivity";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final int PERMISSION_LOCATION=1;
    private Menu mMenu;
    public  Context contextOfApplication;
    private Location mLastLocation;
    private GridView androidGridView;
    private View mLayout;


    String lat, lon;

    String[] gridViewString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridViewString = getResources().getStringArray(R.array.games_array);
        mLayout = findViewById(R.id.android_gridview_example);

        int[] gridViewImageId = {
                R.drawable.basketball, R.drawable.cricket, R.drawable.football, R.drawable.tennis,
                R.drawable.frisbee, R.drawable.pingpong, R.drawable.soccer, R.drawable.volleyball
        };
       // contextOfApplication = getApplicationContext();//required to retreive context in another class
        CustomGridViewActivity adapterViewAndroid = new CustomGridViewActivity(MainActivity.this, gridViewString, gridViewImageId);
        androidGridView = (GridView) findViewById(R.id.grid_view_image_text);
        androidGridView.setAdapter(adapterViewAndroid);
        androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int i, long id) {
                Toast.makeText(MainActivity.this, "Chosen Game: " + gridViewString[+i], Toast.LENGTH_LONG).show();
               // invalidateOptionsMenu();
                mMenu.getItem(0).setVisible(true);
            }
        });
        //setup GoogleApiclient
        buildGoogleApiClient();
        Log.d(TAG, "After build api client");
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_LOCATION) {
            // BEGIN_INCLUDE(permission_result)
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for location permission request.");

            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "LOCATION permission is granted");
                Snackbar.make(mLayout, R.string.permision_available_location  ,
                        Snackbar.LENGTH_SHORT).show();
            } else {
                Log.i(TAG, "LOCATION permission was NOT granted.");
                Snackbar.make(mLayout, R.string.permissions_not_granted,
                        Snackbar.LENGTH_SHORT).show();
                Snackbar.make(mLayout, R.string.close_app,
                        Snackbar.LENGTH_LONG).show();
                this.finish();

            }
            // END_INCLUDE(permission_result)

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.d(TAG, "GoogleAPIclient init ..............");

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();

    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        Log.d(TAG, "onStop fired ..............");
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
            Log.d(TAG, "Requesting Permissions");
        } else{
            startLocationServices();
        }
    }

    private void startLocationServices() {
        Log.d(TAG, "Starting Location Services");
        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            Log.d(TAG, "" + mLastLocation);
            if (mLastLocation != null) {
                lat = String.valueOf(mLastLocation.getLatitude());
                lon = String.valueOf(mLastLocation.getLongitude());
            }
            mLocationRequest = LocationRequest.create();
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(100); // Update location every 10 seconds
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch (SecurityException exception){
            Log.d(TAG, exception.toString());
        }
        storeprefs(lat, lon);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onLocationChanged(Location location) {
        // New location has now been determined
        Toast.makeText(MainActivity.this,"Location changed",Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Triggering location changed");
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());
        storeprefs(lat,lon);
    }

    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        mMenu.getItem(0).setVisible(false);

        //  By default no Menu
        return super.onPrepareOptionsMenu(menu);
    }

    //respond to menu item selection
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.menu_next) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        }
        return true;
    }

    void storeprefs(String lat, String lon) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("latitude", lat);
        editor.putString("longtitude", lon);
        editor.apply();
        String mLat = prefs.getString("latitude","");
        String mLon = prefs.getString("longtitude","");
    }
}