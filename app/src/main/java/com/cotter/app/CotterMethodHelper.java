package com.cotter.app;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class CotterMethodHelper {
    Context ctx;

    public CotterMethodHelper(Context ctx) {
        this.ctx = ctx;
    }

    // Check if biometric is enrolled in this device
    public void biometricEnrolled(final CotterMethodChecker callback) {
        String pubKey = BiometricHelper.getPublicKey();
        Cotter.authRequest.CheckEnrolledMethod(ctx, Cotter.BiometricMethod, pubKey, new Callback(){
            public void onSuccess(JSONObject response){
                try {
                    if(response.getBoolean("enrolled") && response.getString("method").equals(Cotter.BiometricMethod)){
                        callback.onCheck(true);
                    } else {
                        callback.onCheck(false);
                    }
                } catch (Exception e) {
                    callback.onCheck(false);
                    Log.e("COTTER_REQ_BIO_ENROLLED", e.toString());
                }
            }
            public void onError(String error){
                Log.e("fetch User Error", error);
            }
        });
    }

    // Check if biometric is enrolled in any device (not necessarily this device
    public void biometricEnrolledAny(final CotterMethodChecker callback) {
        Cotter.authRequest.GetUser(ctx, new Callback(){
            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                // Convert String Array to List
                List<String> enrolled = Arrays.asList(user.enrolled);

                if(enrolled.contains(Cotter.BiometricMethod)){
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

    public void biometricDefault(final CotterMethodChecker callback) {

        Cotter.authRequest.GetUser(ctx, new Callback(){
            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                if(user.default_method != null && user.default_method.equals(Cotter.BiometricMethod)){
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
        String pubKey = TrustedDeviceHelper.getPublicKey();

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
