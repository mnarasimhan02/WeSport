package com.my.game.wesport.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.R;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.UserModel;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class OtherUserProfileActivity extends AppCompatActivity {

    private static String EXTRA_USER_ID = "user_id";
    private static String EXTRA_GAME_KEY = "game_key";
    private ImageView coverImage, avatarImage;
    private TextView nameText, emailText, bioText;
    private UserModel userModel;
    private String userUid;
    private String gameKey;

    public static Intent newIntent(Context context, String userUid, String gameKey) {
        Intent chatIntent = new Intent(context, OtherUserProfileActivity.class);
        chatIntent.putExtra(EXTRA_USER_ID, userUid);
        chatIntent.putExtra(EXTRA_GAME_KEY, gameKey);
        return chatIntent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_profile);

        coverImage = (ImageView) findViewById(R.id.other_user_cover_image);
        avatarImage = (ImageView) findViewById(R.id.other_user_profile_image);
        nameText = (TextView) findViewById(R.id.other_user_name);
        emailText = (TextView) findViewById(R.id.other_user_email);
        bioText = (TextView) findViewById(R.id.other_user_bio);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        userUid = getIntent().getStringExtra(EXTRA_USER_ID);
        gameKey = getIntent().getStringExtra(EXTRA_GAME_KEY);
        FirebaseHelper.getUserRef().child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userModel = dataSnapshot.getValue(UserModel.class);
                updateViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateViews() {
        if (userModel == null) {
            Toast.makeText(this, "Invalid user id.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        nameText.setText(userModel.getDisplayName());
        emailText.setText(userModel.getEmail());
        bioText.setText(userModel.getBio());
        Glide.with(this)
                .load(userModel.getCoverUri())
                .error(R.drawable.image_placeholder_drawable)
                .into(coverImage);
        Glide.with(this)
                .load(userModel.getPhotoUri())
                .error(R.drawable.profile)
                .into(avatarImage);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    public void onRemoveUserClick(View view) {
        showDeleteConfirmationDialog(userUid);
    }


    private void showDeleteConfirmationDialog(final String userUid) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.delete_user_dialog_msg, userModel.getDisplayName()));
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                removeUserFromGameList(userUid);
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void removeUserFromGameList(String potentialUserId) {
        FirebaseHelper.removeFromGame(gameKey, potentialUserId);
    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
