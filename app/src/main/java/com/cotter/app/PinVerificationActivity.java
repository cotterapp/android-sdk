package com.cotter.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class PinVerificationActivity extends AppCompatActivity implements PinInterface, BiometricInterface {
    public static String name = ScreenNames.PinVerification;
    private static String event;
    private static String pin;
    private static int wrong;

    private List<TextView> pins = new ArrayList<TextView>();

    private boolean pinError = false;
    private boolean showPin = false;

    private TextView textTitle;
    private TextView textShow;
    private TextView textError;
    private ConstraintLayout container;
    private LinearLayout bullet;

    public Map<String, String> ActivityStrings;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private String timestamp;

    private boolean biometricAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_verification);

        ActivityStrings = Cotter.strings.PinVerification;

        // Refetch user
        User.refetchUser(getApplicationContext(), Cotter.authRequest);

        pin = "";
        wrong = 0;
        // set pins objects
        pins.add((TextView)findViewById(R.id.input_1));
        pins.add((TextView)findViewById(R.id.input_2));
        pins.add((TextView)findViewById(R.id.input_3));
        pins.add((TextView)findViewById(R.id.input_4));
        pins.add((TextView)findViewById(R.id.input_5));
        pins.add((TextView)findViewById(R.id.input_6));

        bullet = findViewById(R.id.bullet);


        // Set strings
        textTitle = findViewById(R.id.text_title);
        textShow = findViewById(R.id.text_show);
        textError = findViewById(R.id.text_error);
        textTitle.setText(ActivityStrings.get(Strings.Title));
        textShow.setText(ActivityStrings.get(Strings.ShowPin));

        // Set colors
        container = findViewById(R.id.container);
        container.setBackgroundColor(Color.parseColor(Cotter.colors.ColorBackground));
        textShow.setTextColor(Color.parseColor(Cotter.colors.ColorPrimary));
        textError.setTextColor(Color.parseColor(Cotter.colors.ColorDanger));

        // Set up and show toolbar
        setupToolBar();

        Intent intent = getIntent();
        event = intent.getExtras().getString("event");


        // Check if user's biometric is enrolled and available
        final PinVerificationActivity self = this;
        new CotterMethodHelper(this).biometricEnrolled(new CotterMethodChecker() {
            @Override
            public void onCheck(boolean biometricEnrolled) {
                biometricAvailable = BiometricHelper.checkBiometricAvailable(self);

                Log.d("biometricAvailable", String.valueOf(biometricAvailable));
                if (biometricEnrolled && biometricAvailable) {
                    // Setup Biometric Handlers for onAuthSuccess, or onAuthFail, etc.
                    BiometricHelper.setupVerifyBiometricHandler(self, self, self, self);

                    // Create Biometric Prompt
                    promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(ActivityStrings.get(Strings.BiometricTitle))
                            .setSubtitle(ActivityStrings.get(Strings.BiometricSubtitle))
                            .setNegativeButtonText(ActivityStrings.get(Strings.BiometricNegativeButton))
                            .build();

                    // Show Prompt
                    Log.d("DEFAULT AND AVAILABLE", "running prompt");
                    BiometricHelper.PromptBiometric(self);
                }
            }
        });
    }

    // Set up and show toolbar
    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Cotter.strings.Headers.get(name));

        if (toolbar == null) return;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
    }

    // Handle back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pin = "";
        wrong = 0;
        setBullet();
    }

    // ------- PIN HANDLERS ---------
    // Submit the pin to check thru Cotter server
    public void onSubmitPin() {
        // Verify Pin
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Boolean valid;
                try {
                    valid = response.getBoolean("approved");
                } catch (Exception e) {
                    valid = false;
                }
                if (valid) {
                    onContinue();
                } else {
                    invalidPin();
                }
            }
            public void onError(String error){
                Log.e("Verify Pin Error", error);
                invalidPin();
            }
        };

        Date now = new Date();
        long timestamp = now.getTime() / 1000L;
        String strTimestamp = Long.toString(timestamp);
        JSONObject req = Cotter.authRequest.ConstructApprovedEventJSON(event, strTimestamp, Cotter.PinMethod, pin, cb);
        Cotter.authRequest.CreateApprovedEventRequest(this, req, cb);
    }

    // Continue to next screen
    public void onContinue() {
        Class nextScreen = Cotter.PinVerification.nextStep(name);
        Intent in = new Intent(this, nextScreen);
        startActivity(in);
        finish();
    }

    // SETTER AND GETTER FOR PIN INTERFACE
    // Set this.pin
    public void setPin(String updatedPin) {
        pin = updatedPin;
    }
    // Get this.pin
    public String getPin() {
        return pin;
    }
    // Set this.pinError
    public void setPinError(boolean pinErr) {
        pinError = pinErr;
    }
    // Get this.pinError
    public boolean getPinError() {
        return pinError;
    }
    // Set this.showPin
    public void setShowPin(boolean show) {
        showPin = show;
    }
    // Get this.showPin
    public boolean getShowPin() {
        return showPin;
    }

    public void invalidPin() {
        wrong = wrong + 1;
        if (wrong >= 3) {
            finish();
            return;
        }

        String errorString = ActivityStrings.get(Strings.ErrorInvalid);
        PinHelper.shakePin(bullet, pins, errorString, textShow, textError, this, this);
    }

    // Set the bullets to the correct color based on entered pin length,
    // and decide to show bullets or numbers based on showPin
    public void setBullet() {
        PinHelper.setBullet(pinError, showPin, pin, pins, this);
    }

    // Called when keyboard is pressed
    public void onPressKey( View v ) {
        PinHelper.onPressKey(v, this);
    }

    // called when delete key is pressed
    public void onDeleteKey(View v) {
        PinHelper.onDeleteKey(textError, textShow, this);
    }

    // Toggle showing pin or not
    public void onToggleShowPin(View v) {
        setShowPinText(!showPin);
        setBullet();
    }

    // Set showPin to a certain value
    public void setShowPinText(boolean show) {
        PinHelper.setShowPinText(show, textShow, ActivityStrings.get(Strings.ShowPin), ActivityStrings.get(Strings.HidePin), this);
    }

    // ------- BIOMETRIC HANDLERS ---------
    // SETTER AND GETTER FOR BIOMETRIC INTERFACE
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

    public String getStringToSign() {
        Date now = new Date();
        long timestampLong = now.getTime() / 1000L;
        timestamp = Long.toString(timestampLong);

        return Cotter.authRequest.ConstructApprovedEventMsg(event, timestamp, Cotter.BiometricMethod);
    }


    // Submit the signature from biometric
    public void onSubmitBio(String signature) {
        // Verify Pin
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Boolean valid;
                try {
                    valid = response.getBoolean("approved");
                    Log.d("COTTER_PIN_VERIFICATION", "onSubmitBio > onSuccess > Response Success Signature: " + response.toString());
                } catch (Exception e) {
                    valid = false;
                    Log.d("COTTER_PIN_VERIFICATION", "onSubmitBio > onSuccess > Response ERROR Signature: " + response.toString());
                }
                if (valid) {
                    Log.d("COTTER_PIN_VERIFICATION", "onSubmitBio Valid Success Signature: " + response.toString());
                    onContinue();
                } else {
                    invalidSignature();
                }
            }
            public void onError(String error){
                Log.d("COTTER_PIN_VERIFICATION", "onSubmitBio > onError: " + error);
                Log.e("Verify Signature Error", error);
                invalidSignature();
            }
        };

        JSONObject req = Cotter.authRequest.ConstructApprovedEventJSON(event, timestamp, Cotter.BiometricMethod, signature, cb);
        Cotter.authRequest.CreateApprovedEventRequest(this, req, cb);
    }

    public void invalidSignature() {
        final BiometricInterface bi = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                .setTitle(ActivityStrings.get(Strings.DialogTitle))
                .setMessage(ActivityStrings.get(Strings.DialogSubtitle))
                .setPositiveButton(ActivityStrings.get(Strings.DialogPositiveButton), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Input Pin instead
                        dialog.cancel(); // dismiss the alert dialog
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
}
