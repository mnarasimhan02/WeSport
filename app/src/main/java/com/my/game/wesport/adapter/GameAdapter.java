package com.my.game.wesport.adapter;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.GameHelper;
import com.my.game.wesport.model.DataSnapWithFlag;
import com.my.game.wesport.model.GameModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {
    private final List<DataSnapWithFlag> dataSnapShotsWithFlag;
    private final Context mContext;
    private GameAdapterListener listener;
    private String TAG = GameAdapter.class.getSimpleName();

    public GameAdapter(Context mContext, GameAdapterListener listener, List<DataSnapWithFlag> dataSnapShotsWithFlag) {
        this.mContext = mContext;
        this.dataSnapShotsWithFlag = dataSnapShotsWithFlag;
        this.listener = listener;
    }

    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GameViewHolder(mContext, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(GameViewHolder holder, final int position) {
        GameModel gameModel = dataSnapShotsWithFlag.get(position).getDataSnapshot().getValue(GameModel.class);

        // Set display name
        holder.getGameTitle().setText(gameModel.getGameDescription());

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
                    listener.onGameClick(position, dataSnapShotsWithFlag.get(position));
                }
            }
        });

        String titleColor = "#2B3D4D";
        if (dataSnapShotsWithFlag.get(position).isFlag()) {
            titleColor = "#FF0000";
        }

        holder.getGameTitle().setTextColor(Color.parseColor(titleColor));
    }

    @Override
    public int getItemCount() {
        return dataSnapShotsWithFlag.size();
    }

    public void add(DataSnapWithFlag value, boolean refreshFlags) {
        dataSnapShotsWithFlag.add(value);
        notifyItemInserted(dataSnapShotsWithFlag.size() - 1);
        if (refreshFlags){
            refreshFlags();
        }
    }

    public void update(DataSnapWithFlag snapshot, boolean refreshFlags) {
        for (int i = 0; i < dataSnapShotsWithFlag.size(); i++) {
            if (dataSnapShotsWithFlag.get(i).getDataSnapshot().getKey().equals(snapshot.getDataSnapshot().getKey())) {
                dataSnapShotsWithFlag.set(i, snapshot);
                notifyItemChanged(i);
                if (refreshFlags){
                    refreshFlags();
                }
                break;
            }
        }
    }

    public void remove(DataSnapWithFlag dataSnapshot, boolean refreshFlags) {
        for (int i = 0; i < dataSnapShotsWithFlag.size(); i++) {
            if (dataSnapShotsWithFlag.get(i).getDataSnapshot().getKey().equals(dataSnapshot.getDataSnapshot().getKey())) {
                dataSnapShotsWithFlag.remove(i);
                notifyItemRemoved(i);
                if (refreshFlags){
                    refreshFlags();
                }
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

        public TextView getGameTitle() {
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


    private void refreshFlags() {
        for (int i = 0; i < dataSnapShotsWithFlag.size(); i++) {
            boolean flag = false;
            DataSnapWithFlag currentItem = dataSnapShotsWithFlag.get(i);
            GameModel currentGame = currentItem.getDataSnapshot().getValue(GameModel.class);

//            if game is from other user then skip it
            if (!currentGame.getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                continue;
            }

            for (int j = 0; j < dataSnapShotsWithFlag.size(); j++) {
                DataSnapWithFlag targetItem = dataSnapShotsWithFlag.get(j);
                GameModel targetGame = targetItem.getDataSnapshot().getValue(GameModel.class);


                Log.d(TAG, "refreshFlags: " + currentItem.getDataSnapshot().getKey() + ", " + targetItem.getDataSnapshot().getKey());
//                if both are not same items and have same date
                if (!currentItem.getDataSnapshot().getKey().equals(targetItem.getDataSnapshot().getKey())
                        && currentGame.getGameDate().equals(targetGame.getGameDate())) {
                    try {
                        Log.d(TAG, "refreshFlags: start matching time");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.US);
                        Date currentGameStartTime = simpleDateFormat.parse(currentGame.getStartTime());
                        Date currentGameEndTime = simpleDateFormat.parse(currentGame.getEndTime());

                        Date targetGameStartTime = simpleDateFormat.parse(targetGame.getStartTime());
                        Date targetGameEndTime = simpleDateFormat.parse(targetGame.getEndTime());

                        if (currentGameStartTime.before(targetGameEndTime) && currentGameStartTime.after(targetGameStartTime)) {
                            flag = true;
                        } else if (currentGameEndTime.before(targetGameEndTime) && currentGameEndTime.after(targetGameStartTime)) {
                            flag = true;
                        } else if (currentGameStartTime.before(targetGameStartTime) && currentGameEndTime.after(targetGameEndTime)) {
                            flag = true;
                        }

//                        if time overlap then break loop
                        if (flag) {
                            break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.d(TAG, "refreshFlags: " + e.getMessage());
                    }
                }
            }   // inner loop end

//            if flag is not correct then update it
            if (flag != currentItem.isFlag()) {
                dataSnapShotsWithFlag.get(i).setFlag(flag);
                notifyItemChanged(i);
            }

        }   // outer loop end
    }

    public void refreshFlags(List<DataSnapshot> snapshots) {
        for (int i = 0; i < dataSnapShotsWithFlag.size(); i++) {
            boolean flag = false;
            DataSnapWithFlag currentItem = dataSnapShotsWithFlag.get(i);
            GameModel currentGame = currentItem.getDataSnapshot().getValue(GameModel.class);

//            if game is from other user then skip it
            if (!currentGame.getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                continue;
            }

            for (int j = 0; j < snapshots.size(); j++) {
                DataSnapshot targetItem = snapshots.get(j);
                GameModel targetGame = targetItem.getValue(GameModel.class);


                Log.d(TAG, "refreshFlags: " + currentItem.getDataSnapshot().getKey() + ", " + targetItem.getKey());
//                if both are not same items and have same date
                if (!currentItem.getDataSnapshot().getKey().equals(targetItem.getKey())
                        && currentGame.getGameDate().equals(targetGame.getGameDate())) {
                    try {
                        Log.d(TAG, "refreshFlags: start matching time");
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.US);
                        Date currentGameStartTime = simpleDateFormat.parse(currentGame.getStartTime());
                        Date currentGameEndTime = simpleDateFormat.parse(currentGame.getEndTime());

                        Date targetGameStartTime = simpleDateFormat.parse(targetGame.getStartTime());
                        Date targetGameEndTime = simpleDateFormat.parse(targetGame.getEndTime());

                        if (currentGameStartTime.before(targetGameEndTime) && currentGameStartTime.after(targetGameStartTime)) {
                            flag = true;
                        } else if (currentGameEndTime.before(targetGameEndTime) && currentGameEndTime.after(targetGameStartTime)) {
                            flag = true;
                        } else if (currentGameStartTime.before(targetGameStartTime) && currentGameEndTime.after(targetGameEndTime)) {
                            flag = true;
                        }

//                        if time overlap then break loop
                        if (flag) {
                            break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Log.d(TAG, "refreshFlags: " + e.getMessage());
                    }
                }
            }   // inner loop end

//            if flag is not correct then update it
            if (flag != currentItem.isFlag()) {
                dataSnapShotsWithFlag.get(i).setFlag(flag);
                notifyItemChanged(i);
            }

        }   // outer loop end
    }

    public interface GameAdapterListener {
        void onGameClick(int position, DataSnapWithFlag snapshot);
    }
}
