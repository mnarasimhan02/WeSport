package com.my.game.wesport.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.auth.AuthUI;
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
import com.my.game.wesport.adapter.GameAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.DataSnapWithFlag;
import com.my.game.wesport.model.GameInviteModel;
import com.my.game.wesport.model.GameModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays list of games that were entered and stored in the app.
 */
public class GameListFragment extends Fragment implements GameAdapter.GameAdapterListener {

    private static final String EXTRA_TYPE_GAMES = "type_games";
    private static String EXTRA_PlACE_ID = "place_id";
    private RecyclerView mRecyclerView;
    private GameAdapter adapter;
    private String placeId;
    public static final int TYPE_USER_GAMES = 1;
    public static final int TYPE_ALL_GAMES = 2;
    public static final int TYPE_ACCEPTED_GAMES = 3;

    private View emptyView;
    private int listType = TYPE_USER_GAMES;

    //    this is for all games
    private static List<DataSnapshot> allGameSnapshots = new ArrayList<>();

    public static GameListFragment newInstance(String placeId, int type) {
        Bundle args = new Bundle();
        args.putInt(EXTRA_TYPE_GAMES, type);
        args.putString(EXTRA_PlACE_ID, placeId);
        GameListFragment fragment = new GameListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_game_list, container, false);
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle.containsKey(EXTRA_TYPE_GAMES)) {
            listType = bundle.getInt(EXTRA_TYPE_GAMES);
        }
        if (bundle.containsKey(EXTRA_PlACE_ID)) {
            placeId = bundle.getString(EXTRA_PlACE_ID);
        }

        // Setup FAB to open GameEditActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GameEditActivity.class);
                startActivity(intent);
            }
        });

        fab.setVisibility(listType == TYPE_USER_GAMES ? View.VISIBLE : View.GONE);

        // Find the ListView which will be populated with the game data
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.game_recyclerView);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = rootView.findViewById(R.id.empty_view);

        // Setup an Adapter to create a list item for each row of game data in the Cursor.
        // There is no game data yet (until the loader finishes) so pass in null for the Cursor.
        setUserRecyclerView();

        setHasOptionsMenu(true);

        if (listType == TYPE_ACCEPTED_GAMES) {
            emptyView.setVisibility(View.GONE);
        }
        // Kick off the loader
        // getLoaderManager().initLoader(GAME_LOADER, null, this);
        return rootView;

    }

    private void setUserRecyclerView() {
        //show edit action only if not all games
        adapter = new GameAdapter(getActivity(), this, new ArrayList<DataSnapWithFlag>(), listType == TYPE_USER_GAMES);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        if (listType == TYPE_ACCEPTED_GAMES) {
            FirebaseHelper.getInvitesRef(FirebaseHelper.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot inviteSnapShot, String s) {
                    GameInviteModel invite = inviteSnapShot.getValue(GameInviteModel.class);
                    if (invite.isAccepted()) {
                        FirebaseHelper.getGamesRef(invite.getAuthorUid()).child(invite.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot gameSnapshot) {
                                adapter.add(new DataSnapWithFlag(gameSnapshot, false), false);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot inviteSnapShot, String s) {
                    GameInviteModel invite = inviteSnapShot.getValue(GameInviteModel.class);
                    if (!invite.isAccepted()) {
                        adapter.remove(invite.getGameKey(), false);
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
            });
        } else {
            FirebaseHelper.getGamesRef(FirebaseHelper.getCurrentUser().getUid()).addChildEventListener(getGamesChildEventListener());
        }
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
                GameModel gameModel = dataSnapshot.getValue(GameModel.class);

                if (gameModel.getParkId().equals(placeId)) {
                    adapter.addAtFirst(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_ALL_GAMES);
                } else {
                    adapter.add(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_ALL_GAMES);
                }
                emptyView.setVisibility(View.GONE);
                if (listType == TYPE_ALL_GAMES) {
                    allGameSnapshots.add(dataSnapshot);
                } else {
                    adapter.refreshFlags(allGameSnapshots);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (listType == TYPE_USER_GAMES && !dataSnapshot.getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                    return;
                }
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    adapter.update(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_ALL_GAMES);
                    if (listType == TYPE_ALL_GAMES) {
                        for (int i = 0; i < allGameSnapshots.size(); i++) {
                            if (allGameSnapshots.get(i).getKey().equals(dataSnapshot.getKey())) {
                                allGameSnapshots.set(i, dataSnapshot);
                            }
                        }
                    } else {
                        adapter.refreshFlags(allGameSnapshots);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                adapter.remove(new DataSnapWithFlag(dataSnapshot, false), listType == TYPE_ALL_GAMES);
                if (adapter.getItemCount() < 1) {
                    emptyView.setVisibility(View.VISIBLE);
                }
                if (listType == TYPE_ALL_GAMES) {
                    for (int i = 0; i < allGameSnapshots.size(); i++) {
                        if (allGameSnapshots.get(i).getKey().equals(dataSnapshot.getKey())) {
                            allGameSnapshots.remove(i);
                        }
                    }
                } else {
                    adapter.refreshFlags(allGameSnapshots);
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
            startActivity(GameEditActivity.newIntent(getActivity(), snapshot.getDataSnapshot()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listType == TYPE_ALL_GAMES && adapter != null) {
            adapter.refreshFlags(allGameSnapshots);
        }
    }
}