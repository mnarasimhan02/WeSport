package com.my.game.wesport.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.my.game.wesport.R;
import com.my.game.wesport.fragment.LocaleContactListFragment;
import com.my.game.wesport.fragment.NearbyUsersListFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class InviteUserActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private static String EXTRA_GAME_KEY = "";
    String gameKey = "";

    public static Intent newIntent(Context context, String gameKey) {
        Intent intent = new Intent(context, InviteUserActivity.class);
        intent.putExtra(EXTRA_GAME_KEY, gameKey);
        return intent;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_user);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        getSupportActionBar().setTitle("Invite Users");

        gameKey = getIntent().getStringExtra(EXTRA_GAME_KEY);

        InviteUserActivity.InviteUsersSectionsPagerAdapter mSectionsPagerAdapter =
                new InviteUsersSectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.invite_user_activity_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.invite_user_activity_tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class InviteUsersSectionsPagerAdapter extends FragmentPagerAdapter {

        public InviteUsersSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return NearbyUsersListFragment.newInstance(gameKey);
                case 1:
                    return LocaleContactListFragment.newInstance(gameKey);
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Nearby Users";
                case 1:
                    return "Local Contacts";
            }
            return null;
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
