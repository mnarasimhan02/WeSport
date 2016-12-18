package com.example.android.wesport;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.IOException;
import java.util.ArrayList;


public class MyGames extends AppCompatActivity {


    ArrayList<String> savedGames=new ArrayList<>();
    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_games);
        savedGames.clear();
        ListView listView=(ListView) findViewById(R.id.listview);
        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        try {
            savedGames=(ArrayList<String>)ObjectSerializer.deserialize(prefs.getString("games",
                    ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //games.add("My Saved Games...");
        arrayAdapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, savedGames);
        listView.setAdapter(arrayAdapter);

        //  }
       /* listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)   {
                Toast.makeText(MyGames.this, i, Toast.LENGTH_SHORT).show();
            }
        });
        */
    }
}