package com.my.game.wesport.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.model.UserListItem;
import com.my.game.wesport.model.UserModel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class UsersChatListAdapter extends RecyclerView.Adapter<UsersChatListAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private final List<UserListItem> chatListItems;
    private final Context mContext;
    private double mLat;
    private double mLon;
    private ChatListInterface listener;
    private String TAG = UsersChatListAdapter.class.getSimpleName();

    public UsersChatListAdapter(Context context, List<UserListItem> fireChatUserModels, ChatListInterface listener) {
        chatListItems = fireChatUserModels;
        mContext = context;
        this.listener = listener;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext, LayoutInflater.from(parent.getContext()).
                inflate(R.layout.user_chat_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, final int position) {

        final UserListItem chatListItem = chatListItems.get(position);
        UserModel fireChatUserModel = chatListItem.getUser();
        if (chatListItem.getCounter() > 0) {
            holder.chatCounter.setVisibility(View.VISIBLE);
            holder.chatCounter.setText(String.valueOf(chatListItem.getCounter()));
        } else {
            holder.chatCounter.setVisibility(View.GONE);
        }
        // Set avatar
        String mPhotoUri;
        mPhotoUri = fireChatUserModel.getPhotoUri();
        try {
            Glide.with(mContext)
                    .load(mPhotoUri)
                    .error(R.drawable.profile)
                    .into(holder.mUserAvatar);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Set display name
        holder.getUserDisplayName().setText(fireChatUserModel.getDisplayName());


        // Set Location to distance
        holder.getUserLocation().setText(
                LocationHelper.getDistance(mLat, mLon, fireChatUserModel.getLatitude(),
                        fireChatUserModel.getLongitude())
                        + " " + mContext.getString(R.string.miles_away));
        holder.bioTextView.setText(fireChatUserModel.getBio());
        // Set presence status
        holder.getStatusConnection().setText(fireChatUserModel.getConnection());

        // Set presence text color
        if (fireChatUserModel.getConnection().equals(ONLINE)) {
            // Green color
            holder.getStatusConnection().setTextColor(Color.parseColor("#00FF00"));
        } else {
            // Red color
            holder.getStatusConnection().setTextColor(Color.parseColor("#FF0000"));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onUserItemClick(position, chatListItem);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onUserItemLongClick(position, chatListItem);
                }
                return false;
            }
        });
    }

    @SuppressWarnings("UnusedReturnValue")
    private static List<UserListItem> compareDistance(List<UserListItem> mUserModels, final double mLat, final double mLon) {
        Comparator<UserListItem> distance = new Comparator<UserListItem>() {
            @Override
            public int compare(UserListItem item1, UserListItem item2) {
                UserModel user1 = item1.getUser();
                UserModel user2 = item2.getUser();

                float[] result1 = new float[3];
                Float distance1 = result1[0];
                if (user1.getLatitude() != null && user1.getLongitude() != null) {
                    android.location.Location.distanceBetween(mLat, mLon,
                            user1.getLatitude() != null ? Double.parseDouble(user1.getLatitude()) : 0,
                            user1.getLongitude() != null ? Double.parseDouble(user1.getLongitude()) : 0, result1);
                    distance1 = result1[0];
                }
                float[] result2 = new float[3];
                Float distance2 = null;
                if (user2.getLatitude() != null && user2.getLongitude() != null) {
                    android.location.Location.distanceBetween(mLat, mLon,
                            user2.getLatitude() != null ? Double.parseDouble(user2.getLatitude()) : 0,
                            user2.getLongitude() != null ? Double.parseDouble(user2.getLongitude()) : 0, result2);
                    distance2 = result2[0];
                } else if ((user2.getLatitude() == null || user2.getLongitude() == null)) {
                    distance2 = (float) 0;
                }
                return distance1.compareTo(distance2);
            }
        };
        Collections.sort(mUserModels, distance);
        return mUserModels;
    }

    @Override
    public int getItemCount() {
        return chatListItems.size();
    }

    public void refill(UserListItem chatListItem) {
//        if item already exists then ignore
        if (indexOf(chatListItem.getUserUid()) != -1) {
            return;
        }
        chatListItems.add(chatListItem);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                mContext);
        try {
            mLat = Double.parseDouble(preferences.getString("latitude", ""));
            mLon = Double.parseDouble(preferences.getString("longtitude", ""));
            compareDistance(chatListItems, mLat, mLon);
        } catch (Exception e) {
            Log.d(TAG, "refill: " + e.getMessage());
        }
        notifyDataSetChanged();
    }

    public void changeUser(int index, UserListItem chatListItem) {
        chatListItems.set(index, chatListItem);
        notifyDataSetChanged();
    }

    public void updateUser(UserListItem chatListItem) {
        int index = indexOf(chatListItem.getUserUid());
        if (index != -1) {
            chatListItems.set(index, chatListItem);
            notifyItemChanged(index);
        }
    }

    public UserListItem getItem(String userUid) {
        int index = indexOf(userUid);

        if (index != -1) {
            return chatListItems.get(index);
        }

        return null;
    }

    public int indexOf(String userUid) {
        for (int i = 0; i < chatListItems.size(); i++) {
            if (chatListItems.get(i).getUserUid().equals(userUid)) {
                return i;
            }
        }
        return -1;
    }

    public void removeUser(int index) {
        if (index != -1) {
            chatListItems.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void clear() {
        chatListItems.clear();
    }

    public boolean contains(String userKey) {
        return indexOf(userKey) != -1;
    }

    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder {

        private final ImageView mUserAvatar;
        private final TextView mUserDisplayName;
        private final TextView mStatusConnection;
        private final TextView bioTextView;
        private final Context mContextViewHolder;
        private final TextView mStatusLocation;
        private final TextView chatCounter;


        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            mUserAvatar = (ImageView) itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView) itemView.findViewById(R.id.text_view_display_name);
            mStatusConnection = (TextView) itemView.findViewById(R.id.text_view_connection_status);
            mStatusLocation = (TextView) itemView.findViewById(R.id.text_view_location);
            bioTextView = (TextView) itemView.findViewById(R.id.text_view_bio);
            chatCounter = (TextView) itemView.findViewById(R.id.text_unread_message_count);
            mContextViewHolder = context;
        }

        public ImageView getUserAvatar() {
            return mUserAvatar;
        }

        public TextView getUserDisplayName() {
            return mUserDisplayName;
        }

        public TextView getStatusConnection() {
            return mStatusConnection;
        }

        public TextView getUserLocation() {
            return mStatusLocation;
        }
    }

    public interface ChatListInterface {
        void onUserItemClick(int position, UserListItem userListItem);

        void onUserItemLongClick(int position, UserListItem userListItem);
    }
}