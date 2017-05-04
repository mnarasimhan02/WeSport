package com.my.game.wesport.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.my.game.wesport.R;
import com.my.game.wesport.model.GameCategoryModel;

import java.util.ArrayList;
import java.util.List;


public class SimpleGameCategoryAdapter extends RecyclerView.Adapter<SimpleGameCategoryAdapter.ViewHolderUsers> {
    private Context mContext;
    private List<GameCategoryModel> gameCategoryModels = new ArrayList<>();
    private SimpleGameCategoryListListener listener;

    public SimpleGameCategoryAdapter(Context mContext, List<GameCategoryModel> gameCategoryModels, SimpleGameCategoryListListener listener) {
        this.mContext = mContext;
        this.gameCategoryModels = gameCategoryModels;
        this.listener = listener;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.simple_game_category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, final int position) {

        final GameCategoryModel gameCategoryModel = gameCategoryModels.get(position);

        Glide.with(mContext)
                .load(gameCategoryModel.getImage())
                .error(R.drawable.profile)
                .into(holder.image);

        holder.title.setText(gameCategoryModel.getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onGameCategoryClick(position, gameCategoryModel);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return gameCategoryModels.size();
    }


    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder {

        public final ImageView image;
        public final TextView title;


        public ViewHolderUsers(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
        }
    }

    public interface SimpleGameCategoryListListener {
        void onGameCategoryClick(int position, GameCategoryModel gameCategoryModel);
    }
}