package com.cotter.app;

import java.util.HashMap;

public class ScreenNames {
    public static String PinEnrollmentEnterPin = "PinEnrollmentEnterPin";
    public static String PinEnrollmentReEnterPin = "PinEnrollmentReEnterPin";
    public static String PinVerification = "PinVerification";

    public static HashMap<String, Class> nameToScreen = new HashMap<>();
    static {
        nameToScreen.put(PinEnrollmentEnterPin, PinEnrollmentEnterPinActivity.class);
        nameToScreen.put(PinEnrollmentReEnterPin, PinEnrollmentReEnterPinActivity.class);
        nameToScreen.put(PinVerification, PinVerificationActivity.class);
    }

    public static Class getClassFromName(String name) {
        return nameToScreen.get(name);
    }

}
