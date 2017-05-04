package com.my.game.wesport.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.my.game.wesport.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FullScreenImageFragment extends Fragment {

    public static final String EXTRA_IMAGE = "image";
    ImageView imageView;
    ProgressBar progressBar;
    String imageLink;

    public static FullScreenImageFragment newInstance(String imageLink) {
        Bundle args = new Bundle();
        args.putString(EXTRA_IMAGE, imageLink);
        FullScreenImageFragment fragment = new FullScreenImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public FullScreenImageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_full_screen_image, container, false);

        imageView = (ImageView) view.findViewById(R.id.imageView);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);

        Bundle bundle = getArguments();
        if (bundle.containsKey(EXTRA_IMAGE)) {
            imageLink = bundle.getString(EXTRA_IMAGE);
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageLink).listener(new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).error(R.drawable.image_placeholder_drawable).into(imageView);
        }

        return view;
    }

}
