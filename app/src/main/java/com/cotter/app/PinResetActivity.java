package com.cotter.app;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PinResetActivity extends AppCompatActivity implements PinInterface {
    public static String name = ScreenNames.PinReset;
    private String pin;
    private List<TextView> pins = new ArrayList<TextView>();

    private boolean pinError = false;
    private boolean showPin = false;

    private TextView textTitle;
    private TextView textSubtitle;
    private TextView textSubtitleTo;
    private TextView textResend;
    private TextView textError;
    private ConstraintLayout container;
    private LinearLayout bullet;
    private FrameLayout loadingOverlay;


    private int challengeID;
    private String challenge;
    private String sendingMethod;
    private String sendingDestination;

    public Map<String, String> ActivityStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_reset);

        ActivityStrings = Cotter.strings.PinReset;
        Cotter.PinReset.addActivityStack(this);


        // User Information for sending reset pin
        Intent intent = getIntent();
        challenge = intent.getExtras().getString("challenge");
        challengeID = intent.getExtras().getInt("challenge_id");
        sendingMethod = intent.getExtras().getString("sending_method");
        sendingDestination = intent.getExtras().getString("sending_destination");


        pin = "";
        // set pins objects
        pins.add((TextView)findViewById(R.id.input_1));
        pins.add((TextView)findViewById(R.id.input_2));
        pins.add((TextView)findViewById(R.id.input_3));
        pins.add((TextView)findViewById(R.id.input_4));

        // set bullet obj
        bullet = findViewById(R.id.bullet);

        // set loading overlay
        loadingOverlay = findViewById(R.id.loading_overlay);

        // Set strings
        textTitle = findViewById(R.id.text_title);
        textSubtitle = findViewById(R.id.text_subtitle);
        textSubtitleTo = findViewById(R.id.text_subtitle_to);
        textResend = findViewById(R.id.text_resend);
        textError = findViewById(R.id.text_error);
        textTitle.setText(ActivityStrings.get(Strings.Title));
        textSubtitle.setText(ActivityStrings.get(Strings.Subtitle));
        textResend.setText(ActivityStrings.get(Strings.ResendCode));

        String sendingDestinationStars;
        if (sendingMethod.equals(Cotter.EmailSendingMethod)) {
            String[] arrOfDest = sendingDestination.split("@", 2);
            String emailFront = replaceEmailWithStars(arrOfDest[0]);
            sendingDestinationStars = emailFront + "@" + arrOfDest[1];
        } else {
            sendingDestinationStars = replacePhoneWithStars(sendingDestination);
        }
        textSubtitleTo.setText(sendingDestinationStars);


        // Set colors
        container = findViewById(R.id.container);
        container.setBackgroundColor(Color.parseColor(Cotter.colors.ColorBackground));

        // Set up and show toolbar
        setupToolBar();
    }

    // Set up and show toolbar
    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Cotter.strings.Headers.get(name));

        if (toolbar == null) return;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
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


    // SETTER AND GETTER FOR CLASS ATTRIBUTES
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


    // Set the bullets to the correct color based on entered pin length,
    // and decide to show bullets or numbers based on showPin
    public void setBullet() {
        String color = Cotter.colors.ColorBlack;
        String bgColor = Cotter.colors.ColorPrimaryLight;

        // Set color to red if pin is in error condition
        if (pinError) {
            color = Cotter.colors.ColorDanger;
            bgColor = Cotter.colors.ColorDangerLight;
        }

        // if showPin, then show the numbers
        char[] pinChar = pin.toCharArray();

        for(int i=0; i<pins.size(); i++) {
            TextView currPin = pins.get(i);
            if (pin.length() > i) {
                currPin.setText(String.valueOf(pinChar[i]));
                currPin.setTextColor(Color.parseColor(color));

                Drawable dr = getResources().getDrawable(R.drawable.background_pin_input);
                dr.setColorFilter(Color.parseColor(bgColor), PorterDuff.Mode.SRC_ATOP);
                currPin.setBackground(dr);
                currPin.setPadding(15, 15, 15, 15);
            } else {
                // unset pin positions will stay as bullets
                currPin.setText("");
                currPin.setTextColor(getResources().getColor(R.color.colorLightGrey));

                Drawable dr = getResources().getDrawable(R.drawable.background_pin_input);
                dr.setColorFilter(Color.parseColor(Cotter.colors.ColorSuperLightGrey), PorterDuff.Mode.SRC_ATOP);
                currPin.setBackground(dr);
                currPin.setPadding(15, 15, 15, 15);
            }
        }

    }


    // Called when keyboard is pressed
    public void onPressKey( View v ) {
        PinHelper.onPressKey(v, this, 4);
    }

    // called when delete key is pressed
    public void onDeleteKey(View v) {
        if (pin.length() > 0) {
            pin = pin.substring(0, pin.length()-1);
        }
        // if pin is in error condition, reset pin to ""
        if (pinError) {
            pin = "";
            pinError = false;
            textError.setVisibility(View.GONE);
        }
        setBullet();
    }

    // ------- PIN HANDLERS ---------
    // Submit the pin to check thru Cotter server
    public void onSubmitPin() {
        setLoading(true);
        // Verify Pin
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                boolean success;
                try {
                    success = response.getBoolean("success");
                } catch (Exception e) {
                    success = false;
                }
                if (success) {
                    onContinue();
                } else {
                    setLoading(false);
                    invalidPin();
                }
            }
            public void onError(String error){
                setLoading(false);
                Log.e("Verify Pin Error", error);
                errorOther();
            }
        };

       Cotter.authRequest.ResetVerify(this, Cotter.PinMethod, pin, challengeID, challenge, cb);
    }

    // onContinue goes to the next page, called by onSubmit
    public void onContinue() {
        Class nextScreen = Cotter.PinReset.nextStep(name);
        Intent nextIntent = new Intent(this, nextScreen);
        nextIntent.putExtra("challenge", challenge);
        nextIntent.putExtra("challenge_id", challengeID);
        nextIntent.putExtra("reset_code", pin);
        nextIntent.putExtra("reset_pin", true);
        startActivity(nextIntent);
        finish();
    };


    // if reset code is wrong, this method is invoked
    public void invalidPin() {
        String errorString = ActivityStrings.get(Strings.ErrorInvalid);
        PinHelper.shakePin(bullet, pins, errorString, null, textError, this, this);
        this.setBullet();
    }
    // Invoked when server error
    public void errorOther() {
        String errorString = ActivityStrings.get(Strings.ErrorOther);
        PinHelper.shakePin(bullet, pins, errorString, null, textError, this, this);
    }
    // toggle Show Pin
    public void onToggleShowPin(View v) {};

    // set Show Pin to a certain value
    public void setShowPinText(boolean show) {};

    // add loading overlay
    public void setLoading(boolean loading) {
        if (loading) {
            loadingOverlay.setVisibility(View.VISIBLE);
        } else {
            loadingOverlay.setVisibility(View.GONE);
        }
    }

    public void error() {
        String errorString = ActivityStrings.get(Strings.ErrorOther);
        PinHelper.shakePin(bullet, pins, errorString, null, textError, this, this);
    }

    // Resend Verification Code
    public void onResendCode(View view) {
        setLoading(true);
        onDeleteKey(view);
        User user = Cotter.getUser();
        String name = user.name;
        String sendingMethod = user.sendingMethod;
        String sendingDestination = user.sendingDestination;
        // Verify Pin
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                try {
                    challenge = response.getString("challenge");
                    challengeID = response.getInt("challenge_id");
                    setLoading(false);
                } catch (Exception e) {
                    setLoading(false);
                    error();
                    Log.e("COTTER RESET PIN", "Error parsing response from pin reset resend code");
                }
            }
            public void onError(String error){
                setLoading(false);
                Log.e("COTTER RESET PIN", error);
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

    // replace middle of email number with stars
    public String replaceEmailWithStars(String email) {
        // convert the given string to character array
        char[] chars = email.toCharArray();
        for (int i=0; i < email.length(); i++) {
            if (i > 1 && i < email.length()-2) {
                chars[i] = '*';
            }
        }
        return String.valueOf(chars);
    }

    // replace middle of phone number with stars
    public String replacePhoneWithStars(String phone) {
        // convert the given string to character array
        char[] chars = phone.toCharArray();
        for (int i=0; i < phone.length(); i++) {
            if (i < phone.length()-2) {
                chars[i] = '*';
            }
        }
        return String.valueOf(chars);
    }
}
