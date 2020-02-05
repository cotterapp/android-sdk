package com.cotter.app;

import java.util.HashMap;
import java.util.Map;

public class Strings {

    // Strings
    public Map<String, String> Headers = new HashMap<>();
    public Map<String, String> PinEnrollmentEnterPin = new HashMap<>();
    public Map<String, String> PinEnrollmentReEnterPin = new HashMap<>();
    public Map<String, String> PinEnrollmentSuccess = new HashMap<>();
    public Map<String, String> PinVerification = new HashMap<>();
    public Map<String, String> PinChangeVerifyPin = new HashMap<>();
    public Map<String, String> PinChangeEnterPin = new HashMap<>();
    public Map<String, String> PinChangeReEnterPin = new HashMap<>();
    public Map<String, String> PinChangeSuccess = new HashMap<>();

    // Pin Enrollment
    public static String Title = "TITLE";
    public static String Subtitle = "SUBTITLE";
    public static String ButtonText = "BUTTON_TEXT";
    public static String ShowPin = "SHOW_PIN";
    public static String HidePin = "HIDE_PIN";
    public static String ErrorCombination = "ERROR_COMBINATION";
    public static String ErrorNoMatch = "ERROR_NO_MATCH";
    public static String ErrorInvalid = "ERROR_INVALID";

    public static String DialogTitle = "DIALOG_TITLE";
    public static String DialogSubtitle = "DIALOG_SUBTITLE";
    public static String DialogPositiveButton = "POSITIVE_BUTTON_TEXT";
    public static String DialogNegativeButton = "NEGATIVE_BUTTON_TEXT";

    // Biometric enrollment
    public static String BiometricTitle = "BIOMETRIC_TITLE";
    public static String BiometricSubtitle = "BIOMETRIC_SUBTITLE";
    public static String BiometricNegativeButton = "BIOMETRIC_NEGATIVE_BUTTON";

    public Strings() {

        Headers.put(ScreenNames.PinEnrollmentEnterPin, "Activate Pin");
        Headers.put(ScreenNames.PinEnrollmentReEnterPin, "Confirm Pin");
        Headers.put(ScreenNames.PinVerification, "Verify Pin");
        Headers.put(ScreenNames.PinChangeVerifyPin, "Change Pin");
        Headers.put(ScreenNames.PinChangeEnterPin, "Enter New Pin");
        Headers.put(ScreenNames.PinChangeReEnterPin, "Confirm New Pin");

        // Pin Enrollment Default Strings
        PinEnrollmentEnterPin.put(Title, "Enter Pin");
        PinEnrollmentEnterPin.put(ShowPin, "Show Pin");
        PinEnrollmentEnterPin.put(HidePin, "Hide Pin");
        PinEnrollmentEnterPin.put(ErrorCombination, "Your PIN is weak. Please enter a stronger PIN.");
        PinEnrollmentEnterPin.put(DialogTitle, "Are you sure you don't want to setup PIN?");
        PinEnrollmentEnterPin.put(DialogSubtitle, "Setting up your PIN is important to secure your account.");
        PinEnrollmentEnterPin.put(DialogPositiveButton, "Setup PIN");
        PinEnrollmentEnterPin.put(DialogNegativeButton, "Next Time");

        PinEnrollmentReEnterPin.put(Title, "Re-Enter Pin");
        PinEnrollmentReEnterPin.put(ShowPin, "Show Pin");
        PinEnrollmentReEnterPin.put(HidePin, "Hide Pin");
        PinEnrollmentReEnterPin.put(ErrorNoMatch, "Your PIN doesn't match your previous PIN.");

        PinEnrollmentSuccess.put(Title, "Successfully Activated PIN");
        PinEnrollmentSuccess.put(Subtitle, "You can now use your PIN to unlock your account and make transactions");
        PinEnrollmentSuccess.put(ButtonText, "Done");
        PinEnrollmentSuccess.put(BiometricTitle, "Biometric Verification");
        PinEnrollmentSuccess.put(BiometricSubtitle, "Protect your account using Biometrics");
        PinEnrollmentSuccess.put(BiometricNegativeButton, "Cancel");
        PinEnrollmentSuccess.put(DialogTitle, "Something went wrong");
        PinEnrollmentSuccess.put(DialogSubtitle, "You may have already enrolled Biometrics before.");
        PinEnrollmentSuccess.put(DialogPositiveButton, "Skip this step");
        PinEnrollmentSuccess.put(DialogNegativeButton, "Try Again");

        // Pin Verification default strings
        PinVerification.put(Title, "Enter Pin");
        PinVerification.put(ShowPin, "Show Pin");
        PinVerification.put(HidePin, "Hide Pin");
        PinVerification.put(ErrorInvalid, "Your PIN is invalid");
        PinVerification.put(BiometricTitle, "Verify Biometric");
        PinVerification.put(BiometricSubtitle, "Verify your biometric to continue");
        PinVerification.put(BiometricNegativeButton, "Input Pin");
        PinVerification.put(DialogTitle, "Unable to verify biometric");
        PinVerification.put(DialogSubtitle, "Do you want to try again or enter pin instead?");
        PinVerification.put(DialogPositiveButton, "Input PIN");
        PinVerification.put(DialogNegativeButton, "Try Again");

        // Pin Change default strings
        PinChangeVerifyPin.put(Title, "Enter Current Pin");
        PinChangeVerifyPin.put(ShowPin, "Show Pin");
        PinChangeVerifyPin.put(HidePin, "Hide Pin");
        PinChangeVerifyPin.put(ErrorInvalid, "Your PIN is invalid");

        PinChangeEnterPin.put(Title, "Enter New Pin");
        PinChangeEnterPin.put(ShowPin, "Show Pin");
        PinChangeEnterPin.put(HidePin, "Hide Pin");
        PinChangeEnterPin.put(ErrorCombination, "Your PIN is weak. Please enter a stronger PIN.");
        PinChangeEnterPin.put(DialogTitle, "Are you sure you don't want to setup your new PIN?");
        PinChangeEnterPin.put(DialogSubtitle, "You will be able to use your old PIN if you don't setup a new PIN.");
        PinChangeEnterPin.put(DialogPositiveButton, "Setup New PIN");
        PinChangeEnterPin.put(DialogNegativeButton, "Next Time");

        PinChangeReEnterPin.put(Title, "Re-Enter New Pin");
        PinChangeReEnterPin.put(ShowPin, "Show Pin");
        PinChangeReEnterPin.put(HidePin, "Hide Pin");
        PinChangeReEnterPin.put(ErrorNoMatch, "Your PIN doesn't match your previous PIN.");

        PinChangeSuccess.put(Title, "Successfully Change PIN");
        PinChangeSuccess.put(Subtitle, "You can now use your new PIN to unlock your account and make transactions");
        PinChangeSuccess.put(ButtonText, "Done");
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
    public void setPinEnrollmentSuccessStrings(String key, String value) {
        PinEnrollmentSuccess.put(key, value);
    }
    public void setPinVerificationStrings(String key, String value) {
        PinVerification.put(key, value);
    }
}
