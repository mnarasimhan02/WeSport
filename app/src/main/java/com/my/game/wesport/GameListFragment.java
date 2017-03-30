package com.my.game.wesport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.my.game.wesport.adapter.GameAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.login.SigninActivity;
import com.my.game.wesport.model.DataSnapWithFlag;
import com.my.game.wesport.model.GameModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays list of games that were entered and stored in the app.
 */
public class GameListFragment extends Fragment implements GameAdapter.GameAdapterListener {

    public static final String EXTRA_ALL_GAMES = "all_games";
    private RecyclerView mRecyclerView;
    private GameAdapter adapter;
    private String placeId;
    private int selectedGamePosition = 0;

    //    this is for all games
    private static List<DataSnapshot> allGameSnapshots = new ArrayList<>();

    public static GameListFragment newInstance(boolean allGames) {
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_ALL_GAMES, allGames);
        GameListFragment fragment = new GameListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Identifier for  GameModel damta loader
     */
    private static final int GAME_LOADER = 0;
    private View emptyView;
    private boolean showAllGames = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_catalog, container, false);
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle.containsKey(EXTRA_ALL_GAMES)) {
            showAllGames = bundle.getBoolean(EXTRA_ALL_GAMES);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        placeId = prefs.getString("place_id", "");
        selectedGamePosition = prefs.getInt("chosenGame_pos", 0);

        // Setup FAB to open GameEditActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GameEditActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the game data
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.game_recyclerView);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        emptyView = rootView.findViewById(R.id.empty_view);

        // Setup an Adapter to create a list item for each row of game data in the Cursor.
        // There is no game data yet (until the loader finishes) so pass in null for the Cursor.
        setUserRecyclerView();

        setHasOptionsMenu(true);

        // Kick off the loader
        // getLoaderManager().initLoader(GAME_LOADER, null, this);
        return rootView;

    }

    private void setUserRecyclerView() {
        //to be updated
        adapter = new GameAdapter(getActivity(), this, new ArrayList<DataSnapWithFlag>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);

        FirebaseHelper.getGamesRef(placeId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (!showAllGames && !dataSnapshot.getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                    return;
                }

                adapter.add(new DataSnapWithFlag(dataSnapshot, false), showAllGames);
                emptyView.setVisibility(View.GONE);
                if (showAllGames) {
                    allGameSnapshots.add(dataSnapshot);
                } else {
                    adapter.refreshFlags(allGameSnapshots);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (!showAllGames && !dataSnapshot.getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
                    return;
                }
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    adapter.update(new DataSnapWithFlag(dataSnapshot, false), showAllGames);
                    if (showAllGames) {
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
                adapter.remove(new DataSnapWithFlag(dataSnapshot, false), showAllGames);
                if (adapter.getItemCount() < 1) {
                    emptyView.setVisibility(View.VISIBLE);
                }
                if (showAllGames) {
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
        });
    }

 /*   */

    /**
     * Helper method to delete all games in the database.
     *//*
    private void deleteAllGames() {
        @SuppressWarnings("UnusedAssignment") int rowsDeleted = getActivity()
                .getContentResolver().delete(GameEntry.CONTENT_URI, null, null);
    }
*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.menu_catalog, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // UserModel clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                // deleteAllGames();
                return true;
            case R.id.action_signout:
                signoutuser();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
        if (snapshot.getDataSnapshot().getValue(GameModel.class).getAuthor().equals(FirebaseHelper.getCurrentUser().getUid())) {
            startActivity(GameEditActivity.newIntent(getContext(), snapshot.getDataSnapshot()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (showAllGames && adapter != null) {
            adapter.refreshFlags(allGameSnapshots);
        }
    }
}