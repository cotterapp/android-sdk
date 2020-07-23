package com.cotter.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.androidbrowserhelper.trusted.TwaLauncher;
import com.google.gson.Gson;

import org.json.JSONObject;

public class IdentityActivity extends Activity {
    static String AUTH_CODE = "code";
    static String STATE = "state";
    static String CHALLENGE_ID = "challenge_id";
    static String KEY_AUTHORIZATION_STARTED = "KEY_AUTHORIZATION_STARTED";
    static String HANDLE_RESPONSE = "HANDLE_RESPONSE";
    static String KEY_TWA_OPENED = "KEY_TWA_OPENED";

    private boolean mAuthorizationStarted = false;
    private boolean twaOpened = false;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);

        // on first open, set authorization started as false
        if (savedInstanceBundle != null) {
            mAuthorizationStarted = savedInstanceBundle.getBoolean(KEY_AUTHORIZATION_STARTED, false);
        }

        Log.i("COTTER_IDENTITY", "onCreate handleIntent");
        handleIntent(getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.i("onSaveInstanceState", outState.toString());
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_AUTHORIZATION_STARTED, mAuthorizationStarted);
        outState.putBoolean(KEY_TWA_OPENED, twaOpened);
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.i("COTTER_IDENTITY", "onResume handleIntent");
        handleIntent(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    public void handleIntent(Intent intent) {
        boolean openTwa = intent.getBooleanExtra(IdentityRequest.OPEN_TWA, false);
        boolean handleResponse = intent.getBooleanExtra(HANDLE_RESPONSE, false);
        Log.i("COTTER_IDENTITY", "handleIntent, openTwa: " + openTwa);
        Log.i("COTTER_IDENTITY", "handleIntent, handleResponse: " + handleResponse);
        Log.i("COTTER_IDENTITY", "handleIntent, mAuthorizationStarted: " + mAuthorizationStarted);
        Log.i("COTTER_IDENTITY", "handleIntent, twaOpened: " + twaOpened);


        // if open twa is true and authorization hasn't started, launch TWA
        if (openTwa && !mAuthorizationStarted) {
            // launch TWA
            String url = intent.getStringExtra(IdentityRequest.TWA_URL);
            new TwaLauncher(this).launch(Uri.parse(url));
            Log.i("COTTER_IDENTITY", "handleIntent, Launched TWA" + url);
            mAuthorizationStarted = true;
            return;
        }

        // if handleResponse requested, then this must come from redirect page
        if (handleResponse) {
            // handle the response from verification request
            Log.i("COTTER_IDENTITY", "handleIntent, start handleResponse");
            handleResponse(this, intent);
            return;
        }

        // if openTwa is true, mAuthorizationStarted has started, but not twaOpened
        // this must be onResume when the WebView is open
        // so set twaOpened = true
        if (openTwa && mAuthorizationStarted && !twaOpened) {
            twaOpened = true;
            Log.i("COTTER_IDENTITY", "handleIntent, openTwa && mAuthorizationStarted && !twaOpened -> set twaOpened true");
            return;
        }

        // if openTwa is true, mAuthorizationStarted has started, and twaOpened is true
        // this must be when the user canceled the WebView
        // so finish the activity
        if (openTwa && mAuthorizationStarted && twaOpened) {
            Log.i("COTTER_IDENTITY", "handleIntent, openTwa && mAuthorizationStarted && twaOpened -> finish");
            finish();
        }
    }

    public void handleResponse(Context ctx, Intent intent) {

        Uri uri = intent.getData();

        if (uri != null) {
            // Getting response codes from the uri
            String authCode = uri.getQueryParameter(AUTH_CODE);
            String state = uri.getQueryParameter(STATE);
            String challengeIDstr = uri.getQueryParameter(CHALLENGE_ID);
            int challengeID = Integer.parseInt(challengeIDstr);

            Log.i("COTTER_IDENTITY", "authcode :" + authCode);
            Log.i("COTTER_IDENTITY", "state :" + state);
            Log.i("COTTER_IDENTITY", "challengeIDstr :" + challengeIDstr);

            // Getting the completion intent and original request for the state
            Intent completeIntent = IdentityManager.getInstance().getPendingIntent(state);
            IdentityRequest idReq = IdentityManager.getInstance().getIdentityRequest(state);

            // Create callback after making a verification http request
            Callback callback = new Callback() {
                @Override
                public void onSuccess(JSONObject result) {
                    // On success, open the completion intent with the token response

                    Gson gson = new Gson();
                    try {
                        Object usr = result.get("user");
                        User updatedUser = gson.fromJson(usr.toString(), User.class);
                        User.updateUser(updatedUser);
                    } catch (Exception e) {
                        Log.e("COTTER_IDENTITY", "Can't parse result to user");
                    }

                    completeIntent.putExtra(IdentityManager.TOKEN_RESPONSE, result.toString());
                    completeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                    try {
                        ctx.startActivity(completeIntent);

                    } catch (Exception e) {
                        Log.e("COTTER_IDENTITY", "Starting completed intent error: " + e.toString());
                    }
                    Log.i("COTTER_IDENTITY", "RequestIdentityToken succeded");
                    finish();
                }

                @Override
                public void onError(String error) {
                    // On error, open the completion intent with the token error

                    completeIntent.putExtra(IdentityManager.TOKEN_ERROR, error);
                    completeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    try {
                        ctx.startActivity(completeIntent);

                    } catch (Exception e) {
                        Log.e("COTTER_IDENTITY", "Starting completed intent error: " + e.toString());
                    }
                    Log.e("COTTER_IDENTITY", "RequestIdentityToken callback error: " + error);
                    finish();
                }
            };

            // Make an http request to exchange auth_code with the identity
            Cotter.authRequest.RequestIdentityToken(ctx, idReq.codeVerifier, authCode, challengeID, IdentityRequest.URL_SCHEME, callback);
        } else {
            Log.e("handleResponse", "uri null, finishing");
            finish();
        }
    }

}
