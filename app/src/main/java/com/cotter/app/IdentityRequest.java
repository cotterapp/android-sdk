package com.cotter.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class IdentityRequest {

    static final String charSet = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static String URL_SCHEME = "app.cotter.android://auth_callback";
    public static String OPEN_TWA = "OPEN_TWA";
    public static String TWA_URL = "TWA_URL";

    String codeVerifier;
    String codeChallenge;
    String state;
    Context ctx;

    public IdentityRequest(Context ctx) {
        this.ctx = ctx;
        codeVerifier = generateCodeVerifier();
        codeChallenge = generateCodeChallenge(codeVerifier);

        Log.i("CODE VERIFIER", codeVerifier);
        Log.i("CODE CHALLENGE", codeChallenge);
        state = generateState();
    }

    public String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String codeVerifier = Base64.encodeToString(bytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
        return codeVerifier;
    }

    public String generateCodeChallenge(String codeVerifier) {
        try {
            byte[] codeVerifierBytes = codeVerifier.getBytes("US-ASCII");
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(codeVerifierBytes);
            byte[] codeChallengeBytes = md.digest();
            String codeChallenge = Base64.encodeToString(codeChallengeBytes, Base64.URL_SAFE | Base64.NO_WRAP | Base64.NO_PADDING);
            return codeChallenge;
        } catch (Exception e) {
            Log.e("COTTER_IDENTITY_HELPER", e.toString());
        }
        return null;
    }

    public String generateState() {
        SecureRandom rnd = new SecureRandom();
        int len = 6;
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( charSet.charAt( rnd.nextInt(charSet.length()) ) );
        return sb.toString();
    }


    public void login(String type, Context ctx, Class callbackClass) {
        // build URL
        String url = "https://js.cotter.app/app?";
        url = url + "api_key=" + Cotter.ApiKeyID;
        url = url + "&redirect_url=" + Uri.encode(URL_SCHEME);
        url = url + "&type=" + type;
        url = url + "&code_challenge=" + codeChallenge;
        url = url + "&state=" + state;

        Log.i("COTTER_IDENTITY", "Login: " + url);

        // Add receiver intent here
        Intent completeIntent = new Intent(ctx, callbackClass);

        IdentityManager.getInstance().addPendingRequestIntent(state, completeIntent, this);

        Intent identityActivity = new Intent(ctx, IdentityActivity.class);
        identityActivity.putExtra(OPEN_TWA, true);
        identityActivity.putExtra(TWA_URL, url);
        ctx.startActivity(identityActivity);
    }

    public void loginWithInput(String type, String input, Context ctx, Class callbackClass) {
        // build URL
        String url = "https://js.cotter.app/app?direct_login=true";
        url = url + "&api_key=" + Cotter.ApiKeyID;
        url = url + "&redirect_url=" + Uri.encode(URL_SCHEME);
        url = url + "&type=" + type;
        url = url + "&code_challenge=" + codeChallenge;
        url = url + "&state=" + state;
        url = url + "&input=" + Uri.encode(input);

        Log.i("COTTER_IDENTITY", "Login with input: " + url);

        // Add receiver intent here
        Intent completeIntent = new Intent(ctx, callbackClass);

        IdentityManager.getInstance().addPendingRequestIntent(state, completeIntent, this);

        Intent identityActivity = new Intent(ctx, IdentityActivity.class);
        identityActivity.putExtra(OPEN_TWA, true);
        identityActivity.putExtra(TWA_URL, url);
        ctx.startActivity(identityActivity);
    }


}
