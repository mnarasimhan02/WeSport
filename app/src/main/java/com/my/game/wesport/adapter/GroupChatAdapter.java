package com.my.game.wesport.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.my.game.wesport.R;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.ChatMessage;
import com.my.game.wesport.model.GroupChatModel;

import java.util.List;

public class GroupChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private final List<GroupChatModel> groupChatList;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;

    public GroupChatAdapter(List<GroupChatModel> chatList) {
        groupChatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        if(groupChatList.get(position).getSenderId().equals(FirebaseHelper.getCurrentUserId())){
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
                View viewRecipient = inflater.inflate(R.layout.layout_recipient_message_group_chat, viewGroup, false);
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
        GroupChatModel senderFireMessage= groupChatList.get(position);
            viewHolderSender.getSenderMessageTextView().setText(senderFireMessage.getMessage());
            viewHolderSender.getSenderMessageTextView().setVisibility(View.VISIBLE);

    }

    private void configureRecipientView(ViewHolderRecipient viewHolderRecipient, int position) {
        GroupChatModel recipientFireMessage = groupChatList.get(position);
        viewHolderRecipient.getRecipientMessageTextView().setText(recipientFireMessage.getMessage());
        viewHolderRecipient.getRecipientUserNameTextView().setText(recipientFireMessage.getSenderName());
        viewHolderRecipient.getRecipientMessageTextView().setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return groupChatList.size();
    }


    public void add(GroupChatModel chatModel){

        /*add new message chat to list*/
        groupChatList.add(chatModel);
        /*refresh view*/
        notifyItemInserted(getItemCount()-1);
    }


    public void cleanUp() {
        groupChatList.clear();
    }


    /*==============ViewHolder===========*/

    /*ViewHolder for Sender*/

    public class ViewHolderSender extends RecyclerView.ViewHolder {

        private final TextView mSenderMessageTextView;

        public ViewHolderSender(View itemView) {
            super(itemView);
            mSenderMessageTextView =(TextView)itemView.findViewById(R.id.text_view_sender_message);
        }
        public TextView getSenderMessageTextView() {
            return mSenderMessageTextView;
        }
    }

    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder {

        private final TextView mRecipientMessageTextView;
        private final TextView mRecipientUserNameTextView;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            mRecipientUserNameTextView =(TextView)itemView.findViewById(R.id.recipient_group_chat_userName);
            mRecipientMessageTextView=(TextView)itemView.findViewById(R.id.text_view_recipient_message);

        }
        public TextView getRecipientMessageTextView() {
            return mRecipientMessageTextView;
        }

        public TextView getRecipientUserNameTextView(){  return mRecipientUserNameTextView; }
    }
}