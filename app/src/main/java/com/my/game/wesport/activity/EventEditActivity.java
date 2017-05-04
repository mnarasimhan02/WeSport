package com.my.game.wesport.activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.my.game.wesport.R;
import com.my.game.wesport.event.NewGameEventAdded;
import com.my.game.wesport.helper.DateHelper;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.EventModel;

import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EventEditActivity extends AppCompatActivity {
    private static String EXTRA_GAME_AUTHOR_ID = "game_author";
    private EditText mEventDate;
    private EditText mEventTime;
    private final Calendar mDateAndTime = Calendar.getInstance();
    EventBus eventBus = EventBus.getDefault();

    /**
     * Boolean flag that keeps track of whether the game has been edited (true) or not (false)
     */
    private boolean eventHasChanged = false;

    /**
     * GameModel for the existing game (null if it's a new game)
     */
    private static DataSnapshot currentEventDataSnapShot = null;

    /**
     * EditText field to enter the game's name
     */
    private EditText mEventTitleEditText;
    /**
     * EditText field to enter the game's notes
     */
    private EditText mEventDesEditText;
    private String gameKey;
    private String gameAuthorId;
    private static final String EXTRA_GAME_KEY = "game_key";

    /**
     * EditText field to enter the game skill
     */

    /*if eventDataSnapShot null then its mean create new game*/
    public static Intent newIntent(Context context, String gameKey, String gameAuthorId, DataSnapshot eventDataSnapShot) {
        currentEventDataSnapShot = eventDataSnapShot;

        Intent intent = new Intent(context, EventEditActivity.class);
        intent.putExtra(EXTRA_GAME_KEY, gameKey);
        intent.putExtra(EXTRA_GAME_AUTHOR_ID, gameAuthorId);
        return intent;
    }

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the eventHasChanged boolean to true.
     */
    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            eventHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Edit Event");
        }

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_GAME_KEY)) {
            gameKey = intent.getStringExtra(EXTRA_GAME_KEY);
            gameAuthorId = intent.getStringExtra(EXTRA_GAME_AUTHOR_ID);
        } else {
            Toast.makeText(this, "Invalid game!", Toast.LENGTH_SHORT).show();
        }

        // Find all relevant views that we will need to read user input from
        mEventTitleEditText = (EditText) findViewById(R.id.edit_event_title);
        mEventDesEditText = (EditText) findViewById(R.id.edit_event_description);
        mEventDate = (EditText) findViewById(R.id.edit_event_date);
        mEventTime = (EditText) findViewById(R.id.edit_event_time);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mEventTitleEditText.setOnTouchListener(mTouchListener);
        mEventDesEditText.setOnTouchListener(mTouchListener);
        mEventDate.setOnTouchListener(mTouchListener);
        mEventTime.setOnTouchListener(mTouchListener);


        if (currentEventDataSnapShot == null) {
            // This is a new game, so change the app bar to say "Add a GameModel"
            setTitle("Add a event");

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing GameModel, so change app bar to say "Edit GameModel"
            setTitle("Edit Event");

            // and display the current values in the editor
            setExistingData();
        }
    }

    public void onDateClicked(View v) {

        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateDisplay(mEventDate);
                timePicker(null);
            }
        };

        DatePickerDialog startDate = new DatePickerDialog(
                EventEditActivity.this, mDateListener,
                mDateAndTime.get(Calendar.YEAR),
                mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH)
        );
        startDate.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        startDate.show();
    }

    public void timePicker(View v) {

        TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDateAndTime.set(Calendar.MINUTE, minute);
                updateTimeDisplay(mEventTime);
            }
        };

        new TimePickerDialog(EventEditActivity.this, mTimeListener,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), false).show();

    }

    private void updateDateDisplay(TextView mtextview) {
        long today = mDateAndTime.getTimeInMillis();
        String dateString = DateHelper.getServerDateFormatter().format(today);
        mtextview.setText(dateString);
    }

    private void updateTimeDisplay(TextView mtextview) {
        mtextview.setText(DateUtils.formatDateTime(this, mDateAndTime.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_TIME));
    }

    private void saveEvent() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String eventTitle = mEventTitleEditText.getText().toString().trim();
        String eventDes = mEventDesEditText.getText().toString().trim();
        String eventDate = mEventDate.getText().toString().trim();
        String eventTime = mEventTime.getText().toString().trim();

        // Check if this is supposed to be a new game
        // and check if all the fields in the editor are blank
        if ((TextUtils.isEmpty(eventTitle) || TextUtils.isEmpty(eventDate) || TextUtils.isEmpty(eventTime))) {
            // Since no fields were modified, we can return early without creating a new game.
            // No need to create ContentValues and no need to do any ContentProvider operations.
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.editor_insert_event_params),
                    Snackbar.LENGTH_LONG).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and game attributes from the editor are the values.
        EventModel eventModel = new EventModel(
                eventTitle,
                eventDes,
                eventDate,
                eventTime,
                FirebaseAuth.getInstance().getCurrentUser().getUid()
        );

        // Determine if this is a new or existing event by checking if currentEventKey is null or not
        if (currentEventDataSnapShot == null) {
            // This is a NEW event, so insert a new event,
            FirebaseHelper.getEventRef(gameKey).push().setValue(eventModel);
            eventBus.post(new NewGameEventAdded(gameKey, gameAuthorId, eventModel));
            Snackbar.make(findViewById(android.R.id.content), getString(R.string.editor_insert_event_successful),
                    Snackbar.LENGTH_LONG).show();
        } else {
            // Otherwise this is an EXISTING event, so update the event
            // we want to modify.

            FirebaseHelper.getEventRef(gameKey).child(currentEventDataSnapShot.getKey()).setValue(eventModel);
        }
//        writeCalendarEvent(eventTitle, eventDes, eventDate, eventTime);

    }


    public void setExistingData() {
        // Extract out the value from the Cursor for the given column index
        EventModel eventModel = currentEventDataSnapShot.getValue(EventModel.class);
        String title = eventModel.getTitle();
        String desc = eventModel.getDescription();
        String stdate = eventModel.getStartDate();
        String sttime = eventModel.getTime();

        // Update the views on the screen with the values from the database
        mEventTitleEditText.setText(title);
        mEventDesEditText.setText(desc);
        mEventDate.setText(stdate);
        mEventTime.setText(sttime);


    }

    private void writeCalendarEvent(String eventTitle, String eventDes, String eDate, String eTime) {

        SimpleDateFormat calstDatFormat;
        final ContentValues event = new ContentValues();
        final Calendar c = Calendar.getInstance();
        int yy = c.get(Calendar.YEAR);

        if (eDate.equals("")) {
            calstDatFormat = new SimpleDateFormat("MMMMM dd yyyy", Locale.getDefault());

        } else {
            calstDatFormat = new SimpleDateFormat("MMMMM dd h:mm a yyyy", Locale.getDefault());
        }

        long stdateInLong = 0, etdateInLong = 0;
        try {
            Date BEGIN_TIME = calstDatFormat.parse(eDate);
            stdateInLong = BEGIN_TIME.getTime();

        } catch (ParseException e) {
            e.printStackTrace();
        }

        event.put(CalendarContract.Events.CALENDAR_ID, 1);
        event.put(CalendarContract.Events.TITLE, eventTitle);
        event.put(CalendarContract.Events.DESCRIPTION, eventDes);
        event.put(CalendarContract.Events.DTSTART, stdateInLong);//startTimeMillis
        event.put(CalendarContract.Events.ALL_DAY, 0); // 0 for false, 1 for true
        String timeZone = TimeZone.getDefault().getID();
        event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

        Uri baseUri;
        baseUri = Uri.parse("content://com.android.calendar/events");
        getApplicationContext().getContentResolver().insert(baseUri, event);

    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the event hasn't changed, continue with handling back button press
        if (!eventHasChanged) {
            super.onBackPressed();
            return;
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
        // If this is a new event, hide the "Delete" menu item.
        if (currentEventDataSnapShot == null) {
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
                // Save event to database
                saveEvent();
                //Show snackbar and then finish activity
                Toast.makeText(this, R.string.calendar_add_event, Toast.LENGTH_SHORT).show();
                EventEditActivity.this.finish();
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
                // which is the {@link EventListFragment}.
                if (!eventHasChanged) {
                    finish();
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
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
                // UserModel clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the event.
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
     * Prompt the user to confirm that they want grouto delete this event.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_event_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // UserModel clicked the "Delete" button, so delete the event.
                deleteEvent();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // UserModel clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the event.
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
     * Perform the deletion of the event in the database.
     */
    private void deleteEvent() {
        // Only perform the delete if this is an existing event.
        if (currentEventDataSnapShot != null) {
//            FirebaseHelper.getEventRef(currentEventDataSnapShot.getKey()).removeValue();
            currentEventDataSnapShot.getRef().removeValue();
        }
        // Close the activity
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        currentEventDataSnapShot = null;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
