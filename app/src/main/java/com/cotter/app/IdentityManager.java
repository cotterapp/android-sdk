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
        Log.v("COTTER_INTENT_REQUEST", "Adding pending intent and identity request for state " + state);
        this.mIntents.put(state, intent);
        this.mIdentityRequests.put(state, req);
    }

    public Intent getPendingIntent(String state) {
        Log.v("COTTER_PENDING_INTENT", "Retrieving pending complete intent for state " + state);
        return (Intent)this.mIntents.remove(state);
    }


    public IdentityRequest getIdentityRequest(String state) {
        Log.v("COTTER_ID_REQUEST", "Retrieving identity request for state " + state);
        return (IdentityRequest)this.mIdentityRequests.remove(state);
    }

    public static Intent createResponseHandlingIntent(Context context, Uri responseUri) {
        Intent intent = new Intent(context, IdentityActivity.class);
        intent.setData(responseUri);
        intent.putExtra(IdentityRequest.OPEN_TWA, false);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    public static String handleResponse(Intent intent) {
        if (intent.hasExtra(TOKEN_RESPONSE)) {
            return intent.getStringExtra(TOKEN_RESPONSE);
        }
        return null;
    }
    public static String handleError(Intent intent) {
        if (intent.hasExtra(TOKEN_ERROR)) {
            return intent.getStringExtra(TOKEN_ERROR);
        }
        return null;
    }
}
