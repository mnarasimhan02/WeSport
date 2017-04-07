package com.my.game.wesport.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.my.game.wesport.R;
import com.my.game.wesport.fragment.DashboardFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TeamsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teams);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("My Games");

        DashboardFragment dashboardFragment = new DashboardFragment();
        //dashboardFragment.setArguments(getIntent().getExtras());
        dashboardFragment.newInstance();
        getFragmentManager().beginTransaction()
                .replace(R.id.dashboard_container, dashboardFragment)
                .commit();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, TeamsActivity.class);
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
