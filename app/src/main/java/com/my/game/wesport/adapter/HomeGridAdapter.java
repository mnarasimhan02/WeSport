package com.my.game.wesport.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.game.wesport.R;
import com.my.game.wesport.activity.GameEditActivity;
import com.my.game.wesport.model.GameCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class HomeGridAdapter extends BaseAdapter {
    private final Context mContext;
    private List<GameCategoryModel> gameCategoryList = new ArrayList<>();
    HomeGridListener listener;

    public HomeGridAdapter(Context context, List<GameCategoryModel> gameCategoryList) {
        mContext = context;
        this.gameCategoryList = gameCategoryList;
    }


    @Override
    public int getCount() {
        return gameCategoryList.size();
    }

    @Override
    public Object getItem(int i) {
        return gameCategoryList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return gameCategoryList.get(i).getId();
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View gridViewAndroid;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            // gridViewAndroid = new View(mContext);
            gridViewAndroid = inflater.inflate(R.layout.gridview_list_item, null);
            TextView textViewAndroid = (TextView) gridViewAndroid.findViewById(R.id.android_gridview_text);
            ImageView imageViewAndroid = (ImageView) gridViewAndroid.findViewById(R.id.android_gridview_image);
            textViewAndroid.setText(gameCategoryList.get(position).getTitle());
            imageViewAndroid.setImageResource(gameCategoryList.get(position).getImage());
        } else {
            gridViewAndroid = convertView;
        }

        gridViewAndroid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onItemClick(position);
                }
            }
        });
        return gridViewAndroid;
    }

    public void setListener(HomeGridListener listener) {
        this.listener = listener;
    }

    public interface HomeGridListener {
        void onItemClick(int position);
    }
}