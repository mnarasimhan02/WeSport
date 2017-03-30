package com.my.game.wesport.adapter;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.DateHelper;
import com.my.game.wesport.model.EventModel;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {
    private List<DataSnapshot> dataSnapshots;
    private final Context mContext;
    private EventAdapterListener listener;

    public EventAdapter(Context mContext, List<DataSnapshot> dataSnapshots, EventAdapterListener listener) {
        this.mContext = mContext;
        this.dataSnapshots = dataSnapshots;
        this.listener = listener;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EventAdapter.EventViewHolder(mContext, LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, final int position) {
        final DataSnapshot dataSnapshot = dataSnapshots.get(position);
        EventModel model = dataSnapshot.getValue(EventModel.class);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onEventClick(position, dataSnapshot);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (listener != null) {
                    listener.onEventLongClick(position, dataSnapshot);
                }
                return true;
            }
        });
        //Set event Title
        holder.getEventTitle().setText(model.getTitle());

        // Set display description
        holder.getEventDescription().setText(model.getDescription());

        //Set start time
        holder.getEventStartTime().setText(model.getTime());

        try {
            Date eventDate = DateHelper.getServerDateFormatter().parse(model.getStartDate());
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(eventDate);

            String appDateFormat = DateHelper.getAppDateFormatter().format(eventDate);
            //Set start date
            holder.getEventStartDate().setText(appDateFormat);

            int dateOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
            String dayOfWeek = DateHelper.getWeekDay(calendar.get(Calendar.DAY_OF_WEEK));
            if (!TextUtils.isEmpty(dayOfWeek)) {
                dayOfWeek = dayOfWeek.substring(0, 3);
            }

            //Set start image date
            holder.getEventImageDate().setText(String.valueOf(dateOfMonth));


            //Set start image day
            holder.getEventImageDay().setText(dayOfWeek);

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return dataSnapshots.size();
    }

    public void update(DataSnapshot dataSnapshot) {
        for (int i = 0; i < dataSnapshots.size(); i++) {
            if (dataSnapshots.get(i).getKey().equals(dataSnapshot.getKey())) {
                dataSnapshots.set(i, dataSnapshot);
                notifyItemChanged(i);
            }
        }
    }

    public void remove(DataSnapshot dataSnapshot) {
        for (int i = 0; i < dataSnapshots.size(); i++) {
            if (dataSnapshots.get(i).getKey().equals(dataSnapshot.getKey())) {
                dataSnapshots.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public void add(DataSnapshot dataSnapshot) {
        dataSnapshots.add(dataSnapshot);
        notifyItemInserted(dataSnapshots.size() - 1);
    }


    public class EventViewHolder extends RecyclerView.ViewHolder {
        private TextView eventTitleTextView;
        private TextView eventDesTextView;
        private TextView eventStartDateTextView;
        private TextView eventStartTimeTextView;
        private TextView eventImageDayTextView;
        private TextView eventImageDateTextView;
        private final Context mContextViewHolder;

        public EventViewHolder(Context context, View itemView) {
            super(itemView);
            eventTitleTextView = (TextView) itemView.findViewById(R.id.event_Title);
            eventDesTextView = (TextView) itemView.findViewById(R.id.event_description);
            eventStartDateTextView = (TextView) itemView.findViewById(R.id.event_start_date);
            eventStartTimeTextView = (TextView) itemView.findViewById(R.id.event_start_time);
            eventImageDayTextView = (TextView) itemView.findViewById(R.id.event_image_day);
            eventImageDateTextView = (TextView) itemView.findViewById(R.id.event_image_date);
            mContextViewHolder = context;
        }

        public TextView getEventTitle() {
            return eventTitleTextView;
        }

        public TextView getEventDescription() {
            return eventDesTextView;
        }

        public TextView getEventStartDate() {
            return eventStartDateTextView;
        }

        public TextView getEventStartTime() {
            return eventStartTimeTextView;
        }

        public TextView getEventImageDate() {
            return eventImageDateTextView;
        }

        public TextView getEventImageDay() {
            return eventImageDayTextView;
        }
    }

    public interface EventAdapterListener {
        void onEventClick(int position, DataSnapshot snapshot);

        void onEventLongClick(int position, DataSnapshot snapshot);
    }
}
