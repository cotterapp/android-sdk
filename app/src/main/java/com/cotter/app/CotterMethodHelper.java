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
    public void biometricEnrolled(final CotterMethodChecker callback) {

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

        Cotter.authRequest.GetUser(ctx, new Callback(){
            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User user = gson.fromJson(response.toString(), User.class);

                // Convert String Array to List
                List<String> enrolled = Arrays.asList(user.enrolled);

                if(enrolled.contains(Cotter.PinMethod)){
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

}
