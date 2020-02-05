package com.cotter.app;

import android.content.Context;

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
//        getRules(authRequest);
        strings = new Strings();
        colors = new Colors();
    }

    public static User getUser(AuthRequest authRequest) {
        return User.getInstance(ctx, authRequest);
    }

    public static Rules getRules(AuthRequest authRequest) {
        return Rules.getInstance(ctx, authRequest);
    }
}
