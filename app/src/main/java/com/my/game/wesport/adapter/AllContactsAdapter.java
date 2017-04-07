package com.my.game.wesport.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.game.wesport.R;
import com.my.game.wesport.model.LocaleContact;

import java.util.List;

public class AllContactsAdapter extends RecyclerView.Adapter<AllContactsAdapter.ContactViewHolder>{

    private List<LocaleContact> contactVOList;
    private Context mContext;
    private SimpleContactListListener listener;


    public AllContactsAdapter(List<LocaleContact> contactVOList, Context mContext, SimpleContactListListener listener){
        this.contactVOList = contactVOList;
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    public ContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.simple_user_list_item, null);
        ContactViewHolder contactViewHolder = new ContactViewHolder(view);
        return contactViewHolder;
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder,final int position) {
        final LocaleContact localeContact = contactVOList.get(position);
        holder.tvContactName.setText(localeContact.getContactName());
        holder.tvPhoneNumber.setText(localeContact.getContactNumber());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onContactClick(position, localeContact);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return contactVOList.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder{

        ImageView ivContactImage;
        TextView tvContactName;
        TextView tvPhoneNumber;

        public ContactViewHolder(View itemView) {
            super(itemView);
            ivContactImage = (ImageView) itemView.findViewById(R.id.img_avatar);
            tvContactName = (TextView) itemView.findViewById(R.id.text_view_display_name);
            tvPhoneNumber = (TextView) itemView.findViewById(R.id.text_view_email);
        }
    }

    public interface SimpleContactListListener {
        void onContactClick(int position, LocaleContact localeContact);
    }
}
