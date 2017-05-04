package com.my.game.wesport.fragment;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.InfoWindowRefresher;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.NearbyUserActivity;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.model.UserMarkerModel;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import java.util.HashMap;
import java.util.Map;

public class NearbyUserFragment extends Fragment implements OnMapReadyCallback, GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener, GeoQueryEventListener {

    private View myContentsView;
    View rootView;

    private GoogleMap map;
    private Marker marker;
    private MarkerOptions userMarkerOptions;
    private SupportMapFragment mainNearbyFragment;
    private Map<String, UserMarkerModel> userMarkerList = new HashMap<>();
    private String TAG = NearbyUserActivity.class.getSimpleName();

    private GeoFire geoFire;
    private GeoQuery geoQuery;
    Runnable setupGeoFireRunnable;
    boolean isGeofireInitialized = false;
    private final double GEO_FIRE_RADIUS_KM = 80.4672;
    private Handler mHandler = new Handler();

    public static NearbyUserFragment newInstance() {
        Bundle args = new Bundle();
        NearbyUserFragment fragment = new NearbyUserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView != null) {
            ViewGroup parent = (ViewGroup) rootView.getParent();
            if (parent != null)
                parent.removeView(rootView);
        }
        try {
            rootView = inflater.inflate(R.layout.fragment_nearby_user, container, false);
        } catch (InflateException e) {
            // map is already there, just return view as it is
        }

        myContentsView = inflater.inflate(R.layout.custom_info_nearby, null);

        setupGeoFireRunnable = new Runnable() {
            @Override
            public void run() {
                setupGeoFire(LocationHelper.getLocationFromPref());
            }
        };

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {

            // Try to obtain the map from the SupportMapFragment.
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mainNearbyFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.nearby_map_fragment);
            mainNearbyFragment.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        map.getUiSettings().setZoomControlsEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }

    //          Getting Nearby Users
    /*private void getNearByUser() {
        FirebaseHelper.getUserRef().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final UserModel recipient = dataSnapshot.getValue(UserModel.class);
                LatLng currentLatLng = LocationHelper.getLocationFromPref();
                setUserMarkerOptions(currentLatLng);
                //Shows only those userMarkerList which are less then 50 miles
                if (Float.parseFloat(LocationHelper.getDistance(currentLatLng.latitude, currentLatLng.longitude, recipient.getLatitude(), recipient.getLongitude())) <= 50) {
                    createUserMarker(dataSnapshot);
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
    }*/

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        mainNearbyFragment.setRetainInstance(true);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);

        LatLng location = LocationHelper.getLocationFromPref();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 9.9f));

        setUserMarkerOptions(location);

        new Handler().post(setupGeoFireRunnable);
        /*try {
            //Instiantiate background task to get nearby userMarkerList
            getNearByUser();

        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    public void setUserMarkerOptions(LatLng latLng) {
        if (userMarkerOptions == null) {
            userMarkerOptions = new MarkerOptions().position(latLng).title(getString(R.string.usermarker_title));
            userMarkerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.user_marker));
            map.addMarker(userMarkerOptions);
        }
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        map.animateCamera(CameraUpdateFactory.zoomTo(12.9f));

    }

    private GoogleMap.OnMarkerClickListener userMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if (userMarkerList.containsKey(marker.getId())) {
                marker.showInfoWindow();
            }
            // show dialog
            return true;
        }
    };


    @Override
    public View getInfoWindow(Marker marker) {
        this.marker = marker;
        UserModel user;
        try {
            user = userMarkerList.get(marker.getId()).getDataSnapshot().getValue(UserModel.class);
        } catch (Exception e) {
            Log.d(TAG, "getInfoWindow: " + e.getMessage());
            return null;
        }
        if (user == null) {
            return null;
        }
        TextView nearbyUserName = ((TextView) myContentsView.findViewById(R.id.nearby_user_name));
        nearbyUserName.setText(user.getDisplayName());
        /*TextView nearbyUserEmail = ((TextView) myContentsView.findViewById(R.id.nearby_user_email));
        nearbyUserEmail.setText(user.getEmail());*/

        ImageView userImage = (ImageView) myContentsView.findViewById(R.id.nearby_user_image);
        Log.d(TAG, "getInfoWindow: " + user.getPhotoUri());
        Glide.with(this)
                .load(user.getPhotoUri())
                .listener(new InfoWindowRefresher(marker))
                .into(userImage);
        return myContentsView;
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (this.marker != null && this.marker.isInfoWindowShown()) {
            this.marker.hideInfoWindow();
            this.marker.showInfoWindow();
        }
        return null;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        DataSnapshot dataSnapshot = userMarkerList.get(marker.getId()).getDataSnapshot();
        UserModel userModel = dataSnapshot.getValue(UserModel.class);
        if (userModel != null) {
            startActivity(ChatActivity.newIntent(getActivity(), userModel, dataSnapshot.getKey()));
        }
    }

    private void createUserMarker(final DataSnapshot dataSnapshot, GeoLocation location) {
        try {
            final UserModel userModel = dataSnapshot.getValue(UserModel.class);
            final LatLng latLng = new LatLng(location.latitude, location.longitude);

            Marker userMarker = map.addMarker(new MarkerOptions()
                    .title(userModel.getDisplayName())
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_user_marker)));

            userMarkerList.put(userMarker.getId(), new UserMarkerModel(dataSnapshot, userMarker));

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onResponse: " + e.getMessage());
        }
    }

    private void setupGeoFire(LatLng latLng) {
        if (latLng == null || (latLng.latitude == 0.0 && latLng.longitude == 0.0)) {
            new Handler().postDelayed(setupGeoFireRunnable, 2000);
            return;
        }

        geoFire = new GeoFire(FirebaseHelper.getUserLocationRef());
        try {
            geoQuery = geoFire.queryAtLocation(
                    new GeoLocation(latLng.latitude, latLng.longitude), GEO_FIRE_RADIUS_KM);
            this.geoQuery.addGeoQueryEventListener(this);
            isGeofireInitialized = true;
        } catch (Exception e) {
//            FirebaseCrash.log(TAG + ": GeoFire Exception: " + e.getMessage());
//            Toast.makeText(mContext, "GeoFire Exception" + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "onCreate: " + e.getMessage());
        }
    }

    @Override
    public void onKeyEntered(final String key, final GeoLocation location) {
        if (key.equals(FirebaseHelper.getCurrentUser().getUid())) {
            return;
        }
        FirebaseHelper.getUserRef().child(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    createUserMarker(dataSnapshot, location);
                } catch (Exception e) {
                    Log.d(TAG, "onDataChange: " + e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: " + databaseError.getMessage());
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        // add an event listener to start updating locations again

        if (mHandler != null) {
            mHandler.removeCallbacks(setupGeoFireRunnable);
        }
        if (this.geoQuery != null) {
            try {
//                avoid error of removeThumb query listener if not added
                this.geoQuery.removeGeoQueryEventListener(this);
            } catch (Exception e) {
                Log.d(TAG, "onStop: " + e.getMessage());
            }
        }

    }

    @Override
    public void onKeyExited(String key) {
        for (Map.Entry<String, UserMarkerModel> userMarker : userMarkerList.entrySet()) {
            if (userMarker.getValue().getDataSnapshot().getKey().equals(key)) {
                userMarkerList.remove(userMarker.getKey());
            }
        }
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
        for (Map.Entry<String, UserMarkerModel> userMarker : userMarkerList.entrySet()) {
            if (userMarker.getValue().getDataSnapshot().getKey().equals(key)) {
                userMarker.getValue().getMarker().setPosition(new LatLng(location.latitude, location.longitude));
                break;
            }
        }
    }

    @Override
    public void onGeoQueryReady() {
        Log.d("existed", "a");
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        isGeofireInitialized = false;
        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage("Unexpected error!" + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
