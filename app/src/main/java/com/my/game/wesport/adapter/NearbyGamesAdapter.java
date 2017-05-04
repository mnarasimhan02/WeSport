package com.my.game.wesport.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.DateHelper;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.GameHelper;
import com.my.game.wesport.model.DataSnapWithFlag;
import com.my.game.wesport.model.GameCategoryModel;
import com.my.game.wesport.model.GameModel;
import com.my.game.wesport.model.UserModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NearbyGamesAdapter extends RecyclerView.Adapter<NearbyGamesAdapter.GameViewHolder> {
    private final List<DataSnapWithFlag> dataSnapShotsWithFlag;
    private final Context mContext;
    private NearbyGameAdapterListener listener;
    private String TAG = GameAdapter.class.getSimpleName();
    private boolean showEditAction = false;

    public NearbyGamesAdapter(Context mContext, List<DataSnapWithFlag> dataSnapShotsWithFlag, NearbyGameAdapterListener listener) {
        this.mContext = mContext;
        this.dataSnapShotsWithFlag = dataSnapShotsWithFlag;
        this.listener = listener;
    }

    @Override
    public NearbyGamesAdapter.GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new NearbyGamesAdapter.GameViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_games_new_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final NearbyGamesAdapter.GameViewHolder holder, final int position) {
        GameModel gameModel = dataSnapShotsWithFlag.get(position).getDataSnapshot().getValue(GameModel.class);
        if (gameModel == null) {
            return;
        }

        UserModel userModel = dataSnapShotsWithFlag.get(position).getDataSnapshot().getValue(UserModel.class);

        // Set display name
        holder.getGameTitle().setText(gameModel.getGameDescription());

        //Set game summery
        holder.getGameSummery().setText(gameModel.getNotes());
        holder.address.setText("at " + gameModel.getAddress());
        String ownerName = gameModel.getAuthorName();

        if (FirebaseHelper.getCurrentUser().getUid().equals(gameModel.getAuthor())) {
            ownerName = App.getInstance().getUserModel().getDisplayName();
        }

        holder.owner.setText(ownerName);

        GameCategoryModel categoryModel = GameHelper.getGameCategory(gameModel.getCategoryId());

        if (categoryModel != null) {
            Glide.with(mContext)
                    .load(categoryModel.getImage())
                    .asBitmap()
                    .into(holder.chatView);
        }

        // getting user image
       /* Glide.with(mContext)
                .load(userModel.getPhotoUri())
                .asBitmap()
                .into(holder.avatarImage);*/

        //get Start Date
        String date;
        try {
            date = DateHelper.getAppDateFormatter().format(DateHelper.getServerDateFormatter().parse(gameModel.getGameDate()));
        } catch (Exception e) {
            e.printStackTrace();
            date = gameModel.getGameDate();
        }
        holder.getGameStartDate().setText(date + ", " + gameModel.getStartTime() + "-" + gameModel.getEndTime());
        holder.sendInviteBtn.setVisibility(View.VISIBLE);
        holder.sendInviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onSendInviteClick(position, dataSnapShotsWithFlag.get(position));
                }
            }
        });
        /*holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listener != null) {
                    listener.onSendInviteClick(position, dataSnapShotsWithFlag.get(position));
                }
            }
        });*/

        String titleColor = "#FFFFFF";
        if (dataSnapShotsWithFlag.get(position).isFlag()) {
            titleColor = "#FF0000";
        }

        holder.getGameTitle().setTextColor(Color.parseColor(titleColor));

        /*boolean enableEditing = gameModel.getAuthor().equals(FirebaseHelper.getCurrentUser().getUid()) && showEditAction;
        holder.editAction.setVisibility(enableEditing ? View.VISIBLE : View.GONE);
        holder.editAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onGameEditClick(position, dataSnapShotsWithFlag.get(position));
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return dataSnapShotsWithFlag.size();
    }

    public void add(DataSnapWithFlag value, boolean refreshFlags) {
        dataSnapShotsWithFlag.add(value);
        notifyItemInserted(dataSnapShotsWithFlag.size() - 1);
        if (refreshFlags) {
            refreshFlags();
        }
    }

    public void addAtFirst(DataSnapWithFlag value, boolean refreshFlags) {
        dataSnapShotsWithFlag.add(0, value);
        notifyItemInserted(0);
        if (refreshFlags) {
            refreshFlags();
        }
    }

    public void update(DataSnapWithFlag snapshot, boolean refreshFlags) {
        for (int i = 0; i < dataSnapShotsWithFlag.size(); i++) {
            if (dataSnapShotsWithFlag.get(i).getDataSnapshot().getKey().equals(snapshot.getDataSnapshot().getKey())) {
                dataSnapShotsWithFlag.set(i, snapshot);
                notifyItemChanged(i);
                if (refreshFlags) {
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
                if (refreshFlags) {
                    refreshFlags();
                }
                break;
            }
        }
    }

    public void remove(String gameKey, boolean refreshFlags) {
        int index = indexOf(gameKey);
        if (index != -1) {
            dataSnapShotsWithFlag.remove(index);
            notifyItemRemoved(index);
            if (refreshFlags) {
                refreshFlags();
            }
        }
    }

    public int indexOf(String gameKey) {
        int index = -1;
        for (int i = 0; i < dataSnapShotsWithFlag.size(); i++) {
            DataSnapWithFlag dataSnapWithFlag = dataSnapShotsWithFlag.get(i);
            if (dataSnapWithFlag.getDataSnapshot().getKey().equals(gameKey)) {
                return i;
            }
        }

        return index;
    }

    public class GameViewHolder extends RecyclerView.ViewHolder {
        private TextView nameTextView;
        private TextView startdate;
        //private TextView starttime;
        private TextView summaryTextView;
        private ImageView chatView;
        private TextView owner;
        private TextView address;
        private TextView sendInviteBtn;
        private ImageView editAction;
        //private ImageView avatarImage;

        public GameViewHolder(View itemView) {
            super(itemView);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            startdate = (TextView) itemView.findViewById(R.id.startdate);
            summaryTextView = (TextView) itemView.findViewById(R.id.summary);
            chatView = (ImageView) itemView.findViewById(R.id.chatimage);
            owner = (TextView) itemView.findViewById(R.id.owner);
            address = (TextView) itemView.findViewById(R.id.address);
            sendInviteBtn = (TextView) itemView.findViewById(R.id.send_invite_btn);
            // avatarImage = (ImageView) itemView.findViewById(R.id.my_games_user_profile_image);
        }

        public TextView getGameTitle() {
            return nameTextView;
        }

        public TextView getGameStartDate() {
            return startdate;
        }

        /*public TextView getGameStartTime() {
            return starttime;
        }*/

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

    public interface NearbyGameAdapterListener {
        void onSendInviteClick(int position, DataSnapWithFlag snapshot);

        //void onGameEditClick(int position, DataSnapWithFlag snapshot);
    }
}
