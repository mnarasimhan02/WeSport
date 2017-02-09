package com.example.android.wesport;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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


public class DownloadTask extends AsyncTask<String, Void, String> {

    private Context context;
    GoogleMap map;


    //save the context recievied via constructor in a local variable

    public DownloadTask(Context context, GoogleMap gMap) {
        this.context = context;
        map = gMap;

    }

    @Override
    protected String doInBackground(String... params) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
        Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        Uri.Builder builder = new Uri.Builder();
        final String BASE_URL =
                "https://maps.googleapis.com";
        final String RADIUS_PARAM = "radius";
        final String TYPE_PARAM = "type";
        final String KEY_PARAM = "key";
        double lat, lon;

        Uri builtUri = Uri.parse(BASE_URL)
                .buildUpon()
                .path("maps/api/place/nearbysearch/json")
                .appendQueryParameter("location", String.valueOf(mLat) + "," + String.valueOf(mLon))
                .appendQueryParameter(RADIUS_PARAM, context.getString((R.string.radius_param)))
                .appendQueryParameter(TYPE_PARAM, context.getString((R.string.type_param)))
                .appendQueryParameter(KEY_PARAM, context.getString((R.string.place_api_key)))
                .build();
        String SERVICE_URL = builtUri.toString();
        String result = "";
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
            SharedPreferences downloadapi = PreferenceManager.getDefaultSharedPreferences(context);
            downloadapi.edit().putString("result", String.valueOf(result)).apply();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            String parkInfo = jsonObject.getString("results");
            String latitude = "";
            String longitude = "";
            double lat=0, lon = 0;

            JSONArray jsonArray = new JSONArray(parkInfo);
            for (int i = 0; i < jsonArray.length(); i++) {
                // Create a marker for each near by park in the JSON data.
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                latitude = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = jsonObj.getJSONObject("geometry").getJSONObject("location").getString("lng");
                lat = Double.parseDouble(latitude);
                lon = Double.parseDouble(longitude);
                String parkName = jsonObj.getString("name");
                map.addMarker(new MarkerOptions()
                        .title(parkName)
                        .position(new LatLng(lat, lon))
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.play_marker)));
            }
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 12.9f));
        } catch (JSONException e ) {
            e.printStackTrace();
        }
    }
}