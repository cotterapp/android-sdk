package com.cotter.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class CotterMethodHelper {
    Context ctx;
    public static String BiometricEnrolledThisDeviceKey = "BIOMETRIC_ENROLLED_THIS_DEVICE";
    public static String BiometricEnrolledAnyKey = "BIOMETRIC_ENROLLED_ANY";
    public static String BiometricEnrolledDefaultKey = "BIOMETRIC_ENROLLED_DEFAULT";

    public CotterMethodHelper(Context ctx) {
        this.ctx = ctx;
    }

    // Check if biometric is enrolled in this device
    public void biometricEnrolled(final CotterMethodChecker callback) {
        biometricEnrolledForce(callback, false);
    }

    // Check if biometric is enrolled in this device
    public void biometricEnrolledForce(final CotterMethodChecker callback, boolean forceFetch) {

        // check in shared preferences
        SharedPreferences sharedPref = ctx.getSharedPreferences(
                Cotter.getSharedPreferenceFileKeyPrefix(), Context.MODE_PRIVATE);

        // if shared preferences exist
        if (sharedPref.contains(BiometricEnrolledThisDeviceKey) && !forceFetch) {
            Log.d("COTTER_BIO_ENROLLED", "Getting data from shared preferences, sharedPref contain: " + sharedPref.contains(BiometricEnrolledThisDeviceKey) + ", force: " + forceFetch);
            boolean enrolledThisDevice = sharedPref.getBoolean(BiometricEnrolledThisDeviceKey, false);
            callback.onCheck(enrolledThisDevice);
            return;
        }

        // otherwise, check and save
        Log.d("COTTER_BIO_ENROLLED", "Getting data from server, sharedPref contain: " + sharedPref.contains(BiometricEnrolledThisDeviceKey) + ", force: " + forceFetch);
        String pubKey = BiometricHelper.getPublicKey();
        Cotter.authRequest.CheckEnrolledMethod(ctx, Cotter.BiometricMethod, pubKey, new Callback(){
            public void onSuccess(JSONObject response){
                // Update shared preferences
                SharedPreferences.Editor editor = sharedPref.edit();
                try {
                    if(response.getBoolean("enrolled") && response.getString("method").equals(Cotter.BiometricMethod)){
                        callback.onCheck(true);
                        editor.putBoolean(BiometricEnrolledThisDeviceKey, true);
                        Log.d("COTTER_BIO_ENROLLED", "Result true: Success updating data to shared pref");
                    } else {
                        callback.onCheck(false);
                        editor.putBoolean(BiometricEnrolledThisDeviceKey, false);
                        Log.d("COTTER_BIO_ENROLLED", "Result false: Success updating data to shared pref");
                    }
                    editor.commit();
                } catch (Exception e) {
                    callback.onCheck(false);
                    Log.e("COTTER_BIO_ENROLLED","Exception: " +  e.toString());
                }
            }
            public void onError(String error){
                callback.onCheck(false);
                Log.e("COTTER_BIO_ENROLLED", "onError: " + error);
            }
        });
    }

    // Check if biometric is enrolled in any device (not necessarily this device
    public void biometricEnrolledAny(final CotterMethodChecker callback) {
        // check in shared preferences
        SharedPreferences sharedPref = ctx.getSharedPreferences(
                Cotter.getSharedPreferenceFileKeyPrefix(), Context.MODE_PRIVATE);

        // if shared preferences exist
        if (sharedPref.contains(BiometricEnrolledAnyKey)) {
            boolean enrolledAny = sharedPref.getBoolean(BiometricEnrolledAnyKey, false);
            callback.onCheck(enrolledAny);
            return;
        }

        Cotter.authRequest.GetUser(ctx, new Callback(){
            public void onSuccess(JSONObject response){
                // Update shared preferences
                SharedPreferences.Editor editor = sharedPref.edit();

                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                // Convert String Array to List
                List<String> enrolled = Arrays.asList(user.enrolled);

                if(enrolled.contains(Cotter.BiometricMethod)){
                    callback.onCheck(true);
                    editor.putBoolean(BiometricEnrolledAnyKey, true);
                } else {
                    callback.onCheck(false);
                    editor.putBoolean(BiometricEnrolledAnyKey, false);
                }
                editor.commit();
            }
            public void onError(String error){

                callback.onCheck(false);
                Log.e("fetch User Error", error);
            }
        });
    }

    public void biometricDefault(final CotterMethodChecker callback) {
        // check in shared preferences
        SharedPreferences sharedPref = ctx.getSharedPreferences(
                Cotter.getSharedPreferenceFileKeyPrefix(), Context.MODE_PRIVATE);

        // if shared preferences exist
        if (sharedPref.contains(BiometricEnrolledDefaultKey)) {
            boolean enrolledDefault = sharedPref.getBoolean(BiometricEnrolledDefaultKey, false);
            callback.onCheck(enrolledDefault);
            return;
        }

        Cotter.authRequest.GetUser(ctx, new Callback(){
            // Update shared preferences
            SharedPreferences.Editor editor = sharedPref.edit();

            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                if(user.default_method != null && user.default_method.equals(Cotter.BiometricMethod)){
                    callback.onCheck(true);
                    editor.putBoolean(BiometricEnrolledDefaultKey, true);
                } else {
                    callback.onCheck(false);
                    editor.putBoolean(BiometricEnrolledDefaultKey, false);
                }
                editor.commit();
            }
            public void onError(String error){
                Log.e("fetch User Error", error);
            }
        });
    }

    public void biometricAvailable(CotterMethodChecker callback) {
        if (BiometricHelper.checkBiometricAvailable(ctx)) {
            callback.onCheck(true);
        } else {
            callback.onCheck(false);
        }
    }

    public void pinEnrolled(final CotterMethodChecker callback) {
        Cotter.authRequest.CheckEnrolledMethod(ctx, Cotter.PinMethod, null, new Callback(){
            public void onSuccess(JSONObject response){
                try {
                    if(response.getBoolean("enrolled") && response.getString("method").equals(Cotter.PinMethod)){
                        callback.onCheck(true);
                    } else {
                        callback.onCheck(false);
                    }
                } catch (Exception e) {
                    callback.onCheck(false);
                    Log.e("COTTER_PIN_ENROLLED", e.toString());
                }
            }
            public void onError(String error){
                Log.e("COTTER_REQ_PIN_ENROLLED", error);
            }
        });
    }

    public void pinDefault(final CotterMethodChecker callback) {
        Cotter.authRequest.GetUser(ctx, new Callback(){
            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                if(user.default_method != null && user.default_method.equals(Cotter.PinMethod)){
                    callback.onCheck(true);
                } else {
                    callback.onCheck(false);
                }
            }
            public void onError(String error){
                Log.e("fetch User Error", error);
            }
        });
    }

    // Check if trusted device is enrolled in this device
    public void trustedDeviceEnrolled(final CotterMethodChecker callback) {
        String pubKey = TrustedDeviceHelper.getPublicKey(ctx);

        Cotter.authRequest.CheckEnrolledMethod(ctx, Cotter.TrustedDeviceMethod, pubKey, new Callback(){
            public void onSuccess(JSONObject response){
                try {
                    if(response.getBoolean("enrolled") && response.getString("method").equals(Cotter.TrustedDeviceMethod)){
                        callback.onCheck(true);
                    } else {
                        callback.onCheck(false);
                    }
                } catch (Exception e) {
                    callback.onCheck(false);
                    Log.e("COTTER_TRUST_ENROLLED", e.toString());
                }
            }
            public void onError(String error){
                Log.e("fetch User Error", error);
            }
        });
    }

    // Check if biometric is enrolled in any device (not necessarily this device
    public void trustedDeviceEnrolledAny(final CotterMethodChecker callback) {
        Cotter.authRequest.GetUser(ctx, new Callback(){
            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                // Convert String Array to List
                List<String> enrolled = Arrays.asList(user.enrolled);

                if(enrolled.contains(Cotter.TrustedDeviceMethod)){
                    callback.onCheck(true);
                } else {
                    callback.onCheck(false);
                }
            }
            public void onError(String error){
                Log.e("fetch User Error", error);
            }
        });
    }

    public void trustedDeviceDefault(final CotterMethodChecker callback) {
        Cotter.authRequest.GetUser(ctx, new Callback(){
            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                if(user.default_method != null && user.default_method.equals(Cotter.TrustedDeviceMethod)){
                    callback.onCheck(true);
                } else {
                    callback.onCheck(false);
                }
            }
            public void onError(String error){
                Log.e("fetch User Error", error);
            }
        });
    }


}
