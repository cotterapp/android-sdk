package com.cotter.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import androidx.biometric.BiometricPrompt;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.Executor;

public class BiometricPromptStandalone implements BiometricInterface {

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String publicKey;

    private boolean biometricAvailable;

    private Context ctx;

    private Map<String, String> ActivityStrings;

    private CotterBiometricCallback callback;

    public BiometricPromptStandalone(Context ctx, FragmentActivity fragmentAct, Activity act, CotterBiometricCallback callback) {
        ActivityStrings = Cotter.strings.BiometricChange;
        this.ctx = ctx;
        this.callback = callback;
        publicKey = BiometricHelper.getPublicKey();

        biometricAvailable = BiometricHelper.checkBiometricAvailable(ctx);

        if (biometricAvailable) {
            // Setup Biometric Handlers for onAuthSuccess, or onAuthFail, etc.
            BiometricHelper.setupEnrollBiometricHandler(this, ctx, fragmentAct, act);

            // Create Biometric Prompt
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(ActivityStrings.get(Strings.BiometricTitle))
                    .setSubtitle(ActivityStrings.get(Strings.BiometricSubtitle))
                    .setNegativeButtonText(ActivityStrings.get(Strings.BiometricNegativeButton))
                    .build();
        }
    }

    // SETTER AND GETTER
    public void setBiometricPrompt(BiometricPrompt bp) {
        biometricPrompt = bp;
    }
    public BiometricPrompt getBiometricPrompt() {
        return biometricPrompt;
    }
    public void setExecutor(Executor ex) {
        executor = ex;
    }
    public Executor getExecutor() {
        return executor;
    }
    public void setPromptInfo(BiometricPrompt.PromptInfo pi) {
        promptInfo = pi;
    }
    public BiometricPrompt.PromptInfo getPromptInfo() {
        return promptInfo;
    }

    public void onSubmitBio(String signature) {
        // Signature is null for enrollment
        // Enroll Biometric
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.e("Submit Key Success", response.toString());
                callback.onSuccess(true);
                User.refetchUser(ctx, Cotter.authRequest);
            }
            public void onError(String error){
                Log.e("Submit Key Error", error);
                invalidEnrollBiometric();
                callback.onError(error);
                User.refetchUser(ctx, Cotter.authRequest);
            }
        };

        Cotter.authRequest.EnrollMethod(ctx, Cotter.BiometricMethod, publicKey, cb);
    }

    // unused BiometricInterface methods
    public String getStringToSign() {return null;}


    // When Enroll Pin inside onSubmitBio succeed, this will be invoked, going to the next page
    public boolean onContinue(boolean success) {
        return success;
    }


    // Fail http request -> already enrolled
    public void invalidEnrollBiometric() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.CustomDialogTheme)
                .setTitle(ActivityStrings.get(Strings.DialogTitle))
                .setMessage(ActivityStrings.get(Strings.DialogSubtitle))
                .setPositiveButton(ActivityStrings.get(Strings.DialogPositiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Skipping biometrics
                        User.refetchUser(ctx, Cotter.authRequest);
                        callback.onCanceled();
                    }
                })
                .setNegativeButton(ActivityStrings.get(Strings.DialogNegativeButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retry Biometric
                        enableBiometric(); // Prompt again
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }


    // Fail http request -> already enrolled
    public void invalidDisableBiometric() {
        final BiometricInterface bi = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.CustomDialogTheme)
                .setTitle(ActivityStrings.get(Strings.DialogTitle))
                .setMessage(ActivityStrings.get(Strings.DialogDisabledSubtitle))
                .setPositiveButton(ActivityStrings.get(Strings.DialogPositiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Skipping biometrics
                        User.refetchUser(ctx, Cotter.authRequest);
                        callback.onCanceled();
                    }
                })
                .setNegativeButton(ActivityStrings.get(Strings.DialogNegativeButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retry Biometric
                        disableBiometric(); // Prompt again
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }

    // Open Biometric Prompt
    public void enableBiometric() {
        if (publicKey == null) {
            // Generate keypair that can only be accessed by biometrics
            publicKey = BiometricHelper.generateKeyPair();
        }
        BiometricHelper.PromptBiometric(this);
    }

    public void disableBiometric() {
        String pubKey = BiometricHelper.getPublicKey();

        // Signature is null for enrollment
        // Enroll Biometric
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.e("Delete Key Success", response.toString());
                User.refetchUser(ctx, Cotter.authRequest);
                callback.onSuccess(false);
            }
            public void onError(String error){
                Log.e("Delete Key Error", error);
                invalidDisableBiometric();
                User.refetchUser(ctx, Cotter.authRequest);
                callback.onError(error);
            }
        };

        Cotter.authRequest.DeleteMethod(ctx, Cotter.BiometricMethod, pubKey, cb);
    }
}
