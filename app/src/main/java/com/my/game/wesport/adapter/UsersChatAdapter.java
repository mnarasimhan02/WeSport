package com.my.game.wesport.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.my.game.wesport.FireChatHelper.ChatHelper;
import com.my.game.wesport.FireChatHelper.ExtraIntent;
import com.my.game.wesport.R;
import com.my.game.wesport.model.User;
import com.my.game.wesport.ui.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.facebook.FacebookSdk.getApplicationContext;


public class UsersChatAdapter extends RecyclerView.Adapter<UsersChatAdapter.ViewHolderUsers>  {

    public static final String ONLINE = "online";
    public static final String OFFLINE = "offline";
    private final List<User> mUsers;
    private Context mContext;
    private String mCurrentUserEmail;
    private Long mCurrentUserCreatedAt;
    private String mCurrentUserId,mPhotoUrl;
    private String mlatitude;
    private String mlongitude;
    private String mUserdistance;
    private double mLat;
    private double mLon;

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

        int userAvatarId= ChatHelper.getDrawableAvatarId(fireChatUser.getNonAvatarId());
        // Set avatar
        String mPhotoUri;
        mPhotoUri = fireChatUser.getPhotoUri();
        try {
            if (mPhotoUri.equals("null")) {
                Drawable avatarDrawable = ContextCompat.getDrawable(mContext, userAvatarId);
                holder.getUserAvatar().setImageDrawable(avatarDrawable);
            } else {
                Picasso.with(mContext)
                        .load(mPhotoUri)
                        .into(holder.mUserAvatar);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        // Set display name
        holder.getUserDisplayName().setText(fireChatUser.getDisplayName());

        //Arrays.sort(mUsers, UsersChatAdapter.distComparator);

        // Set Location to distance
        holder.getUserLocation().setText(
                getDistance(fireChatUser.getLatitude(),fireChatUser.getLongitude())
                        + " " + mContext.getString(R.string.miles_away));

        //holder.getUserLocation().setText(getDistance(fireChatUser.getLatitude(),fireChatUser.getLongitude())+ " " + mContext.getString(R.string.miles_away));

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

    private String getDistance(String lat, String lon) {
        double distance = 0;
        Location mCurrentLocation = new Location("mCurrentLocation");
        //SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //mLat = Double.parseDouble(preferences.getString("latitude", ""));
        //mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        mCurrentLocation.setLatitude(mLat);
        mCurrentLocation.setLongitude(mLon);
        Log.d("distance1.mLat", String.valueOf(mLat));
        Log.d("distance1.mLon", String.valueOf(mLon));
        //compareDistance(mUsers, mLat, mLon);
        Location newLocation = new Location("newlocation");
        if ( lat != null || lon != null ) {
            newLocation.setLatitude(Double.parseDouble(lat));
            newLocation.setLongitude(Double.parseDouble(lon));
            distance =  ((mCurrentLocation.distanceTo(newLocation) / 1000)/1.6); // in miles
            return String.format(Locale.US, "%.0f", distance);
        }
        return String.format(Locale.US, "%.0f", distance);
    }

    private static List<User> compareDistance(List<User> mUsers, final double mLat,final double mLon) {
        Comparator<User> distance = new Comparator<User>() {
            @Override
            public int compare(User o, User o2) {
                float[] result1 = new float[3];
                Float distance1 = result1[0];
                if (o.getLatitude()!=null && o.getLongitude()!=null) {
                    android.location.Location.distanceBetween(mLat, mLon,
                            o.getLatitude() != null ? Double.parseDouble(o.getLatitude()) : 0,
                            o.getLongitude() != null ? Double.parseDouble(o.getLongitude()) : 0, result1);
                    distance1 = result1[0];
                }
                float[] result2 = new float[3];
                Float distance2 = null;
                    if (o2.getLatitude() != null && o2.getLongitude() != null ) {
                        android.location.Location.distanceBetween(mLat, mLon,
                                o2.getLatitude() != null ? Double.parseDouble(o2.getLatitude()) : 0,
                                o2.getLongitude() != null ? Double.parseDouble(o2.getLongitude()) : 0, result2);
                        distance2 = result2[0];
                    }
                    //Log.d("distance1.compareTo", String.valueOf(distance1.compareTo(distance2)));
                 else if((o2.getLatitude() == null || o2.getLongitude() == null )){
                    distance2= Float.valueOf(0);
                }
                Log.d("distance1.compareTo", String.valueOf(distance1.compareTo(distance2)));
                return distance1.compareTo(distance2);
            }
        };
        Collections.sort(mUsers, distance);
        return mUsers;
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public void refill(User users) {
        mUsers.add(users);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mLat = Double.parseDouble(preferences.getString("latitude", ""));
        mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        compareDistance(mUsers, mLat, mLon);
        Log.d("compareDistance.mUsers", String.valueOf(mUsers));
        Log.d("users", String.valueOf(mUsers.size()));
        notifyDataSetChanged();
    }

    public void changeUser(int index, User user) {
        mUsers.set(index,user);
        notifyDataSetChanged();
    }

    public void setCurrentUserInfo(String userUid, String email, long createdAt, String photoUri,String latitude,
                                   String longitude, String distance) {
        mCurrentUserId = userUid;
        mCurrentUserEmail = email;
        mCurrentUserCreatedAt = createdAt;
        mPhotoUrl=photoUri;
        mlatitude=latitude;
        mlongitude=longitude;
        mUserdistance= distance;
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
        private TextView mStatusLocation;


        public ViewHolderUsers(Context context, View itemView) {
            super(itemView);
            mUserAvatar = (ImageView)itemView.findViewById(R.id.img_avatar);
            mUserDisplayName = (TextView)itemView.findViewById(R.id.text_view_display_name);
            mStatusConnection = (TextView)itemView.findViewById(R.id.text_view_connection_status);
            mStatusLocation = (TextView)itemView.findViewById(R.id.text_view_location);
            mContextViewHolder = context;
            itemView.setOnClickListener(this);
        }

        public ImageView getUserAvatar() {
            return mUserAvatar;
        }
        public ImageView getUserNonAvatar() {
            return mUserAvatar;
        }
        public TextView getUserDisplayName() {return mUserDisplayName;}
        public TextView getStatusConnection() {
            return mStatusConnection;
        }
        public TextView getUserLocation() {
            return mStatusLocation;
        }

        @Override
        public void onClick(View view) {
            try {
                String chatRef = null;
                User user = mUsers.get(getLayoutPosition());
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