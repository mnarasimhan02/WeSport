package com.my.game.wesport;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.my.game.wesport.data.GameContract.GameEntry;
import com.my.game.wesport.login.SigninActivity;

/**
 * Displays list of games that were entered and stored in the app.
 */
public class CatalogActivity extends Fragment implements
        LoaderCallbacks<Cursor> {

    /**
     * Identifier for  Game data loader
     */
    private static final int GAME_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    private GameCursorAdapter mCursorAdapter;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_catalog, container, false);
        super.onCreate(savedInstanceState);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the game data
        ListView gameListView = (ListView) rootView.findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = rootView.findViewById(R.id.empty_view);
        gameListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of game data in the Cursor.
        // There is no game data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new GameCursorAdapter(getActivity(), null);
        gameListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(getActivity(), EditorActivity.class);

                // Form the content URI that represents the specific game that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link gameentry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.games/games/2"
                // if the game with ID 2 was clicked on.
                Uri currentGameUri = ContentUris.withAppendedId(GameEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentGameUri);

                // Launch the {@link EditorActivity} to display the data for the current game.
                startActivity(intent);
            }
        });
        setHasOptionsMenu(true);

        // Kick off the loader
        getLoaderManager().initLoader(GAME_LOADER, null, this);
        return rootView;

    }

    /**
     * Helper method to delete all games in the database.
     */
    private void deleteAllGames() {
        @SuppressWarnings("UnusedAssignment") int rowsDeleted = getActivity().getContentResolver().delete(GameEntry.CONTENT_URI, null, null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        inflater.inflate(R.menu.menu_catalog, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllGames();
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_USER_NAME,
                GameEntry.COLUMN_GAME_DESC,
                GameEntry.COLUMN_START_DATE,
                GameEntry.COLUMN_START_TIME,
                GameEntry.COLUMN_END_TIME,
                GameEntry.COLUMN_GAME_SKILL,
                GameEntry.COLUMN_GAME_ADDRESS,
                GameEntry.COLUMN_GAME_NOTES
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(getActivity(),   // Parent activity context
                GameEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link GameCursorAdapter} with this new cursor containing updated game data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }


}