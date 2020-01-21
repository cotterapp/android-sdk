package com.cotter.app;

import android.content.Context;

public class CoreLibrary {
    public static String UserID;
    public static String ApiKeyID;
    public static String ApiSecretKey;

    //    Methods
    public static String PinMethod = "PIN";

    private static Context ctx;
    public static Flow PinEnrollment = new Flow(new String[] { ScreenNames.PinEnrollmentEnterPin, ScreenNames.PinEnrollmentReEnterPin });
    public static Flow PinVerification= new Flow(new String[] { ScreenNames.PinVerification });

    public static void init(Context context, String mainServerURL, String userID, String apiKeyID, String apiSecretKey) {
        UserID = userID;
        ApiKeyID = apiKeyID;
        ApiSecretKey = apiSecretKey;
        ctx = context;
        getUser();
        getRules();
        AuthRequest.SetMainServerURL(mainServerURL);
    }

    public static User getUser() {
        return User.getInstance(ctx);
    }

    public static Rules getRules() {
        return Rules.getInstance(ctx);
    }
}
