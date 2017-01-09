
package com.example.android.wesport;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import com.example.android.wesport.data.GameContract.GameEntry;

/**
 * Displays list of games that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the pet data loader */
    private static final int GAME_LOADER = 0;

    /** Adapter for the ListView */
    GameCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the game data
        ListView gameListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        gameListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of game data in the Cursor.
        // There is no game data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new GameCursorAdapter(this, null);
        gameListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific game that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link gameentry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.games/games/2"
                // if the pet with ID 2 was clicked on.
                Uri currentGameUri = ContentUris.withAppendedId(GameEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentGameUri);

                // Launch the {@link EditorActivity} to display the data for the current pet.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(GAME_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded pet data into the database. For debugging purposes only.
     */
    private void insertGame() {
        // Create a ContentValues object where column names are the keys,
        // and Toto's pet attributes are the values.
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_GAME_DESC, "Tennis");
        values.put(GameEntry.COLUMN_START_DATE, "01-01-2013");
        values.put(GameEntry.COLUMN_START_TIME, "12:00");
        values.put(GameEntry.COLUMN_END_TIME, "02:00");
        values.put(GameEntry.COLUMN_GAME_SKILL, GameEntry.COLUMN_GAME_SKILL);
        values.put(GameEntry.COLUMN_GAME_NOTES, "Bring spare racket for the game and pick up john near starbucks");

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link GameEntry#CONTENT_URI} to indicate that we want to insert
        // into the games database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all games in the database.
     */
    private void deleteAllGames() {
        int rowsDeleted = getContentResolver().delete(GameEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from games database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertGame();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllGames();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_DESC,
                GameEntry.COLUMN_START_DATE,
                GameEntry.COLUMN_START_TIME,
                GameEntry.COLUMN_END_TIME,
                GameEntry.COLUMN_GAME_SKILL,
                GameEntry.COLUMN_GAME_NOTES
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
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
