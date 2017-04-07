package com.my.game.wesport.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.my.game.wesport.POJO.Example;
import com.my.game.wesport.POJO.ParkModel;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.GameEditActivity;
import com.my.game.wesport.activity.GroupActivity;
import com.my.game.wesport.adapter.GameAdapter;
import com.my.game.wesport.api.RetrofitMaps;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.DataSnapWithFlag;
import com.my.game.wesport.model.GameModel;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;


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
        gamesRef.addChildEventListener(gamesChildEventListener);
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
        if (snapshot.getDataSnapshot().getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
            startActivity(GameEditActivity.newIntent(getActivity(), snapshot.getDataSnapshot()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void getAllNearbyPlaces() {
        //Instiantiate background task to download places list and address list for respective locations
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Double mLat = Double.parseDouble(preferences.getString("latitude", ""));
        Double mLon = Double.parseDouble(preferences.getString("longtitude", ""));
        build_retrofit_and_get_response(getString((R.string.type_param)), mLat, mLon);
    }

    //          get all nearby places
    private void build_retrofit_and_get_response(String type, double mLat, double mLon) {
        String url = "https://maps.googleapis.com/maps/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        RetrofitMaps service = retrofit.create(RetrofitMaps.class);
        Call<Example> call = service.getNearbyPlaces(type, mLat + "," + mLon, PROXIMITY_RADIUS, OPEN_NOW);
        call.enqueue(new Callback<Example>() {
            @Override
            public void onResponse(Response<Example> response, Retrofit retrofit) {
                try {
                    // This loop will go through all the parkModels and add marker on each location.
                    nearbyParkModels = response.body().getParkModels();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "onResponse: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (gamesChildEventListener != null) {
            gamesRef.removeEventListener(gamesChildEventListener);
        }
    }
}
