package com.cotter.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.Executor;

public class PinEnrollmentSuccessActivity extends AppCompatActivity implements BiometricInterface {
    public static String name = ScreenNames.PinEnrollmentSuccess;

    private TextView textTitle;
    private TextView textSubtitle;
    private Button button;
    private ImageView img;
    private ConstraintLayout constraint;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String publicKey;
    private FrameLayout loadingOverlay;

    private boolean biometricAvailable = false;

    public Map<String, String> ActivityStrings;

    private boolean changePin = false;
    private boolean resetPin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_enrollment_success);

        ActivityStrings = Cotter.strings.PinEnrollmentSuccess;

        Intent intent = getIntent();
        changePin = intent.getExtras().getBoolean("change_pin");
        resetPin = intent.getExtras().getBoolean("reset_pin");
        if (changePin) {
            name = ScreenNames.PinChangeSuccess;
            ActivityStrings = Cotter.strings.PinChangeSuccess;
        } else if (resetPin) {
            name = ScreenNames.PinResetSuccess;
            ActivityStrings = Cotter.strings.PinResetSuccess;
            Cotter.PinReset.addActivityStack(this);
        }

        textTitle = findViewById(R.id.title);
        textSubtitle = findViewById(R.id.subtitle);
        button = findViewById(R.id.button);
        img = findViewById(R.id.success_image);
        constraint = findViewById(R.id.container);


        // set loading overlay
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Set Strings
        textTitle.setText(ActivityStrings.get(Strings.Title));
        textSubtitle.setText(ActivityStrings.get(Strings.Subtitle));
        button.setText(ActivityStrings.get(Strings.ButtonText));

        // Set Colors
        button.setTextColor(Color.parseColor(Cotter.colors.ColorPrimary));
        img.setImageResource(Cotter.colors.SuccessImage);
        constraint.setBackgroundColor(Color.parseColor(Cotter.colors.ColorBackground));

        // Check that biometric is enabled
        biometricAvailable = BiometricHelper.checkBiometricAvailable(this) && !changePin && !resetPin;

        if (biometricAvailable) {


            CotterBiometricCallback callbackBiometric = new CotterBiometricCallback() {
                @Override
                public void onSuccess(boolean biometricEnabled) {

                }

                @Override
                public void onCanceled() {
                    Log.i("COTTER_BIOMETRIC", "CANCELLED");
                    onContinue();
                }

                @Override
                public void onError(String error) {

                }

                @Override
                public void onLoading(boolean loading) {

                }
            };
            // Setup Biometric Handlers for onAuthSuccess, or onAuthFail, etc.
            BiometricHelper.setupEnrollBiometricHandler(this, this, this, this, callbackBiometric);

            // Create Biometric Prompt
            promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(ActivityStrings.get(Strings.BiometricTitle))
                    .setSubtitle(ActivityStrings.get(Strings.BiometricSubtitle))
                    .setNegativeButtonText(ActivityStrings.get(Strings.BiometricNegativeButton))
                    .build();

            publicKey = BiometricHelper.getPublicKey();

            if (publicKey == null) {
                // Generate keypair that can only be accessed by biometrics
                publicKey = BiometricHelper.generateKeyPair();
            }
        }
    }


    @Override
    public void onBackPressed() {
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


    public void onCheckBiometric(View v) {
        if (biometricAvailable) {
            BiometricHelper.PromptBiometric(this);
        } else {
            onContinue();
        }
    }


    public void onSubmitBio(String signature) {
        setLoading(true);
        // Signature is null for enrollment
        // Enroll Biometric
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.i("Submit Key Success", response.toString());
                onContinue();
                setLoading(false);
            }
            public void onError(String error){
                Log.e("Submit Key Error", error);
                setLoading(false);
                invalidEnrollBiometric();
            }
        };

        Cotter.authRequest.EnrollMethod(this, Cotter.BiometricMethod, publicKey, cb);
    }

    // When Enroll Pin inside onSubmitBio succeed, this will be invoked, going to the next page
    public void onContinue() {
        // Refetch user
        User.refetchUser(getApplicationContext(), Cotter.authRequest);

        Class nextScreen = Cotter.PinEnrollment.nextStep(name);
        if (changePin) {
            nextScreen = Cotter.PinChange.nextStep(name);
        } else if (resetPin) {
            nextScreen = Cotter.PinReset.nextStep(name);
            Cotter.PinVerification.removeAllActivityStack();
            Cotter.PinReset.removeAllActivityStack();
        }
        Intent in = new Intent(this, nextScreen);
        startActivity(in);
        finish();
    }

    // unused BiometricInterface methods
    public String getStringToSign() {return null;}

    // Fail http request -> already enrolled
    public void invalidEnrollBiometric() {
        final BiometricInterface bi = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle(ActivityStrings.get(Strings.DialogTitle))
                .setMessage(ActivityStrings.get(Strings.DialogSubtitle))
                .setPositiveButton(ActivityStrings.get(Strings.DialogPositiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Skipping biometrics
                        onContinue();
                    }
                })
                .setNegativeButton(ActivityStrings.get(Strings.DialogNegativeButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Retry Biometric
                        BiometricHelper.PromptBiometric(bi); // Prompt again
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
    }

    // add loading overlay
    public void setLoading(boolean loading) {
        if (loading) {
            loadingOverlay.setVisibility(View.VISIBLE);
        } else {
            loadingOverlay.setVisibility(View.GONE);
        }
    }
}
