package com.my.game.wesport;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Events;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.common.api.GoogleApiClient;
import com.my.game.wesport.data.GameContract.GameEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import static com.my.game.wesport.R.id.start_time;


/**
 * Allows user to create a new game or edit an existing one.
 */
@SuppressWarnings("UnusedParameters")
public class EditorActivity extends AppCompatActivity implements
        LoaderCallbacks<Cursor> {

    /**
     * Identifier for the game data loader
     */
    private static final int EXISTING_GAME_LOADER = 0;
    private EditText mstartDate;
    private EditText mstartTime;
    private EditText mendTime;
    private final Calendar mDateAndTime = Calendar.getInstance();
    private View mLayout;
    public static final int MY_PERMISSIONS_REQUEST_WRITE_CALENDAR = 123;
    /**
     * Content URI for the existing game (null if it's a new game)
     */
    private Uri mCurrentGameUri;
    /**
     * EditText field to enter the game's name
     */
    private EditText mNameEditText;
    /**
     * EditText field to enter the game's notes
     */
    private EditText mnotesEditText;
    /**
     * EditText field to enter the game skill
     */
    private Spinner mSkillSpinner;

    /**
     * Skill for the Game. The possible valid values are in the GameContract.java file:
     */
    private int mSkill = GameEntry.SKILL_ROOKIES;

    /**
     * Boolean flag that keeps track of whether the game has been edited (true) or not (false)
     */
    private boolean mGameHasChanged = false;

    /* String to retreive address, chosengame and username*/

    private String gameaddress = "";

    private String selectedGame = "";

    private String mUserName = "";

    private AlertDialog alert;


    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mGameHasChanged boolean to true.
     */
    private final OnTouchListener mTouchListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGameHasChanged = true;
            return false;
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @SuppressWarnings("UnusedAssignment")
    public EditorActivity() {
        // Assign current Date and Time Values to Variables
        final Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        int mHour = c.get(Calendar.HOUR_OF_DAY);
        int mMinute = c.get(Calendar.MINUTE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mLayout = findViewById(android.R.id.content);

        //Get Location and Selected Game from sharedpreferences

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences chGame = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        gameaddress = prefs.getString("games", "Your Location");
        selectedGame = chGame.getString("chosenGame", "Other");

        //Get Username from sharedpreferences
        SharedPreferences prefUser = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mUserName = prefUser.getString("displayName", "");

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new Game or editing an existing one.
        Intent intent = getIntent();
        mCurrentGameUri = intent.getData();
        // If the intent DOES NOT contain a Game content URI, then we know that we are
        // creating a new game.
        if (mCurrentGameUri == null) {
            // This is a new game, so change the app bar to say "Add a Game"
            setTitle(getString(R.string.editor_activity_title_new_game));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing Game, so change app bar to say "Edit Game"
            setTitle(getString(R.string.editor_activity_title_edit_game));

            // Initialize a loader to read the Game data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_GAME_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_game_name);
        mnotesEditText = (EditText) findViewById(R.id.edit_game_notes);
        mSkillSpinner = (Spinner) findViewById(R.id.spinner_game);
        mstartDate = (EditText) findViewById(R.id.startdate);
        mstartTime = (EditText) findViewById(start_time);
        mendTime = (EditText) findViewById(R.id.end_time);

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mnotesEditText.setOnTouchListener(mTouchListener);
        mSkillSpinner.setOnTouchListener(mTouchListener);
        mstartDate.setOnTouchListener(mTouchListener);
        mstartTime.setOnTouchListener(mTouchListener);
        mendTime.setOnTouchListener(mTouchListener);
        setupSpinner();
    }


    public void onDateClicked(View v) {

        OnDateSetListener mDateListener = new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateDisplay(mstartDate);
            }
        };

        DatePickerDialog startDate = new DatePickerDialog(EditorActivity.this, mDateListener,
                mDateAndTime.get(Calendar.YEAR),
                mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH));
        startDate.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        startDate.show();
    }

    public void stTimePicker(View v) {

        OnTimeSetListener mTimeListener = new OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDateAndTime.set(Calendar.MINUTE, minute);
                updateTimeDisplay(mstartTime);
                mDateAndTime.add(Calendar.HOUR, 2);
                mDateAndTime.set(Calendar.MINUTE, minute);
                updateTimeDisplay(mendTime);
            }
        };

        new TimePickerDialog(EditorActivity.this, mTimeListener,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), false).show();

    }

    public void etTimePicker(View v) {

        OnTimeSetListener mTimeListener = new OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDateAndTime.set(Calendar.MINUTE, minute);
                updateTimeDisplay(mendTime);
            }
        };
        new TimePickerDialog(EditorActivity.this, mTimeListener,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), false).show();

    }

    private void updateTimeDisplay(TextView mtextview) {
        mtextview.setText(DateUtils.formatDateTime(this, mDateAndTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME));
        /*if (!mendTime.getText().toString().isEmpty() &&
                !TimeValidator(mstartTime.getText().toString(), mendTime.getText().toString())
                ) {
            Snackbar.make(mLayout, getString(R.string.date_compare_string),
                    Snackbar.LENGTH_LONG).show();
            mendTime.setText("");
        }*/

    }

    private void updateDateDisplay(TextView mtextview) {
        long today = mDateAndTime.getTimeInMillis();
        SimpleDateFormat sdfDate = new SimpleDateFormat("MMMM dd", Locale.getDefault());
        String dateString = sdfDate.format(today);
        mtextview.setText(dateString);
    }

    private boolean TimeValidator(String time1, String time2) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        boolean b = false;
        try {
            Date startTime = sdf.parse(time1);
            Date endTime = sdf.parse(time2);
            long difference = endTime.getTime() - startTime.getTime();

            if (difference < 0) {
                difference = (endTime.getTime() - startTime.getTime()) + (endTime.getTime() - startTime.getTime());
            }
            int days = (int) (difference / (1000 * 60 * 60 * 24));
            int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
            int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
            //boolean mindiff = (min < 0 ? false : true);
            b = !(hours < 0 || min < 0);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the skill for the game.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_game_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mSkillSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mSkillSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.game_vet))) {
                        mSkill = GameEntry.SKILL_VET;
                    } else if (selection.equals(getString(R.string.game_pro))) {
                        mSkill = GameEntry.SKILL_PRO;
                    } else {
                        mSkill = GameEntry.SKILL_ROOKIES;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSkill = GameEntry.SKILL_ROOKIES;
            }
        });

    }

    /**
     * Get user input from editor and save game into database.
     */
    private void saveGames() {

        String[] selectionArgs;
        String sttring=null;
        String etString=null;

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String sdString = mstartDate.getText().toString().trim();
        sttring = mstartTime.getText().toString().trim();
        etString = mendTime.getText().toString().trim();
        String notesString = mnotesEditText.getText().toString().trim();


        // Check if this is supposed to be a new game
        // and check if all the fields in the editor are blank
        if (mCurrentGameUri == null &&
                (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(sdString)) &&
                mSkill == GameEntry.SKILL_ROOKIES) {
            // Since no fields were modified, we can return early without creating a new game.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Snackbar.make(mLayout, getString(R.string.editor_insert_game_params),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(GameEntry.COLUMN_USER_NAME, mUserName);
        values.put(GameEntry.COLUMN_GAME_NAME, selectedGame);
        values.put(GameEntry.COLUMN_GAME_DESC, nameString);
        values.put(GameEntry.COLUMN_START_DATE, String.valueOf(sdString));
        values.put(GameEntry.COLUMN_START_TIME, String.valueOf(sttring));
        values.put(GameEntry.COLUMN_END_TIME, String.valueOf(etString));
        values.put(GameEntry.COLUMN_GAME_SKILL, mSkill);
        values.put(GameEntry.COLUMN_GAME_ADDRESS, gameaddress);
        values.put(GameEntry.COLUMN_GAME_NOTES, notesString);

        // Determine if this is a new or existing game by checking if mCurrentGameUri is null or not
        if (mCurrentGameUri == null) {
            // This is a NEW game, so insert a new game into the provider,
            // returning the content URI for the new game.
            Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Snackbar.make(mLayout, getString(R.string.editor_insert_game_failed),
                        Snackbar.LENGTH_LONG).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Snackbar.make(mLayout, getString(R.string.editor_insert_game_successful),
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            // Otherwise this is an EXISTING game, so update the game with content URI: mCurrentGameUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentGameUri will already identify the correct row in the database that
            // we want to modify.
            // Construct a selection clause that matches the word that the user entered.

            // Use the user name from shared preferences as the (only) selection argument to filter games only for that user.
            selectionArgs = new String[]{mUserName};
            int rowsAffected = getContentResolver().update(mCurrentGameUri, values, GameEntry.COLUMN_USER_NAME, selectionArgs);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Snackbar.make(mLayout, getString(R.string.editor_update_game_failed),
                        Snackbar.LENGTH_LONG).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Snackbar.make(mLayout, getString(R.string.editor_update_game_failed),
                        Snackbar.LENGTH_LONG).show();
            }
        }
        writeCalendarEvent(gameaddress, selectedGame, nameString, sdString, sttring, etString, notesString);
    }

    private void writeCalendarEvent(String gameaddress, String selectedGame, String nameString, String sdString,
                                    String sttring, String etString, String notesString) {

        SimpleDateFormat calstDatFormat;
        final ContentValues event = new ContentValues();
        final Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);

        if (sttring.equals("")) {
            calstDatFormat = new SimpleDateFormat("MMMMM dd yyyy", Locale.getDefault());
            etString = sdString + " "+ yy;
            sdString = sdString + " "+ yy;

        } else {
            calstDatFormat = new SimpleDateFormat("MMMMM dd h:mm a yyyy", Locale.getDefault());
            etString = sdString + " " + etString+" "+ yy;
            sdString = sdString + " " + sttring +" "+ yy;
        }

        long stdateInLong = 0, etdateInLong=0;
        try {
            Date BEGIN_TIME = calstDatFormat.parse(sdString);
            Date END_TIME = calstDatFormat.parse(etString);
            stdateInLong = BEGIN_TIME.getTime();
            etdateInLong = END_TIME.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        event.put(Events.CALENDAR_ID, 1);
        event.put(Events.EVENT_LOCATION, gameaddress);
        event.put(Events.TITLE, selectedGame);
        event.put(Events.DESCRIPTION, nameString + " "+ notesString);
        event.put(Events.DTSTART, stdateInLong);//startTimeMillis
        event.put(Events.DTEND, etdateInLong);//endTimeMillis
        event.put(Events.ALL_DAY, 0); // 0 for false, 1 for true
        String timeZone = TimeZone.getDefault().getID();
        event.put(Events.EVENT_TIMEZONE, timeZone);

        Uri baseUri;
        if (VERSION.SDK_INT >= 8) {
            baseUri = Uri.parse("content://com.android.calendar/events");
        } else {
            baseUri = Uri.parse("content://calendar/events");
        }
        getApplicationContext().getContentResolver().insert(baseUri, event);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new game, hide the "Delete" menu item.
        if (mCurrentGameUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save game to database
                saveGames();
                //Show snackbar and then finish activity
                Snackbar.make(mLayout, R.string.calendar_add_game,Snackbar.LENGTH_SHORT)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar transientBottomBar,
                                                    int event) {
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                    EditorActivity.this.finish();
                                }
                            }
                        }).show();
                // Exit activity
                //finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the game hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                OnClickListener discardButtonClickListener =
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the game hasn't changed, continue with handling back button press
        if (!mGameHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        OnClickListener discardButtonClickListener =
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all game attributes, define a projection that contains
        // all columns from the game table
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_USER_NAME,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_DESC,
                GameEntry.COLUMN_START_DATE,
                GameEntry.COLUMN_START_TIME,
                GameEntry.COLUMN_END_TIME,
                GameEntry.COLUMN_GAME_SKILL,
                GameEntry.COLUMN_GAME_ADDRESS,
                GameEntry.COLUMN_GAME_NOTES,
        };


        // Defines a string to contain the selection clause
        String selectionClause = null;

        // An array to contain selection arguments
        String[] selectionArgs = null;

        if (!TextUtils.isEmpty(mUserName)) {
            // Construct a selection clause that matches the entered username.
            selectionClause = GameEntry.COLUMN_USER_NAME + " = ?";

            // Use the user name from shared preferences as the (only) selection argument to filter games only for that user.
            selectionArgs = new String[]{mUserName};
        }
        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentGameUri,         // Query the content URI for the current game
                projection,             // Columns to include in the resulting Cursor
                selectionClause,                   //  selection clause
                selectionArgs,                   // selection arguments
                null);                  // Default sort order
    }

    @SuppressWarnings("UnusedAssignment")
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of game attributes that we're interested in
            int usernameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_USER_NAME);
            int gamenameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NAME);
            int descColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_DESC);
            int startDateColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_START_DATE);
            int startTimeColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_START_TIME);
            int endTimeColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_END_TIME);
            int skillColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_SKILL);
            int notesColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NOTES);
            int locColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_ADDRESS);


            // Extract out the value from the Cursor for the given column index
            String username = cursor.getString(usernameColumnIndex);
            String gamename = cursor.getString(gamenameColumnIndex);
            String desc = cursor.getString(descColumnIndex);
            String stdate = cursor.getString(startDateColumnIndex);
            String sttime = cursor.getString(startTimeColumnIndex);
            String ettime = cursor.getString(endTimeColumnIndex);
            int skill = cursor.getInt(skillColumnIndex);
            String notes = cursor.getString(notesColumnIndex);
            String location = cursor.getString(locColumnIndex);

            // Update the views on the screen with the values from the database

            mNameEditText.setText(desc);
            mnotesEditText.setText(notes);
            mstartDate.setText(stdate);
            mstartTime.setText(sttime);
            mendTime.setText(ettime);

            // Skill is a dropdown spinner, so map the constant value from the database
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (skill) {
                case GameEntry.SKILL_VET:
                    mSkillSpinner.setSelection(1);
                    break;
                case GameEntry.SKILL_PRO:
                    mSkillSpinner.setSelection(2);
                    break;
                default:
                    mSkillSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mnotesEditText.setText("");
        mstartDate.setText("");
        mstartTime.setText("");
        mendTime.setText("");
        mSkillSpinner.setSelection(0);
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(
            OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this game.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the game.
                deleteGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the game.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * Perform the deletion of the game in the database.
     */
    private void deleteGame() {
        // Only perform the delete if this is an existing game.
        if (mCurrentGameUri != null) {
            // Call the ContentResolver to delete the game at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentGameUri
            // content URI already identifies the game that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentGameUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Snackbar.make(mLayout, getString(R.string.editor_delete_game_failed),
                        Snackbar.LENGTH_LONG).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Snackbar.make(mLayout, getString(R.string.editor_delete_game_failed),
                        Snackbar.LENGTH_LONG).show();
            }
        }
        // Close the activity
        finish();
    }
}