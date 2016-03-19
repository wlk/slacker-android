package com.wlangiewicz.slacker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EstimoteActivity extends AppCompatActivity {

    private static final Map<String, String> CHANNELS_BY_BEACONS;
    private static final Map<String, String> CHANNEL_MAPPING;

    static {
        Map<String, String> placesByBeacons = new HashMap<>();
        placesByBeacons.put("4300:64947", "kitchen"); //blueberry
        placesByBeacons.put("21333:11327", "at-desk"); //ice
        placesByBeacons.put("20478:33460", "meeting-room"); //mint
        CHANNELS_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    static {
        Map<String, String> channelMapping = new HashMap<>();
        channelMapping.put("at-desk", "C0TULBRPA");
        channelMapping.put("kitchen", "C0TU94G8N");
        channelMapping.put("meeting-room", "C0TT0LS4B");
        CHANNEL_MAPPING = Collections.unmodifiableMap(channelMapping);
    }

    String currentChannel = "";
    private BeaconManager beaconManager;
    private Region region;
    private String apiToken = "";

    private String channelToJoin(Beacon beacon) {
        String beaconKey = String.format(Locale.US, "%d:%d", beacon.getMajor(), beacon.getMinor());
        if (CHANNELS_BY_BEACONS.containsKey(beaconKey)) {
            return CHANNELS_BY_BEACONS.get(beaconKey);
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        apiToken = getResources().getString(R.string.mySlackKey);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    String channel = channelToJoin(nearestBeacon);

                    if (!channel.equals(currentChannel)) {
                        Log.d("EstimoteActivity", "Joining channel:" + channel);
                        Toast.makeText(EstimoteActivity.this, "Joining channel:" + channel, Toast.LENGTH_SHORT).show();
                        makeHttpRequest("leave", apiToken, currentChannel);
                        currentChannel = channel;
                        makeHttpRequest("join", apiToken, currentChannel);
                    }

                } else {
                    Log.d("EstimoteActivity", "Leaving channels");
                    Toast.makeText(EstimoteActivity.this, "Leaving channels", Toast.LENGTH_SHORT).show();
                    makeHttpRequest("leave", apiToken, currentChannel);
                    currentChannel = "";
                }
            }
        });
        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    private void makeHttpRequest(final String operation, String token, final String channelName) {
        if (!channelName.equals("")) {

            final String channelNameMapped = operation.equals("leave") ? CHANNEL_MAPPING.get(channelName) : channelName;
            final String channelKeyName = operation.equals("leave") ? "channel" : "name";

            String requestUrl = "https://slack.com/api/channels." + operation + "?token=" + token + "&" + channelKeyName + "=" + channelNameMapped + "&pretty=1";

            RequestQueue queue = Volley.newRequestQueue(this);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    Log.d("EstimoteActivity", "Request, operation:" + operation + ", channel: " + channelNameMapped);
                    Log.d("EstimoteActivity", "Response " + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("EstimoteActivity", "Error " + error);
                }
            });
            // Add the request to the RequestQueue.
            queue.add(stringRequest);
        }
    }
}
