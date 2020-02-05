package com.cotter.app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PinEnrollmentReEnterPinActivity extends AppCompatActivity implements PinInterface {
    public static String name = ScreenNames.PinEnrollmentReEnterPin;
    private static String pin;
    private static String originalPin;
    private static int wrong;
    private List<TextView> pins = new ArrayList<TextView>();
    private LinearLayout bullet;

    private TextView textTitle;
    private TextView textShow;
    private TextView textError;
    private ConstraintLayout container;

    private boolean pinError = false;
    private boolean showPin = false;

    public Map<String, String> ActivityStrings;

    private boolean changePin = false;
    private String currentPin = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_enrollment_re_enter_pin);
        pin = "";
        wrong = 0;

        ActivityStrings = Cotter.strings.PinEnrollmentReEnterPin;

        // Checking if this was called from Change Pin or Enroll Pin
        Intent intent = getIntent();
        changePin = intent.getExtras().getBoolean("change_pin");
        currentPin = intent.getExtras().getString("current_pin");
        if (changePin) {
            name = ScreenNames.PinChangeReEnterPin;
            ActivityStrings = Cotter.strings.PinChangeReEnterPin;
        }

        // set pins objects
        pins.add((TextView)findViewById(R.id.input_1));
        pins.add((TextView)findViewById(R.id.input_2));
        pins.add((TextView)findViewById(R.id.input_3));
        pins.add((TextView)findViewById(R.id.input_4));
        pins.add((TextView)findViewById(R.id.input_5));
        pins.add((TextView)findViewById(R.id.input_6));

        // set bullet obj and original pin from PinEnrollmentEnterPinActivity
        bullet = findViewById(R.id.bullet);
        originalPin = intent.getExtras().getString("pin");

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

    // What to do when user "backed" to this page
    @Override
    protected void onResume() {
        super.onResume();
        pin = "";
        wrong = 0;
        setBullet();
    }

    // when pin reaches 6 digits, onSubmitPin will be invoked
    public void onSubmitPin() {

        // Check if pin is valid
        if (!originalPin.equals(pin)) {
            invalidPin();
            return;
        }

        // Enroll Pin
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.i("Submit Pin Success", response.toString());
                onContinue();
            }
            public void onError(String error){
                Log.e("Submit Pin Error", error);
            }
        };

        if (changePin) {
            Cotter.authRequest.ChangeMethod(this, Cotter.PinMethod, pin, currentPin, cb);
        } else {
            Cotter.authRequest.EnrollMethod(this, Cotter.PinMethod, pin, cb);
        }
    }

    // When Enroll Pin inside onSubmitPin succeed, this will be invoked, going to the next page
    public void onContinue() {
        PinEnrollmentEnterPinActivity.instance.finish();
        Class nextScreen = Cotter.PinEnrollment.nextStep(name);
        if (changePin) {
            nextScreen = Cotter.PinChange.nextStep(name);
        }
        Intent in = new Intent(this, nextScreen);
        if (changePin) {
            in.putExtra("change_pin", true);
        } else {
            in.putExtra("change_pin", false);
        }
        startActivity(in);
        finish();
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


    // Invoked when pin is invalid
    public void invalidPin() {
        // Allow 3 times wrong pin before closes the page and back to beginning
        wrong = wrong + 1;
        if (wrong >= 3) {
            finish();
            return;
        }

        String errorString = ActivityStrings.get(Strings.ErrorNoMatch);
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

}