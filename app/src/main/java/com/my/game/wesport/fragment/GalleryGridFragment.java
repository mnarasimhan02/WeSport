package com.my.game.wesport.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.FullscreenImageActivity;
import com.my.game.wesport.adapter.GridRecyclerViewAdapter;
import com.my.game.wesport.adapter.SpacesItemDecoration;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.FGridImage;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class GalleryGridFragment extends Fragment implements GridRecyclerViewAdapter.ClickListener, DragSelectRecyclerViewAdapter.SelectionListener {


    private static String EXTRA_GAMES_KEY = "game_key";
    private GridRecyclerViewAdapter gridRecyclerViewAdapter;
    private Uri mCropImageUri;
    private RecyclerView recyclerView;
    private String TAG = GalleryGridFragment.class.getSimpleName();
    private String gameKey;
    private boolean isFirstInit = true;

    public static GalleryGridFragment newInstance(String gameKey) {
        Bundle args = new Bundle();
        args.putString(EXTRA_GAMES_KEY, gameKey);
        GalleryGridFragment fragment = new GalleryGridFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public GalleryGridFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery_grid, container, false);

        gameKey = getArguments().getString(EXTRA_GAMES_KEY, "");

        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        view.findViewById(R.id.add_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestCameraImage();
            }
        });
        setupGridImages();
        return view;
    }

    private void setupGridImages() {
        gridRecyclerViewAdapter = new GridRecyclerViewAdapter(this, getContext());
        gridRecyclerViewAdapter.setSelectionListener(this);
        recyclerView.addItemDecoration(new SpacesItemDecoration(getResources().getDimensionPixelSize(R.dimen.grid_image_space)));
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(gridRecyclerViewAdapter);
        updateGridImagesInList();
    }


    private void updateGridImagesInList() {
        FirebaseHelper.getGameImagesRef(gameKey).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                gridRecyclerViewAdapter.add(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                gridRecyclerViewAdapter.remove(dataSnapshot);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*
        FirebaseHelper.getGameImagesRef(gameKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GridImages gridImagesData = dataSnapshot.getValue(GridImages.class);
                if (gridImagesData != null) {
                    gridImages = gridImagesData;
                } else {
                    gridImages = new GridImages();
                }
                if (isFirstInit) {
                    isFirstInit = false;
                    updateGridImagesList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });*/
    }

    private void requestCameraImage() {
        if (CropImage.isExplicitCameraPermissionRequired(getContext())) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE);
        } else {
            CropImage.startPickImageActivity(getActivity());
        }
    }


    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(getActivity());
            } else {
                Toast.makeText(getContext(), R.string.error_permission_not_granted, Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri);
            } else {
                Toast.makeText(getContext(), R.string.cancelling_required_permissions_denied, Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(getContext(), data);
            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(getContext(), imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri;
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE);
            } else {
                // no permissions required or already grunted, can start crop image activity
                startCropImageActivity(imageUri);
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                Uri resultUri = result.getUri();
                String realPath = null;
                try {
                    realPath = getRealPathFromURI(resultUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (TextUtils.isEmpty(realPath)) {
                    realPath = resultUri.toString();
                }

                String uploadedFileName = FirebaseHelper.uploadGameImage(gameKey, resultUri, new FirebaseHelper.FileUploadListener() {
                    @Override
                    public void imageUploaded(Uri fileUri) {
                        FGridImage fGridImage = new FGridImage(fileUri.toString(), fileUri.toString());
                        FirebaseHelper.getGameImagesRef(gameKey).push().setValue(fGridImage);
                    }
                });
                gridRecyclerViewAdapter.addLocalImage(realPath, uploadedFileName);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.d(TAG, "onActivityResult: " + error.getMessage());
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) throws Exception {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void startCropImageActivity(Uri imageUri) {
        CropImage.activity(imageUri)
                .setAspectRatio(1, 1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setBorderLineColor(Color.RED)
                .setGuidelinesColor(Color.GREEN)
                .setBorderLineThickness(getResources().getDimensionPixelSize(R.dimen.thickness))
                .setAutoZoomEnabled(true)
                .start(getActivity());
    }

    @Override
    public void onGridClick(int index) {
        startActivity(FullscreenImageActivity.newIntent(getActivity(), gridRecyclerViewAdapter.getImages(), index));
    }

    @Override
    public void onGridLongClick(int index) {

    }

    @Override
    public void onDragSelectionChanged(int count) {

    }
}
