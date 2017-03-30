package com.my.game.wesport.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.model.ChatListItem;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class UsersChatListAdapter extends RecyclerView.Adapter<UsersChatListAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private final List<ChatListItem> chatListItems;
    private final Context mContext;
    private String mCurrentUserEmail;
    private Long mCurrentUserCreatedAt;
    private double mLat;
    private double mLon;

    public UsersChatListAdapter(Context context, List<ChatListItem> fireChatUserModels) {
        chatListItems = fireChatUserModels;
        mContext = context;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext, LayoutInflater.from(parent.getContext()).
                inflate(R.layout.user_chat_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position) {

        ChatListItem chatListItem = chatListItems.get(position);
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
    }

    @SuppressWarnings("UnusedReturnValue")
    private static List<ChatListItem> compareDistance(List<ChatListItem> mUserModels, final double mLat, final double mLon) {
        Comparator<ChatListItem> distance = new Comparator<ChatListItem>() {
            @Override
            public int compare(ChatListItem item1, ChatListItem item2) {
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

    public void refill(ChatListItem chatListItem) {
        chatListItems.add(chatListItem);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                mContext);
        mLat = Double.parseDouble(preferences.getString("latitude", ""));
        mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        compareDistance(chatListItems, mLat, mLon);
        notifyDataSetChanged();
    }

    public void changeUser(int index, ChatListItem chatListItem) {
        chatListItems.set(index, chatListItem);
        notifyDataSetChanged();
    }

    public void updateUser(ChatListItem chatListItem) {
        int index = indexOf(chatListItem.getUserUid());
        if (index != -1) {
            chatListItems.set(index, chatListItem);
            notifyItemChanged(index);
        }
    }

    public ChatListItem getItem(String userUid) {
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

    @SuppressWarnings("UnusedParameters")
    public void setCurrentUserInfo(String userUid, String email, long createdAt, String photoUri, String latitude,
                                   String longitude, String distance) {
        mCurrentUserEmail = email;
        mCurrentUserCreatedAt = createdAt;
    }

    public void clear() {
        chatListItems.clear();
    }

    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener {

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
            itemView.setOnClickListener(this);
        }

        public ImageView getUserAvatar() {
            return mUserAvatar;
        }

        // --Commented out by Inspection START (3/8/17, 4:18 PM):
//        public ImageView getUserNonAvatar() {
//            return mUserAvatar;
//        }
// --Commented out by Inspection STOP (3/8/17, 4:18 PM)
        public TextView getUserDisplayName() {
            return mUserDisplayName;
        }

        public TextView getStatusConnection() {
            return mStatusConnection;
        }

        public TextView getUserLocation() {
            return mStatusLocation;
        }

        @Override
        public void onClick(View view) {
            try {
                UserModel userModel = chatListItems.get(getLayoutPosition()).getUser();
                if (userModel != null && mCurrentUserCreatedAt != null && mCurrentUserEmail != null) {
                    mContextViewHolder.startActivity(ChatActivity.newIntent(mContextViewHolder, userModel));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}