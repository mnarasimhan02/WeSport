package com.my.game.wesport.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.ListView;

import com.my.game.wesport.R;
import com.my.game.wesport.adapter.HomeGridAdapter;
import com.my.game.wesport.helper.GameHelper;

public class GamesGridActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_ACTIVITY = 34;
    private String[] gridViewString;
    private String chosenGame;
    private View mLayout;
    private double lat;
    private double log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games_grid_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Choose Your Game");

        gridViewString = getResources().getStringArray(R.array.games_array);
        mLayout = findViewById(android.R.id.content);

        HomeGridAdapter adapterViewAndroid = new HomeGridAdapter(GamesGridActivity.this, GameHelper.getGameCategoryList());
        GridView androidGridView = (GridView) findViewById(R.id.grid_view_image_text);
        androidGridView.setAdapter(adapterViewAndroid);
        adapterViewAndroid.setListener(new HomeGridAdapter.HomeGridListener() {
            @Override
            public void onItemClick(int position) {
                try {
                    chosenGame = gridViewString[position];
                    SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    SharedPreferences.Editor editor = chGame.edit();
                    editor.putString("chosenGame", chosenGame).apply();
                    editor.putInt("chosenGame_pos", position).apply();
                    if (isLocationEnabled(getApplicationContext())) {
                        Log.d("MainActivity", "onItemClick: ");
                        Intent intent = new Intent(GamesGridActivity.this, MapsActivity.class);
                        startActivityForResult(intent, REQUEST_CODE_ACTIVITY);
                    } else {
                        Snackbar.make(mLayout, getString(R.string.loc_not_enable),
                                Snackbar.LENGTH_SHORT).show();
                        startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressWarnings("UnusedAssignment")
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            //noinspection deprecation
            locationProviders = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
               finish();
            }
        }
    }
}
