package com.cotter.app;

import java.util.HashMap;
import java.util.Map;

public class Strings {

    // Strings
    public Map<String, String> Headers = new HashMap<>();
    public Map<String, String> PinEnrollmentEnterPin = new HashMap<>();
    public Map<String, String> PinEnrollmentReEnterPin = new HashMap<>();

    // Pin Enrollment
    public static String Title = "TITLE";
    public static String ShowPin = "SHOW_PIN";
    public static String HidePin = "HIDE_PIN";
    public static String ErrorCombination = "ERROR_COMBINATION";
    public static String ErrorNoMatch = "ERROR_NO_MATCH";

    public Strings() {
        Headers.put(ScreenNames.PinEnrollmentEnterPin, "Activate Pin");
        Headers.put(ScreenNames.PinEnrollmentReEnterPin, "Confirm Pin");
        Headers.put(ScreenNames.PinVerification, "Verify Pin");
        PinEnrollmentEnterPin.put(Title, "Enter Pin");
        PinEnrollmentEnterPin.put(ShowPin, "Show Pin");
        PinEnrollmentEnterPin.put(HidePin, "Hide Pin");
        PinEnrollmentEnterPin.put(ErrorCombination, "Your PIN is weak. Please enter a stronger PIN.");
        PinEnrollmentReEnterPin.put(Title, "Re-Enter Pin");
        PinEnrollmentReEnterPin.put(ShowPin, "Show Pin");
        PinEnrollmentReEnterPin.put(HidePin, "Hide Pin");
        PinEnrollmentReEnterPin.put(ErrorNoMatch, "Your PIN doesn't match your previous PIN.");
    }


    public void setHeaders(String screenName, String header) {
        Headers.put(screenName, header);
    }

    public void setPinEnrollmentEnterPinStrings(String key, String value) {
        PinEnrollmentEnterPin.put(key, value);
    }

    public void setPinEnrollmentReEnterPinStrings(String key, String value) {
        PinEnrollmentReEnterPin.put(key, value);
    }
}
