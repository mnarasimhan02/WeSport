package com.my.game.wesport.ui;

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
import com.my.game.wesport.fragment.GameListFragment;
import com.my.game.wesport.fragment.UserChatListFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MyGamesActivity extends AppCompatActivity {
    private static String EXTRA_KEY_GAME = "key_game";
    private static String EXTRA_PLACE_ID = "place_id";

    boolean deleteAllGames = false;
    String placeId;
    String gameKey;

    public static Intent newIntent(Context context, String gameKey, String placeId) {
        Intent intent = new Intent(context, MyGamesActivity.class);
        intent.putExtra(EXTRA_KEY_GAME, gameKey);
        intent.putExtra(EXTRA_PLACE_ID, placeId);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //      WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_my_games);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        getSupportActionBar().setTitle(R.string.app_name);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        gameKey = getIntent().getStringExtra(EXTRA_KEY_GAME);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_games, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    /**
     * A placeholder fragment containing a simple view.
     */


    public static class PlaceholderFragment extends Fragment {
        // --Commented out by Inspection (3/8/17, 4:30 PM):private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

// --Commented out by Inspection START (3/8/17, 4:17 PM):
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
// --Commented out by Inspection STOP (3/8/17, 4:17 PM)
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    deleteAllGames = false;
                    return UserChatListFragment.newInstance(false, null, null);
                case 1:
                    deleteAllGames = true;
                    return GameListFragment.newInstance(placeId, GameListFragment.TYPE_USER_GAMES);
                case 2:
                    deleteAllGames = false;
                    return GameListFragment.newInstance(placeId, GameListFragment.TYPE_ALL_GAMES);
            }
            invalidateOptionsMenu();
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Chat";
                case 1:
                    return "My Games";
                case 2:
                    return "Nearby Games";
            }
            return null;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}