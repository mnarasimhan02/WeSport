package com.my.game.wesport.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.my.game.wesport.R;
import com.my.game.wesport.model.FGridImage;
import com.my.game.wesport.model.GridImageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sabeeh on 29-Oct-16.
 */

public class GridRecyclerViewAdapter extends DragSelectRecyclerViewAdapter<GridRecyclerViewAdapter.MainViewHolder> {
    private List<GridImageModel> imageModels = new ArrayList<>();
    private Context context;

    public void remove(int position) {
        if (position < imageModels.size()) {
            imageModels.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void remove(GridImageModel item) {
        if (imageModels.contains(item)) {
            imageModels.remove(item);
        }
    }

    public void remove(DataSnapshot dataSnapshot) {
        for (int i = 0; i < imageModels.size(); i++) {
            if (imageModels.get(i).getFirebaseKeyName().equals(dataSnapshot.getKey())) {
                remove(i);
                break;
            }
        }
    }

    public interface ClickListener {
        void onGridClick(int index);

        void onGridLongClick(int index);
    }

    private final ClickListener mCallback;

    // Constructor takes click listener callback
    public GridRecyclerViewAdapter(ClickListener callback, Context context) {
        super();
        mCallback = callback;
        this.context = context;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_image_item, parent, false);
        return new MainViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MainViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();

        int paddingSpace = context.getResources().getDimensionPixelSize(R.dimen.grid_image_space) * 6;
        int size = (width - paddingSpace) / 2;
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(size, size));
//        holder.itemView.invalidate();

        GridImageModel gridImageModel = imageModels.get(position);

        holder.progressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(gridImageModel.getRemoteUrl())) {
            Glide.with(context).load(gridImageModel.getRemoteUrl()).asBitmap().placeholder(R.drawable.image_placeholder_drawable).error(R.drawable.image_placeholder_drawable).listener(new RequestListener<String, Bitmap>() {
                @Override
                public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    holder.progressBar.setVisibility(View.GONE);
                    return false;
                }
            }).into(holder.image);
        } else {
            if (!TextUtils.isEmpty(gridImageModel.getFirebaseKeyName())) {
                holder.progressBar.setVisibility(View.GONE);
            }
            Glide.with(context).load(gridImageModel.getLocalPath()).error(R.drawable.places_ic_clear).into(holder.image);
        }

        if (isIndexSelected(position)) {
            holder.actionBtnImage.setVisibility(View.VISIBLE);
            // Item is selected, change it somehow
        } else {
            holder.actionBtnImage.setVisibility(View.GONE);
            // Item is not selected, reset it to a non-selected state
        }
    }

    @Override
    protected boolean isIndexSelectable(int index) {
        // This method is OPTIONAL, returning false will prevent the item at the specified index from being selected.
        // Both initial selection, and drag selection.
        return true;
    }

    @Override
    public int getItemCount() {
        return imageModels.size();
    }

    public class MainViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        ImageView image;
        ImageView actionBtnImage;
        ProgressBar progressBar;

        public MainViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.imageView);
            actionBtnImage = (ImageView) itemView.findViewById(R.id.action_btn);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);

            this.itemView.setOnClickListener(this);
            this.itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onGridClick(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            // Forwards to the adapter's constructor callback
            if (mCallback != null) mCallback.onGridLongClick(getAdapterPosition());
            return true;
        }
    }

    public void updateImages(List<GridImageModel> images) {
        if (images == null) {
            this.imageModels = new ArrayList<>();
        } else {
            this.imageModels = images;
        }
        notifyDataSetChanged();
    }

    public void addLocalImage(String image, String remoteFileName) {
        GridImageModel imageModel = new GridImageModel();
        imageModel.setLocalPath(image);
        imageModel.setRemoteFileName(remoteFileName);
        imageModels.add(imageModel);
        notifyItemInserted(imageModels.size() - 1);
    }

    public void add(DataSnapshot dataSnapshot) {
        FGridImage fGridImage = dataSnapshot.getValue(FGridImage.class);

//        check if image already exists as local
        int existingIndex = -1;
        for (int i = 0; i < imageModels.size(); i++) {
            String remoteFileName = imageModels.get(i).getRemoteFileName();
            if (!TextUtils.isEmpty(remoteFileName) && fGridImage.getThumb().contains(remoteFileName)) {
                existingIndex = i;
            }
        }

        if (existingIndex != -1) {
//            local image uploaded
            imageModels.get(existingIndex).setFirebaseKeyName(dataSnapshot.getKey());
            notifyItemChanged(existingIndex);

        } else {
//            new image added
            GridImageModel imageModel = new GridImageModel();
            imageModel.setRemoteUrl(fGridImage.getThumb());
            imageModel.setFirebaseKeyName(dataSnapshot.getKey());

            imageModels.add(imageModel);
            notifyItemInserted(imageModels.size() - 1);
        }
    }


    public List<String> getSelectedItemsUrl() {
        List<String> selectedItems = new ArrayList<>();
        Integer[] selectedIndices = getSelectedIndices();
        for (Integer selectedIndex : selectedIndices) {
            selectedItems.add(imageModels.get(selectedIndex).getRemoteUrl());
        }
        return selectedItems;
    }

    public List<GridImageModel> getSelectedItems() {
        List<GridImageModel> selectedItems = new ArrayList<>();
        Integer[] selectedIndices = getSelectedIndices();
        for (Integer selectedIndex : selectedIndices) {
            selectedItems.add(imageModels.get(selectedIndex));
        }
        return selectedItems;
    }

    public ArrayList<String> getImages() {
        ArrayList<String> images = new ArrayList<>();
        for (GridImageModel imageModel : imageModels) {
            if (!TextUtils.isEmpty(imageModel.getLocalPath())) {
                images.add(imageModel.getLocalPath());
            } else {
                images.add(imageModel.getRemoteUrl());
            }
        }

        return images;
    }

    public boolean isAnyImageUploading() {
        for (GridImageModel imageModel : imageModels) {
            if (TextUtils.isEmpty(imageModel.getRemoteUrl())) {
                return true;
            }
        }
        return false;
    }

}
