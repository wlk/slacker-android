package com.wlangiewicz.slacker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

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

    private static final Map<String, String> PLACES_BY_BEACONS;

    private static final String[] channels = new String[]{"kitchen", "at-desk", "meeting-room"};

    private BeaconManager beaconManager;
    private Region region;

    String currentChannel = "";

    static {
        Map<String, String> placesByBeacons = new HashMap<>();
        placesByBeacons.put("4300:64947", channels[0]); //blueberry - kitchen
        placesByBeacons.put("21333:11327", channels[1]); //ice - at desk
        placesByBeacons.put("20478:33460", channels[2]); //mint - meeting-room
        PLACES_BY_BEACONS = Collections.unmodifiableMap(placesByBeacons);
    }

    private String channelToJoin(Beacon beacon) {
        String beaconKey = String.format(Locale.US, "%d:%d", beacon.getMajor(), beacon.getMinor());
        if (PLACES_BY_BEACONS.containsKey(beaconKey)) {
            return PLACES_BY_BEACONS.get(beaconKey);
        }
        return "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        currentChannel = channel;
                    }

                } else {
                    Log.d("EstimoteActivity", "Leaving channels");
                    Toast.makeText(EstimoteActivity.this, "Leaving channels", Toast.LENGTH_SHORT).show();
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
}
