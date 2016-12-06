package com.sen5or.bluetooth;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static com.estimote.sdk.internal.utils.EstimoteBeacons.ESTIMOTE_PROXIMITY_UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final int RSSI_THRESHOLD = -90;

    private TextView rssiSignalTextview;
    private Button ipSubmitButton;
    private EditText ipAddressInput;

    private String IpAddressToSendTo = "";

    private BeaconManager beaconManager;

    String TAG = "TEST";
    private Region myRegion;

    private static final Region ALL_ESTIMOTE_BEACONS = new Region("rid", ESTIMOTE_PROXIMITY_UUID, null, null);
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rssiSignalTextview = (TextView) findViewById(R.id.rssi_text);
        ipSubmitButton = (Button) findViewById(R.id.button);
        ipAddressInput = (EditText) findViewById(R.id.editText);


        ipSubmitButton.setOnClickListener(this);

        beaconManager = new BeaconManager(this);
        myRegion = new Region("monitored region", UUID.fromString("e2c56db5-dffb-48d2-b060-d0f5a71096e0"), 0, 30409);


        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, final List beacons) {


                if (beacons.size() > 0) {

                    int rssiSignal = ((Beacon) beacons.get(0)).getRssi();

                    //Log.d(TAG, "Ranged beacons: " + beacons.get(0));
                    //Log.d(TAG, "getRssi: " + rssiSignal);
                    //Log.d(TAG, "getProximityUUID: " + ((Beacon) beacons.get(0)).getProximityUUID());


                    rssiSignalTextview.setText("" + rssiSignal);

                    //Lower RSSI means stronger signal


                    if (rssiSignal >= RSSI_THRESHOLD && rssiSignal != -1) {
                        System.out.println("Sending On request");
                        new SendHttpRequestAsync(MainActivity.this).execute("ScreenOn");

                    } else if (rssiSignal < RSSI_THRESHOLD && rssiSignal != -1) {
                        System.out.println("Sending off request");
                        new SendHttpRequestAsync(MainActivity.this).execute("ScreenOff");

                    }


                } else {
                    rssiSignalTextview.setText("" + -1);
                }


            }
        });

        beaconManager.setNearableListener(new BeaconManager.NearableListener() {
            @Override
            public void onNearablesDiscovered(List nearables) {
                Log.d(TAG, "Discovered nearables: " + nearables);
            }
        });

        beaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override
            public void onEddystonesFound(List eddystones) {
                Log.d(TAG, "Nearby eddystones: " + eddystones);
            }
        });

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                // Beacons ranging.
                beaconManager.startRanging(ALL_ESTIMOTE_BEACONS);

                // Nearable discovery.
                beaconManager.startNearableDiscovery();

                // Eddystone scanning.
                beaconManager.startEddystoneScanning();
            }
        });

        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(myRegion);
            }
        });
    }

    @Override
    protected void onDestroy() {
        beaconManager.disconnect();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    @Override
    public void onClick(View view) {

        if (view == ipSubmitButton) {
            IpAddressToSendTo = ipAddressInput.getText().toString();
        }
    }


    private class SendHttpRequestAsync extends AsyncTask<String, Void, Void> {

        private final Context context;

        public SendHttpRequestAsync(Context c) {
            this.context = c;
        }

        @Override
        protected Void doInBackground(String... params) {

            try {

                //System.out.println("Async param: "+params[0]);

                String urlString = "http://" + IpAddressToSendTo + ":8080/" + params[0];


                if (!IpAddressToSendTo.equalsIgnoreCase("")) {

                    URL url = new URL(urlString);

                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");

                    int responseCode = connection.getResponseCode();
                    System.out.println("Response: " + responseCode);

                } else {

                }


            } catch (IOException e) {

                e.printStackTrace();
            }
            return null;
        }

    }

}


