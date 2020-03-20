package com.gaurav.project.batterfullalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.util.Locale;

public class MainService extends Service {
    private static final String TAG = MainService.class.getName();
    private int mNotificationId = 0;
    private boolean mAlreadyNotified = false;
    private NotificationManager mNotificationManager;

    public static int pluged;
public static int status;
    public static boolean f;
    public static Ringtone ringtone;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "creating service");
        mNotificationManager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
        registerReceiver(mReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public void onDestroy() {
        Log.d(TAG, "destroying service");
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public static void startIfEnabled(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
        boolean isEnabled = preferences.getBoolean(MainActivity.PREFERENCE_KEY_ENABLED, false);
        Intent intent = new Intent(context, MainService.class);
        if (isEnabled) {
            context.startService(intent);
        } else {
            context.stopService(intent);
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                final String action = intent.getAction();

                Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
                Uri path = Uri.parse("android.resource://" + getPackageName() + "/"+R.raw.ringtones);
                ringtone = RingtoneManager.getRingtone(getApplicationContext(),path);

                if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                     status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, Integer.MIN_VALUE);

                    if (status == BatteryManager.BATTERY_STATUS_FULL) {
                        if (!mAlreadyNotified) {
                            mAlreadyNotified = true;
                            SharedPreferences preferences = context.getSharedPreferences(MainActivity.PREFERENCES_NAME, 0);
                            int defaults = Notification.DEFAULT_LIGHTS;
                            if (preferences.getBoolean(MainActivity.PREFERENCE_KEY_VIBRATE, false)) {
                                defaults |= Notification.DEFAULT_VIBRATE;
                            }
                            if (preferences.getBoolean(MainActivity.PREFERENCE_KEY_SOUND, false)) {
                                defaults |= Notification.DEFAULT_SOUND;
                            }

                            Intent notificationIntent = new Intent(context, MainActivity.class);
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                    notificationIntent, 0);

                            showNotification(getString(R.string.app_name),getString(R.string.full),"",new Intent(context,MainActivity.class));

                            Notification notification = new NotificationCompat.Builder(context)
                                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText(getString(R.string.full))
                                    .setContentIntent(pendingIntent)
                                    .setOnlyAlertOnce(true)
                                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 0))
                                    .setDefaults(defaults)
                                    .build();
                            mNotificationManager.notify(mNotificationId, notification);


                        }
                    } else {
                        mAlreadyNotified = false;
                        mNotificationManager.cancel(mNotificationId);
                    }

                    Log.d(TAG, String.format("battery status: %d", status));
                    Intent notificationIntent = new Intent(context, MainActivity.class);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                            notificationIntent, 0);


                    Notification notification = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.ic_launcher_foreground)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentIntent(pendingIntent)

                            .setContentText(String.format("battery status: %d", status))
                            .build();
                    mNotificationManager.notify(mNotificationId, notification);

                    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                     pluged = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    float batteryPct = level / (float)scale;
                    final float p = batteryPct * 100;


/*
                   if (status==5)
                    {
                        ringtone.play();
                    }
                    else
                    {
                        ringtone.stop();
                    }*/
                    checkBatteryState();
                    /*
                    MainActivity s = null;
                    MainActivity.stop = s.findViewById(R.id.btnstop);
                    stop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ringtone.stop();
                        }
                    });
*/

                }
            }
        }
    };
    public void checkBatteryState() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);

        int chargeState = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        String strState;

        switch (chargeState) {

            case BatteryManager.BATTERY_STATUS_CHARGING:

                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                strState = "charging";

                ringtone.play();
                if (status==4) {
                    ringtone.stop();
                }
                else {

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ringtone.play();
                        }
                    }, 100);
                    ringtone.stop();
                }
                break;
            default:
                strState = "not charging";
                break;
        }

    }
    public void showNotification(String heading, String description, String imageUrl, Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this,"channelID")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(heading)
                .setContentText(description)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationId = 1;
        createChannel(notificationManager);
        notificationManager.notify(notificationId, notificationBuilder.build());
    }

    public void createChannel(NotificationManager notificationManager){
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationChannel channel = new NotificationChannel("channelID","name", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Description");
        notificationManager.createNotificationChannel(channel);
    }
}