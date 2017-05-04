package com.my.game.wesport.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract.Events;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.my.game.wesport.App;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.HomeGamesFragment;
import com.my.game.wesport.adapter.SimpleGameCategoryAdapter;
import com.my.game.wesport.data.GameContract.GameEntry;
import com.my.game.wesport.helper.DateHelper;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.helper.GameHelper;
import com.my.game.wesport.model.GameCategoryModel;
import com.my.game.wesport.model.GameModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import static com.my.game.wesport.R.id.start_time;


/**
 * Allows user to create a new game or edit an existing one.
 */
@SuppressWarnings("UnusedParameters")
public class GameEditActivity extends AppCompatActivity implements PlaceSelectionListener {

    private EditText mstartDate;
    private EditText mstartTime;
    private EditText mendTime;
    private ImageView mSelectedGameImageView;
    private TextView mSelectedGameTextView;

    private final Calendar mDateAndTime = Calendar.getInstance();
    private View mLayout;
    private String placeId;
    private double placeLatitude, placeLongitude;

    private static String EXTRA_LATITUDE = "key_lat";
    private static String EXTRA_LONGITUDE = "key_lon";
    private static String EXTRA_GAME_ADDRESS = "game_address";

    private static String EXTRA_PLACE_ID = "place_id";
    private HomeGamesFragment homeGamesFragment;

    public static final int REQUEST_CODE_ACTIVITY = 24;

    /**
     * GameModel for the existing game (null if it's a new game)
     */
    private static DataSnapshot currentGameDataSnapShot = null;
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
     * Skill for the GameModel. The possible valid values are in the GameContract.java file:
     */
    private int mSkill = GameEntry.SKILL_ROOKIES;

    /**
     * Boolean flag that keeps track of whether the game has been edited (true) or not (false)
     */
    private boolean mGameHasChanged = false;

    /* String to retreive address, chosengame and username*/

    private String gameAddress = "";
    private GameCategoryModel selectedGameCategory;

    private Dialog gameCategoryList;
    private TextView mSelectedGameLocation;
    private PlaceAutocompleteFragment autocompleteFragment;

    /*if gameDataSnapShot null then its mean create new game*/
    public static Intent newIntent(Context context, String placeId, DataSnapshot gameDataSnapShot, double lat, double lon, String gameAddress) {
        currentGameDataSnapShot = gameDataSnapShot;
        Intent intent = new Intent(context, GameEditActivity.class);
        Bundle args = new Bundle();
        args.putDouble(EXTRA_LATITUDE, lat);
        args.putDouble(EXTRA_LONGITUDE, lon);
        intent.putExtras(args);
        intent.putExtra(EXTRA_PLACE_ID, placeId);
        intent.putExtra(EXTRA_GAME_ADDRESS, gameAddress);
        return intent;
    }

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        mLayout = findViewById(android.R.id.content);

        placeId = getIntent().getStringExtra(EXTRA_PLACE_ID);
        gameAddress = getIntent().getStringExtra(EXTRA_GAME_ADDRESS);

        //getting lat and lon for the game
        placeLatitude = getIntent().getDoubleExtra(EXTRA_LATITUDE, 0);
        placeLongitude = getIntent().getDoubleExtra(EXTRA_LONGITUDE, 0);

        //Get Username from sharedpreferences
//        SharedPreferences prefUser = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        String mUserName = prefUser.getString("displayName", "");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Create/Edit Game");
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_game_name);
        mnotesEditText = (EditText) findViewById(R.id.edit_game_notes);
        mSkillSpinner = (Spinner) findViewById(R.id.spinner_game);
        mstartDate = (EditText) findViewById(R.id.startdate);
        mstartTime = (EditText) findViewById(start_time);
        mendTime = (EditText) findViewById(R.id.end_time);
        mSelectedGameImageView = (ImageView) findViewById(R.id.selected_game_ImageView);
        mSelectedGameTextView = (TextView) findViewById(R.id.selected_game_textView);
        //mSelectedGameLocation = (TextView) findViewById(R.id.edit_game_location);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mnotesEditText.setOnTouchListener(mTouchListener);
        mSkillSpinner.setOnTouchListener(mTouchListener);
        mstartDate.setOnTouchListener(mTouchListener);
        mstartTime.setOnTouchListener(mTouchListener);
        mendTime.setOnTouchListener(mTouchListener);



        // mSelectedGameImageView.setOnTouchListener((mTouchListener));

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new GameModel or editing an existing one.
        // If the intent DOES NOT contain a GameModel content URI, then we know that we are
        // creating a new game.
        if (currentGameDataSnapShot == null) {
            // This is a new game, so change the app bar to say "Add a GameModel"
            setTitle(getString(R.string.editor_activity_title_new_game));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();

//            if address already selected then hide address field
            if (!TextUtils.isEmpty(gameAddress)) {
                findViewById(R.id.address_field_layout).setVisibility(View.GONE);
            }
        } else {
            // Otherwise this is an existing GameModel, so change app bar to say "Edit GameModel"
            setTitle(getString(R.string.editor_activity_title_edit_game));

            // and display the current values in the editor
            setExistingData();

            findViewById(R.id.address_field_layout).setVisibility(View.GONE);
        }

        setupSpinner();
        setupGameCategoryList();


        // Retrieve the PlaceAutocompleteFragment.
        autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setHint(getString(R.string.autocomplete_hint));

        // Register a listener to receive callbacks when a place has been selected or an error has
        // occurred and set Filter to retreive only places with precise address
        autocompleteFragment.setOnPlaceSelectedListener(this);
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);

        EditText placeAutoCompleteEt = (EditText) autocompleteFragment.getView().findViewById(R.id.place_autocomplete_search_input);
        placeAutoCompleteEt.setTextSize(14.0f);
        placeAutoCompleteEt.setHint("Select place");
    }

    public void onChooseGameClick(View view) {
        if (currentGameDataSnapShot == null) {
            showGameCategorySelectionSheet();
        }
    }

    /*public void onChooseLocationClick(View view){
        if (currentGameDataSnapShot == null){
            Intent intent = new Intent(GameEditActivity.this, MapsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ACTIVITY);
        }
    }*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
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

        DatePickerDialog startDate = new DatePickerDialog(GameEditActivity.this, mDateListener,
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

        new TimePickerDialog(GameEditActivity.this, mTimeListener,
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
        new TimePickerDialog(GameEditActivity.this, mTimeListener,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), false).show();

    }

    private void updateTimeDisplay(TextView mtextview) {
        mtextview.setText(DateUtils.formatDateTime(this, mDateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_TIME));
    }

    private void updateDateDisplay(TextView mtextview) {
        long today = mDateAndTime.getTimeInMillis();
        String dateString = DateHelper.getServerDateFormatter().format(today);
        mtextview.setText(dateString);
    }

// --Commented out by Inspection START (3/8/17, 4:16 PM):
//    private boolean TimeValidator(String time1, String time2) {
//        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
//        boolean b = false;
//        try {
//            Date startTime = sdf.parse(time1);
//            Date endTime = sdf.parse(time2);
//            long difference = endTime.getTime() - startTime.getTime();
//
//            if (difference < 0) {
//                difference = (endTime.getTime() - startTime.getTime()) + (endTime.getTime() - startTime.getTime());
//            }
//            int days = (int) (difference / (1000 * 60 * 60 * 24));
//            int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
//            int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
//            //boolean mindiff = (min < 0 ? false : true);
//            b = !(hours < 0 || min < 0);
//        } catch (ParseException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//        return b;
//    }
// --Commented out by Inspection STOP (3/8/17, 4:16 PM)

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
    private boolean saveGames() {

        if (selectedGameCategory == null) {
            Toast.makeText(this, "Please select game category!", Toast.LENGTH_SHORT).show();
            return false;
        }

        String startTime;
        String endTime;

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String gameDescription = mNameEditText.getText().toString().trim();
        String startDate = mstartDate.getText().toString().trim();
        startTime = mstartTime.getText().toString().trim();
        endTime = mendTime.getText().toString().trim();
        String notesString = mnotesEditText.getText().toString().trim();


        // Check if this is supposed to be a new game
        // and check if all the fields in the editor are blank
        if (currentGameDataSnapShot == null && (TextUtils.isEmpty(gameDescription) || TextUtils.isEmpty(startDate)) &&
                mSkill == GameEntry.SKILL_ROOKIES) {
            // Since no fields were modified, we can return early without creating a new game.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Snackbar.make(mLayout, getString(R.string.editor_insert_game_params),
                    Snackbar.LENGTH_LONG).show();
            return false;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        GameModel gameModel = new GameModel(
                gameDescription,
                startDate,
                startTime,
                endTime,
                mSkill,
                notesString,
                placeId,
                gameAddress,
                selectedGameCategory.getId(),
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                App.getInstance().getUserModel().getDisplayName()
        );


        // Determine if this is a new or existing game by checking if currentGameKey is null or not
        if (currentGameDataSnapShot == null) {
            if (TextUtils.isEmpty(gameAddress)) {
                Toast.makeText(this, "Please select place!", Toast.LENGTH_SHORT).show();
                return false;
            }
            // This is a NEW game, so insert a new game,
            DatabaseReference gamePush = FirebaseHelper.getGamesRef(FirebaseHelper.getCurrentUser().getUid()).push();
            gamePush.setValue(gameModel);

            FirebaseHelper.getGameAuthorRef(gamePush.getKey()).setValue(FirebaseHelper.getCurrentUser().getUid());
            GeoFire geoFire = new GeoFire(FirebaseHelper.getGameLocationRef());
            geoFire.setLocation(gamePush.getKey(), new GeoLocation(placeLatitude, placeLongitude));
            Snackbar.make(mLayout, getString(R.string.editor_insert_game_successful),
                    Snackbar.LENGTH_LONG).show();
        } else {
            // Otherwise this is an EXISTING game, so update the game
            // we want to modify.
            GameModel existingGameModel = currentGameDataSnapShot.getValue(GameModel.class);
            gameModel.setAddress(existingGameModel.getAddress());
            gameModel.setParkId(existingGameModel.getParkId());
            currentGameDataSnapShot.getRef().setValue(gameModel);
        }
        writeCalendarEvent(gameAddress, selectedGameCategory.getTitle(), gameDescription, startDate, startTime, endTime, notesString);
        return true;
    }

    private void writeCalendarEvent(String gameaddress, String selectedGame, String nameString, String sdString,
                                    String sttring, String etString, String notesString) {

        SimpleDateFormat calstDatFormat;
        final ContentValues event = new ContentValues();
        final Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);

        if (sttring.equals("")) {
            calstDatFormat = new SimpleDateFormat("MMMMM dd yyyy", Locale.getDefault());
            etString = sdString + " " + yy;
            sdString = sdString + " " + yy;

        } else {
            calstDatFormat = new SimpleDateFormat("MMMMM dd h:mm a yyyy", Locale.getDefault());
            etString = sdString + " " + etString + " " + yy;
            sdString = sdString + " " + sttring + " " + yy;
        }

        long stdateInLong = 0, etdateInLong = 0;
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
        event.put(Events.DESCRIPTION, nameString + " " + notesString);
        event.put(Events.DTSTART, stdateInLong);//startTimeMillis
        event.put(Events.DTEND, etdateInLong);//endTimeMillis
        event.put(Events.ALL_DAY, 0); // 0 for false, 1 for true
        String timeZone = TimeZone.getDefault().getID();
        event.put(Events.EVENT_TIMEZONE, timeZone);

        Uri baseUri;
        baseUri = Uri.parse("content://com.android.calendar/events");
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
        if (currentGameDataSnapShot == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // UserModel clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save game to database
                if (mNameEditText.getText().toString().isEmpty()
                        || mstartDate.getText().toString().isEmpty()
                        || mSelectedGameTextView.getText().toString().isEmpty()) {

                    if (mNameEditText.getText().toString().isEmpty()) {
                        mNameEditText.setError("Please enter game name");
                    }

                    if (mstartDate.getText().toString().isEmpty()) {
                        mstartDate.setError("Please enter Start Date for the game");
                    }

                    if (mSelectedGameImageView.getDrawable() == null) {
                        mSelectedGameTextView.setError("Please select a game");
                    }

                } else {
                    if (saveGames()) {
                        //Show snackbar and then finish activityInvitesActivity activity = (InvitesActivity) getActivity();

                        Toast.makeText(this, R.string.calendar_add_game, Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    }
                }

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
                // which is the {@link GameListFragment}.
                if (!mGameHasChanged) {
                    finish();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                OnClickListener discardButtonClickListener =
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // UserModel clicked "Discard" button, navigate to parent activity.
                                finish();
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
                        // UserModel clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }


    public void setExistingData() {
        // Extract out the value from the Cursor for the given column index
        GameModel gameModel = currentGameDataSnapShot.getValue(GameModel.class);
        String desc = gameModel.getGameDescription();
        String stdate = gameModel.getGameDate();
        String sttime = gameModel.getStartTime();
        String ettime = gameModel.getEndTime();
        int skill = gameModel.getSkillLevel();
        String notes = gameModel.getNotes();
        String location = gameModel.getAddress();

        // Update the views on the screen with the values from the database
        mNameEditText.setText(desc);
        mnotesEditText.setText(notes);
        mstartDate.setText(stdate);
        mstartTime.setText(sttime);
        mendTime.setText(ettime);

        selectedGameCategory = GameHelper.getGameCategory(gameModel.getCategoryId());
        if (selectedGameCategory != null) {
            mSelectedGameImageView.setVisibility(View.VISIBLE);
            mSelectedGameImageView.setImageResource(selectedGameCategory.getImage());
            mSelectedGameTextView.setText(selectedGameCategory.getTitle());
        }


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
                // UserModel clicked the "Keep editing" button, so dismiss the dialog
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
        builder.setMessage(R.string.delete_game_dialog_msg);
        builder.setPositiveButton(R.string.delete, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // UserModel clicked the "Delete" button, so delete the game.
                deleteGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // UserModel clicked the "Cancel" button, so dismiss the dialog
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
        if (currentGameDataSnapShot != null) {
            currentGameDataSnapShot.getRef().removeValue();
            FirebaseHelper.deleteGameReferences(currentGameDataSnapShot.getKey());
        }
        // Close the activity
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentGameDataSnapShot = null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    public void setupGameCategoryList() {
        View userListView = LayoutInflater.from(this).inflate(R.layout.game_list_layout, null);
        userListView.findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameCategoryList.dismiss();
            }
        });


        gameCategoryList = new Dialog(this, R.style.MaterialDialogSheetAnim);
        gameCategoryList.setContentView(userListView); // your custom view.
        gameCategoryList.setCancelable(true);
        gameCategoryList.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        gameCategoryList.getWindow().setGravity(Gravity.TOP);

        RecyclerView recyclerView = (RecyclerView) userListView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SimpleGameCategoryAdapter simpleGameCategoryAdapter = new SimpleGameCategoryAdapter(this, GameHelper.getGameCategoryList(), new SimpleGameCategoryAdapter.SimpleGameCategoryListListener() {
            @Override
            public void onGameCategoryClick(int position, GameCategoryModel gameCategoryModel) {
                selectedGameCategory = GameHelper.getGameCategoryList().get(position);
                mSelectedGameImageView.setVisibility(View.VISIBLE);
                mSelectedGameImageView.setImageResource(selectedGameCategory.getImage());
                mSelectedGameTextView.setText(selectedGameCategory.getTitle());
                gameCategoryList.dismiss();
            }
        });
        recyclerView.setAdapter(simpleGameCategoryAdapter);
    }


    public void showGameCategorySelectionSheet() {
        if (gameCategoryList != null) {
            gameCategoryList.show();
        }
    }

    @Override
    public void onPlaceSelected(Place place) {
        LatLng latLng = place.getLatLng();
        placeLatitude = latLng.latitude;
        placeLongitude = latLng.longitude;
        // Either address from marker or address from autocomplete should be the location.
        gameAddress = (String) place.getName();
    }

    @Override
    public void onError(Status status) {
        Toast.makeText(GameEditActivity.this, mLayout + " " + getString(R.string.place_error)
                + status.getStatusMessage(), Toast.LENGTH_LONG).show();
    }
}