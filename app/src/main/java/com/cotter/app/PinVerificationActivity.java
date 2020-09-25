package com.cotter.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
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
    private TextView textForgot;
    private TextView textError;
    private ConstraintLayout container;
    private LinearLayout bullet;
    private FrameLayout loadingOverlay;

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
        Cotter.PinVerification.addActivityStack(this);

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

        // set loading overlay
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Set strings
        textTitle = findViewById(R.id.text_title);
        textForgot = findViewById(R.id.text_forgot);
        textError = findViewById(R.id.text_error);
        textTitle.setText(ActivityStrings.get(Strings.Title));
        textForgot.setText(ActivityStrings.get(Strings.ForgotPin));

        // Set colors
        container = findViewById(R.id.container);
        container.setBackgroundColor(Color.parseColor(Cotter.colors.ColorBackground));
        textForgot.setTextColor(Color.parseColor(Cotter.colors.ColorPrimary));
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
        pinError = false;
        textError.setText("");
        textError.setVisibility(View.GONE);
        setShowPinText(false);
        setBullet();
    }

    // ------- PIN HANDLERS ---------
    // Submit the pin to check thru Cotter server
    public void onSubmitPin() {
        setLoading(true);
        // Verify Pin
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                boolean valid;
                try {
                    valid = response.getBoolean("approved");
                } catch (Exception e) {
                    valid = false;
                }
                if (valid) {
                    onContinue();
                    setLoading(false);
                } else {
                    setLoading(false);
                    invalidPin();
                }
            }
            public void onError(String error){
                Log.e("Verify Pin Error", error);
                setLoading(false);
                errorOther();
            }
        };

        Date now = new Date();
        long timestamp = now.getTime() / 1000L;
        String strTimestamp = Long.toString(timestamp);
        JSONObject req = Cotter.authRequest.ConstructApprovedEventJSON(event, strTimestamp, Cotter.PinMethod, pin, "", cb);
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
        String errorString = ActivityStrings.get(Strings.ErrorInvalid);
        PinHelper.shakePin(bullet, pins, errorString, null, textError, this, this);
    }
    // Invoked when server error
    public void errorOther() {
        String errorString = ActivityStrings.get(Strings.ErrorOther);
        PinHelper.shakePin(bullet, pins, errorString, null, textError, this, this);
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
        PinHelper.onDeleteKey(textError, textForgot, this);
    }

    // Toggle showing pin or not
    public void onToggleShowPin(View v) {
        setShowPinText(!showPin);
        setBullet();
    }

    // Set showPin to a certain value
    public void setShowPinText(boolean show) {
        PinHelper.setShowPinText(show, textForgot, ActivityStrings.get(Strings.ForgotPin), ActivityStrings.get(Strings.ForgotPin), this);
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
        setLoading(true);
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
                    setLoading(false);
                } else {
                    setLoading(false);
                    invalidSignature();
                }
            }
            public void onError(String error){
                Log.d("COTTER_PIN_VERIFICATION", "onSubmitBio > onError: " + error);
                Log.e("Verify Signature Error", error);
                setLoading(false);
                invalidSignature();
            }
        };


        JSONObject req = Cotter.authRequest.ConstructApprovedEventJSON(event, timestamp, Cotter.BiometricMethod, signature, BiometricHelper.getPublicKey(), cb);
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


    // ------- FORGOT PIN ---------

    public void error() {
        String errorString = ActivityStrings.get(Strings.ErrorOther);
        PinHelper.shakePin(bullet, pins, errorString, null, textError, this, this);
    }

    // Send reset code and go to ResetPin flow
    public void onForgotPin(View view) {
        setLoading(true);
        User user = Cotter.getUser();
        String name = user.name;
        String sendingMethod = user.sendingMethod;
        String sendingDestination = user.sendingDestination;
        // Verify Pin
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Class callBack = Cotter.PinVerification.goToCallback();
                Intent nextIntent = Cotter.PinReset.startFlowWithIntent(PinVerificationActivity.this, callBack, "PIN RESET");
                try {
                    nextIntent.putExtra("challenge", response.getString("challenge"));
                    nextIntent.putExtra("challenge_id", response.getInt("challenge_id"));
                    nextIntent.putExtra("sending_method", sendingMethod);
                    nextIntent.putExtra("sending_destination", sendingDestination);
                    startActivity(nextIntent);
                    setLoading(false);
                } catch (Exception e) {
                    setLoading(false);
                    error();
                    Log.e("COTTER RESET PIN", "Error parsing response from pin reset start");
                }
            }
            public void onError(String error){
                setLoading(false);
                Log.e("Verify Pin Error", error);
                error();
            }
        };


        if (name != null && sendingMethod != null && sendingDestination != null) {
            Cotter.authRequest.ResetStart(this, Cotter.PinMethod, sendingMethod, sendingDestination, name, cb);
        } else {
            setLoading(false);
            error();
            Log.e("COTTER RESET PIN", "Please set user's name, sending method and destination. Use `Cotter.getUser().setUserInformation()`" + name + sendingDestination + sendingMethod);
        }
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
