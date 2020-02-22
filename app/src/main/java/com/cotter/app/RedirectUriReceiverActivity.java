package com.cotter.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;


public class RedirectUriReceiverActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        Log.i("COTTER_IDENTITY", "RedirectUriReceiverActivity received");

        // while this does not appear to be achieving much, handling the redirect in this way
        // ensures that we can remove the browser tab from the back stack. See the documentation
        // on AuthorizationManagementActivity for more details.
        startActivity(IdentityManager.createResponseHandlingIntent(this, getIntent().getData()));
        Log.i("COTTER_IDENTITY", "RedirectUriReceiverActivity finished");
        finish();
    }
}
