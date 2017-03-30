package com.my.game.wesport;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.User;
import com.squareup.picasso.Picasso;


public class UserProfile extends AppCompatActivity implements View.OnClickListener {
    //private static final int REQUEST_SELECT_IMAGE = 100;

    private EditText nameText, bioText;
    private TextView emailText;
    private ImageView userProfileImage;
    private Toolbar toolbar;
    private ActionMode editProfileActionMode;

    private int currentState;
    private Uri mCapturedImageURI;
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final String BUNDLE_STATE = "BUNDLE_STATE";

    private static final int STATE_VIEWING = 1;
    private static final int STATE_EDITING = 2;
    private User userProfile;

    // Firebase instance variables
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_user);

        userRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        //setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        nameText = (EditText) findViewById(R.id.edit_user_profile_name);
        bioText = (EditText) findViewById(R.id.edit_Edit_user_bio);
        emailText = (TextView) findViewById(R.id.email_user_profile);
        userProfileImage = (ImageView) findViewById(R.id.user_profile_image);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        userProfileImage.setOnClickListener(this);

        if (savedInstanceState == null) {
            changeState(STATE_VIEWING);
        } else {
            changeState(savedInstanceState.getInt(BUNDLE_STATE));
        }

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                    userProfile = dataSnapshot.getValue(User.class);
                    updateViews(userProfile);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();

        if (viewId == R.id.user_profile_image || viewId == R.id.frame_layout_user_profile) ;
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_dialog_box);
        dialog.setTitle("Choose Your Profile Image");
        dialog.findViewById(R.id.btnChoosePath)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activeGallery();

                        dialog.dismiss();
                    }
                });
        dialog.findViewById(R.id.btnTakePhoto)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activeTakePhoto();

                        dialog.dismiss();
                    }
                });
        // show dialog on screen
        dialog.show();
    }

    private void activeTakePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            String fileName = "temp.jpg";
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            mCapturedImageURI = getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            values);
            takePictureIntent
                    .putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void activeGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_LOAD_IMAGE:
                if (requestCode == RESULT_LOAD_IMAGE &&
                        resultCode == RESULT_OK && null != data) {
                    Uri selectedImage = data.getData();
                   // helper.uploadPicture(selectedImage);
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver()
                            .query(selectedImage, filePathColumn, null, null,
                                    null);
                    cursor.moveToFirst();
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    String picturePath = cursor.getString(columnIndex);
                    cursor.close();

                    userProfileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));

                }
            case REQUEST_IMAGE_CAPTURE:
                if (requestCode == REQUEST_IMAGE_CAPTURE &&
                        resultCode == RESULT_OK) {
                    String[] projection = {MediaStore.Images.Media.DATA};
                    Cursor cursor =
                            managedQuery(mCapturedImageURI, projection, null,
                                    null, null);
                   // helper.uploadPicture(mCapturedImageURI);
                    int column_index_data = cursor.getColumnIndexOrThrow(
                            MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String picturePath = cursor.getString(column_index_data);
                    userProfileImage.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }
        }
    }

    private void updateViews(User userProfile) {
        nameText.setText(userProfile.getDisplayName());
        emailText.setText(userProfile.getEmail());
        bioText.setText(userProfile.getBio());
        Glide.with(this).load(userProfile.getPhotoUri()).error(R.drawable.profile).into(userProfileImage);
        //userProfileImage.setImageURI(Uri.parse(userProfile.getPhotoUri()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(UserProfile.this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.action_edit:
                changeState(STATE_EDITING);
                break;
        }

        return true;
    }

    private void changeState(int state) {
        if (state == currentState)
            return;
        currentState = state;
        if (state == STATE_VIEWING) {
            nameText.setEnabled(false);
            emailText.setEnabled(false);
            bioText.setEnabled(false);
            toolbar.setVisibility(View.VISIBLE);
            userProfileImage.setEnabled(false);

            if (editProfileActionMode != null) {
                editProfileActionMode.finish();
                editProfileActionMode = null;
            }
        } else if (state == STATE_EDITING) {
            nameText.setEnabled(true);
            emailText.setEnabled(true);
            bioText.setEnabled(true);
            toolbar.setVisibility(View.GONE);
            userProfileImage.setEnabled(true);

            editProfileActionMode = toolbar.startActionMode(new EditProfileActionCallback());

        } else
            throw new IllegalArgumentException("Invalid State" + state);
    }


    private class EditProfileActionCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_activity_profile_edit, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int itemId = item.getItemId();
            if (itemId == R.id.activity_profile_edit_menuDone) {
                userProfile.setBio(bioText.getText().toString());
                userProfile.setDisplayName(nameText.getText().toString());
                userRef.setValue(userProfile);
                changeState(STATE_VIEWING);
                return true;
            }

            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            if (currentState != STATE_VIEWING)
                changeState(STATE_VIEWING);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putInt(BUNDLE_STATE, currentState);
        // Save the user's current game state
        if (mCapturedImageURI != null) {
            outState.putString("mCapturedImageURI",
                    mCapturedImageURI.toString());
        }
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        if (savedInstanceState.containsKey("mCapturedImageURI")) {
            mCapturedImageURI = Uri.parse(
                    savedInstanceState.getString("mCapturedImageURI"));
        }
    }
}
