package gameswidget;

import android.annotation.TargetApi;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.wesport.R;
import com.example.android.wesport.data.GameContract.GameEntry;


/**
 * RemoteViewsService controlling the data being shown in the scrollable weather detail widget
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class DetailWidgetRemoteViewsService extends RemoteViewsService {
    public final String LOG_TAG = DetailWidgetRemoteViewsService.class.getSimpleName();
    private static final String[] GAME_COLUMNS = {
            GameEntry.TABLE_NAME + "." + GameEntry._ID,
            GameEntry.COLUMN_GAME_DESC,
            GameEntry.COLUMN_START_DATE,
            GameEntry.COLUMN_START_TIME,
            GameEntry.COLUMN_END_TIME,
            GameEntry.COLUMN_GAME_SKILL,
            GameEntry.COLUMN_GAME_ADDRESS,
            GameEntry.COLUMN_USER_NAME
    };
    // these indices must match the projection
    static final int INDEX_GAME_ID = 0;

    Uri gameDataForUri = GameEntry.CONTENT_URI;
    String selection =   GameEntry.COLUMN_USER_NAME+ "=?" ;

    String[] selectionArgs = null;
    String username = "";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;

            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();
                // Get data from the ContentProvider
                username= GameEntry.getUserName(DetailWidgetRemoteViewsService.this);
                Log.d("Remote Views Service",username);
                selectionArgs = new String[]{username};
                data = getContentResolver().query(gameDataForUri,
                        GAME_COLUMNS,
                        null,
                        null,
                        null);
                Binder.restoreCallingIdentity(identityToken);
                Log.d("Remote Views Service", String.valueOf(GAME_COLUMNS));
                Log.d("Remote Views Service", String.valueOf(selection));
                Log.d("Remote Views Service", String.valueOf(selectionArgs));
                Log.d("Remote Views Service", String.valueOf(data));

            }

            @Override
            public void onDestroy() {
                if (data != null) {
                    data.close();
                    data = null;
                }
            }

            @Override
            public int getCount() {
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_detail_list_item);

                // Find the columns of game attributes that we're interested in
                int nameColumnIndex = data.getColumnIndex(GameEntry.COLUMN_GAME_DESC);
                int startDateIndex = data.getColumnIndex(GameEntry.COLUMN_START_DATE);
                int startTimeIndex = data.getColumnIndex(GameEntry.COLUMN_START_TIME);
                int endTimeIndex = data.getColumnIndex(GameEntry.COLUMN_END_TIME);
                int notesIndex = data.getColumnIndex(GameEntry.COLUMN_GAME_NOTES);
                int locColumnIndex = data.getColumnIndex(GameEntry.COLUMN_GAME_ADDRESS);

                // Read the game attributes from the Cursor for the current game
                String gameName = data.getString(nameColumnIndex);
                String stDate = data.getString(startDateIndex);
                String stTime = data.getString(startTimeIndex);
                String etTime = data.getString(endTimeIndex);
                Log.d("Remote Views Service", String.valueOf(gameName));

                String gameaddress = data.getString(locColumnIndex);
//                String notes = data.getString(notesIndex);
                Log.d("gameName", gameName);
                Log.d("gameaddress", gameaddress);


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    setRemoteContentDescription(views, gameName);
                }
                views.setTextViewText(R.id.widget_date, stDate);
                views.setTextViewText(R.id.widget_description, gameName);
                views.setTextViewText(R.id.widget_start_time, stTime);
                views.setTextViewText(R.id.widget_end_time, etTime);
                final Intent fillInIntent = new Intent();
                fillInIntent.setData(gameDataForUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);
                return views;
            }

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
            private void setRemoteContentDescription(RemoteViews views, String description) {
                views.setContentDescription(R.id.widget_icon, description);
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_GAME_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}