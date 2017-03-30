package com.my.game.wesport;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.my.game.wesport.POJO.Example;
import com.my.game.wesport.POJO.ParkModel;
import com.my.game.wesport.interfaces.InfoWindowRefresherNearBy;
import com.my.game.wesport.ui.MyGames;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

@SuppressWarnings("ALL")
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PlaceSelectionListener,
        InfoWindowAdapter, OnInfoWindowClickListener {

    private static final String LOG_TAG = "GooglePlaces";
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
    private String OPEN_NOW = "true";
    private String CLOSED_NOW = "false";


    private Menu mMenu;
    private LatLng mlatlng;

    /*Autocomplete Widget*/
    private TextView mPlaceDetailsText;
    private TextView mPlaceAttribution;
    private double lat, lon;

    /*Infowindow*/
    private View myContentsView;
    private String mopen_now = null;
    private String ratingstr = null, vicinitystr;
    private Uri placeImageURI = null;
    private String placeOpenstr;
    private String parkName;


    private Marker marker;
    private Hashtable<String, Uri> markers;
    private Hashtable<String, ParkModel> parks = new Hashtable<>();


    //private List<String> photoReference = new ArrayList<String>();

    private int photoReferenceSize = 0;


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
        try {
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

            if (!EventBus.getDefault().hasSubscriberForEvent(GetAddressTask.class)) {
                EventBus.getDefault().register(this);
            }
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
        if (ActivityCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
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
        mainFragment.setRetainInstance(true);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);
        try {
            //Instiantiate background task to download places list and address list for respective locations
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
            Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
            build_retrofit_and_get_response(getString((R.string.type_param)), mLat, mLon);
            SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            selectedGame = chGame.getString("chosenGame", "Other");
            setUserMarker(new LatLng(mLat, mLon));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void build_retrofit_and_get_response(String type, double mLat, double mLon) {
        markers = new Hashtable<String, Uri>();

        final int[] photoWidth = new int[1];
        final int[] photoRefsize = new int[1];
        final int[] widthSize = new int[1];
        final String[] photoReference = {null};

        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getNearbyPlaces(type, mLat + "," + mLon, PROXIMITY_RADIUS, OPEN_NOW);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {
                try {
                    // This loop will go through all the parkModels and add marker on each location.
                    List<ParkModel> parkModels = response.body().getParkModels();
                    if (parkModels.size() > 0) {
                        Toast.makeText(MapsActivity.this, R.string.map_help, Toast.LENGTH_LONG).show();
                    }
                    for (int i = 0; i < parkModels.size(); i++) {
                        Double lat = parkModels.get(i).getGeometry().getLocation().getLat();
                        Double lng = parkModels.get(i).getGeometry().getLocation().getLng();
                        parkName = parkModels.get(i).getName();
                        widthSize[0] = parkModels.get(i).getPhotos().size();
                        //Get if park is open_now
                        /*OpeningHours mOpencheck = response.body().getParkModels().get(i).getOpeningHours();
                                if(mOpencheck!=null) {
                                    mopen_now = response.body().getParkModels().get(i).getOpeningHours().getOpenNow();
                                    if (!mopen_now.equals(null)) {
                                        if (mopen_now.equals("false")) {
                                            placeOpenstr = "closed";
                                        } else {
                                            placeOpenstr = "Open now";
                                        }
                                    }
                                }
                                */
                        //Get PhotoMaxWidth
                        if (i < widthSize[0]) {
                            photoWidth[0] = parkModels.get(i).getPhotos().get(i).getWidth(i);
                            photoRefsize[0] = parkModels.get(i).getPhotos().size();
                        }
                        //Get photoreference of first photo of all parks
                        photoReferenceSize = parkModels.get(i).getPhotos().size();
                        if (photoReferenceSize != 0) {
                            photoReference[0] = (parkModels.get(i).getPhotos().get(0).getPhotoReference());
                        }
                        if (!photoReference[0].isEmpty()) {
                            placeImageURI = Uri.parse("https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + photoWidth[0] +
                                    "&photoreference=" + photoReference[0] + "&key=" + getString(R.string.places_api_key));
                        }
                        //Get ratings for parks
                      /*  if (!(ratingstr==null || ratingstr==""|| ratingstr.equals(null) ||ratingstr.equals(" "))) {
                            ratingstr = String.valueOf(response.body().getParkModels().get(i).getRating());
                        }
*/
                        vicinitystr = String.valueOf(parkModels.get(i).getVicinity());
                        LatLng latLng = new LatLng(lat, lng);
                        Marker parkMarker = map.addMarker(new MarkerOptions()
                                .title(parkName)
                                .position(latLng)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.play_marker)));
                        markers.put(parkMarker.getId(), placeImageURI);
                        ratingstr = String.valueOf(parkModels.get(i).getRating());
                        parks.put(parkMarker.getId(), parkModels.get(i));

                        //placeOpen.put(parkMarker.getId(), placeOpenstr);}
                        map.animateCamera(CameraUpdateFactory.zoomTo(12.9f));
                        /*Snackbar.make(mLayout, getString(R.string.map_help),
                                Snackbar.LENGTH_LONG).show();*/
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "onResponse: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    @Override
    public View getInfoContents(Marker marker) {
        if (MapsActivity.this.marker != null && MapsActivity.this.marker.isInfoWindowShown()) {
            MapsActivity.this.marker.hideInfoWindow();
            MapsActivity.this.marker.showInfoWindow();
        }
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        MapsActivity.this.marker = marker;
        Uri uri = null;
        //String ratingstr=null;
        if (marker.getId() != null && markers != null && markers.size() > 0) {
            if (markers.get(marker.getId()) != null &&
                    markers.get(marker.getId()) != null) {
                uri = markers.get(marker.getId());
                ratingstr = String.valueOf(parks.get(marker.getId()).getRating());
                vicinitystr = parks.get(marker.getId()).getVicinity();
            }
        }
        Log.d(TAG, "getInfoWindow: imageUri: " + uri);
        TextView infoTitle = ((TextView) myContentsView.findViewById(R.id.title));
        infoTitle.setText(marker.getTitle());
        TextView infoSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
        infoSnippet.setText(vicinitystr);
        //TextView infoOpennow = (TextView) myContentsView.findViewById(open_now);
        // infoOpennow.setText(placeOpenstr);
        ImageView placeImage = (ImageView) myContentsView.findViewById(R.id.place_image);
        if (uri == null) {
            placeImage.setVisibility(View.GONE);
        } else {

            Glide.with(MapsActivity.this)
                    .load(uri)
                    .listener(new InfoWindowRefresherNearBy(marker))
                    .into(placeImage);
//            new InfoWindowRefresher(marker)
        }
        // declare RatingBar object
        RatingBar infoRating = (RatingBar) myContentsView.findViewById(R.id.place_rating);// create RatingBar object
        if (infoRating != null && ratingstr != null) {
            if (!(ratingstr.equals("null"))) {
                try {
                    infoRating.setRating(Float.parseFloat(ratingstr));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                infoRating.setRating(0.0f);
            }
        }
        return myContentsView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    // This method will be called when a GetAddressTask is posted
    public void onEvent(String address) {
        // store address details for the game
        if (address.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyyMMdd", Locale.getDefault());
            address = sdf.format(new Date());
        }
        /*Snackbar.make(mLayout, selectedGame + " " + getString(R.string.save_game) + " " + address
                        + "  " + getString(R.string.save_game_text),
                Snackbar.LENGTH_SHORT).show();*/

        Toast.makeText(MapsActivity.this, mLayout + " " + selectedGame + " " + getString(R.string.save_game)
                + " " + address
                + "  " + getString(R.string.save_game_text), Toast.LENGTH_LONG).show();
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
        }
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        //mMenu.getItem(0).setVisible(false);
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
        Toast.makeText(this, selectedGame + " " + getString(R.string.save_game) + " " + address + "  " +
                getString(R.string.save_game_text), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MapsActivity.this, MyGames.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString("games", address).apply();
        prefs.edit().putString("place_id", place.getId()).apply();
        //mMenu.getItem(0).setVisible(true);
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        /*Snackbar.make(mLayout, getString(R.string.place_error) + status.getStatusMessage(),
                Snackbar.LENGTH_LONG).show();*/
        Toast.makeText(MapsActivity.this, mLayout + " " + getString(R.string.place_error)
                + status.getStatusMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        try {
            LatLng latLng = marker.getPosition();
            Double marLat = latLng.latitude;
            Double marLon = latLng.longitude;
            //new GetAddressTask(this,marLat,marLon).execute();
            //mMenu.getItem(0).setVisible(true);
            ParkModel parkModel = parks.get(marker.getId());
            vicinitystr = parkModel.getVicinity();
            address = vicinitystr;
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
            prefs.edit().putString("games", address).apply();
            prefs.edit().putString("place_id", parkModel.getId()).apply();

            Toast.makeText(this, selectedGame + " " + getString(R.string.save_game) + " " + address + "  " +
                    getString(R.string.save_game_text), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MapsActivity.this, MyGames.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}