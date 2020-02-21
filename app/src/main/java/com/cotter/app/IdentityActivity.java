package com.cotter.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.androidbrowserhelper.trusted.TwaLauncher;

import org.json.JSONObject;

public class IdentityActivity extends Activity {
    static String AUTH_CODE = "code";
    static String STATE = "state";
    static String CHALLENGE_ID = "challenge_id";
    static String KEY_AUTHORIZATION_STARTED = "KEY_AUTHORIZATION_STARTED";

    private boolean mAuthorizationStarted = false;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);


        if (savedInstanceBundle != null) {
            mAuthorizationStarted = savedInstanceBundle.getBoolean(KEY_AUTHORIZATION_STARTED, false);
        }

        Log.e("authstart on create", mAuthorizationStarted +"");
        Intent intent = getIntent();
        boolean openTwa = intent.getBooleanExtra(IdentityRequest.OPEN_TWA, false);
        Log.e("OPEN TWA on create", openTwa +"");
        if (openTwa && !mAuthorizationStarted) {
            mAuthorizationStarted = true;
            String url = intent.getStringExtra(IdentityRequest.TWA_URL);
            new TwaLauncher(this).launch(Uri.parse(url));
            Log.e("LAUNCHED TWA", url);
            return;
        }

        Log.e("HANDLE RESP", "handle");
        handleResponse(this, intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_AUTHORIZATION_STARTED, mAuthorizationStarted);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.e("authstart on resume", mAuthorizationStarted +"");


        Intent intent = getIntent();
        boolean openTwa = intent.getBooleanExtra(IdentityRequest.OPEN_TWA, false);
        Log.e("OPEN TWA on resume", openTwa +"");

        if (openTwa && !mAuthorizationStarted) {
            mAuthorizationStarted = true;
            String url = intent.getStringExtra(IdentityRequest.TWA_URL);
            new TwaLauncher(this).launch(Uri.parse(url));
            Log.e("LAUNCHED TWA", url);
            return;
        }

        Log.e("HANDLE RESP", "handle");
        handleResponse(this, intent);
    }

    public void handleResponse(Context ctx, Intent intent) {

        Uri uri = intent.getData();


        if (uri != null) {
            Log.e("handleResponse", uri.getQueryParameter(AUTH_CODE));
            String authCode = uri.getQueryParameter(AUTH_CODE);
            String state = uri.getQueryParameter(STATE);
            String challengeIDstr = uri.getQueryParameter(CHALLENGE_ID);
            int challengeID = Integer.parseInt(challengeIDstr);

            Intent completeIntent = IdentityManager.getInstance().getPendingIntent(state);
            IdentityRequest idReq = IdentityManager.getInstance().getIdentityRequest(state);

            Callback callback = new Callback() {
                @Override
                public void onSuccess(JSONObject result) {
                    completeIntent.putExtra(IdentityManager.TOKEN_RESPONSE, result.toString());
                    completeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try {
                        ctx.startActivity(completeIntent);
//                        finish();

                    } catch (Exception e) {
                        Log.e("Intent send response", e.toString());
                    }
                    Log.e("ver code result", result.toString());
                }

                @Override
                public void onError(String error) {
                    completeIntent.putExtra(IdentityManager.TOKEN_ERROR, error);
                    completeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        ctx.startActivity(completeIntent);
//                        finish();

                    } catch (Exception e) {
                        Log.e("Intent send error", e.toString());
                    }
                    Log.e("ver code error", error);
                }
            };

            Cotter.authRequest.RequestIdentityToken(ctx, idReq.codeVerifier, authCode, challengeID, IdentityRequest.URL_SCHEME, callback);
        } else {
            Log.e("handleResponse", "uri null");
        }
    }

}
