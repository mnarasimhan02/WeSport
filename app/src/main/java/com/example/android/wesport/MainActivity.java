package com.example.android.wesport;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

public class MainActivity extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private final String TAG = "LocationActivity";

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationManager locationManager;

    String lat, lon;
    Location mLastLocation;
    GridView androidGridView;
    String[] gridViewString = {
            "Basketball", "Cricket", "Football", "Tennis", "Frisbee", "Pingpong", "Soccer", "Volleyball"
    } ;
    int[] gridViewImageId = {
            R.drawable.basketball, R.drawable.cricket, R.drawable.football,R.drawable.tennis,
            R.drawable.frisbee, R.drawable.pingpong, R.drawable.soccer,R.drawable.volleyball
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CustomGridViewActivity adapterViewAndroid = new CustomGridViewActivity(MainActivity.this, gridViewString, gridViewImageId);
        androidGridView = (GridView) findViewById(R.id.grid_view_image_text);
        androidGridView.setAdapter(adapterViewAndroid);
        androidGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int i, long id) {
                Toast.makeText(MainActivity.this, "GridView Item: " + gridViewString[+i], Toast.LENGTH_LONG).show();
            }
        });
//setup GoogleApiclient

        buildGoogleApiClient();
//Check Run time permissions
        if (BuildConfig.VERSION_CODE <23 && ContextCompat.checkSelfPermission
                (this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //Request for Permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Display UI and wait for user interaction
            } else {
                ActivityCompat.requestPermissions(
                        this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
        } else {
            // permission has been granted, continue as usual
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        }
        storeprefs();
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000); // Update location every 10 seconds


        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                //Request for Permission
                // ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // Display UI and wait for user interaction

                } else {
                    ActivityCompat.requestPermissions(
                            this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            1);
                }
            } else {
                // permission has been granted, continue as usual
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            }
        } catch (SecurityException e)
            {
                Log.e("Location Update",e+" ");
            }

        if (mLastLocation != null) {
            lat = String.valueOf(mLastLocation.getLatitude());
            lon = String.valueOf(mLastLocation.getLongitude());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if ((ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission
                    (this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                Location myLocation =
                        LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
     //           Log.i(TAG, myLocation.toString());
            }
        }
    }
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
        mGoogleApiClient.connect();
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
        String lat = Double.toString(location.getLatitude());
        String lon = Double.toString(location.getLongitude());
        storeprefs();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

    }
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_options, menu);
        return true;
    }
    //respond to menu item selection
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        if (item.getItemId()==R.id.menu_next) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
           //intent.putExtra(mCurrentLocation,"Current Location");
            startActivity(intent);
        }
        return true;
    }

    void storeprefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("latitude", lat);
        editor.putString("longtitude", lon);
        editor.commit();
        String mLat = prefs.getString("latitude","");
        String mLon = prefs.getString("longtitude","");
        Log.i(TAG, mLat);
    }
}