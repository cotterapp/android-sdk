package com.cotter.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class RedirectUriReceiverActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        Log.i("COTTER_IDENTITY", "RedirectUriReceiverActivity received");

        // Handling the redirect this way ensures that the WebView closes after redirect
        startActivity(IdentityManager.createResponseHandlingIntent(this, getIntent().getData()));
        Log.i("COTTER_IDENTITY", "RedirectUriReceiverActivity finished");
        finish();
    }
}
