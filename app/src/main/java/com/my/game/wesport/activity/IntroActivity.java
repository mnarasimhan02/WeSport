package com.my.game.wesport.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import com.my.game.wesport.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

@SuppressWarnings("ALL")
public class IntroActivity extends AppIntro {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDepthAnimation();

        // Instead of fragments, you can also use our default slide
        // Just set a title, description, background and image. AppIntro will do the rest.
        addSlide(AppIntroFragment.newInstance(getString(R.string.first_screen), getString(R.string.scr1_desc),
                R.drawable.screen1, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.second_screen), getString(R.string.scr2_desc),
                R.drawable.screen2, ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.third_screen), getString(R.string.scr3_desc),
                R.drawable.screen3, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.fourth_screen), getString(R.string.scr4_desc),
                R.drawable.screen4, ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.fourtha_screen), getString(R.string.scr4a_desc),
                R.drawable.screen4a, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));

        addSlide(AppIntroFragment.newInstance(getString(R.string.five_screen), getString(R.string.scr5_desc),
                R.drawable.screen5, ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.six_screen), getString(R.string.scr6_desc),
                R.drawable.screen6, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.seven_screen), getString(R.string.scr7_desc),
                R.drawable.screen7, ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.eighta_screen), getString(R.string.scr8a_desc),
                R.drawable.screen8a, ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary)));
        addSlide(AppIntroFragment.newInstance(getString(R.string.nine_screen), getString(R.string.scr9_desc),
                R.drawable.screen9, ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));

        // Hide Skip/Done button.
        showSkipButton(false);
        setProgressButtonEnabled(true);
        // SHOW or HIDE the statusbar
        showStatusBar(true);
    }

    @Override
    public void onDonePressed() {
        // Do something when users tap on Done button.
        finish();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}