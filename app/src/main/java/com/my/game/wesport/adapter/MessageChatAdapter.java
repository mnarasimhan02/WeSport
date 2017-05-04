package com.my.game.wesport.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.ChatMessage;

import java.util.List;

public class MessageChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<DataSnapshot> mChatDataSnapshotList;
    public static final int SENDER_MESSAGE = 0;
    public static final int SENDER_ATTACHMENT = 1;
    public static final int RECIPIENT_MESSAGE = 2;

    public static final int RECIPIENT_ATTACHMENT = 3;

    private ChatListener chatListener;

    public MessageChatAdapter(List<DataSnapshot> listOfFireChats) {
        mChatDataSnapshotList = listOfFireChats;
    }

    public void setChatListener(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage chatMessage = mChatDataSnapshotList.get(position).getValue(ChatMessage.class);
        if (chatMessage.getSender().equals(FirebaseHelper.getCurrentUserId())) {
            if (chatMessage.isAttachment()) {
                return SENDER_ATTACHMENT;
            } else {
                return SENDER_MESSAGE;
            }
        } else {
            if (chatMessage.isAttachment()) {
                return RECIPIENT_ATTACHMENT;
            } else {
                return RECIPIENT_MESSAGE;
            }
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case SENDER_MESSAGE:
                View viewSender = inflater.inflate(R.layout.layout_sender_message, viewGroup, false);
                viewHolder = new ViewHolderSender(viewSender);
                break;
            case RECIPIENT_MESSAGE:
                View viewRecipient = inflater.inflate(R.layout.layout_recipient_message, viewGroup, false);
                viewHolder = new ViewHolderRecipient(viewRecipient);
                break;
            case RECIPIENT_ATTACHMENT:
                View viewRecipientAttachment = inflater.inflate(R.layout.layout_recipient_attachment, viewGroup, false);
                viewHolder = new ViewHolderAttachment(viewRecipientAttachment);
                break;
            case SENDER_ATTACHMENT:
                View viewSenderAttachment = inflater.inflate(R.layout.layout_sender_attachment, viewGroup, false);
                viewHolder = new ViewHolderAttachment(viewSenderAttachment);
                break;
            default:
                View viewSenderDefault = inflater.inflate(R.layout.layout_sender_message, viewGroup, false);
                viewHolder = new ViewHolderSender(viewSenderDefault);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final DataSnapshot dataSnapshot = mChatDataSnapshotList.get(position);
        switch (viewHolder.getItemViewType()) {
            case SENDER_MESSAGE:
                ViewHolderSender viewHolderSender = (ViewHolderSender) viewHolder;
                configureSenderView(viewHolderSender, position);
                break;
            case RECIPIENT_MESSAGE:
                ViewHolderRecipient viewHolderRecipient = (ViewHolderRecipient) viewHolder;
                configureRecipientView(viewHolderRecipient, position);
                break;
            case SENDER_ATTACHMENT:
                ViewHolderAttachment viewHolderSenderAttachment = (ViewHolderAttachment) viewHolder;
                configureAttachmentView(viewHolderSenderAttachment, position);
                break;
            case RECIPIENT_ATTACHMENT:
                ViewHolderAttachment viewHolderRecipientAttachment = (ViewHolderAttachment) viewHolder;
                configureAttachmentView(viewHolderRecipientAttachment, position);
                break;
        }

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chatListener != null) {
                    chatListener.onMessageClick(viewHolder.getAdapterPosition(), dataSnapshot);
                }
            }
        });
    }

    private void configureSenderView(ViewHolderSender viewHolderSender, int position) {
        ChatMessage senderFireMessage = mChatDataSnapshotList.get(position).getValue(ChatMessage.class);
        viewHolderSender.getSenderMessageTextView().setText(senderFireMessage.getMessage());
        viewHolderSender.getSenderMessageTextView().setVisibility(View.VISIBLE);

    }

    private void configureRecipientView(ViewHolderRecipient viewHolderRecipient, int position) {
        ChatMessage recipientFireMessage = mChatDataSnapshotList.get(position).getValue(ChatMessage.class);
        viewHolderRecipient.getRecipientMessageTextView().setText(recipientFireMessage.getMessage());
        viewHolderRecipient.getRecipientMessageTextView().setVisibility(View.VISIBLE);
    }

    private void configureAttachmentView(ViewHolderAttachment viewHolderAttachment, int position) {
        ChatMessage chatMessage = mChatDataSnapshotList.get(position).getValue(ChatMessage.class);
        final ProgressBar progressBar = viewHolderAttachment.progressBar;
        progressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(chatMessage.getMessage())) {
//            image uploading
            Glide
                    .with(viewHolderAttachment.itemView.getContext())
                    .load(chatMessage.getMessage())
                    .placeholder(R.drawable.image_placeholder_drawable)
                    .error(R.drawable.image_placeholder_drawable)
                    .listener(new RequestListener<String, GlideDrawable>() {

                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(viewHolderAttachment.imageView);
        } else {
            Glide.with(viewHolderAttachment.itemView.getContext()).load(R.drawable.image_placeholder_drawable).into(viewHolderAttachment.imageView);
        }

    }

    @Override
    public int getItemCount() {
        return mChatDataSnapshotList.size();
    }


    public void add(DataSnapshot chatDataSnapshot) {

        /*add new message chat to list*/
        mChatDataSnapshotList.add(chatDataSnapshot);
        /*refresh view*/
        notifyItemInserted(getItemCount() - 1);
    }

    public void update(DataSnapshot chatDataSnapshot) {

        int index = indexOf(chatDataSnapshot.getKey());
        if (index != -1) {
            /*add new message chat to list*/
            mChatDataSnapshotList.set(index, chatDataSnapshot);
            /*refresh view*/
            notifyItemChanged(index);
        }
    }

    public int indexOf(String key) {
        for (int i = 0; i < mChatDataSnapshotList.size(); i++) {
            if (mChatDataSnapshotList.get(i).getKey().equals(key)) {
                return i;
            }
        }

        return -1;
    }


    public void cleanUp() {
        mChatDataSnapshotList.clear();
    }


    /*==============ViewHolder===========*/

    /*ViewHolder for Sender*/

    public class ViewHolderSender extends RecyclerView.ViewHolder {

        private final TextView mSenderMessageTextView;

        public ViewHolderSender(View itemView) {
            super(itemView);
            mSenderMessageTextView = (TextView) itemView.findViewById(R.id.text_view_sender_message);
        }

        public TextView getSenderMessageTextView() {
            return mSenderMessageTextView;
        }
    }

    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder {

        private final TextView mRecipientMessageTextView;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            mRecipientMessageTextView = (TextView) itemView.findViewById(R.id.text_view_recipient_message);
        }

        public TextView getRecipientMessageTextView() {
            return mRecipientMessageTextView;
        }
    }


    public class ViewHolderAttachment extends RecyclerView.ViewHolder {

        public final ImageView imageView;
        public final ProgressBar progressBar;

        public ViewHolderAttachment(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.image);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }

    public interface ChatListener {
        void onMessageClick(int position, DataSnapshot chatDataSnapshot);
    }
}