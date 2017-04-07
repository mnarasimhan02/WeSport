package com.my.game.wesport.fragment;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.my.game.wesport.BuildConfig;
import com.my.game.wesport.R;
import com.my.game.wesport.adapter.AllContactsAdapter;
import com.my.game.wesport.model.LocaleContact;

import java.util.ArrayList;
import java.util.List;

import static com.facebook.FacebookSdk.getApplicationContext;

public class LocaleContactListFragment extends Fragment implements AllContactsAdapter.SimpleContactListListener {
    public static final String EXTRA_GAME_KEY = "game_key";
    public static final int REQUEST_PERMISSION = 32;
    RecyclerView mRecyclerView;
    private List<LocaleContact> localContactList;

    String gameKey = "";

    public static LocaleContactListFragment newInstance(String gameKey) {
        Bundle args = new Bundle();
        args.putString(EXTRA_GAME_KEY, gameKey);
        LocaleContactListFragment fragment = new LocaleContactListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_locale_contact_list, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.locale_contacts_recycler_view);
        gameKey = getArguments().getString(EXTRA_GAME_KEY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(android.Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.WRITE_CONTACTS}, REQUEST_PERMISSION);
        } else {
            setupLocalContactList();
        }

        return rootView;
    }

    private void setupLocalContactList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getAllContacts();
                FragmentActivity activity = getActivity();
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupAdapter();
                        }
                    });
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupLocalContactList();
        }
    }

    private void getAllContacts() {
        localContactList = new ArrayList();
        LocaleContact contactVO;

        ContentResolver contentResolver = getActivity().getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                if (hasPhoneNumber > 0) {
                    String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    contactVO = new LocaleContact();
                    contactVO.setContactName(name);

                    Cursor phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    if (phoneCursor.moveToNext()) {
                        String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactVO.setContactNumber(phoneNumber);
                    }

                    phoneCursor.close();

                    Cursor emailCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (emailCursor.moveToNext()) {
                        String emailId = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    }
                    localContactList.add(contactVO);
                }
            }
        }
    }

    private void setupAdapter() {
        AllContactsAdapter contactAdapter = new AllContactsAdapter(localContactList, getApplicationContext(), this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(contactAdapter);
    }

    @Override
    public void onContactClick(int position, LocaleContact localeContact) {
       /* FirebaseUser currentUser = FirebaseHelper.getCurrentUser();
        UserModel userModel = App.getInstance().getUserModel();
        GameInviteModel gameInviteModel = new GameInviteModel("", localeContact.getContactName(), localeContact.getContactName());
        FirebaseHelper.inviteUserInGame(localeContact.getContactNumber(), gameInviteModel);
        NotificationHelper.sendMessageByTopic(localeContact.getContactName(), "Invitation to game", "by " + userModel.getDisplayName(), "", NotificationHelper.getInvitationMessage(currentUser.getUid()));
        Toast.makeText(getActivity(), "Invitation sent to " + localeContact.getContactName(), Toast.LENGTH_SHORT).show();
    */
    }
}
