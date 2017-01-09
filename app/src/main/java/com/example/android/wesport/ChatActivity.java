package com.example.android.wesport;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.android.wesport.data.GameContract.GameEntry;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.android.wesport.SigninActivity.RC_SIGN_IN;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "SigninActivity";

    public static final String ANONYMOUS = "anonymous";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    public static final String FRIENDLY_MSG_LENGTH_KEY = "friendly_msg_length";

    private static final int RC_PHOTO_PICKER = 2;

    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private Button mSendButton;

    private String mUsername;

    // Firebase instance variables
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mMessagesDatabaseReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private StorageReference mChatPhotosStorageReference;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private FirebaseStorage mFirebaseStorage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        // Initialize Firebase components
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseStorage = FirebaseStorage.getInstance();
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        mMessagesDatabaseReference = mFirebaseDatabase.getReference().child("messages");
        mChatPhotosStorageReference = mFirebaseStorage.getReference().child("chat_photos");


        // Initialize references to views
        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (Button) findViewById(R.id.sendButton);

        //Get Username from sharedpreferences
        SharedPreferences prefUser = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        final String mUserName = prefUser.getString("displayName","");
        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);

        //Attach database listener
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
        // Enable Send button when there's text to send
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, mUserName);
                Log.d(TAG, mMessageEditText.getText().toString());
                FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), mUserName, null);
                mMessagesDatabaseReference.push().setValue(friendlyMessage);

                // Clear input box
                mMessageEditText.setText("");
            }
        });

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    onSignedInInitialize(user.getDisplayName());
                } else {
                    // User is signed out
                    onSignedOutCleanup();
                }
            }
        };



        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(FRIENDLY_MSG_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();

    }


    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Make the fetched config available
                        // via FirebaseRemoteConfig get<type> calls, e.g., getLong, getString.
                        mFirebaseRemoteConfig.activateFetched();

                        // Update the EditText length limit with
                        // the newly retrieved values from Remote Config.
                        applyRetrievedLengthLimit();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // An error occurred when fetching the config.
                        Log.w(TAG, "Error fetching config", e);

                        // Update the EditText length limit with
                        // the newly retrieved values from Remote Config.
                        applyRetrievedLengthLimit();
                    }
                });
    }

    /**
     * Apply retrieved length limit to edit text field. This result may be fresh from the server or it may be from
     * cached values.
     */
    private void applyRetrievedLengthLimit() {
        Long friendly_msg_length = mFirebaseRemoteConfig.getLong(FRIENDLY_MSG_LENGTH_KEY);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(friendly_msg_length.intValue())});
        Log.d(TAG, FRIENDLY_MSG_LENGTH_KEY + " = " + friendly_msg_length);
    }

    //used for reading data from Firebase

    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    Log.d(TAG, String.valueOf(friendlyMessage));
                    mMessageAdapter.add(friendlyMessage);
                }

                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                }

                public void onChildRemoved(DataSnapshot dataSnapshot) {
                }

                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                }

                public void onCancelled(DatabaseError databaseError) {
                }
            };
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                AuthUI.getInstance()
                        .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                public void onComplete(@NonNull Task<Void> task) {
                    // user is now signed out
                    startActivity(new Intent(ChatActivity.this, SigninActivity.class));
                    finish();
                }
            });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void onSignedInInitialize(String username) {
        mUsername = username;
        attachDatabaseReadListener();
    }

    private void onSignedOutCleanup() {
        mUsername = ANONYMOUS;
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // Sign-in succeeded, set up the UI
                Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {
                // Sign in was canceled by the user, finish the activity
                Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            // Get a reference to store file at chat_photos/<FILENAME>
            StorageReference photoRef = mChatPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // When the image has successfully uploaded, we get its download URL
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();

                            // Set the download URL to the message box, so that the user can send it to the database
                            FriendlyMessage friendlyMessage = new FriendlyMessage(null, mUsername, downloadUrl.toString());
                            mMessagesDatabaseReference.push().setValue(friendlyMessage);
                        }
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
        mMessageAdapter.clear();
        detachDatabaseReadListener();
    }

    /**
     * Displays list of games that were entered and stored in the app.
     */
    public static class CatalogActivity extends AppCompatActivity implements
            LoaderManager.LoaderCallbacks<Cursor> {

        /** Identifier for the game data loader */
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
                    // if the game with ID 2 was clicked on.
                    Uri currentGameUri = ContentUris.withAppendedId(GameEntry.CONTENT_URI, id);

                    // Set the URI on the data field of the intent
                    intent.setData(currentGameUri);

                    // Launch the {@link EditorActivity} to display the data for the current game.
                    startActivity(intent);
                }
            });

            // Kick off the loader
            getLoaderManager().initLoader(GAME_LOADER, null, this);
        }

        /**
         * Helper method to insert hardcoded game data into the database. For debugging purposes only.
         */
        private void insertGame() {
            // Create a ContentValues object where column names are the keys,
            // and Toto's game attributes are the values.
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

    /**
     * Allows user to create a new game or edit an existing one.
     */
    public static class EditorActivity extends AppCompatActivity implements
            LoaderManager.LoaderCallbacks<Cursor>  {

        /** Identifier for the game data loader */
        private static final int EXISTING_GAME_LOADER = 0;

        /** Content URI for the existing game (null if it's a new game) */
        private Uri mCurrentGameUri;

        /** EditText field to enter the game's name */
        private EditText mNameEditText;

        /** EditText field to enter the game's notes */
        private EditText mnotesEditText;

        /** EditText field to enter the game skill */
        private Spinner mSkillSpinner;

        public EditText mstartDate;
        public EditText mstartTime;
        public EditText mendTime;
        Calendar mDateAndTime = Calendar.getInstance();

        // declare  the variables to Show/Set the date and time when Time and  Date Picker Dialog first appears

        private int mYear, mMonth, mDay,mHour,mMinute;

        /**
         * Skill for the Game. The possible valid values are in the GameContract.java file:
         */
        private int mSkill = GameEntry.SKILL_ROOKIES;

        /** Boolean flag that keeps track of whether the game has been edited (true) or not (false) */
        private boolean mGameHasChanged = false;


        public EditorActivity()
        {
            // Assign current Date and Time Values to Variables
            final Calendar c = Calendar.getInstance();
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);
        }

        /**
         * OnTouchListener that listens for any user touches on a View, implying that they are modifying
         * the view, and we change the mGameHasChanged boolean to true.
         */
        private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
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
                // (It doesn't make sense to delete a Game that hasn't been created yet.)
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
            mstartTime = (EditText) findViewById(R.id.start_time);
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

           // setCurrentDateTimeOnView();
            setupSpinner();
        }


        public void onDateClicked(View v) {

            DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    mDateAndTime.set(Calendar.YEAR, year);
                    mDateAndTime.set(Calendar.MONTH, monthOfYear);
                    mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    updateDateDisplay(mstartDate);
                }
            };

            new DatePickerDialog(EditorActivity.this, mDateListener,
                    mDateAndTime.get(Calendar.YEAR),
                    mDateAndTime.get(Calendar.MONTH),
                    mDateAndTime.get(Calendar.DAY_OF_MONTH)).show();
        }

        public void stTimePicker(View v) {

            TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    mDateAndTime.set(Calendar.MINUTE, minute);
                    updateTimeDisplay(mstartTime);
                }
            };

            new TimePickerDialog(EditorActivity.this, mTimeListener,
                    mDateAndTime.get(Calendar.HOUR_OF_DAY),
                    mDateAndTime.get(Calendar.MINUTE), false).show();

        }

        public void etTimePicker(View v) {

            TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
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
            mtextview.setText(DateUtils.formatDateTime(this,
                    mDateAndTime.getTimeInMillis(),
                    DateUtils.FORMAT_SHOW_TIME));
        }

        private void updateDateDisplay(TextView mtextview) {
            mtextview.setText(DateUtils.formatDateTime(this,
                    mDateAndTime.getTimeInMillis(), 0));
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
            mSkillSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

            // Read from input fields
            // Use trim to eliminate leading or trailing white space
            String nameString = mNameEditText.getText().toString().trim();
            String sdString = mstartDate.getText().toString().trim();
            String sttring = mstartTime.getText().toString().trim();
            String etString = mendTime.getText().toString().trim();
            String notesString = mnotesEditText.getText().toString().trim();

            // Check if this is supposed to be a new game
            // and check if all the fields in the editor are blank
            if (mCurrentGameUri == null &&
                    TextUtils.isEmpty(nameString)  &&
                    TextUtils.isEmpty(notesString) && mSkill == GameEntry.SKILL_ROOKIES) {
                // Since no fields were modified, we can return early without creating a new game.
                // No need to create ContentValues and no need to do any ContentProvider operations.
                return;
            }

            // Create a ContentValues object where column names are the keys,
            // and game attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(GameEntry.COLUMN_GAME_DESC, nameString);
            values.put(GameEntry.COLUMN_START_DATE, String.valueOf(sdString));
            values.put(GameEntry.COLUMN_START_TIME, String.valueOf(sttring));
            values.put(GameEntry.COLUMN_END_TIME, String.valueOf(etString));
            values.put(GameEntry.COLUMN_GAME_SKILL, mSkill);
            values.put(GameEntry.COLUMN_GAME_NOTES, notesString);

            // Determine if this is a new or existing game by checking if mCurrentGameUri is null or not
            if (mCurrentGameUri == null) {
                // This is a NEW game, so insert a new game into the provider,
                // returning the content URI for the new game.
                Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_game_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_game_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING game, so update the game with content URI: mCurrentGameUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentGameUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentGameUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_game_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_game_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
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
                    // Exit activity
                    finish();
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
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
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
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
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
                    GameEntry.COLUMN_GAME_DESC,
                    GameEntry.COLUMN_START_DATE,
                    GameEntry.COLUMN_START_TIME,
                    GameEntry.COLUMN_END_TIME,
                    GameEntry.COLUMN_GAME_SKILL,
                    GameEntry.COLUMN_GAME_NOTES };

            // This loader will execute the ContentProvider's query method on a background thread
            return new CursorLoader(this,   // Parent activity context
                    mCurrentGameUri,         // Query the content URI for the current game
                    projection,             // Columns to include in the resulting Cursor
                    null,                   // No selection clause
                    null,                   // No selection arguments
                    null);                  // Default sort order
        }

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
                int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_DESC);
                int startDateColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_START_DATE);
                int startTimeColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_START_TIME);
                int endTimeColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_END_TIME);
                int skillColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_SKILL);
                int notesColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NOTES);

                // Extract out the value from the Cursor for the given column index
                String name = cursor.getString(nameColumnIndex);
                String stdate = cursor.getString(startDateColumnIndex);
                String sttime = cursor.getString(startTimeColumnIndex);
                String ettime = cursor.getString(endTimeColumnIndex);
                int skill = cursor.getInt(skillColumnIndex);
                String notes = cursor.getString(notesColumnIndex);

                // Update the views on the screen with the values from the database
                mNameEditText.setText(name);
                mnotesEditText.setText(notes);
                mstartDate.setText(stdate);
                mstartTime.setText(sttime);
                mendTime.setText(ettime);

                // Skill is a dropdown spinner, so map the constant value from the database
                // into one of the dropdown options (0 is Unknown, 1 is Male, 2 is Female).
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
            mSkillSpinner.setSelection(0); // Select "Unknown" skill
        }

        /**
         * Show a dialog that warns the user there are unsaved changes that will be lost
         * if they continue leaving the editor.
         *
         * @param discardButtonClickListener is the click listener for what to do when
         *                                   the user confirms they want to discard their changes
         */
        private void showUnsavedChangesDialog(
                DialogInterface.OnClickListener discardButtonClickListener) {
            // Create an AlertDialog.Builder and set the message, and click listeners
            // for the postivie and negative buttons on the dialog.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.unsaved_changes_dialog_msg);
            builder.setPositiveButton(R.string.discard, discardButtonClickListener);
            builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
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
            builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User clicked the "Delete" button, so delete the game.
                    deleteGame();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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
                    Toast.makeText(this, getString(R.string.editor_delete_game_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the delete was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_delete_game_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }

            // Close the activity
            finish();
        }
    }

    /**
     * {@link GameCursorAdapter} is an adapter for a list view
     * that uses a {@link Cursor} of Game data as its data source. This adapter knows
     * how to create list items for each row of game data in the {@link Cursor}.
     */
    public static class GameCursorAdapter extends CursorAdapter {

        /**
         * Constructs a new {@link GameCursorAdapter}.
         *
         * @param context The context
         * @param c       The cursor from which to get the data.
         */
        public GameCursorAdapter(Context context, Cursor c) {
            super(context, c, 0 /* flags */);
        }

        /**
         * Makes a new blank list item view. No data is set (or bound) to the views yet.
         *
         * @param context app context
         * @param cursor  The cursor from which to get the data. The cursor is already
         *                moved to the correct position.
         * @param parent  The parent to which the new view is attached to
         * @return the newly created list item view.
         */
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            // Inflate a list item view using the layout specified in list_item.xml
            return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        /**
         * This method binds the game data (in the current row pointed to by cursor) to the given
         * list item layout. For example, the name for the current game can be set on the name TextView
         * in the list item layout.
         *
         * @param view    Existing view, returned earlier by newView() method
         * @param context app context
         * @param cursor  The cursor from which to get the data. The cursor is already moved to the
         *                correct row.
         */
        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            // Find individual views that we want to modify in the list item layout
            TextView nameTextView = (TextView) view.findViewById(R.id.name);
            TextView startdate = (TextView) view.findViewById(R.id.startdate);
            TextView summaryTextView = (TextView) view.findViewById(R.id.summary);

            // Find the columns of game attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_DESC);
            int startDateIndex = cursor.getColumnIndex(GameEntry.COLUMN_START_DATE);
            int notesIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NOTES);

            // Read the game attributes from the Cursor for the current game
            String gameName = cursor.getString(nameColumnIndex);
            String stDate = cursor.getString(startDateIndex);
            String notes = cursor.getString(notesIndex);

            // Update the TextViews with the attributes for the current Game
            nameTextView.setText(gameName);
            startdate.setText(stDate);
            summaryTextView.setText(notes);
        }
    }
}