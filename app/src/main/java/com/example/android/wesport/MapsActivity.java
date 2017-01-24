package com.example.android.wesport;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapLongClickListener, PlaceSelectionListener {

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
    /*Autocomplete Widget*/
    private TextView mPlaceDetailsText;
    private TextView mPlaceAttribution;
    private double lat, lon;
    private ArrayList<LatLng> pointList = new ArrayList<LatLng>();
    private ArrayList<String> markerTitle = new ArrayList<String>();

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
        setUpMapIfNeeded();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //Instiantiate background task to download places list

        DownloadTask task = new DownloadTask();
        task.execute();
        // Retrieve the PlaceAutocompleteFragment.
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint("Find play places");

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred and set Filter to retreive only places with precise address
        autocompleteFragment.setOnPlaceSelectedListener(this);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);
    }

    public void setUserMarker(LatLng latLng) {
        if (userMarker == null) {
            userMarker = new MarkerOptions().position(latLng).title("My Location");
            userMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            map.addMarker(userMarker);
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));

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
        Log.i(LOG_TAG, "Populate markers for parks");
        map.setOnMapLongClickListener(this);
        mainFragment.setRetainInstance(true);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
        Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        selectedGame = chGame.getString("chosenGame", "Other");
        setUserMarker(new LatLng(mLat, mLon));
    }

    void createMarkersFromJson(String json) throws JSONException {
        // De-serialize the JSON string into an array of park objects
        JSONObject jsonObject = new JSONObject(json);
        String parkInfo = jsonObject.getString("results");
        Log.d("parkname", parkInfo);
        String latitude = "";
        String longitude = "";
        JSONArray jsonArray = new JSONArray(parkInfo);
        for (int i = 0; i < jsonArray.length(); i++) {
            // Create a marker for each near by park in the JSON data.
            JSONObject jsonObj = jsonArray.getJSONObject(i);
            latitude = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lng");
            lat = Double.parseDouble(latitude);
            lon = Double.parseDouble(longitude);
            String parkName = jsonObj.getString("name");
            LatLngBounds bounds = this.map.getProjection().getVisibleRegion().latLngBounds;
            LatLng markerPoint = new LatLng(lat, lon);
            if (bounds.contains(markerPoint)) {
                this.map.addMarker(new MarkerOptions()
                        .title(parkName)
                        .position(new LatLng(lat, lon))

                );
            }
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));
            // Adding the currently created marker and title position to  arraylist
            pointList.add(new LatLng(lat, lon));
            markerTitle.add(parkName);
        }
    }

    /* Get address for new places when user long click's on the map and show the address*/
    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (listAddresses != null && listAddresses.size() > 0) {
                if (listAddresses.get(0).getThoroughfare() != null) {
                    if (listAddresses.get(0).getSubThoroughfare() != null) {
                        address += listAddresses.get(0).getSubThoroughfare() + " ";
                    }
                    address += listAddresses.get(0).getThoroughfare();
                    Log.d("address", address);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (address.equals("")) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm yyyyMMdd", Locale.getDefault());
            address = sdf.format(new Date());
        }
        Toast.makeText(this, selectedGame + " Game at  " + address + " saved ", Toast.LENGTH_LONG).show();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString("games", address).apply();
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

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());

        // Either address from marker or address from autocomplete should be the location.
        String address = (String) place.getName();
        Toast.makeText(this, selectedGame + " game at  " + address + " saved ", Toast.LENGTH_LONG).show();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        prefs.edit().putString("games", address).apply();
    }

    /**
     * Callback invoked when PlaceAutocompleteFragment encounters an error.
     */
    @Override
    public void onError(Status status) {
        Log.e(TAG, "onError: Status = " + status.toString());

        Toast.makeText(this, "Place selection failed: " + status.getStatusMessage(),
                Toast.LENGTH_SHORT).show();
    }


    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
            Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
            Uri.Builder builder = new Uri.Builder();
            final String BASE_URL =
                    "https://maps.googleapis.com";
            final String RADIUS_PARAM = "radius";
            final String TYPE_PARAM = "type";
            final String KEY_PARAM = "key";

            Uri builtUri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .path("maps/api/place/nearbysearch/json")
                    .appendQueryParameter("location", String.valueOf(mLat) + "," + String.valueOf(mLon))
                    .appendQueryParameter(RADIUS_PARAM, getString(R.string.radius_param))
                    .appendQueryParameter(TYPE_PARAM, getString(R.string.type_param))
                    .appendQueryParameter(KEY_PARAM, getString(R.string.place_api_key))
                    .build();
            String SERVICE_URL = builtUri.toString();
            String result = "";
            Log.i("SERVICE_URL", SERVICE_URL);
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                // Connect to the web service
                url = new URL(SERVICE_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                // Read the JSON data into the StringBuilder
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        // Create markers for the city data.
        // Must run this on the UI thread since it's a UI operation.
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                createMarkersFromJson(result);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON", e);
            }

        }

    }
}