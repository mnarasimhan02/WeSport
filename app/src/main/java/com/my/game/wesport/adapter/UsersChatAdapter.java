package com.my.game.wesport.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.game.wesport.FireChatHelper.ExtraIntent;
import com.my.game.wesport.R;
import com.my.game.wesport.model.User;
import com.my.game.wesport.ui.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UsersChatAdapter extends RecyclerView.Adapter<UsersChatAdapter.ViewHolderUsers> {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private final List<User> mUsers;
    private Context mContext;
    private String mCurrentUserEmail;
    private Long mCurrentUserCreatedAt;
    private String mCurrentUserId;

    public UsersChatAdapter(Context context, List<User> fireChatUsers) {
        mUsers = fireChatUsers;
        mContext = context;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(mContext,LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, int position) {

        User fireChatUser = mUsers.get(position);
        Log.d("fireChatUser", fireChatUser.getDisplayName());
        // Set avatar
        Uri mPhotoUri= fireChatUser.getAvatarId();
       // Drawable  avatarDrawable = ContextCompat.getDrawable(mContext,userAvatarId);

        //holder.getUserAvatar().setImageDrawable(avatarDrawable);
        if (mPhotoUri == null) {
            holder.getUserAvatar().setVisibility(View.GONE);
        } else {
            Picasso.with(mContext)
                    .load(mPhotoUri)
                    .into(holder.mUserAvatar);
        }

        // Set display name
        holder.getUserDisplayName().setText(fireChatUser.getDisplayName());

        // Set presence status
        holder.getStatusConnection().setText(fireChatUser.getConnection());

        // Set presence text color
        if(fireChatUser.getConnection().equals(ONLINE)) {
            // Green color
            holder.getStatusConnection().setTextColor(Color.parseColor("#00FF00"));
        }else {
            // Red color
            holder.getStatusConnection().setTextColor(Color.parseColor("#FF0000"));
        }
    }
    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void refill(User users) {
        mUsers.add(users);
        notifyDataSetChanged();
    }

    public void changeUser(int index, User user) {
        mUsers.set(index,user);
        notifyDataSetChanged();
    }

    public void setCurrentUserInfo(String userUid, String email, long createdAt) {
        mCurrentUserId = userUid;
        mCurrentUserEmail = email;
        mCurrentUserCreatedAt = createdAt;

    }

    public void clear() {
        mUsers.clear();
    }


    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder implements View.OnClickListener{

        private final ImageView mUserAvatar;
        private TextView mUserDisplayName;
        private final TextView mStatusConnection;
        private Context mContextViewHolder;

        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            mUserAvatar = (ImageView)itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView)itemView.findViewById(R.id.text_view_display_name);
            mStatusConnection = (TextView)itemView.findViewById(R.id.text_view_connection_status);
            mContextViewHolder = context;
            itemView.setOnClickListener(this);
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


        @Override
        public void onClick(View view) {
            try {
                String chatRef = null;
                User user = mUsers.get(getLayoutPosition());
                Log.d("mUserDisplayName", String.valueOf(user.getDisplayName()));
                if (user != null && mCurrentUserCreatedAt != null && mCurrentUserEmail != null) {
                    chatRef = user.createUniqueChatRef(mCurrentUserCreatedAt, mCurrentUserEmail);
                }
                Intent chatIntent = new Intent(mContextViewHolder, ChatActivity.class);
                chatIntent.putExtra(ExtraIntent.EXTRA_CURRENT_USER_ID, mCurrentUserId);
                chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_ID, user.getRecipientId());
                chatIntent.putExtra(ExtraIntent.EXTRA_CHAT_REF, chatRef);
                chatIntent.putExtra(ExtraIntent.EXTRA_RECIPIENT_USERNAME, user.getDisplayName());

                // Start new activity
                mContextViewHolder.startActivity(chatIntent);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
