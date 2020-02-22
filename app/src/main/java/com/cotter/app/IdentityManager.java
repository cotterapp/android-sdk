package com.cotter.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class IdentityManager {
    static String TOKEN_RESPONSE = "TOKEN_RESPONSE";
    static String TOKEN_ERROR = "TOKEN_ERROR";

    private Map<String, Intent> mIntents = new HashMap();
    private Map<String, IdentityRequest> mIdentityRequests = new HashMap();
    private static IdentityManager sInstance;

    private IdentityManager() {
    }

    public static synchronized IdentityManager getInstance() {
        if (sInstance == null) {
            sInstance = new IdentityManager();
        }

        return sInstance;
    }


    public void addPendingRequestIntent(String state, Intent intent, IdentityRequest req) {
        Log.i("COTTER_INTENT_REQUEST", "Adding pending intent and identity request for state " + state);
        this.mIntents.put(state, intent);
        this.mIdentityRequests.put(state, req);
    }

    public Intent getPendingIntent(String state) {
        Log.i("COTTER_PENDING_INTENT", "Retrieving pending complete intent for state " + state);
        return (Intent)this.mIntents.remove(state);
    }


    public IdentityRequest getIdentityRequest(String state) {
        Log.i("COTTER_ID_REQUEST", "Retrieving identity request for state " + state);
        return (IdentityRequest)this.mIdentityRequests.remove(state);
    }

    public static Intent createResponseHandlingIntent(Context context, Uri responseUri) {
        Log.i("createResponseIntent", responseUri.toString());

        // Creating an intent of the IdentityActivity to handle the uri from redirect
        Intent intent = new Intent(context, IdentityActivity.class);
        intent.setData(responseUri);
        intent.putExtra(IdentityRequest.OPEN_TWA, false);
        intent.putExtra(IdentityActivity.HANDLE_RESPONSE, true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    // Use for the client's activity to process token response attached to their intent
    public static String handleResponse(Intent intent) {
        if (intent.hasExtra(TOKEN_RESPONSE)) {
            return intent.getStringExtra(TOKEN_RESPONSE);
        }
        return null;
    }

    // Use for the client's activity to process token error attached to their intent
    public static String handleError(Intent intent) {
        if (intent.hasExtra(TOKEN_ERROR)) {
            return intent.getStringExtra(TOKEN_ERROR);
        }
        return null;
    }
}
