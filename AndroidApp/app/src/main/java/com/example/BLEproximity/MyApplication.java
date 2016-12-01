package com.example.BLEproximity;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import 	android.os.SystemClock;
import android.util.Log;


public class MyApplication extends Application {

    private double avg = 0;
    private boolean flag = false;
    private BeaconManager beaconManager;
    private String myIP;
    private Region myRegion =
            new Region("monitored region", UUID.fromString("e2c56db5-dffb-48d2-b060-d0f5a71096e0"), 0, 30409);

    public void sendGetRequest() { new GetClass(this).execute(); }

    @Override
    public void onCreate() {
        super.onCreate();

        beaconManager = new BeaconManager(getApplicationContext());
        beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {
            @Override
            public void onEnteredRegion(Region region, List<Beacon> list) {
                beaconManager.startRanging(region);
            }
            @Override
            public void onExitedRegion(Region region) {
                beaconManager.stopRanging(region);
            }
        });
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {

                if (!list.isEmpty()) {

                    avg = Math.abs(list.get(0).getRssi());

                    if (avg <= 80 && !flag) {
                        sendGetRequest();
                        SystemClock.sleep(7000);
                        flag = true;
                        //showNotification("Near Proximity is ", String.valueOf(avg));
                        showNotification("Nearby mirror detected.", "Login using your voice or face");
                    }
                    else if (avg > 80 && flag){
                        sendGetRequest();
                        SystemClock.sleep(7000);
                        flag = false;
                        //showNotification("Far Proximity is ", String.valueOf(avg));
                        showNotification("Mirror out of range, logging off", "");
                    }

                }

            }//end onBeaconsDiscovered
        });
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startMonitoring(myRegion);
            }
        });
    }

    public void showNotification(String title, String message) {
        Intent notifyIntent = new Intent(this, MainActivity.class);
        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0,
                new Intent[]{notifyIntent}, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();
        notification.defaults |= Notification.DEFAULT_SOUND;
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, notification);
    }

    /*
    * Get the IP address of the RPI using given MAC address (b8:27:eb:ab:cd:a7)
    *
    * */
    public String getIPFromArpCache(String mac) {
        if (mac == null)
            return null;
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4 && mac.equals(splitted[3])) {
                    // Basic sanity check
                    String ip = splitted[0];
                    //showNotification("IP is", ip);
                    return ip;
                    /*
                    if (ip.matches(".{3}\\..{1,3}\\..{1,3}\\..{1,3}")) {
                        return ip;
                    } else {
                        return "IP did not match format";
                    }
                    */
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "NULL IP";
    }

    private class GetClass extends AsyncTask<String, Void, Void> {

        private final Context context;

        public GetClass(Context c){
            this.context = c;
        }

        @Override
        protected Void doInBackground(String... params) {

            try {

                myIP = getIPFromArpCache("b8:27:eb:ab:cd:a7");
                String ip;
                if (myIP == "NULL IP") {
                    showNotification("NULL IP", "");
                    ip = "http://192.168.2.17";
                }
                else{ ip = "http://" + myIP; }

                //Append port at the end of IP address depending on screen on/off
                //Flag means Screen is on. 1025 to turn off, 1024 to turn on
                if (flag){ ip += ":8080"; }
                else { ip += ":80"; }

                //showNotification(ip, "Dist is "+String.valueOf(avg)+" Flag is "+String.valueOf(flag));

                URL url = new URL(ip);

                HttpURLConnection connection = (HttpURLConnection)url.openConnection();

                //String urlParameters = "fizz=buzz";
                connection.setRequestMethod("GET");
                connection.setRequestProperty("USER-AGENT", "Mozilla/5.0");
                connection.setRequestProperty("ACCEPT-LANGUAGE", "en-US,en;0.5");

                int responseCode = connection.getResponseCode();
                //Log.d("resp", String.valueOf(responseCode));

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                showNotification("Malformed URL","");
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }

    }//end GetClass

    /*
    public static double getAverage(int[] array, int size){

        int sum = 0;
        for(int i = 0; i<size; i++) sum += array[i];

        return sum/size;

    }*/

}//end MyApplication
