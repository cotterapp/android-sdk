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
    public Map<String, String> BiometricChange = new HashMap<>();
    public Map<String, String> NetworkError = new HashMap<>();
    public Map<String, String> HttpError = new HashMap<>();

    public Map<String, String> ApproveRequest = new HashMap<>();
    public Map<String, String> RequestAuth = new HashMap<>();
    public Map<String, String> RequestAuthError = new HashMap<>();
    public Map<String, String> SuccessSheet = new HashMap<>();
    public Map<String, String> SuccessSheetError = new HashMap<>();
    public Map<String, String> QRCodeShow = new HashMap<>();
    public Map<String, String> QRCodeShowError = new HashMap<>();

    // Pin Enrollment
    public static String Title = "TITLE";
    public static String Subtitle = "SUBTITLE";
    public static String ButtonText = "BUTTON_TEXT";
    public static String ShowPin = "SHOW_PIN";
    public static String HidePin = "HIDE_PIN";
    public static String ErrorCombination = "ERROR_COMBINATION";
    public static String ErrorNoMatch = "ERROR_NO_MATCH";
    public static String ErrorOther = "ERROR_OTHER";
    public static String ErrorInvalid = "ERROR_INVALID";

    public static String DialogTitle = "DIALOG_TITLE";
    public static String DialogSubtitle = "DIALOG_SUBTITLE";
    public static String DialogDisabledSubtitle = "DIALOG_DISABLED_SUBTITLE";
    public static String DialogPositiveButton = "POSITIVE_BUTTON_TEXT";
    public static String DialogNegativeButton = "NEGATIVE_BUTTON_TEXT";

    // Biometric enrollment
    public static String BiometricTitle = "BIOMETRIC_TITLE";
    public static String BiometricSubtitle = "BIOMETRIC_SUBTITLE";
    public static String BiometricNegativeButton = "BIOMETRIC_NEGATIVE_BUTTON";

    // Auth Request
    public static String DefaultNetworkErrorTitle = "Connection Lost";
    public static String DefaultNetworkErrorSubtitle = "Please establish a stronger internet connection and try again.";
    public static String DefaultNetworkPositiveButton = "Try Again";

    // Trusted Device
    public static String ButtonNo = "BUTTON_NO";
    public static String ButtonYes = "BUTTON_YES";

    public Strings() {

        Headers.put(ScreenNames.PinEnrollmentEnterPin, "Activate Pin");
        Headers.put(ScreenNames.PinEnrollmentReEnterPin, "Confirm Pin");
        Headers.put(ScreenNames.PinVerification, "Verify Pin");
        Headers.put(ScreenNames.PinChangeVerifyPin, "Change Pin");
        Headers.put(ScreenNames.PinChangeEnterPin, "Enter New Pin");
        Headers.put(ScreenNames.PinChangeReEnterPin, "Confirm New Pin");
        Headers.put(ScreenNames.RegisterDeviceQRScanner, "Scan QR Code");
        Headers.put(ScreenNames.RegisterDeviceQRShow, "");

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
        PinEnrollmentReEnterPin.put(ErrorOther, "Something went wrong.");

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

        // Enable Disable Biometric
        BiometricChange.put(BiometricTitle, "Biometric Verification");
        BiometricChange.put(BiometricSubtitle, "Protect your account using Biometrics");
        BiometricChange.put(BiometricNegativeButton, "Cancel");
        BiometricChange.put(DialogTitle, "Something went wrong");
        BiometricChange.put(DialogSubtitle, "You may have already enrolled Biometrics before.");
        BiometricChange.put(DialogDisabledSubtitle, "You may not have Biometrics enrolled.");
        BiometricChange.put(DialogPositiveButton, "Cancel");
        BiometricChange.put(DialogNegativeButton, "Try Again");

        // Network Error Dialog
        NetworkError.put(DialogTitle, "Connection Lost");
        NetworkError.put(DialogSubtitle, "Please establish a stronger internet connection and try again.");
        NetworkError.put(DialogPositiveButton, "Try Again");

        // HTTP Error Dialog
        HttpError.put(DialogTitle, "Something went wrong");
        HttpError.put(DialogSubtitle, "Looks like there's an error on our end. Please try again.");
        HttpError.put(DialogPositiveButton, "Try Again");

        // Approve login from Non-Trusted Device
        ApproveRequest.put(Title, "Are you trying to sign in?");
        ApproveRequest.put(Subtitle, "Someone is trying to sign in to your account from another device.");
        ApproveRequest.put(ButtonYes, "Yes");
        ApproveRequest.put(ButtonNo, "No, it's not me");

        // Request Auth
        RequestAuth.put(DialogTitle, "Approve this login from your phone");
        RequestAuth.put(DialogSubtitle, "A notification is sent to your trusted device to confirm it's you.");
        RequestAuthError.put(DialogTitle, "Something went wrong");
        RequestAuthError.put(DialogSubtitle, "We're unable to confirm that it's you. Please try again.");

        // Success Sheet
        SuccessSheet.put(DialogTitle, "Success Registering New Device");
        SuccessSheet.put(DialogSubtitle, "You can now use your new device to access your account without approval.");
        SuccessSheetError.put(DialogTitle, "Unable to Register New Device");
        SuccessSheetError.put(DialogSubtitle, "Please try again.");

        // QR Code Show
        QRCodeShow.put(Title, "Register this Device");
        QRCodeShow.put(Subtitle, "Please scan this QR Code from a Trusted Device.");
        QRCodeShowError.put(Title, "Something went wrong");
        QRCodeShowError.put(Subtitle, "The request timed out. Please try again.");
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

    public void setNetworkErrorStrings(String key, String value) {
        NetworkError.put(key, value);
    }


    // Trusted Devices
    public void setApproveRequestStrings(String key, String value) {
        ApproveRequest.put(key, value);
    }
    public void setRequestAuthStrings(String key, String value) {
        RequestAuth.put(key, value);
    }
    public void setRequestAuthErrorStrings(String key, String value) {
        RequestAuthError.put(key, value);
    }
    public void setSuccessSheetStrings(String key, String value) {
        SuccessSheet.put(key, value);
    }
    public void setSuccessSheetErrorStrings(String key, String value) {
        SuccessSheetError.put(key, value);
    }
    public void setQRCodeShowStrings(String key, String value) {
        QRCodeShow.put(key, value);
    }
    public void setQRCodeShowErrorStrings(String key, String value) {
        QRCodeShowError.put(key, value);
    }
}
