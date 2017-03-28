package com.my.game.wesport.helper;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class NotificationHelper {
    private static final String FCM_MESSAGE_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String TAG = NotificationHelper.class.getSimpleName();
    public static final String EXTRA_MESSAGE = "sender";
    public static final String SERVER_KEY = "AIzaSyBmEpSt0jy6YbuUXnwJT6GzgabYNeOjqJE";
    private static OkHttpClient mClient = new OkHttpClient();

    public static void sendMessageByTopic(final String topic, final String title, final String body, final String icon, final String message) {
        sendMessage("/topics/" + topic, title, body, icon, message);
    }

    public static void sendMessage(final String recipients, final String title, final String body, final String icon, final String message) {
        Log.d(TAG, "sendMessage() called with: recipients = [" + recipients + "], title = [" + title + "], body = [" + body + "], icon = [" + icon + "], message = [" + message + "]");
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    JSONObject root = new JSONObject();
                    JSONObject notification = new JSONObject();
                    notification.put("body", body);
                    notification.put("title", title);
                    notification.put("sound", "notification");
                    notification.put("click_action", "OPEN_HOME_ACTIVITY");
                    if (!TextUtils.isEmpty(icon)) {
                        notification.put("icon", icon);
                    }

                    JSONObject data = new JSONObject();
                    data.put(EXTRA_MESSAGE, message);
                    root.put("notification", notification);
                    root.put("data", data);
                    root.put("to", recipients);

                    String result = postToFCM(root.toString());
                    Log.d(TAG, "ParkModel: " + result);
                    return result;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                /*try {
                    JSONObject resultJson = new JSONObject(result);
                    int success, failure;
                    success = resultJson.getInt("success");
                    failure = resultJson.getInt("failure");
                    Toast.makeText(context, "Message Success: " + success + "Message Failed: " + failure, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Message Failed, Unknown error occurred.", Toast.LENGTH_LONG).show();
                }*/
            }
        }.execute();
    }

    private static String postToFCM(String bodyString) throws IOException {
        Log.d(TAG, "postToFCM() called with: bodyString = [" + bodyString + "]");
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), bodyString);
        Request request = new Request.Builder()
                .url(FCM_MESSAGE_URL)
                .post(body)
                .addHeader("Authorization", "key=" + SERVER_KEY)
                .build();
        Response response = mClient.newCall(request).execute();
        return response.body().string();
    }

    public static void unsubscribeAndLogout() {
        Log.d(TAG, "unsubscribeAndLogout: ");
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(FirebaseAuth.getInstance().getCurrentUser().getUid());
            FirebaseInstanceId.getInstance().deleteInstanceId();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "unsubcribeAndLogout: " + e.getLocalizedMessage());
        }
    }

    public static void subscribe() {
        try {
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            FirebaseMessaging.getInstance().subscribeToTopic(uid);
            Log.d(TAG, "subscribe: " + uid);
        } catch (Exception e) {
            Log.d(TAG, "subscribe: " + e.getLocalizedMessage());
        }
    }
}
