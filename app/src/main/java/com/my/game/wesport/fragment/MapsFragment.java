package com.my.game.wesport.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.my.game.wesport.POJO.Example;
import com.my.game.wesport.POJO.ParkModel;
import com.my.game.wesport.POJO.Photo;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.GameEditActivity;
import com.my.game.wesport.activity.MainActivity;
import com.my.game.wesport.api.RetrofitMaps;
import com.my.game.wesport.event.EventLocationUpdated;
import com.my.game.wesport.interfaces.InfoWindowRefresherNearBy;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class MapsFragment extends Fragment implements OnMapReadyCallback, PlaceSelectionListener,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnInfoWindowClickListener {

    private static final String LOG_TAG = "GooglePlaces";
    private final String TAG = "MapActivity";
    FragmentManager fm;
    //Variables to store games and locations from marker click
    String games = "";
    private GoogleMap map;
    private SupportMapFragment mainFragment;
    private MarkerOptions userMarker;
    private String address = "";
    private String addressResult;
    private View mLayout;
    private int PROXIMITY_RADIUS = 3000;
    private String OPEN_NOW = "true";
    private String CLOSED_NOW = "false";

    private boolean isFirstTimeStarted = true;

    private Menu mMenu;
    private LatLng mlatlng;

    /*Autocomplete Widget*/
    private TextView mPlaceDetailsText;
    private TextView mPlaceAttribution;
    private double lat, lon;

    EventBus eventBus = EventBus.getDefault();

    /*Infowindow*/
    private View myContentsView;
    private String mopen_now = null;
    private String ratingstr = null, vicinitystr;
    private String placeOpenstr;
    private String parkName;


    View rootView;
    private Marker marker;
    private Hashtable<String, ParkModel> parks = new Hashtable<>();


    //private List<String> photoReference = new ArrayList<String>();
    public static final int REQUEST_CODE_ACTIVITY = 13;

    private int photoReferenceSize = 0;
    public static List<ParkModel> parkModels = new ArrayList<>();

    public static MapsFragment newInstance() {
        Bundle args = new Bundle();
        MapsFragment fragment = new MapsFragment();
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
            rootView = inflater.inflate(R.layout.fragment_maps, container, false);
            mLayout = rootView.findViewById(android.R.id.content);
        } catch (InflateException e) {
            // map is already there, just return view as it is
        }

        myContentsView = inflater.inflate(R.layout.custom_info_content, null);


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


        /*AdView mAdView = (AdView) rootView.findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("8D55278B12588486D7D396079CB75B6B")
                .build();
        mAdView.loadAd(adRequest);*/

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
        if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }
        setUpMapIfNeeded();
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
            mainFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            mainFragment.getMapAsync(this);
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

    @Override
    public void onMapReady(GoogleMap sMap) {
        map = sMap;
        mainFragment.setRetainInstance(true);
        map.setInfoWindowAdapter(this);
        map.setOnInfoWindowClickListener(this);
        updateMapParks();
    }

    private void updateMapParks() {
        try {
            //Instiantiate background task to download places list and address list for respective locations
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
            Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
            Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));

            build_retrofit_and_get_response(getString((R.string.type_param)), mLat, mLon);
            setUserMarker(new LatLng(mLat, mLon));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void build_retrofit_and_get_response(String type, double mLat, double mLon) {
        Log.d(TAG, "build_retrofit_and_get_response() called with: type = [" + type + "], mLat = [" + mLat + "], mLon = [" + mLon + "]");

        final int[] photoWidth = new int[1];
        final int[] photoRefsize = new int[1];

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
                    parkModels = response.body().getParkModels();
                    Log.d(TAG, "onResponse: parks: " + parkModels.size());
                    if (parkModels.size() > 0 && ((MainActivity) getActivity()).isShowMapToast()) {
                        ((MainActivity) getActivity()).setShowMapToast(false);
                        Toast.makeText(getActivity(), R.string.map_help, Toast.LENGTH_LONG).show();
                    }
                    for (int i = 0; i < parkModels.size(); i++) {
                        Double lat = parkModels.get(i).getGeometry().getLocation().getLat();
                        Double lng = parkModels.get(i).getGeometry().getLocation().getLng();
                        parkName = parkModels.get(i).getName();
                        int widthSize = parkModels.get(i).getPhotos().size();
                        //Get if park is open_now
                        /*OpeningHours mOpencheck = response.body().getParkModels().get(i).getOpeningHours();
                                if(mOpencheck!=null) {
                                    mopen_now = response.body().getParkModels().get(i).getOpeningHours().getOpenNow();
                                    if (!mopen_now.equals(null)) {
                                        if (mopen_now.equals("false")) {
                                            placeOpenstr = "closed";
                                        } else {
                                            placeOpenstr- = "Open now";
                                        }
                                    }
                                }
                                */

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
        if (this.marker != null && this.marker.isInfoWindowShown()) {
            this.marker.hideInfoWindow();
            this.marker.showInfoWindow();
        }
        return null;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        this.marker = marker;
        Uri uri = null;
        //String ratingstr=null;
        if (marker.getId() != null && parks != null && parks.size() > 0) {
            ParkModel parkModel = parks.get(marker.getId());
            if (parkModel != null) {
                List<Photo> photos = parkModel.getPhotos();
                if (photos != null && photos.size() > 0) {
                    String photoReference = photos.get(0).getPhotoReference();
                    int width = photos.get(0).getWidth(0);
                    uri = Uri.parse("https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + width +
                            "&photoreference=" + photoReference + "&key=" + getString(R.string.places_api_key));
                }
                /*photoWidth = parkModels.get(i).getPhotos().get(i).getWidth(i);
                photoRefsize[0] = parkModels.get(i).getPhotos().size();

                //Get photoreference of first photo of all parks
                photoReferenceSize = parkModels.get(i).getPhotos().size();
                if (photoReferenceSize != 0) {
                    String photoReference = (parkModels.get(i).getPhotos().get(0).getPhotoReference());
                }
                uri = Uri.parse("https://maps.googleapis.com/maps/api/place/photo?maxwidth=" + photoWidth[0] +
                        "&photoreference=" + photoReference[0] + "&key=" + getString(R.string.places_api_key));*/
                ratingstr = String.valueOf(parkModel.getRating());
                vicinitystr = parkModel.getVicinity();
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
            placeImage.setImageResource(R.drawable.image_placeholder_drawable);
        } else {
            Glide.with(this)
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity().getBaseContext());
        prefs.edit().putString("game_address", address).apply();
    }

    private GoogleMap.OnMarkerClickListener userMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            // show dialog
            marker.showInfoWindow();
            return true;
        }
    };

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        LatLng latLng = place.getLatLng();
        Double placeLat = latLng.latitude;
        Double placeLon = latLng.longitude;
        // Either address from marker or address from autocomplete should be the location.
        String address = (String) place.getName();
        Intent intent = GameEditActivity.newIntent(getActivity(), place.getId(), null, placeLat, placeLon, address);
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY);
        //mMenu.getItem(0).setVisible(true);
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        /*Snackbar.make(mLayout, getString(R.string.place_error) + status.getStatusMessage(),
                Snackbar.LENGTH_LONG).show();*/
        Toast.makeText(getActivity(), mLayout + " " + getString(R.string.place_error)
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

            Intent intent = GameEditActivity.newIntent(getActivity(), parkModel.getId(), null, marLat, marLon, address);
            startActivityForResult(intent, REQUEST_CODE_ACTIVITY);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ACTIVITY && resultCode == Activity.RESULT_OK) {
            ((MainActivity) getActivity()).switchPage(1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        eventBus.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLocationUpdated(EventLocationUpdated event) {
        if (isFirstTimeStarted && map != null) {
            isFirstTimeStarted = false;
            updateMapParks();
        }
    }
}
