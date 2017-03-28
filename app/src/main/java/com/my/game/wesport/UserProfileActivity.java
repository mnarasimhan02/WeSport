package com.my.game.wesport;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.UserModel;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;


public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "TAG";
    //private static final int REQUEST_SELECT_IMAGE = 100;

    private EditText nameText, bioText;
    private TextView emailText;
    private ImageView userProfileImage, userProfileCoverImage;
    private Toolbar toolbar;

    private boolean isEditable = false;
    private Uri mCropImageUri;

    private UserModel userModelProfile;

    // Firebase instance variables
    private DatabaseReference userRef;
    public final static int EDIT_COVER_IMAGE = 1;
    public final static int EDIT_AVATAR = 2;
    private int editImageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        userRef = FirebaseDatabase.getInstance().getReference().child("users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        nameText = (EditText) findViewById(R.id.edit_user_profile_name);
        bioText = (EditText) findViewById(R.id.user_bio);
        emailText = (TextView) findViewById(R.id.email_user_profile);
        userProfileImage = (ImageView) findViewById(R.id.user_profile_image);
        userProfileCoverImage = (ImageView) findViewById(R.id.user_profile_cover_image);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        userProfileImage.setOnClickListener(this);
        userModelProfile = App.getInstance().getUserModel();
        updateViews(userModelProfile);
    }

    @Override
    public void onClick(View view) {
        if (!isEditable) {
            return;
        }
        avatarEdit();
    }

    public void onCoverImageClick(View view) {
        if (!isEditable) {
            return;
        }
        coverImageEdit();
    }

    public void coverImageEdit() {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            }
        } else {
            CropImage.startPickImageActivity(this);
            editImageType = EDIT_COVER_IMAGE;
        }
    }

    public void avatarEdit() {
        if (CropImage.isExplicitCameraPermissionRequired(this)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
            }
        } else {
            CropImage.startPickImageActivity(this);
            editImageType = EDIT_AVATAR;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
                Uri imageUri = CropImage.getPickImageResultUri(this, data);
                // For API >= 23 we need to check specifically that we have permissions to read external storage.
                if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                    // request permissions and handle the result in onRequestPermissionsResult()
                    mCropImageUri = imageUri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{
                                android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
                    }
                } else {
                    // no permissions required or already grunted, can start crop image activity
                    startCropImageActivity(imageUri);
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                Uri resultUri = result.getUri();
                FirebaseHelper.uploadPicture(resultUri, editImageType);
                if (editImageType == EDIT_COVER_IMAGE) {
                    Glide.with(this).load(resultUri).into(userProfileCoverImage);
                } else {
                    Glide.with(this).load(resultUri).into(userProfileImage);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this);
            } else {
                Toast.makeText(this, R.string.cancelling_required_permissions_denied, Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(this, R.string.cancelling_required_permissions_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startCropImageActivity(Uri imageUri) {
        if (editImageType == EDIT_AVATAR) {
            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setBorderLineColor(Color.RED)
                    .setGuidelinesColor(Color.GREEN)
                    .setBorderLineThickness(getResources().getDimensionPixelSize(R.dimen.thickness))
                    .setAutoZoomEnabled(true)
                    .start(this);
        } else {
            CropImage.activity(imageUri)
                    .setAspectRatio(16, 9)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setBorderLineColor(Color.RED)
                    .setGuidelinesColor(Color.GREEN)
                    .setBorderLineThickness(getResources().getDimensionPixelSize(R.dimen.thickness))
                    .setAutoZoomEnabled(true)
                    .start(this);
        }
    }

    private void updateViews(UserModel userModelProfile) {
        nameText.setText(userModelProfile.getDisplayName());
        emailText.setText(userModelProfile.getEmail());
        bioText.setText(userModelProfile.getBio());
        Glide.with(this).load(userModelProfile.getPhotoUri()).error(R.drawable.profile).into(userProfileImage);
        Glide.with(this).load(userModelProfile.getCoverUri()).into(userProfileCoverImage);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_activity_profile, menu);
        MenuItem item = menu.findItem(R.id.action_edit);
        if (isEditable) {
            item.setIcon(R.drawable.ic_done);
        } else {
            item.setIcon(R.drawable.ic_create_24dp);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_edit:
                changeState(!isEditable);
                invalidateOptionsMenu();
                break;
            case R.id.sign_out_menu:
                break;
        }
        return true;
    }

    private void changeState(boolean state) {
        isEditable = state;
        if (state) {
            nameText.setEnabled(true);
            emailText.setEnabled(true);
            bioText.setEnabled(true);
            userProfileImage.setEnabled(true);
            userProfileCoverImage.setEnabled(true);
        } else {
            userModelProfile.setBio(bioText.getText().toString());
            userModelProfile.setDisplayName(nameText.getText().toString());
            userRef.setValue(userModelProfile);

            nameText.setEnabled(false);
            emailText.setEnabled(false);
            bioText.setEnabled(false);
            userProfileImage.setEnabled(false);
            userProfileCoverImage.setEnabled(false);
        }
    }

}
