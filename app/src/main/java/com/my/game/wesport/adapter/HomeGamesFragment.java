package com.my.game.wesport.adapter;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.POJO.ParkModel;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.GameEditActivity;
import com.my.game.wesport.activity.GroupActivity;
import com.my.game.wesport.activity.MapsActivity;
import com.my.game.wesport.fragment.UserChatListFragment;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.LocationHelper;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.DataSnapWithFlag;
import com.my.game.wesport.model.GameInviteModel;
import com.my.game.wesport.model.GameModel;
import com.my.game.wesport.model.UserModel;
import com.my.game.wesport.ui.ChatActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeGamesFragment extends Fragment implements NewMyGamesAdapter.GameAdapterListener, GeoQueryEventListener, NearbyGamesAdapter.NearbyGameAdapterListener {

    private static final String EXTRA_REQUEST_MESSAGE = "msg";
    private GeoFire geoFire;
    private GeoQuery geoQuery;
    Runnable setupGeoFireRunnable;
    boolean isGeofireInitialized = false;
    private final double GEO_FIRE_RADIUS_KM = 80.4672;
    private Handler mHandler = new Handler();

    private RecyclerView myGamesRecyclerView, nearbyGamesRecyclerView;
    private UserChatListFragment userChatListFragment;

    private static final String EXTRA_TYPE_GAMES = "type_games";
    private static final String EXTRA_TYPE_NEARBY_GAMES = "nearby_type_games";

    private static String EXTRA_PlACE_ID = "place_id";
    private NewMyGamesAdapter myGamesAdapter;
    private NearbyGamesAdapter nearbyAdapter;
    private String placeId;
    public static final int TYPE_USER_GAMES = 1;
    public static final int TYPE_ALL_GAMES = 2;
    public static final int TYPE_ACCEPTED_GAMES = 3;
    public static final int TYPE_NEARBY_GAMES = 4;
    private int PROXIMITY_RADIUS = 3000;
    private String OPEN_NOW = "true";

    private final String TAG = "GameListFragment";
    private List<ParkModel> nearbyParkModels = new ArrayList<>();

    private View emptyView;
    private int listType = TYPE_USER_GAMES;

    //    this is for all games
    private static List<DataSnapshot> allGameSnapshots = new ArrayList<>();
    private double lat;
    private double lon;
    private int nearbyType;
    private UserModel selectedGameUserModel = null;
    private String selectedGameUserId = "";

    public static HomeGamesFragment newInstance(String placeId, int type, int nearbyType) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_TYPE_GAMES, type);
        args.putInt(EXTRA_TYPE_NEARBY_GAMES, nearbyType);
        args.putString(EXTRA_PlACE_ID, placeId);
        HomeGamesFragment fragment = new HomeGamesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_game_list, container, false);
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle.containsKey(EXTRA_TYPE_GAMES)) {
            listType = bundle.getInt(EXTRA_TYPE_GAMES);
            nearbyType = bundle.getInt(EXTRA_TYPE_NEARBY_GAMES);
        }
        if (bundle.containsKey(EXTRA_PlACE_ID)) {
            placeId = bundle.getString(EXTRA_PlACE_ID);
        }

        // Setup FAB to open GameEditActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(GameEditActivity.newIntent(getActivity(), null, null, lat, lon, null));
            }
        });

        fab.setVisibility(listType == TYPE_USER_GAMES ? View.VISIBLE : View.GONE);
        if (listType == TYPE_USER_GAMES) {
            setHasOptionsMenu(true);
        }

        // Find the ListView which will be populated with the game data
        myGamesRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_my_games);

        nearbyGamesRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView_nearby_games);
        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        // emptyView = rootView.findViewById(R.id.empty_view);


        // setting User Games recyclerView
        setUserRecyclerView();

        // setting Nearby Games recyclerView
        setNearbyGamesRecyclerView();

        /*if (listType == TYPE_ACCEPTED_GAMES) {
            emptyView.setVisibility(View.GONE);
        }*/
        // Kick off the loader
        // getLoaderManager().initLoader(GAME_LOADER, null, this);

       /* AdView mAdView = (AdView) rootView.findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("8D55278B12588486D7D396079CB75B6B")
                .build();
        mAdView.loadAd(adRequest);*/

        return rootView;
    }

    private void setUserRecyclerView() {
        //show edit action only if not all games
        myGamesAdapter = new NewMyGamesAdapter(getActivity(), this, new ArrayList<DataSnapWithFlag>(), listType == TYPE_USER_GAMES);
        myGamesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        myGamesRecyclerView.setAdapter(myGamesAdapter);

        // Getting Accepted games
        FirebaseHelper.getInvitesRef(FirebaseHelper.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
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
                    myGamesAdapter.remove(invite.getGameKey(), false);
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
                        myGamesAdapter.remove(new DataSnapWithFlag(gameSnapshot, false), false);
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
        });

        // Getting User's created games
        FirebaseHelper.getGamesRef(FirebaseHelper.getCurrentUser().getUid()).addChildEventListener(getGamesChildEventListener());

    }

    @NonNull
    private ChildEventListener getGamesChildEventListener() {
        return new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (listType == TYPE_USER_GAMES && !dataSnapshot.getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                    return;
                } else if (listType == TYPE_ALL_GAMES && !showGame(dataSnapshot)) {
                    return;
                }
                myGamesAdapter.add(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_ALL_GAMES);
                if (listType == TYPE_ALL_GAMES) {
                    allGameSnapshots.add(dataSnapshot);
                } else {
                    myGamesAdapter.refreshFlags(allGameSnapshots);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (listType == TYPE_USER_GAMES && !dataSnapshot.getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                    return;
                }
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    myGamesAdapter.update(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_ALL_GAMES);
                    if (listType == TYPE_ALL_GAMES) {
                        for (int i = 0; i < allGameSnapshots.size(); i++) {
                            if (allGameSnapshots.get(i).getKey().equals(dataSnapshot.getKey())) {
                                allGameSnapshots.set(i, dataSnapshot);
                            }
                        }
                    } else {
                        myGamesAdapter.refreshFlags(allGameSnapshots);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                myGamesAdapter.remove(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_ALL_GAMES);
                if (listType == TYPE_ALL_GAMES) {
                    for (int i = 0; i < allGameSnapshots.size(); i++) {
                        if (allGameSnapshots.get(i).getKey().equals(dataSnapshot.getKey())) {
                            allGameSnapshots.remove(i);
                        }
                    }
                } else {
                    myGamesAdapter.refreshFlags(allGameSnapshots);
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


    private void addInvitedGameToAdapter(GameInviteModel invite) {
        FirebaseHelper.getGamesRef(invite.getAuthorUid()).child(invite.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot gameSnapshot) {
//                add only if not already exists
                if (myGamesAdapter.indexOf(gameSnapshot.getKey()) == -1) {
                    myGamesAdapter.add(new DataSnapWithFlag(gameSnapshot, false), false);

//                    if game added in my games then remove it from nearby
                    if (nearbyAdapter != null) {
                        nearbyAdapter.remove(gameSnapshot.getKey(), false);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public void setNearbyGamesRecyclerView() {
        nearbyAdapter = new NearbyGamesAdapter(getActivity(), new ArrayList<DataSnapWithFlag>(), this);
        nearbyGamesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        nearbyGamesRecyclerView.setAdapter(nearbyAdapter);
        // FirebaseHelper.getGamesRef(FirebaseHelper.getCurrentUser().getUid()).addChildEventListener(getNearbyGamesChildEventListener());

        //Setting up geoFire Runnable
        setupGeoFireRunnable = new Runnable() {
            @Override
            public void run() {
                setupGeoFire(LocationHelper.getLocationFromPref());
            }
        };
        new Handler().post(setupGeoFireRunnable);
    }

    private boolean showGame(DataSnapshot dataSnapshot) {
        GameModel gameModel = dataSnapshot.getValue(GameModel.class);
        for (ParkModel parkModel : MapsActivity.parkModels) {
            if (gameModel.getParkId().equals(parkModel.getId())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        if (listType == TYPE_USER_GAMES) {
            inflater.inflate(R.menu.menu_catalog, menu);
        }
//        MenuItem item = menu.findItem(R.id.action_delete_all_entries);
//        item.setVisible(!showAllGames);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // UserModel clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                if (listType == TYPE_USER_GAMES) {
                    deleteAllGames();
                }
                return true;
            /*case R.id.action_signout:
                signoutuser();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllGames() {
        FirebaseHelper.getGamesRef(FirebaseHelper.getCurrentUser().getUid()).removeValue();
    }

    private void signoutuser() {
        //FirebaseAuth.getInstance().signOut();
        AuthUI.getInstance()
                .signOut(getActivity())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // user is now signed out
                        getActivity().startActivity(new Intent(getActivity(), SigninActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NEW_TASK));
                        getActivity().finish();
                    }
                });
    }

    @Override
    public void onGameClick(int position, DataSnapWithFlag snapshot) {
        GameModel gameModel = snapshot.getDataSnapshot().getValue(GameModel.class);
        startActivity(GroupActivity.newIntent(getActivity(), snapshot.getDataSnapshot().getKey(), gameModel.getAuthor()));
    }

    @Override
    public void onGameEditClick(int position, DataSnapWithFlag snapshot) {
        if (snapshot.getDataSnapshot().getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
            startActivity(GameEditActivity.newIntent(getActivity(), null, snapshot.getDataSnapshot(), lat, lon, null));
        }
    }

    @Override
    public void onSendInviteClick(int position, DataSnapWithFlag snapshot) {
        final GameModel gameModel = snapshot.getDataSnapshot().getValue(GameModel.class);
        selectedGameUserId = gameModel.getAuthor();
        FirebaseHelper.getUserRef().child(gameModel.getAuthor()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                selectedGameUserModel = dataSnapshot.getValue(UserModel.class);
                Intent intent = new Intent(ChatActivity.newIntent(getActivity(), selectedGameUserModel, selectedGameUserId));
                intent.putExtra(EXTRA_REQUEST_MESSAGE, getString(R.string.join_game_invitation_text, gameModel.getGameDescription()));
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        /*AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle("Do you want to join this Game?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Intent intent = new Intent(ChatActivity.newIntent(getActivity(), selectedGameUserModel, selectedGameUserId));
                intent.putExtra(EXTRA_REQUEST_MESSAGE, getString(R.string.join_game_invitation_text, gameModel.getGameDescription()));
                startActivity(intent);
            }
        });

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
            }
        });
        alert.show();*/


    }

    @Override
    public void onResume() {
        super.onResume();
        if (listType == TYPE_ALL_GAMES && myGamesAdapter != null) {
            myGamesAdapter.refreshFlags(allGameSnapshots);
        }
    }

    private void setupGeoFire(LatLng latLng) {
        if (latLng == null || (latLng.latitude == 0.0 && latLng.longitude == 0.0)) {
            new Handler().postDelayed(setupGeoFireRunnable, 2000);
            return;
        }

        geoFire = new GeoFire(FirebaseHelper.getGameLocationRef());
        try {
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latLng.latitude, latLng.longitude), GEO_FIRE_RADIUS_KM);
            this.geoQuery.addGeoQueryEventListener(this);
            isGeofireInitialized = true;
        } catch (Exception e) {
//            FirebaseCrash.log(TAG + ": GeoFire Exception: " + e.getMessage());
//            Toast.makeText(mContext, "GeoFire Exception" + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.d(TAG, "onCreate: " + e.getMessage());
        }
    }

    private void createNearbyGames(final DataSnapshot dataSnapshot) {
        try {
            GameModel gameModel = dataSnapshot.getValue(GameModel.class);
            if (gameModel != null) {
                nearbyAdapter.add(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_NEARBY_GAMES);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "onResponse: " + e.getMessage());
        }
    }

    //Methods for handling GeoFire
    @Override
    public void onKeyEntered(String key, final GeoLocation location) {
        FirebaseHelper.getGameAuthorRef(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userUid = (String) dataSnapshot.getValue();
                if (TextUtils.isEmpty(userUid) || userUid.equals(FirebaseHelper.getCurrentUserId())) {
                    return;
                }
                FirebaseHelper.getGamesRef(userUid).child(dataSnapshot.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        try {
                            createNearbyGames(dataSnapshot);
                        } catch (Exception e) {
                            Log.d(TAG, "onDataChange: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @Override
    public void onKeyExited(String key) {
        nearbyAdapter.remove(key, false);
    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {
    }

    @Override
    public void onGeoQueryReady() {
    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
    }

    @Override
    public void onStop() {
        super.onStop();
        // add an event listener to start updating locations again
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacks(setupGeoFireRunnable);
        }
        if (this.geoQuery != null) {
            try {
//                avoid error of removeThumb query listener if not added
                this.geoQuery.removeGeoQueryEventListener(this);
            } catch (Exception e) {
                Log.d(TAG, "onStop: " + e.getMessage());
            }
        }
    }
}
