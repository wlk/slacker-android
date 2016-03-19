package com.wlangiewicz.slacker;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class EstimoteActivity extends AppCompatActivity {

    private static final Map<String, String> CHANNELS_BY_BEACONS;
    private static final ArrayList<String> CHANNEL_LIST = new ArrayList<>(Arrays.asList("kitchen", "at-desk", "meeting-room"));

    static {
        Map<String, String> placesByBeacons = new HashMap<>();
        placesByBeacons.put("4300:64947", "kitchen"); //blueberry
        placesByBeacons.put("21333:11327", "at-desk"); //ice
        placesByBeacons.put("20478:33460", "meeting-room"); //mint
        CHANNELS_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    String currentChannel = "";
    private SlackProvider slack;
    private BeaconManager beaconManager;
    private Region region;

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
        final TextView channelNameTextView = (TextView) findViewById(R.id.current_channel_name);
        slack = new SlackProvider(this, getResources().getString(R.string.mySlackKey));

        leaveALlChannels();

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    String channel = channelToJoin(nearestBeacon);

                    if (!channel.equals(currentChannel)) {
                        Log.d("EstimoteActivity", "Joining channel:" + channel);
                        Toast.makeText(EstimoteActivity.this, "Joining channel: " + channel, Toast.LENGTH_LONG).show();
                        slack.leaveChannel(currentChannel);
                        currentChannel = channel;
                        slack.joinChannel(currentChannel);

                        if (channelNameTextView != null) {
                            channelNameTextView.setText(currentChannel);
                        }
                    }

                } else {
                    Log.d("EstimoteActivity", "Leaving channels");
                    Toast.makeText(EstimoteActivity.this, "Leaving all channels", Toast.LENGTH_LONG).show();
                    currentChannel = "";

                    leaveALlChannels();

                    if (channelNameTextView != null) {
                        channelNameTextView.setText("outside of the office!");
                    }
                }
            }
        });
        region = new Region("ranged region", UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);
    }

    private void leaveALlChannels() {
        for (String channel : CHANNEL_LIST) {
            slack.leaveChannel(channel);
        }
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


}
