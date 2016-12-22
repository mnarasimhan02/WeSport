package com.example.android.wesport;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.widget.ArrayAdapter;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;

import java.io.IOException;
import java.util.ArrayList;

import static com.example.android.wesport.R.id.listView;


public class MyGames extends AppCompatActivity {

    ArrayList<String> savedGames=new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;
    private SwipeMenuListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_games);
        savedGames.clear();
        // ListView listView=(ListView) findViewById(R.id.listView);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            savedGames = (ArrayList<String>) ObjectSerializer.deserialize(prefs.getString("games",
                    ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //games.add("My Saved Games...");
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, savedGames);
        mListView = (SwipeMenuListView) findViewById(listView);
        mListView.setAdapter(arrayAdapter);
        //Added swipe layout

        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {
                // create "delete" item
                SwipeMenuItem chatItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                // deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                //         0x3F, 0x25)));
                // set item width
                chatItem.setWidth(100);
                chatItem.setTitle("chat");
                chatItem.setTitleColor(Color.WHITE);
                // set a icon
                chatItem.setIcon(R.drawable.ic_chat);
                // add to menu
                menu.addMenuItem(chatItem);
                // create "chat" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                // deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                //         0x3F, 0x25)));
                // set item width
                deleteItem.setWidth(100);
                deleteItem.setTitle("del");
                deleteItem.setTitleColor(Color.RED);
                // set a icon
                deleteItem.setIcon(R.drawable.ic_delete);
                // add to menu
                menu.addMenuItem(deleteItem);

            }

        };
        // set creator
        mListView.setMenuCreator(creator);
        // set SwipeListener
        mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

            @Override
            public void onSwipeStart(int position) {
                // swipe start
            }

            @Override
            public void onSwipeEnd(int position) {


            }
        });

        //* step 2. listener item click event
        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                // swipe end

                String item = savedGames.get(position);
                switch (index) {
                    case 1:
                        try {
                            savedGames.remove(position);
                            arrayAdapter.notifyDataSetChanged();
                            break;
                        } catch (NumberFormatException ex) { // handle your exception
                            ex.printStackTrace();
                        }

                    case 0:
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        startActivity(intent);
                }
                return false;
            }
        });

    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

}

//  }
       /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)   {
                Toast.makeText(MyGames.this, i, Toast.LENGTH_SHORT).show();
            }
        });
        */