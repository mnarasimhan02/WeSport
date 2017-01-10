package com.example.android.wesport;

import android.Manifest.permission;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, OnMapLongClickListener , PlaceSelectionListener {

    private static final String LOG_TAG = "GooglePlaces ";
    private GoogleMap map;
    private final String TAG = "MapActivity";
    private SupportMapFragment mainFragment;
    private MarkerOptions userMarker;

    /*Autocomplete Widget*/
    private TextView mPlaceDetailsText;

    private TextView mPlaceAttribution;

    FragmentManager fm;
    //Variables to store games and locations from marker click
    String games = "";
    //private ArrayAdapter<String> arrayAdapter;

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
        setContentView(R.layout.activity_maps);
        //Instiantiate view to Save Games
        setUpMapIfNeeded();
        DownloadTask task = new DownloadTask();
        task.execute();
        // Retrieve the PlaceAutocompleteFragment.
                PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred.
        autocompleteFragment.setOnPlaceSelectedListener(this);

        /*autocompleteFragment.setBoundsBias(new LatLngBounds(
                new LatLng(-33.880490, 151.184363),
                new LatLng(-33.858754, 151.229596)));
        */
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();

        autocompleteFragment.setFilter(typeFilter);

        // Retrieve the TextViews that will display details about the selected place.
       // mPlaceDetailsText = (TextView) findViewById(R.id.place_details);
        //mPlaceAttribution = (TextView) findViewById(R.id.place_attribution);

    }

    public void setUserMarker(LatLng latLng)
    {
        if (userMarker==null){
            Log.d("userMarker", "Inside setUsermarker");
            userMarker=new MarkerOptions().position(latLng).title("Current Location");
            userMarker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
            map.addMarker(userMarker);
        }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,12));

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
        map=sMap;
        Log.i(LOG_TAG, "Populate markers for parks");
        map.setOnMapLongClickListener(this);
        //  map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
        Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        setUserMarker(new LatLng(mLat,mLon));
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
                    .appendQueryParameter(RADIUS_PARAM, "5000")
                    .appendQueryParameter(TYPE_PARAM, "park")
                    .appendQueryParameter(KEY_PARAM, "AIzaSyCgAtXv1F6IYFp-b64WjX7acDMKCeW5_3g")
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

    void createMarkersFromJson(String json) throws JSONException {
        // De-serialize the JSON string into an array of park objects
       // map.clear();
        JSONObject jsonObject = new JSONObject(json);
        String parkInfo = jsonObject.getString("results");
        Log.d("parkname", parkInfo);
        String latitude = "";
        String longitude = "";
        JSONArray jsonArray = new JSONArray(parkInfo);
        for (int i = 0; i < jsonArray.length(); i++) {
            // Create a marker for each near by park in the JSON data.
            JSONObject jsonObj = jsonArray.getJSONObject(i);
           /* Log.e(LOG_TAG, "" +jsonArray.getJSONObject(i));
            Log.e(LOG_TAG, "" +jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lat"));
            Log.e(LOG_TAG, "" +jsonObj.getString("name"));
            */
            latitude = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lng");
            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);
            //map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 12));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));
            map.addMarker(new MarkerOptions()
                    .title(jsonObj.getString("name"))
                    .position(new LatLng(lat, lon))

            );
        }
    }

    /* Get address for new places when user long click's on the map and show the address*/
    @Override
    public void onMapLongClick(LatLng latLng) {
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address = "";
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
        Toast.makeText(this, "Game at  " + address + " saved under My Games", Toast.LENGTH_SHORT).show();
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
        if (item.getItemId() == R.id.map_menu) {
            Intent intent = new Intent(getApplicationContext(), CatalogActivity.class);
            startActivity(intent);
        }
        return true;
    }

    /**
     * Callback invoked when a place has been selected from the PlaceAutocompleteFragment.
     */
    @Override
    public void onPlaceSelected(Place place) {
        Log.i(TAG, "Place Selected: " + place.getName());

        // Format the returned place's details and display them in the TextView.
       // mPlaceDetailsText.setText(formatPlaceDetails(getResources(), place.getName(), place.getId(),
         //       place.getAddress(), place.getPhoneNumber(), place.getWebsiteUri()));

        CharSequence attributions = place.getAttributions();
        if (!TextUtils.isEmpty(attributions)) {
            mPlaceAttribution.setText(Html.fromHtml(attributions.toString()));
        } else {
            mPlaceAttribution.setText("");
        }
    }

    /**
     * Helper method to format information about a place nicely.
     */
    private Spanned formatPlaceDetails(Resources res, CharSequence name, String id,
                                              CharSequence address, CharSequence phoneNumber, Uri websiteUri) {
        Log.e(TAG, res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));
        return Html.fromHtml(res.getString(R.string.place_details, name, id, address, phoneNumber,
                websiteUri));

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
}