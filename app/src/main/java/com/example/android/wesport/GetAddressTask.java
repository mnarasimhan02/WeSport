package com.example.android.wesport;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class GetAddressTask extends AsyncTask<String, Void, String> {

    private Context context;
    double mLat;
    double mLon;
    public String address;

    //save the context recievied via constructor in a local variable

    public GetAddressTask(Context context, double marLat, double marLon) {
        this.context = context;
        mLat = marLat;
        mLon = marLon;
    }

    public GetAddressTask(String address) {
        this.address=address;
    }

    @Override
    protected String doInBackground(String... params) {
        Uri.Builder builder = new Uri.Builder();
        final String BASE_URL =
                "http://maps.googleapis.com";
        final String SENSOR_PARAM = "sensor";
        Uri builtUri = Uri.parse(BASE_URL)
                .buildUpon()
                .path("maps/api/geocode/json")
                .appendQueryParameter("latlng", String.valueOf(mLat) + "," + String.valueOf(mLon))
                .appendQueryParameter(SENSOR_PARAM, context.getString((R.string.sensor_param)))
                .build();
        String SERVICE_URL = builtUri.toString();
        Log.d("SERVICE_URL", SERVICE_URL);
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
            String stNumber = "";
            String stRoute = "";
            JSONArray arrayResult = jsonObject.getJSONArray("results");
            JSONArray arrComponent = arrayResult.getJSONObject(0).getJSONArray("address_components");
            for (int i = 0; i < arrComponent.length(); i++) {
                JSONArray arrType = arrComponent.getJSONObject(i).getJSONArray("types");
                for (int j = 0; j < arrType.length(); j++) {
                    if (arrType.getString(j).equals("street_number")) {
                        stNumber = arrComponent.getJSONObject(i).getString("short_name");
                    }
                    if (arrType.getString(j).equals("route")) {
                        stRoute = arrComponent.getJSONObject(i).getString("short_name");
                    }
                }
                address = stNumber + " , " + stRoute;
                EventBus.getDefault().post(address);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}