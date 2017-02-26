package com.my.game.wesport;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

import static com.facebook.FacebookSdk.getApplicationContext;

@SuppressWarnings("ALL")
public class MapsActivity extends Fragment implements OnMapReadyCallback, OnMapLongClickListener, PlaceSelectionListener, InfoWindowAdapter {

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


    private Marker marker;
    private Hashtable<String, Uri> markers;
    private Hashtable<String, String> rating;

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
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restoring the markers on configuration changes
        //setContentView(R.layout.activity_maps);
        View rootView = inflater.inflate(R.layout.activity_maps, container, false);
        mLayout = (CoordinatorLayout) getView().findViewById(android.R.id.content);
        myContentsView = inflater.inflate(R.layout.custom_info_content, null);

        setUpMapIfNeeded();
        ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((ActionBarActivity)getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
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
        return rootView;
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
            mainFragment = (SupportMapFragment) getFragmentManager()
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
        if (ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap sMap) {
        map = sMap;
        map.setOnMapLongClickListener(this);
        mainFragment.setRetainInstance(true);
        map.setInfoWindowAdapter(this);
        try {
            //Instiantiate background task to download places list and address list for respective locations
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
            Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
            build_retrofit_and_get_response(getString((R.string.type_param)), mLat, mLon);
            SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            selectedGame = chGame.getString("chosenGame", "Other");
            setUserMarker(new LatLng(mLat, mLon));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void build_retrofit_and_get_response(String type, double mLat, double mLon) {
        markers = new Hashtable<String, Uri>();
        rating = new Hashtable<String, String>();
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
                            widthSize[0] = response.body().getResults().get(i).getPhotos().size();
                            //Get if park is open_now
                            if (open_now == null) {
                                open_now = response.body().getResults().get(i).getOpeningHours().getOpenNow();
                                placeOpen = String.valueOf(open_now != null ? "Yes" : "");
                            }
                            //Get PhotoMaxWidth
                            if (i < widthSize[0]) {
                                photoWidth[0] = response.body().getResults().get(i).getPhotos().get(i).getWidth(i);
                                photoRefsize[0] = response.body().getResults().get(i).getPhotos().size();
                            }
                            //Get photoreference of first photo of all parks
                            photoReferenceSize =response.body().getResults().get(i).getPhotos().size();
                            if (photoReferenceSize !=0) {
                                photoReference[0] = (response.body().getResults().get(i).getPhotos().get(0).getPhotoReference());
                            }
                            if (!photoReference[0].isEmpty()) {
                                        placeImageURI = Uri.parse("https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + photoWidth[0] +
                                                "&photoreference="+ photoReference[0] + "&key=" + getString(R.string.places_api_key));
                            }
                                //Get ratings for parks
                            ratingstr = String.valueOf(response.body().getResults().get(i).getRating());
                            LatLng latLng = new LatLng(lat, lng);
                                Marker parkMarker = map.addMarker(new MarkerOptions()
                                        .title(parkName)
                                        .position(latLng)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.play_marker)));
                                markers.put(parkMarker.getId(), placeImageURI);
                                rating.put(parkMarker.getId(), ratingstr);
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
        if (MapsActivity.this.marker != null
                && MapsActivity.this.marker.isInfoWindowShown()) {
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
            if ( markers.get(marker.getId()) != null &&
                    markers.get(marker.getId()) != null) {
                uri = markers.get(marker.getId());
                ratingstr= rating.get(marker.getId());
            }
        }
        TextView infoTitle = ((TextView) myContentsView.findViewById(R.id.title));
        infoTitle.setText(marker.getTitle());
        TextView infoSnippet = ((TextView) myContentsView.findViewById(R.id.snippet));
        infoSnippet.setText(marker.getSnippet());
        TextView infoOpennow = (TextView) myContentsView.findViewById(R.id.open_now);
        infoOpennow.setText(placeOpen);
        ImageView placeImage = (ImageView) myContentsView.findViewById(R.id.place_image);
        if (uri == null) {
            placeImage.setVisibility(View.GONE);
        } else {
            Picasso.with(getActivity())
                    .load(uri)
                    .into(placeImage, new InfoWindowRefresher(marker));
        }
        // declare RatingBar object
        RatingBar infoRating = (RatingBar) myContentsView.findViewById(R.id.place_rating);// create RatingBar object
        if (!(ratingstr.equals("null"))) {
            try {
                infoRating.setRating(Float.parseFloat(ratingstr));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        } else {
            infoRating.setRating(0.0f);
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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
        new GetAddressTask(getActivity(),marLat,marLon).execute();
        mMenu.getItem(0).setVisible(true);
    }

   /* public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.maps_menu, menu);
        return true;
    }*/

    //respond to menu item selection
    public boolean onOptionsItemSelected(MenuItem item) {

        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;
            case R.id.map_menu:
                Intent intent = new Intent(getActivity(), CatalogActivity.class);
                startActivity(intent);
                break;
        }
        return true;
    }

    /*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mMenu = menu;
        mMenu.getItem(0).setVisible(false);
        //  By default no Menu
        return super.onPrepareOptionsMenu(menu);
    }
    */

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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
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

}