package com.my.game.wesport.adapter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.GameHelper;
import com.my.game.wesport.model.GameModel;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
    private final List<DataSnapshot> snapshots;
    private final Context mContext;
    private final Calendar mDateAndTime = Calendar.getInstance();
    GameAdapterListener listener;

    public GameAdapter(Context mContext, GameAdapterListener listener, List<DataSnapshot> snapshots) {
        this.mContext = mContext;
        this.snapshots = snapshots;
        this.listener = listener;
    }

    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GameViewHolder(mContext, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(GameViewHolder holder, final int position) {
        GameModel gameModel = snapshots.get(position).getValue(GameModel.class);

        // Set display name
        holder.getGameDescrription().setText(gameModel.getGameDescription());

        //Set game summery
        holder.getGameSummery().setText(gameModel.getNotes());

        if (TextUtils.isEmpty(gameModel.getImage())) {
            Drawable gameDrawable = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                gameDrawable = mContext.getDrawable(R.drawable.basketball);
            }
            holder.getChatImage().setImageDrawable(gameDrawable);
        } else {
            Glide.with(mContext)
                    .load(GameHelper.getGameImage(gameModel.getImage()))
                    .asBitmap()
                    .into(holder.chatView);
        }
        //get Start Date
        holder.getGameStartDate().setText(gameModel.getGameDate() + ", " + gameModel.getStartTime() + "-" + gameModel.getEndTime());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onGameClick(position, snapshots.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return snapshots.size();
    }

    public void add(DataSnapshot value) {
        snapshots.add(value);
        notifyItemInserted(snapshots.size() - 1);
    }

    public void update(DataSnapshot snapshot) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (snapshots.get(i).getKey().equals(snapshot.getKey())) {
                snapshots.set(i, snapshot);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void remove(DataSnapshot dataSnapshot) {
        for (int i = 0; i < snapshots.size(); i++) {
            if (snapshots.get(i).getKey().equals(dataSnapshot.getKey())) {
                snapshots.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public class GameViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView startdate;
        private TextView starttime;
        private TextView summaryTextView;
        private ImageView chatView;
        private final Context mContextViewHolder;

        public GameViewHolder(Context context, View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            startdate = (TextView) itemView.findViewById(R.id.startdate);
            starttime = (TextView) itemView.findViewById(R.id.start_time);
            summaryTextView = (TextView) itemView.findViewById(R.id.summary);
            chatView = (ImageView) itemView.findViewById(R.id.chatimage);
            mContextViewHolder = context;
        }

        public TextView getGameDescrription() {
            return nameTextView;
        }

        public TextView getGameStartDate() {
            return startdate;
        }

        public TextView getGameStartTime() {
            return starttime;
        }

        public TextView getGameSummery() {
            return summaryTextView;
        }

        public ImageView getChatImage() {
            return chatView;
        }


    }

    public interface GameAdapterListener {
        void onGameClick(int position, DataSnapshot snapshot);
    }

}
