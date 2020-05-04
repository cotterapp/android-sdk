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
    private FragmentActivity fragmentAct;
    private Activity act;

    private Map<String, String> ActivityStrings;

    private CotterBiometricCallback callback;

    public BiometricPromptStandalone(Context ctx, FragmentActivity fragmentAct, Activity act, CotterBiometricCallback callback) {
        ActivityStrings = Cotter.strings.BiometricChange;
        this.ctx = ctx;
        this.fragmentAct = fragmentAct;
        this.act = act;
        this.callback = callback;
        publicKey = BiometricHelper.getPublicKey();

        biometricAvailable = BiometricHelper.checkBiometricAvailable(ctx);

        if (biometricAvailable) {
            // Setup Biometric Handlers for onAuthSuccess, or onAuthFail, etc.
            BiometricHelper.setupEnrollBiometricHandler(this, ctx, fragmentAct, act, callback);

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
        callback.onLoading(true);
        // Signature is null for enrollment
        // Enroll Biometric
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.e("Submit Key Success", response.toString());
                callback.onSuccess(true);
                User.refetchUser(ctx, Cotter.authRequest);
                callback.onLoading(false);
            }
            public void onError(String error){
                Log.e("Submit Key Error", error);
                if (!error.equals(AuthRequest.NETWORK_ERROR_MESSAGE)) {
                    invalidEnrollBiometric();
                    User.refetchUser(ctx, Cotter.authRequest);
                }
                callback.onError(error);
                callback.onLoading(false);
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

        // Show Network Error if needed
        if (!AuthRequest.networkIsAvailable(ctx)) {
            Callback cb = new Callback() {
                @Override
                public void onSuccess(JSONObject result) {
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            };
            AuthRequest.showNetworkErrorDialogIfNecessary(ctx, cb);
            return;
        }

        // Continue showing Biometric Prompt
        final BiometricInterface bi = this;
        Cotter.methods.pinEnrolled(new CotterMethodChecker() {
            @Override
            public void onCheck(boolean result) {
                if (result) {
                    // if PIN already enrolled, can proceed with biometric
                    if (!biometricAvailable) {
                        Log.e("COTTER BIOMETRIC PROMPT", "Biometric is not available on this device");
                        return;
                    }
                    publicKey = BiometricHelper.getPublicKey();
                    if (publicKey == null) {
                        // Generate keypair that can only be accessed by biometrics
                        publicKey = BiometricHelper.generateKeyPair();
                    }
                    BiometricHelper.PromptBiometric(bi);
                } else {
                    // otherwise, MUST start with pin enrollment
                    Cotter.PinEnrollment.startFlow(ctx, ctx.getClass(), "PIN_ENROLLMENT");
                    act.finish();
                }
            }
        });
    }

    public void disableBiometric() {
        if (!biometricAvailable) {
            Log.e("COTTER BIOMETRIC PROMPT", "Biometric is not available on this device");
            return;
        }
        callback.onLoading(true);

        String pubKey = BiometricHelper.getPublicKey();

        // Signature is null for enrollment
        // Enroll Biometric
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.e("Delete Key Success", response.toString());
                User.refetchUser(ctx, Cotter.authRequest);
                callback.onSuccess(false);
                callback.onLoading(false);
            }
            public void onError(String error){
                Log.e("Delete Key Error", error);
                if (!error.equals(AuthRequest.NETWORK_ERROR_MESSAGE)) {
                    invalidDisableBiometric();
                    User.refetchUser(ctx, Cotter.authRequest);
                }
                callback.onError(error);
                callback.onLoading(false);
            }
        };

        Cotter.authRequest.DeleteMethod(ctx, Cotter.BiometricMethod, pubKey, cb);
    }
}
