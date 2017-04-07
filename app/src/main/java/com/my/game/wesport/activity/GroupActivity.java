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

import com.my.game.wesport.fragment.EventListFragment;
import com.my.game.wesport.R;
import com.my.game.wesport.fragment.GalleryGridFragment;
import com.my.game.wesport.fragment.UserChatListFragment;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GroupActivity extends AppCompatActivity {
    private static final String EXTRA_KEY_GAME = "key_game";
    private static final String EXTRA_GAME_AUTHOR = "game_author";
    private TabLayout tabLayout;
//    EventBus eventBus = EventBus.getDefault();

    String gameKey;
    String gameAuthorId;
    private EventListFragment eventListFragment;
    private UserChatListFragment userChatListFragment;


    public static Intent newIntent(Context context, String gameKey, String gameAuthorId) {
        Intent intent = new Intent(context, GroupActivity.class);
        intent.putExtra(EXTRA_KEY_GAME, gameKey);
        intent.putExtra(EXTRA_GAME_AUTHOR, gameAuthorId);
        return intent;
    }

    private int[] tabIcons = {
            R.drawable.ic_group_black_24dp,
            R.drawable.ic_event_black_24dp,
            R.drawable.ic_image_black_24dp
    };
    private GalleryGridFragment galleryGridFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        getSupportActionBar().setTitle(R.string.app_name);


        gameKey = getIntent().getStringExtra(EXTRA_KEY_GAME);
        gameAuthorId = getIntent().getStringExtra(EXTRA_GAME_AUTHOR);

        GroupSectionsPagerAdapter mSectionsPagerAdapter =
                new GroupSectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.group_activity_container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        tabLayout = (TabLayout) findViewById(R.id.group_activity_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        galleryGridFragment = GalleryGridFragment.newInstance(gameKey, gameAuthorId);
        userChatListFragment = UserChatListFragment.newInstance(true, gameKey, gameAuthorId);
        eventListFragment = EventListFragment.newInstance(gameKey, gameAuthorId);

        setupTabIcons();
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class GroupSectionsPagerAdapter extends FragmentPagerAdapter {

        public GroupSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position) {
                case 0:
                    return userChatListFragment;
                case 1:
                    return eventListFragment;
                case 2:
                    return galleryGridFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            /*switch (position) {
                case 0:
                    return "Users";
                case 1:
                    return "Events";
                case 2:
                    return "Images";
            }*/
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (galleryGridFragment != null) {
            galleryGridFragment.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (!eventBus.isRegistered(this)) {
            eventBus.register(this);
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        eventBus.unregister(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
