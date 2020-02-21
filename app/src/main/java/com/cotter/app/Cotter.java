package com.cotter.app;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.fragment.app.FragmentActivity;

public class Cotter {
    public static String UserID;
    public static String ApiKeyID;
    public static String ApiSecretKey;

    // AuthRequest
    public static AuthRequest authRequest;

    //    Methods
    public static String PinMethod = "PIN";
    public static String BiometricMethod = "BIOMETRIC";

    // Colors
    public static Colors colors;

    // Strings
    public static Strings strings;

    public static BiometricPromptStandalone biometricPrompt;
    public static CotterMethodHelper methods;
    public static IdentityRequest identity;


    private static Context ctx;
    public static Flow PinEnrollment = new Flow(new String[] { ScreenNames.PinEnrollmentEnterPin, ScreenNames.PinEnrollmentReEnterPin, ScreenNames.PinEnrollmentSuccess });
    public static Flow PinVerification= new Flow(new String[] { ScreenNames.PinVerification });
    public static Flow PinChange = new Flow(new String[] { ScreenNames.PinChangeVerifyPin, ScreenNames.PinChangeEnterPin, ScreenNames.PinChangeReEnterPin, ScreenNames.PinChangeSuccess });

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

    public static User getUser(AuthRequest authRequest) {
        return User.getInstance(ctx, authRequest);
    }

    public static User getUser() {
        if (authRequest == null) {
            Log.e("COTTER_NOT_INITIALIZED", "Cotter is not yet initialized!");
        }
        return User.getInstance(ctx, authRequest);
    }

    public static Rules getRules(AuthRequest authRequest) {
        return Rules.getInstance(ctx, authRequest);
    }

    public static void initBiometricSwitch(Context ctx, FragmentActivity fragmentAct, Activity act, CotterBiometricCallback callback) {
        biometricPrompt = new BiometricPromptStandalone(ctx, fragmentAct, act, callback);
    }


    // Identity Request
    public static IdentityRequest getIdentity() {
        if (identity == null) {
            Log.e("COTTER IDENTITY", "You need to call newIdentity when calling .login first, then getIdentity in .handleCallbackURL");
            identity = new IdentityRequest(ctx);
        }
        return identity;
    }
    public static IdentityRequest newIdentity(Context ctx) {
        identity = new IdentityRequest(ctx);
        return identity;
    }
}
