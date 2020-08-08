package com.example.covid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class MyService extends Service {
    private String update = "";
    private String currUpdate = "";
    private final String CHANNEL_ID = "personal_notification";
    private boolean skipOnce = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timer timer = new Timer();
        timer.schedule( new TimerTask() {

            public void run() {
                refresh();
                System.out.println("update" + update);
                System.out.println("currUpdate" + currUpdate);
                // true for the first time
                if(!currUpdate.equals(update)) {
                    if(!skipOnce) {
                        sendNotification();
                    }

                    skipOnce = false;
                    currUpdate = update;
                }
            }
        }, 0, 5*1000);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void refresh() {
        new Thread(new Runnable() {
            String lastUpdate;
            @Override
            public void run() {
                final String url1 = "https://github.com/nychealth/coronavirus-data/blob/master/summary.csv";
                Document doc;

                try {
                    doc = Jsoup.connect(url1).get();
                    Elements container = doc.select("tr");
                    int i = 1;
                    for(Element e : container) {
                        if(i++ == 5)
                            lastUpdate = e.text();
                    }

                    update = lastUpdate;
                } catch (IOException e1) {e1.printStackTrace();} // if no internet do something
            }
        }).start();
    }

    private void sendNotification() {
        createNotificationChannel();

        Intent landingIntent = new Intent(this, MainActivity.class);
        landingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent landingPendingIntent = PendingIntent.getActivity(this, 0, landingIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder =new NotificationCompat.Builder(this,CHANNEL_ID);
        builder.setSmallIcon(R.drawable.icon);
        builder.setContentTitle("Data Update");
        builder.setContentText("COVID data has been updated");
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(landingPendingIntent);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);

        final int NOTIFICATION_ID = 001;
        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Notifications";
            String description = "Include all the personal notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name,importance);

            notificationChannel.setDescription(description);

            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
