package com.my.game.wesport.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.my.game.wesport.R;
import com.my.game.wesport.activity.GroupActivity;
import com.my.game.wesport.activity.InvitesActivity;
import com.my.game.wesport.adapter.InvitesAdapter;
import com.my.game.wesport.helper.FirebaseHelper;
import com.my.game.wesport.model.GameInviteModel;
import com.my.game.wesport.model.GameModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class InvitesListFragment extends Fragment implements InvitesAdapter.GameAdapterListener {
    private InvitesAdapter invitesAdapter;
    private String TAG = InvitesListFragment.class.getSimpleName();
    private View emptyView;

    public static InvitesListFragment newInstance() {
        Bundle args = new Bundle();
        InvitesListFragment fragment = new InvitesListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public InvitesListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invites_list, container, false);
        setupRecyclerView((RecyclerView) view.findViewById(R.id.recycler_view));
        emptyView = view.findViewById(R.id.empty_view);
        setupInvitesDataListener();
        return view;
    }

    private void setupInvitesDataListener() {
        FirebaseHelper.getInvitesRef(FirebaseHelper.getCurrentUser().getUid()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                GameInviteModel invite = dataSnapshot.getValue(GameInviteModel.class);
                Log.d(TAG, "onChildAdded: dataSnapshot: " + dataSnapshot.getRef());
                Log.d(TAG, "onChildAdded: invite: " + invite.toString());

                if (invite.isRejected() || invite.isAccepted()) {
                    return;
                }
                if (TextUtils.isEmpty(invite.getGameKey())) {
                    return;
                }
                FirebaseHelper.getGamesRef(invite.getAuthorUid()).child(invite.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        invitesAdapter.add(dataSnapshot);
                        updateEmptyView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                GameInviteModel invite = dataSnapshot.getValue(GameInviteModel.class);
                if (invite.isRejected() || invite.isAccepted()) {
                    invitesAdapter.remove(invite.getGameKey());
                    updateEmptyView();
                } else {
                    FirebaseHelper.getGamesRef(invite.getAuthorUid()).child(invite.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            invitesAdapter.update(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                GameInviteModel invite = dataSnapshot.getValue(GameInviteModel.class);
                FirebaseHelper.getGamesRef(invite.getAuthorUid()).child(invite.getGameKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        invitesAdapter.remove(dataSnapshot.getKey());
                        updateEmptyView();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateEmptyView() {
        if (invitesAdapter.getItemCount() > 0) {
            emptyView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView(RecyclerView recyclerView) {
        invitesAdapter = new InvitesAdapter(getActivity(), this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(invitesAdapter);
    }

    @Override
    public void onGameClick(int position, DataSnapshot snapshot) {

    }

    @Override
    public void onAcceptClick(int position, DataSnapshot snapshot) {
        FirebaseHelper.acceptGameInvitation(snapshot.getKey());
        GameModel gameModel = snapshot.getValue(GameModel.class);
        /*InvitesActivity activity = (InvitesActivity) getActivity();
        if (activity != null) {
            activity.switchPage(0);
        }*/
        startActivity(new Intent(GroupActivity.newIntent(getActivity(), snapshot.getKey(),  gameModel.getAuthor())));
    }

    @Override
    public void onRejectClick(int position, DataSnapshot snapshot) {
        FirebaseHelper.rejectGameInvitation(snapshot.getKey());
    }
}
