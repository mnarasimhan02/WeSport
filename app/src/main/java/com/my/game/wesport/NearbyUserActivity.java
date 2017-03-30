package com.my.game.wesport;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import java.util.HashMap;
import java.util.Map;

public class NearbyUserActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {
    private View myContentsView;

    private GoogleMap map;
    private Marker marker;
    private MarkerOptions userMarker;
    private SupportMapFragment mainNearbyFragment;
    private Map<String, DataSnapshot> users = new HashMap<>();
    private String TAG = NearbyUserActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_user);
        myContentsView = getLayoutInflater().inflate(R.layout.custom_info_nearby, null);
        setUpMapIfNeeded();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }


    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mainNearbyFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nearby_map);
            mainNearbyFragment.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        map.getUiSettings().setZoomControlsEnabled(false);
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }

    //          Getting Nearby Users
    private void getNearByUser() {
        FirebaseHelper.getUserRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final UserModel recipient = dataSnapshot.getValue(UserModel.class);
                LatLng currentLatLng = getLocationFromPref();
                //Shows only those users which are less then 50 miles
                if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, recipient.getLatitude(), recipient.getLongitude())) <= 50) {
                    createMarkers(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

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

    private LatLng getLocationFromPref() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        LatLng latLng = new LatLng(Double.parseDouble(prefs.getString("latitude", "0.0")),
                Double.parseDouble(prefs.getString("longtitude", "0.0"))
        );
        setUserMarker(latLng);
        return latLng;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mainNearbyFragment.setRetainInstance(true);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);
        try {
            //Instiantiate background task to get nearby users
            getNearByUser();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setUserMarker(LatLng latLng) {
        if (userMarker == null) {
            userMarker = new MarkerOptions().position(latLng).title(getString(R.string.usermarker_title));
            userMarker.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
            map.addMarker(userMarker);
        }
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(12.9f));

    }

    private GoogleMap.OnMarkerClickListener userMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (users.containsKey(marker.getId())) {
                marker.showInfoWindow();
            }
            // show dialog
            return true;
        }
    };

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.nearby_maps_menu, menu);
        return true;
    }

    //respond to menu item selection
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return true;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        NearbyUserActivity.this.marker = marker;
        UserModel user = users.get(marker.getId()).getValue(UserModel.class);
        if (user == null) {
            return null;
        }
        TextView nearbyUserName = ((TextView) myContentsView.findViewById(R.id.nearby_user_name));
        nearbyUserName.setText(user.getDisplayName());
        TextView nearbyUserEmail = ((TextView) myContentsView.findViewById(R.id.nearby_user_email));
        nearbyUserEmail.setText(user.getEmail());

        ImageView userImage = (ImageView) myContentsView.findViewById(R.id.nearby_user_image);
        Log.d(TAG, "getInfoWindow: " + user.getPhotoUri());
        Glide.with(NearbyUserActivity.this)
                .load(user.getPhotoUri())
                .listener(new InfoWindowRefresher(marker))
                .into(userImage);
        return myContentsView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (NearbyUserActivity.this.marker != null && NearbyUserActivity.this.marker.isInfoWindowShown()) {
            NearbyUserActivity.this.marker.hideInfoWindow();
            NearbyUserActivity.this.marker.showInfoWindow();
        }
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        DataSnapshot dataSnapshot = users.get(marker.getId());
        UserModel userModel = dataSnapshot.getValue(UserModel.class);
        if (userModel != null) {
            userModel.setRecipientId(dataSnapshot.getKey());
            startActivity(ChatActivity.newIntent(this, userModel));
        }
    }

    private void createMarkers(DataSnapshot dataSnapshot) {
        try {
            UserModel userModel = dataSnapshot.getValue(UserModel.class);
            LatLng latLng = new LatLng(Double.parseDouble(userModel.getLatitude()), Double.parseDouble(userModel.getLongitude()));
            Marker parkMarker = map.addMarker(new MarkerOptions()
                    .title(userModel.getDisplayName())
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.play_marker)));

            users.put(parkMarker.getId(), dataSnapshot);

            //placeOpen.put(parkMarker.getId(), placeOpenstr);}
            map.animateCamera(CameraUpdateFactory.zoomTo(12.9f));
                        /*Snackbar.make(mLayout, getString(R.string.map_help),
                                Snackbar.LENGTH_LONG).show();*/
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onResponse: " + e.getMessage());
        }
    }
}
