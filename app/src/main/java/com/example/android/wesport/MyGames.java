package com.example.android.wesport;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;


public class MyGames extends AppCompatActivity {

    static ArrayList<String> games =new ArrayList<>();
    static ArrayList<LatLng> locations=new ArrayList<>();
    static ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_games);
        ListView listView=(ListView) findViewById(R.id.listview);
        locations.add(new LatLng(0,0));
        games.add ("My Saved Games...");
        arrayAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,games);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)   {
                Toast.makeText(MyGames.this, i, Toast.LENGTH_SHORT).show();
            }

        });
    }
}
