package com.cotter.app;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.gson.Gson;

import org.json.JSONObject;

public class Cotter {
    public static String UserID;
    public static String ApiKeyID;
    public static String ApiSecretKey;

    // AuthRequest
    public static AuthRequest authRequest;

    //    Methods
    public static String PinMethod = "PIN";
    public static String BiometricMethod = "BIOMETRIC";
    public static String TrustedDeviceMethod = "TRUSTED_DEVICE";

    // Sending Methods
    public static String EmailSendingMethod = "EMAIL";
    public static String SMSSendinngMethod = "SMS";

    // Colors
    public static Colors colors;

    // Strings
    public static Strings strings;

    public static BiometricPromptStandalone biometricPrompt;
    public static CotterMethodHelper methods;
    public static IdentityRequest identity;

    public static boolean allowClosePinEnrollment = false;

    public static User user;

    public static String sharedPreferenceFileKeyPrefix = "com.cotter.app.COTTER_PREFERENCES";


    private static Context ctx;
    public static Flow PinEnrollment = new Flow(new String[] { ScreenNames.PinEnrollmentEnterPin, ScreenNames.PinEnrollmentReEnterPin, ScreenNames.PinEnrollmentSuccess });
    public static Flow PinVerification = new Flow(new String[] { ScreenNames.PinVerification });
    public static Flow PinChange = new Flow(new String[] { ScreenNames.PinChangeVerifyPin, ScreenNames.PinChangeEnterPin, ScreenNames.PinChangeReEnterPin, ScreenNames.PinChangeSuccess });
    public static Flow PinReset = new Flow(new String[] { ScreenNames.PinReset, ScreenNames.PinResetEnterPin, ScreenNames.PinResetReEnterPin, ScreenNames.PinResetSuccess });

    public static String MAIN_SERVER_URL = "https://www.cotter.app/api/v0";

    public static void init(Context context, String mainServerURL, String userID, String apiKeyID, String apiSecretKey) {
        UserID = userID;
        ApiKeyID = apiKeyID;
        ApiSecretKey = apiSecretKey;
        ctx = context;
        authRequest = new AuthRequest(mainServerURL);
        getUser(authRequest);
        // getRules(authRequest);
        strings = new Strings();
        colors = new Colors();
        methods = new CotterMethodHelper(context);
    }

    public static String getSharedPreferenceFileKeyPrefix() {
        return sharedPreferenceFileKeyPrefix + "_" + Cotter.ApiKeyID + "_" + Cotter.UserID;
    }


    public static void setUser(User nUser) {
        user = nUser;
    }

    // Init function for Identity Request (No user yet + using PKCE)
    public static void init(Context context, String mainServerURL, String apiKeyID) {
        ApiKeyID = apiKeyID;
        ctx = context;
        authRequest = new AuthRequest(mainServerURL);
        strings = new Strings();
        colors = new Colors();
        methods = new CotterMethodHelper(context);
    }

    public static User getUser(AuthRequest authRequest) {
        return User.getInstance(ctx, authRequest);
    }

    public static User getUser() {
        return user;
    }

    public static Rules getRules(AuthRequest authRequest) {
        return Rules.getInstance(ctx, authRequest);
    }

    public static void initBiometricSwitch(Context ctx, FragmentActivity fragmentAct, Activity act, CotterBiometricCallback callback) {
        biometricPrompt = new BiometricPromptStandalone(ctx, fragmentAct, act, callback);
    }


    // Identity Request
    public static IdentityRequest newIdentity(Context ctx, String urlScheme) {
        identity = new IdentityRequest(ctx, urlScheme);
        return identity;
    }
    public static void setAllowClosePinEnrollment(boolean allow) {
        allowClosePinEnrollment = allow;
    }


    // ==========================
    //     Device Based Auth
    // ==========================


    public static void init(Context context, String apiKeyID) {
        ApiKeyID = apiKeyID;
        ApiSecretKey = apiKeyID;
        ctx = context;
        authRequest = new AuthRequest(MAIN_SERVER_URL);
        strings = new Strings();
        colors = new Colors();
        methods = new CotterMethodHelper(context);
    }

    public static void signUpWithDevice(Context ctx, String identifier, Callback callback) {
        User.registerUser(ctx, authRequest, identifier, new Callback() {
            @Override
            public void onSuccess(JSONObject result) {
                Gson gson = new Gson();
                User us = gson.fromJson(result.toString(), User.class);
                setUser(us);
                UserID = us.client_user_id;
                authRequest = new AuthRequest(MAIN_SERVER_URL);
                TrustedDeviceHelper.enrollDeviceWithCotterUserID(ctx, us.ID, callback);
                User.refetchUser(ctx, authRequest);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public static void signInWithDevice(Context ctx, String identifier, AppCompatActivity act, Class callbackClass, Callback callback) {
        User.getByIdentifier(ctx, authRequest, identifier, new Callback() {
            @Override
            public void onSuccess(JSONObject result) {
                Gson gson = new Gson();
                User us = gson.fromJson(result.toString(), User.class);
                setUser(us);
                UserID = us.client_user_id;
                authRequest = new AuthRequest(MAIN_SERVER_URL);
                TrustedDeviceHelper.requestAuth(ctx, "LOGIN", act, callbackClass, callback);
                User.refetchUser(ctx, authRequest);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
}
