package com.wlangiewicz.slacker;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SlackProvider {

    private static final Map<String, String> CHANNEL_MAPPING;

    static {
        Map<String, String> channelMapping = new HashMap<>();
        channelMapping.put("at-desk", "C0TULBRPA");
        channelMapping.put("kitchen", "C0TU94G8N");
        channelMapping.put("meeting-room", "C0TT0LS4B");
        CHANNEL_MAPPING = Collections.unmodifiableMap(channelMapping);
    }

    private Context app;
    private String token;


    SlackProvider(Context app, String token) {
        this.app = app;
        this.token = token;
    }

    public void joinChannel(final String channelName) {
        makeHttpRequest("join", channelName, "name");
    }

    public void leaveChannel(final String channelName) {
        makeHttpRequest("leave", CHANNEL_MAPPING.get(channelName), "channel");
    }

    private void makeHttpRequest(final String operation, final String channelName, final String channelKeyName) {
        if (channelName != null && !channelName.equals("")) {

            String requestUrl = "https://slack.com/api/channels." + operation + "?token=" + token + "&" + channelKeyName + "=" + channelName + "&pretty=1";

            RequestQueue queue = Volley.newRequestQueue(app);

            // Request a string response from the provided URL.
            StringRequest stringRequest = new StringRequest(Request.Method.GET, requestUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    //Log.d("EstimoteActivity", "Request, operation:" + operation + ", channel: " + channelNameMapped);
                    //Log.d("EstimoteActivity", "Response " + response);
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
