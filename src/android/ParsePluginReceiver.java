package org.apache.cordova.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.ezerapp.MainActivity;
import com.ezerapp.R;
import com.parse.ParseAnalytics;
import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class ParsePluginReceiver extends ParsePushBroadcastReceiver {
    private static final String TAG = "ParsePluginReceiver";
    private static final String RECEIVED_IN_FOREGROUND = "receivedInForeground";
    private Random random = new Random();
    private int NOTIFICATION_ID = 0;

    /*
        @Override
        protected int getSmallIconId(Context context, Intent intent) {
            return R.drawable.notification_small_icon;
        }

        @Override
        protected Bitmap getLargeIcon(Context context, Intent intent) {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_big_icon);
            return bitmap;
        }
    */
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        JSONObject pushData = getPushData(intent);
//        Log.e(TAG, "Push data received " + pushData);
        if (pushData != null) {
            if (ParsePlugin.isInForeground()) {
                ParsePlugin.javascriptEventCallback(pushData);
            } else {
                String title = null;
                String message = null;

                try {
                    if (pushData.has("title")) {
                        title = pushData.getString("title");
                    }

                    if (pushData.has("alert")) {
                        message = pushData.getString("alert");
                    }

                    if (message != null) {
                        // generate local notification with sound
                        if (pushData.has("customData")) {
                            JSONObject customData = pushData.getJSONObject("customData");
                            if (customData.has("customSound")) {
                                generateNotification(context, title, message, customData.getBoolean("customSound"));
                            } else {
                                generateNotification(context, title, message, false);
                            }
                        } else {
                            generateNotification(context, title, message, false);
                        }
                    }
                    // super.onPushReceive(context, intent);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONException while parsing push data:", e);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        JSONObject pushData = getPushData(intent);

        if (pushData != null) {
            if (ParsePlugin.isInForeground()) {
                ParseAnalytics.trackAppOpened(intent);
                ParsePlugin.javascriptEventCallback(pushData);
            } else {
                super.onPushOpen(context, intent);
                ParsePlugin.setLaunchNotification(pushData);
            }
        }
    }

    private void generateNotification(Context context, String title, String message, Boolean customSound) {
//        Log.e(TAG, "Generating notification sound: " + customSound);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, 0);

        NotificationManager mNotifM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (title == null) {
            title = context.getResources().getString(R.string.app_name);
        }

        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (customSound) {
            soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.push_notification);
        }

        final Notification.Builder mBuilder =
                new Notification.Builder(context)
                        .setSmallIcon(R.drawable.notification_small_icon)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.notification_big_icon))
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new Notification.BigTextStyle()
                                .bigText(message))
                        .setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setLights(Color.RED, 1100, 800)
                        .setSound(soundUri);

        mBuilder.setContentIntent(contentIntent);
        NOTIFICATION_ID = random.nextInt();
        mNotifM.notify(NOTIFICATION_ID, mBuilder.build());
    }

    private static JSONObject getPushData(Intent intent) {
        JSONObject pushData = null;
        try {
            pushData = new JSONObject(intent.getStringExtra("com.parse.Data"));
            pushData.put(RECEIVED_IN_FOREGROUND, ParsePlugin.isInForeground());
        } catch (JSONException e) {
            Log.e(TAG, "JSONException while parsing push data:", e);
        } finally {
            return pushData;
        }
    }
}