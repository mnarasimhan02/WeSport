package com.example.android.wesport;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class DownloadTask extends AsyncTask<String, Void, String> {

    private Context context;

    //save the context recievied via constructor in a local variable

    public DownloadTask(Context context){
        this.context=context;
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

        Uri builtUri = Uri.parse(BASE_URL)
                .buildUpon()
                .path("maps/api/place/nearbysearch/json")
                .appendQueryParameter("location", String.valueOf(mLat) + "," + String.valueOf(mLon))
                .appendQueryParameter(RADIUS_PARAM, context.getString((R.string.radius_param)))
                .appendQueryParameter(TYPE_PARAM, context.getString((R.string.type_param)))
                .appendQueryParameter(KEY_PARAM, context.getString((R.string.place_api_key)))
                .build();
        String SERVICE_URL = builtUri.toString();
        Log.d("SERVICE_URL",SERVICE_URL);
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

}