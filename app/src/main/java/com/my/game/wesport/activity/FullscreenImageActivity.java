package com.my.game.wesport.activity;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.my.game.wesport.R;
import com.my.game.wesport.databinding.ActivityFullscreenImageBinding;
import com.my.game.wesport.fragment.FullScreenImageFragment;

import java.util.ArrayList;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenImageActivity extends AppCompatActivity {
    private static final String EXTRA_IMAGES = "images";
    private static String EXTRA_INDEX = "image_index";
    ActivityFullscreenImageBinding binding;
    ArrayList<String> images = new ArrayList<>();
    int defaultIndex = 0;


    public static Intent newIntent(Activity activity, ArrayList<String> images, int index) {
        Intent intent = new Intent(activity, FullscreenImageActivity.class);
        intent.putStringArrayListExtra(EXTRA_IMAGES, images);
        intent.putExtra(EXTRA_INDEX, index);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_fullscreen_image);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            images = bundle.getStringArrayList(EXTRA_IMAGES);
            defaultIndex = bundle.getInt(EXTRA_INDEX);
        }

        binding.viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return FullScreenImageFragment.newInstance(images.get(position));
            }

            @Override
            public int getCount() {
                return images.size();
            }
        });

        if (images.size() > defaultIndex) {
            binding.viewPager.setCurrentItem(defaultIndex);
        }

        hide();
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public void onBackClick(View view) {
        finish();
    }
}
