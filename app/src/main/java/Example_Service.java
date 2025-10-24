package com.gmail.yahlieyal.lostnfound;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import static com.gmail.yahlieyal.lostnfound.App.CHANNEL_ID;

public class Example_Service extends Service {

    public static String description;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaPlayer mp= MediaPlayer.create(this,R.raw.ringtone); // starts ringtone
        Notification_Activity.mediaPlayer = mp;
        mp.start();
        //starts  notification
        Intent notificationIntent = new Intent(this, Show_List_Activity.class);
        notificationIntent.putExtra("ActivityType", "My List Service"); //when click on the notification
        description = intent.getStringExtra("strDescription");
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Old Item Alert")
                .setContentText(intent.getStringExtra("strExtra"))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND)
                .build();


        NotificationManager notificationManager=(NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() { // when service is Destroy
        Notification_Activity.mediaPlayer = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
