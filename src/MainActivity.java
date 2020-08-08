package com.example.covid;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    TextView data;
    TextView update;
    WebView graph;
    String currUpdate;
    boolean skip = true, skipOnce = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        data = findViewById(R.id.data);
        update = findViewById(R.id.lastUpdate);
        graph = findViewById(R.id.graph);

        displayInfo();
        displayBrowser();
        currUpdate = update.getText().toString();

        startService(new Intent(this, MyService.class));

        Timer timer = new Timer();
        timer.schedule( new TimerTask() {

            public void run() {
                displayInfo();

                if(!skip) {
                    displayBrowser();
                    skip = true;
                }

                // true for the first time
                if(!currUpdate.equals(update.getText().toString())) {
                    if(!skipOnce) {
                        displayBrowser();
                    }

                    skipOnce = false;
                    currUpdate = update.getText().toString();
                }
            }
        }, 0, 5*1000);

    }

    private void displayInfo() {
        new Thread(new Runnable() {
            ArrayList<String> info;
            String lastUpdate;
            @Override
            public void run() {

                final String url1 = "https://github.com/nychealth/coronavirus-data/blob/master/summary.csv";
                final String url2 = "https://github.com/nychealth/coronavirus-data/blob/master/case-hosp-death.csv";
                Document doc;

                try {
                    doc = Jsoup.connect(url2).get();

                    // info
                    info = new ArrayList<>();
                    Elements container1 = doc.select("tr");
                    for(Element e : container1) {
                        info.add(e.text());
                    }

                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                try {
                    doc = Jsoup.connect(url1).get();
                    Elements container = doc.select("tr");
                    int i = 1;
                    for(Element e : container) {
                        if(i++ == 5)
                            lastUpdate = e.text();
                    }

                } catch (IOException e1) {
                    if(!update.getText().toString().contains("No internet connection"))
                        update.append("\nNo internet connection");

                    skip = false;
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String result = "DATE\t\t\t\t\t\t\t\t\t\t\t\t  CASES\t\t\t\t\tHOSPIT\t\t\t\tDEATH\n";
                        for(int i = info.size()-7; i < info.size(); i++) {
                            String[] arr = info.get(i).split(" ");
                            for(String s : arr) {
                                if(s.length() < 3) {
                                    int l = s.length();
                                    for(int j = 0; j < 3 - l; j++)
                                        s += " ";
                                }

                                result += s + "\t\t\t\t\t\t\t\t";
                            }

                            result += "\n";
                        }

                        data.setText(result);
                        update.setText(lastUpdate);
                    }
                });
            }
        }).start();
    }

    private void displayBrowser() {
        graph.post(new Runnable() {
            @Override
            public void run() {
                graph.setWebViewClient(new WebViewClient());
                graph.getSettings().setLoadsImagesAutomatically(true);
                graph.getSettings().setJavaScriptEnabled(true);
                graph.getSettings().setBuiltInZoomControls(true);
                graph.loadUrl("https://datawrapper.dwcdn.net/yqfdF/5/");
            }
        });
    }

//    private void notification() {
//        createNotificationChannel();
//
//        Intent landingIntent = new Intent(this, MainActivity.class);
//        landingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
//
//        PendingIntent landingPendingIntent = PendingIntent.getActivity(this, 0, landingIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        NotificationCompat.Builder builder =new NotificationCompat.Builder(this,CHANNEL_ID);
//        builder.setSmallIcon(R.drawable.icon);
//        builder.setContentTitle("Data Update");
//        builder.setContentText("COVID data has been updated");
//        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        builder.setContentIntent(landingPendingIntent);
//
//        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
//
//        final int NOTIFICATION_ID = 001;
//        notificationManagerCompat.notify(NOTIFICATION_ID,builder.build());
//    }
//
//    private void createNotificationChannel() {
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            CharSequence name = "Notifications";
//            String description = "Include all the personal notifications";
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//
//            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,name,importance);
//
//            notificationChannel.setDescription(description);
//
//            NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//    }



}