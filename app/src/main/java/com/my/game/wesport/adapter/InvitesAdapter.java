package com.my.game.wesport.adapter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.gson.Gson;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.DateHelper;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.GameHelper;
import com.my.game.wesport.model.GameModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class InvitesAdapter extends RecyclerView.Adapter<InvitesAdapter.GameViewHolder> {
    private final List<DataSnapshot> dataSnapShots = new ArrayList<>();
    private final Context mContext;
    private GameAdapterListener listener;
    private String TAG = InvitesAdapter.class.getSimpleName();

    public InvitesAdapter(Context mContext, GameAdapterListener listener) {
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GameViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.game_invite_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final GameViewHolder holder, final int position) {
        final DataSnapshot dataSnapshot = dataSnapShots.get(position);
        GameModel gameModel = dataSnapshot.getValue(GameModel.class);

        Log.d(TAG, "onBindViewHolder: " + new Gson().toJson(gameModel));

        // Set display name
        holder.nameTextView.setText(gameModel.getGameDescription());

        //Set game summery
        holder.summaryTextView.setText(gameModel.getNotes());
        holder.address.setText("at " + gameModel.getAddress());
        String ownerName = gameModel.getAuthorName();

        if (FirebaseHelper.getCurrentUser().getUid().equals(gameModel.getAuthor())) {
            ownerName = App.getInstance().getUserModel().getDisplayName();
        }

        holder.owner.setText("by " + ownerName);

        if (TextUtils.isEmpty(gameModel.getImage())) {
            Drawable gameDrawable = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                gameDrawable = mContext.getDrawable(R.drawable.basketball);
            }
            holder.chatView.setImageDrawable(gameDrawable);
        } else {
            Glide.with(mContext)
                    .load(GameHelper.getGameImage(gameModel.getImage()))
                    .asBitmap()
                    .into(holder.chatView);
        }
        //get Start Date
        String date;
        try {
            date = DateHelper.getAppDateFormatter().format(DateHelper.getServerDateFormatter().parse(gameModel.getGameDate()));
        } catch (Exception e) {
            e.printStackTrace();
            date = gameModel.getGameDate();
        }
        holder.startdate.setText(date + ", " + gameModel.getStartTime() + "-" + gameModel.getEndTime());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onGameClick(position, dataSnapshot);
                }
            }
        });

        holder.actionReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onRejectClick(position, dataSnapshot);
                }
            }
        });
        holder.actionAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onAcceptClick(position, dataSnapshot);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return dataSnapShots.size();
    }

    public void addAtFirst(DataSnapshot value) {
        dataSnapShots.add(0, value);
        notifyItemInserted(0);
    }

    public void update(DataSnapshot snapshot) {
        for (int i = 0; i < dataSnapShots.size(); i++) {
            if (dataSnapShots.get(i).getKey().equals(snapshot.getKey())) {
                dataSnapShots.set(i, snapshot);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void remove(String key) {
        for (int i = 0; i < dataSnapShots.size(); i++) {
            if (dataSnapShots.get(i).getKey().equals(key)) {
                dataSnapShots.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public void add(DataSnapshot dataSnapshot) {
        dataSnapShots.add(dataSnapshot);
        notifyItemInserted(dataSnapShots.size() - 1);
    }

    public class GameViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView startdate;
        private TextView summaryTextView;
        private ImageView chatView;
        private TextView owner;
        private TextView address;
        private Button actionAccept;
        private Button actionReject;

        public GameViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            startdate = (TextView) itemView.findViewById(R.id.startdate);
            summaryTextView = (TextView) itemView.findViewById(R.id.summary);
            chatView = (ImageView) itemView.findViewById(R.id.chatimage);
            owner = (TextView) itemView.findViewById(R.id.owner);
            address = (TextView) itemView.findViewById(R.id.address);
            actionAccept = (Button) itemView.findViewById(R.id.accept_btn);
            actionReject = (Button) itemView.findViewById(R.id.reject_btn);
        }

    }

    public interface GameAdapterListener {
        void onGameClick(int position, DataSnapshot snapshot);

        void onAcceptClick(int position, DataSnapshot snapshot);

        void onRejectClick(int position, DataSnapshot snapshot);
    }
}
