package com.my.game.wesport.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.POJO.ParkModel;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.GameEditActivity;
import com.my.game.wesport.activity.GroupActivity;
import com.my.game.wesport.adapter.GameAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.DataSnapWithFlag;
import com.my.game.wesport.model.GameInviteModel;
import com.my.game.wesport.model.GameModel;

import java.util.ArrayList;
import java.util.List;


public class DashboardFragment extends android.app.Fragment implements GameAdapter.GameAdapterListener {
    private RecyclerView mRecyclerView;
    private String placeId;

    public static final int TYPE_USER_GAMES = 1;
    public static final int TYPE_ALL_GAMES = 2;
    public static final int TYPE_ACCEPTED_GAMES = 3;

    private int PROXIMITY_RADIUS = 3000;
    private String OPEN_NOW = "true";
    private final String TAG = "MapActivity";
    private View emptyView;

    //    this is for all games

    private List<ParkModel> nearbyParkModels = new ArrayList<>();
    private GameAdapter adapter;
    private DatabaseReference gamesRef;
    private ChildEventListener gamesChildEventListener;
    private double lat, lon;

    public static DashboardFragment newInstance() {
        Bundle args = new Bundle();
        DashboardFragment fragment = new DashboardFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);
        super.onCreate(savedInstanceState);

        gamesRef = FirebaseHelper.getGamesRef(FirebaseHelper.getCurrentUser().getUid());

        // Find the ListView which will be populated with the game data
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.dashboard_recyclerView);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = rootView.findViewById(R.id.empty_view);

        //Initialising adMob
        MobileAds.initialize(getActivity().getApplicationContext(), String.valueOf(R.string.admob_app_id));
        AdView mAdView = (AdView) rootView.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice("")
                .build();
        mAdView.loadAd(adRequest);

//        getAllNearbyPlaces();

        setUserRecyclerView();
        // setHasOptionsMenu(true);

        return rootView;
    }

    private void setUserRecyclerView() {
        //show edit action only if not all games
        adapter = new GameAdapter(getActivity(), this, new ArrayList<DataSnapWithFlag>(), true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);

        gamesChildEventListener = getGamesChildEventListener();
        FirebaseHelper.getInvitesRef(FirebaseHelper.getCurrentUser().getUid()).addChildEventListener(getInvitedGamesChildEventListener());
        gamesRef.addChildEventListener(gamesChildEventListener);
    }

    @NonNull
    private ChildEventListener getInvitedGamesChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot inviteSnapShot, String s) {
                GameInviteModel invite = inviteSnapShot.getValue(GameInviteModel.class);
                if (invite.isAccepted()) {
                    addInvitedGameToAdapter(invite);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot inviteSnapShot, String s) {
                GameInviteModel invite = inviteSnapShot.getValue(GameInviteModel.class);
                if (!invite.isAccepted()) {
                    adapter.remove(invite.getGameKey(), false);
                } else if (invite.isAccepted()) {
                    addInvitedGameToAdapter(invite);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot inviteSnapShot) {
                GameInviteModel invite = inviteSnapShot.getValue(GameInviteModel.class);
                FirebaseHelper.getGamesRef(invite.getAuthorUid()).child(invite.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot gameSnapshot) {
                        adapter.remove(new DataSnapWithFlag(gameSnapshot, false), false);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }

    private void addInvitedGameToAdapter(GameInviteModel invite) {
        FirebaseHelper.getGamesRef(invite.getAuthorUid()).child(invite.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot gameSnapshot) {
//                add only if not already exists
                if (adapter.indexOf(gameSnapshot.getKey()) == -1) {
                    adapter.add(new DataSnapWithFlag(gameSnapshot, false), false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @NonNull
    private ChildEventListener getGamesChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                    return;
                }

                emptyView.setVisibility(View.GONE);
//                GameModel gameModel = dataSnapshot.getValue(GameModel.class);
                adapter.add(new DataSnapWithFlag(dataSnapshot, false), false);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (!dataSnapshot.getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                    return;
                }
                if (dataSnapshot.getValue() != null) {
                    adapter.update(new DataSnapWithFlag(dataSnapshot, false), false);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove(new DataSnapWithFlag(dataSnapshot, false), false);
                if (adapter.getItemCount() > 0) {
                    emptyView.setVisibility(View.GONE);
                } else {
                    emptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onGameClick(int position, DataSnapWithFlag snapshot) {
        GameModel gameModel = snapshot.getDataSnapshot().getValue(GameModel.class);
        startActivity(GroupActivity.newIntent(getActivity(), snapshot.getDataSnapshot().getKey(), gameModel.getAuthor()));
    }

    @Override
    public void onGameEditClick(int position, DataSnapWithFlag snapshot) {
        GameModel gameModel = snapshot.getDataSnapshot().getValue(GameModel.class);
        if (gameModel.getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
            startActivity(GameEditActivity.newIntent(getActivity(), null, snapshot.getDataSnapshot(), lat, lon, null));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gamesChildEventListener != null) {
            gamesRef.removeEventListener(gamesChildEventListener);
        }
    }
}
