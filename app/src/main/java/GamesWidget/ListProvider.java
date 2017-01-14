package gameswidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import com.example.android.wesport.ObjectSerializer;
import com.example.android.wesport.R;

import java.io.IOException;
import java.util.ArrayList;

/**
 * If you are familiar with Adapter of ListView,this is the same as adapter
 * with few changes
 *
 */
public class ListProvider implements RemoteViewsFactory {
    private ArrayList<gameswidget.ListItem> listItemList = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;
    ArrayList<String> savedGames=new ArrayList<>();


    public ListProvider(Context context, Intent intent) {
        this.context = context;
        // Context applicationContext = MainActivity.getContextOfApplication();

        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            savedGames = (ArrayList<String>) ObjectSerializer.deserialize(prefs.getString("games",
                    ObjectSerializer.serialize(new ArrayList<String>())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(String.valueOf(savedGames.size()), "SavedGames Count");
        populateListItem(savedGames);
    }

    private void populateListItem(ArrayList<String> savedGames) {
        for (int i = 0; i < savedGames.size(); i++) {
            gameswidget.ListItem listItem = new gameswidget.ListItem();
//            listItem.heading="My Saved Games";
            listItem.content = savedGames.get(i);
            Log.d(savedGames.get(i), "SavedGames");
            listItemList.add(listItem);
        }

    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     *Similar to getView of Adapter where instead of View
     *we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.list_row);
        final RemoteViews remotetxtView = new RemoteViews(
                context.getPackageName(), R.layout.widget_layout);
        remotetxtView.setTextViewText(R.id.heading, context.getResources().getString(R.string.savedgames_string));
        gameswidget.ListItem listItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.content, listItem.content);
        return remoteView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

}