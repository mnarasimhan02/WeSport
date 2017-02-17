package com.my.game.wesport;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.my.game.wesport.POJO.Example;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static android.R.attr.rating;

@SuppressWarnings("ALL")
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapLongClickListener, PlaceSelectionListener, InfoWindowAdapter {

    private static final String LOG_TAG = "GooglePlaces ";
    private final String TAG = "MapActivity";
    FragmentManager fm;
    //Variables to store games and locations from marker click
    String games = "";
    private GoogleMap map;
    private SupportMapFragment mainFragment;
    private MarkerOptions userMarker;
    private String address = "";
    private String selectedGame;
    private String addressResult;
    private View mLayout;
    private int PROXIMITY_RADIUS = 3000;
    private Menu mMenu;
    private LatLng mlatlng;

    /*Autocomplete Widget*/
    private TextView mPlaceDetailsText;
    private TextView mPlaceAttribution;
    private double lat, lon;

    /*Infowindow*/
    private View myContentsView;
    private Boolean open_now=null;
    private String ratingstr;
    private Uri placeImageURI=null;
    private String placeOpen;
    private String parkName;
    private int photoWidth;


    public MapsActivity() {
        // Required empty public constructor
    }

    public static MapsActivity newInstance() {
        MapsActivity fragment = new MapsActivity();
        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restoring the markers on configuration changes
        setContentView(R.layout.activity_maps);
        mLayout = findViewById(android.R.id.content);
        myContentsView = getLayoutInflater().inflate(R.layout.custom_info_content, null);

        setUpMapIfNeeded();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint(getString(R.string.autocomplete_hint));

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred and set Filter to retreive only places with precise address
        autocompleteFragment.setOnPlaceSelectedListener(this);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        if(!EventBus.getDefault().hasSubscriberForEvent(GetAddressTask.class)) {
            EventBus.getDefault().register(this);
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

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mainFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mainFragment.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        map.getUiSettings().setZoomControlsEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap sMap) {
        map = sMap;
        map.setOnMapLongClickListener(this);
        mainFragment.setRetainInstance(true);
        map.setInfoWindowAdapter(this);

        //Instiantiate background task to download places list and address list for respective locations
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
        Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        build_retrofit_and_get_response(getString((R.string.type_param)),mLat,mLon);
        SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        selectedGame = chGame.getString("chosenGame", "Other");
        setUserMarker(new LatLng(mLat, mLon));

    }

    private void build_retrofit_and_get_response(String type, double mLat, double mLon) {

        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getNearbyPlaces(type, mLat + "," + mLon, PROXIMITY_RADIUS);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {
                try {
                    // This loop will go through all the results and add marker on each location.
                    for (int i = 0; i < response.body().getResults().size(); i++) {
                        Double lat = response.body().getResults().get(i).getGeometry().getLocation().getLat();
                        Double lng = response.body().getResults().get(i).getGeometry().getLocation().getLng();
                        parkName = response.body().getResults().get(i).getName();
                        int size= response.body().getResults().get(i).getPhotos().size();
                        if (i<size) {
                            photoWidth = response.body().getResults().get(i).getPhotos().get(i).getWidth(i);
                        }
                        Log.d("photos().size()", String.valueOf(size));
                        Log.d("getResults().size()", String.valueOf(response.body().getResults().size()));
                        Log.d("photoWidth", String.valueOf(photoWidth));
                        //int photoHeight=response.body().getResults().get(i).getPhotos().get(i).getHeight();
                        String photoReference=response.body().getResults().get(i).getPhotos().get(i).getPhotoReference();
                        if (open_now==null) {
                            open_now = response.body().getResults().get(i).getOpeningHours().getOpenNow();
                             placeOpen = String.valueOf(open_now != null ? open_now : "");
                        }
                        ratingstr=String.valueOf(response.body().getResults().get(i).getRating());
                        placeImageURI = Uri.parse("https://maps.googleapis.com/maps/api/place/photo?maxwidth="+photoWidth+
                                "&photoreference="+photoReference+"&key="+getString(R.string.places_api_key));
                        Log.d("placeOpen", String.valueOf(placeOpen));
                        Log.d("placeImageURI", String.valueOf(placeImageURI));
                        Log.d("ratingstr", String.valueOf(ratingstr));

                        //Log.d("placeImageURI", String.valueOf(placeImageURI));
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(lat, lng);
                        map.addMarker(new MarkerOptions()
                                .title(parkName)
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.play_marker)));
                        //map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        map.animateCamera(CameraUpdateFactory.zoomTo(12.9f));
                        Snackbar.make(mLayout, getString(R.string.map_help),
                                Snackbar.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFailure(Throwable t) {
            }
        });
    }

    @Override
    public View getInfoContents(Marker marker) {
        TextView infoTitle = ((TextView)myContentsView.findViewById(R.id.title));
        infoTitle.setText(marker.getTitle());
        TextView infoSnippet = ((TextView)myContentsView.findViewById(R.id.snippet));
        infoSnippet.setText(marker.getSnippet());
        TextView infoOpennow = (TextView) myContentsView.findViewById(R.id.open_now);
        infoSnippet.setText(placeOpen);
        ImageView placeImage = (ImageView) myContentsView.findViewById(R.id.place_image);
        if (placeImageURI == null) {
            placeImage.setVisibility(View.GONE);
        }else{
            Glide.with(MapsActivity.this).load(placeImageURI).into(placeImage);
        }

        // declare RatingBar object
        RatingBar ratingval=(RatingBar) myContentsView.findViewById(R.id.place_rating);// create RatingBar object
        if( !(ratingstr.equals("null") )){
            ratingval.setRating(Float.parseFloat(String.valueOf(rating)));
        }

        return myContentsView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)    // This method will be called when a GetAddressTask is posted
    public void onEvent(String address){
        // store address details for the game
        if (address.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyyMMdd", Locale.getDefault());
            address = sdf.format(new Date());
        }
        Snackbar.make(mLayout, selectedGame + " " + getString(R.string.save_game) + " " + address
                + "  " + getString(R.string.save_game_text),
                Snackbar.LENGTH_LONG).show();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString("games", address).apply();
    }

    private OnMarkerClickListener userMarkerClickListener = new OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            // show dialog
            marker.showInfoWindow();
            return true;
        }
    };

    /* Get address for new places when user long click's on the map and show the address*/
    @Override
    public void onMapLongClick(LatLng latLng) {
        Double marLat = latLng.latitude;
        Double marLon = latLng.longitude;
        new GetAddressTask(this,marLat,marLon).execute();
        mMenu.getItem(0).setVisible(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_menu, menu);
        return true;
    }

    //respond to menu item selection
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.map_menu:
                Intent intent = new Intent(this, CatalogActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        mMenu.getItem(0).setVisible(false);
        //  By default no Menu
        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        // Either address from marker or address from autocomplete should be the location.
        String address = (String) place.getName();
        Snackbar.make(mLayout, selectedGame + " " + getString(R.string.save_game) + " " + address + "  " +
                getString(R.string.save_game_text),
                Snackbar.LENGTH_LONG).show();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString("games", address).apply();
        mMenu.getItem(0).setVisible(true);
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Snackbar.make(mLayout, getString(R.string.place_error) + status.getStatusMessage(),
                Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }


}