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
import com.my.game.wesport.model.UserListItem;
import com.my.game.wesport.model.UserModel;

import java.util.ArrayList;
import java.util.List;


public class SimpleUserListAdapter extends RecyclerView.Adapter<SimpleUserListAdapter.ViewHolderUsers> {
    private List<UserListItem> chatListItems = new ArrayList<>();
    private final Context mContext;
    private SimpleUserListListener listener;

    public SimpleUserListAdapter(Context context, SimpleUserListListener listener) {
        mContext = context;
        this.listener = listener;
    }

    @Override
    public ViewHolderUsers onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolderUsers(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.simple_user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolderUsers holder, final int position) {

        final UserListItem chatListItem = chatListItems.get(position);
        UserModel fireChatUserModel = chatListItem.getUser();

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
        holder.mUserDisplayName.setText(fireChatUserModel.getDisplayName());

        holder.email.setText(fireChatUserModel.getEmail());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onUserClick(position, chatListItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatListItems.size();
    }

    public void add(UserListItem chatListItem) {
        chatListItems.add(chatListItem);
        notifyItemInserted(chatListItems.size() - 1);
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

    public void updateList(List<UserListItem> users) {
        chatListItems = users;
        notifyDataSetChanged();
    }

    /* ViewHolder for RecyclerView */
    public class ViewHolderUsers extends RecyclerView.ViewHolder {

        public final ImageView mUserAvatar;
        public final TextView mUserDisplayName;
        public final TextView email;


        public ViewHolderUsers(View itemView) {
            super(itemView);
            mUserAvatar = (ImageView) itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView) itemView.findViewById(R.id.text_view_display_name);
            email = (TextView) itemView.findViewById(R.id.text_view_email);
        }
    }

    public interface SimpleUserListListener {
        void onUserClick(int position, UserListItem userListItem);
    }
}