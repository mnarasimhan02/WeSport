package com.my.game.wesport;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.my.game.wesport.adapter.EventAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.EventModel;

import java.util.ArrayList;

public class EventListFragment extends Fragment implements EventAdapter.EventAdapterListener {
    public static final String EXTRA_GAMES_KEY = "game_key";

    private RecyclerView mRecyclerView;
    private EventAdapter adapter;
    private String gameKey;

    public static EventListFragment newInstance(String gameKey) {
        Bundle args = new Bundle();
        args.putString(EXTRA_GAMES_KEY, gameKey);
        EventListFragment fragment = new EventListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_event_list, container, false);
        super.onCreate(savedInstanceState);

        gameKey = getArguments().getString(EXTRA_GAMES_KEY, "");

        // Setup FAB to open GameEditActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.event_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(EventEditActivity.newIntent(getContext(), gameKey, null));
            }
        });
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.event_recyclerView);

        setUserRecyclerView();

        return rootView;
    }

    private void setUserRecyclerView() {
        //to be updated
        adapter = new EventAdapter(getActivity(), new ArrayList<DataSnapshot>(), this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);

        FirebaseHelper.getEventRef(gameKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    adapter.add(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    adapter.update(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onEventClick(int position, DataSnapshot snapshot) {
        EventModel eventModel = snapshot.getValue(EventModel.class);
        if (eventModel.getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
            startActivity(EventEditActivity.newIntent(getContext(), gameKey, snapshot));
        }
    }

    @Override
    public void onEventLongClick(int position, final DataSnapshot snapshot) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Do you want to Remove this Event?");

            String positiveText = getString(android.R.string.ok);
            builder.setPositiveButton(positiveText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                                EventModel eventModel = snapshot.getValue(EventModel.class);
                            if (eventModel.getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                                snapshot.getRef().removeValue();
                                dialog.dismiss();
                            }
                        }
                    });

            String negativeText = getString(android.R.string.cancel);
            builder.setNegativeButton(negativeText,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            AlertDialog dialog = builder.create();
            // display dialog
            dialog.show();

    }


}
