
package com.example.android.wesport;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.wesport.data.GameContract.GameEntry;

/**
 * {@link GameCursorAdapter} is an adapter for a list view
 * that uses a {@link Cursor} of Game data as its data source. This adapter knows
 * how to create list items for each row of game data in the {@link Cursor}.
 */
public class GameCursorAdapter extends CursorAdapter {

    public GameCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

   @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the game data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current game can be set on the name TextView
     * in the list item layout.
     */


    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView startdate = (TextView) view.findViewById(R.id.startdate);
        TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        ImageView chatview =(ImageView) view.findViewById(R.id.chatimage);

        // Find the columns of game attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_DESC);
        int startDateIndex = cursor.getColumnIndex(GameEntry.COLUMN_START_DATE);
        int notesIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NOTES);
        int locColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_ADDRESS);


        // Read the game attributes from the Cursor for the current game
        String gameName = cursor.getString(nameColumnIndex);
        String stDate = cursor.getString(startDateIndex);
        String gameaddress = cursor.getString(locColumnIndex);

        String notes = cursor.getString(notesIndex);

        // Update the TextViews with the attributes for the current Game
        nameTextView.setText(gameName);
        startdate.setText(stDate+" "+gameaddress);
        summaryTextView.setText(notes);

        chatview.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChatActivity.class);
                v.getContext().startActivity(intent);
            }
        });
    }


}
