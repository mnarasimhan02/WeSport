package com.my.game.wesport.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.my.game.wesport.R;
import com.my.game.wesport.model.ChatMessage;

import java.util.List;

public class MessageChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ChatMessage> mChatList;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;

    public MessageChatAdapter(List<ChatMessage> listOfFireChats) {
        mChatList = listOfFireChats;
    }

    @Override
    public int getItemViewType(int position) {
        if(mChatList.get(position).getRecipientOrSenderStatus()==SENDER){
            return SENDER;
        }else {
            return RECIPIENT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case SENDER:
                View viewSender = inflater.inflate(R.layout.layout_sender_message, viewGroup, false);
                viewHolder= new ViewHolderSender(viewSender);
                break;
            case RECIPIENT:
                View viewRecipient = inflater.inflate(R.layout.layout_recipient_message, viewGroup, false);
                viewHolder=new ViewHolderRecipient(viewRecipient);
                break;
            default:
                View viewSenderDefault = inflater.inflate(R.layout.layout_sender_message, viewGroup, false);
                viewHolder= new ViewHolderSender(viewSenderDefault);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        switch (viewHolder.getItemViewType()){
            case SENDER:
                ViewHolderSender viewHolderSender=(ViewHolderSender)viewHolder;
                configureSenderView(viewHolderSender,position);
                break;
            case RECIPIENT:
                ViewHolderRecipient viewHolderRecipient=(ViewHolderRecipient)viewHolder;
                configureRecipientView(viewHolderRecipient,position);
                break;
        }
    }

    private void configureSenderView(ViewHolderSender viewHolderSender, int position) {
        ChatMessage senderFireMessage= mChatList.get(position);
        viewHolderSender.getSenderMessageTextView().setText(senderFireMessage.getMessage());
        boolean isPhoto = senderFireMessage.getPhotoUrl() != null;
        if (isPhoto) {
            viewHolderSender.getSenderMessageTextView().setVisibility(View.GONE);
           // viewHolderSender.getSenderphotoImageView().setVisibility(View.VISIBLE);
            Glide.with(viewHolderSender.getSenderphotoImageView().getContext())
                    .load(senderFireMessage.getPhotoUrl())
                    .into(viewHolderSender.mSenderphotoImageView);
        } else {
            viewHolderSender.getSenderMessageTextView().setVisibility(View.VISIBLE);
//            viewHolderSender.getSenderphotoImageView().setVisibility(View.GONE);
            viewHolderSender.getSenderMessageTextView().setText(senderFireMessage.getMessage());
        }
    }

    private void configureRecipientView(ViewHolderRecipient viewHolderRecipient, int position) {
        ChatMessage recipientFireMessage = mChatList.get(position);
        viewHolderRecipient.getRecipientMessageTextView().setText(recipientFireMessage.getMessage());
        boolean isPhoto = recipientFireMessage.getPhotoUrl() != null;
        if (isPhoto) {
            viewHolderRecipient.getRecipientMessageTextView().setVisibility(View.GONE);
            //viewHolderRecipient.getReceiverphotoImageView().setVisibility(View.VISIBLE);
            Glide.with(viewHolderRecipient.getReceiverphotoImageView().getContext())
                    .load(recipientFireMessage.getPhotoUrl())
                    .into(viewHolderRecipient.mReceiverphotoImageView);
        } else {
            viewHolderRecipient.getRecipientMessageTextView().setVisibility(View.VISIBLE);
            //viewHolderRecipient.getReceiverphotoImageView().setVisibility(View.GONE);
            viewHolderRecipient.getRecipientMessageTextView().setText(recipientFireMessage.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        Log.d("adapter size", String.valueOf(mChatList.size()));
        return mChatList.size();

    }


    public void refillAdapter(ChatMessage newFireChatMessage){

        /*add new message chat to list*/
        mChatList.add(newFireChatMessage);
        Log.d("Message Chat adapter", "newFireChatMessage");

        /*refresh view*/
        notifyItemInserted(getItemCount()-1);
        Log.d("refresh", "refresh");

    }


    public void cleanUp() {
        mChatList.clear();
    }


    /*==============ViewHolder===========*/

    /*ViewHolder for Sender*/

    public class ViewHolderSender extends RecyclerView.ViewHolder {

        private TextView mSenderMessageTextView;
        private ImageView mSenderphotoImageView;

        public ViewHolderSender(View itemView) {
            super(itemView);
            mSenderMessageTextView =(TextView)itemView.findViewById(R.id.text_view_sender_message);
            mSenderphotoImageView = (ImageView)itemView.findViewById(R.id.photoPickerButton);
        }
        public TextView getSenderMessageTextView() {
            return mSenderMessageTextView;
        }
        public ImageView getSenderphotoImageView() {
            return mSenderphotoImageView;
        }
    }

    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder {

        private TextView mRecipientMessageTextView;
        private ImageView mReceiverphotoImageView;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            mRecipientMessageTextView=(TextView)itemView.findViewById(R.id.text_view_recipient_message);
            mReceiverphotoImageView = (ImageView) itemView.findViewById(R.id.photoPickerButton);
        }
        public TextView getRecipientMessageTextView() {
            return mRecipientMessageTextView;
        }
        public ImageView getReceiverphotoImageView() {
            return mReceiverphotoImageView;
        }

    }
}