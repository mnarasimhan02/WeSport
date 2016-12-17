package com.example.android.wesport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String LOG_TAG = "GooglePlaces ";
    //String SERVICE_URL="";
    //location=39.1266246,-77.207643&radius=500&type=\"park\"&key=AIzaSyDCJelWAYPKAev6dIaAgLIx4e19NGO3UFw\n";

    private GoogleMap map;
    private final String TAG = "MapActivity";


    public static MapsActivity newInstance() {
        MapsActivity fragment = new MapsActivity();
        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        DownloadTask task = new DownloadTask();
        task.execute();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment map = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            map.getMapAsync(this);
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        map.getUiSettings().setZoomControlsEnabled(false);
    }

     /*   // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d(TAG, "outside create Fragment");
    }
*/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        Log.i(LOG_TAG ,"Populate markers for parks");

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Double mLat = Double.parseDouble(preferences.getString("latitude",""));
            Double mLon = Double.parseDouble(preferences.getString("longtitude",""));
            Uri.Builder builder = new Uri.Builder();
            final String BASE_URL =
                    "https://maps.googleapis.com";
            final String RADIUS_PARAM = "radius";
            final String TYPE_PARAM = "type";
            final String KEY_PARAM = "key";

            Uri builtUri = Uri.parse(BASE_URL)
                    .buildUpon()
                    .path("maps/api/place/nearbysearch/json")
                    .appendQueryParameter("location",String.valueOf(mLat)+","+String.valueOf(mLon))
                    .appendQueryParameter(RADIUS_PARAM , "5000")
                    .appendQueryParameter(TYPE_PARAM, "park")
                    .appendQueryParameter(KEY_PARAM, "API_KEY")
                    .build();
            String SERVICE_URL = builtUri.toString();
            String result = "";
            Log.i("SERVICE_URL",SERVICE_URL);
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
                createMarkersFromJson(result.toString());
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Error processing JSON", e);
            }
        }

    }

    void createMarkersFromJson(String json) throws JSONException {
        // De-serialize the JSON string into an array of park objects
        map.clear();
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
            //CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12));
            map.addMarker(new MarkerOptions()
                    .title(jsonObj.getString("name"))
                    .position(new LatLng(lat, lon))

            );
        }
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
            Intent intent = new Intent(getApplicationContext(), MyGames.class);
            //intent.putExtra(mCurrentLocation,"Current Location");
            startActivity(intent);
        }
        return true;
    }
}