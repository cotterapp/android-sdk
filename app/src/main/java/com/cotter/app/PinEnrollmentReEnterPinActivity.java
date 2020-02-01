package com.cotter.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PinEnrollmentReEnterPinActivity extends AppCompatActivity {
    public static String name = ScreenNames.PinEnrollmentReEnterPin;
    private static String pin;
    private static String originalPin;
    private static int wrong;
    private List<TextView> pins = new ArrayList<TextView>();
    private LinearLayout bullet;

    private TextView textTitle;
    private TextView textShow;
    private TextView textError;

    private boolean pinError = false;
    private boolean showPin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_enrollment_re_enter_pin);
        pin = "";
        wrong = 0;
        pin = "";

        // set pins objects
        pins.add((TextView)findViewById(R.id.input_1));
        pins.add((TextView)findViewById(R.id.input_2));
        pins.add((TextView)findViewById(R.id.input_3));
        pins.add((TextView)findViewById(R.id.input_4));
        pins.add((TextView)findViewById(R.id.input_5));
        pins.add((TextView)findViewById(R.id.input_6));

        // set bullet obj and original pin from PinEnrollmentEnterPinActivity
        bullet = findViewById(R.id.bullet);
        Intent intent = getIntent();
        originalPin = intent.getExtras().getString("pin");

        // get views for texts
        textTitle = findViewById(R.id.text_title);
        textShow = findViewById(R.id.text_show);
        textError = findViewById(R.id.text_error);

        // Set strings
        textTitle.setText(CoreLibrary.strings.PinEnrollmentReEnterPin.get(Strings.Title));
        textShow.setText(CoreLibrary.strings.PinEnrollmentReEnterPin.get(Strings.ShowPin));
        textShow.setTextColor(Color.parseColor(CoreLibrary.colors.ColorPrimary));
        textError.setTextColor(Color.parseColor(CoreLibrary.colors.ColorDanger));

        // Set up and show toolbar
        setupToolBar();
    }

    // Set up and show toolbar
    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(CoreLibrary.strings.Headers.get(name));

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

    // when pin reaches 6 digits, onSubmit will be invoked
    public void onSubmit() {

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

        AuthRequest.EnrollMethod(this, CoreLibrary.PinMethod, pin, cb);
    }

    // When Enroll Pin inside onSubmit succeed, this will be invoked, going to the next page
    public void onContinue() {
        Class nextScreen = CoreLibrary.PinEnrollment.nextStep(name);
        Intent in = new Intent(this, nextScreen);
        startActivity(in);
        finish();
    }

    // Invoked when pin is invalid
    public void invalidPin() {
        // Allow 3 times wrong pin before closes the page and back to beginning
        wrong = wrong + 1;
        if (wrong >= 3) {
            finish();
            return;
        }

        // Shake the bullet container
        bullet.startAnimation(shakeError());

        // Shake the phone
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);

        // set the pins to red
        for(int i=0; i<pins.size(); i++) {
            TextView currPin = pins.get(i);
            currPin.setTextColor(Color.parseColor(CoreLibrary.colors.ColorDanger));
        }

        // set hide the "Show Pin" textView and show the error text view
        pinError = true;
        textShow.setVisibility(View.GONE);
        textError.setVisibility(View.VISIBLE);
        textError.setText(CoreLibrary.strings.PinEnrollmentReEnterPin.get(Strings.ErrorNoMatch));
    }

    // Set the bullets to the correct color based on entered pin length,
    // and decide to show bullets or numbers based on showPin
    public void setBullet() {
        String color = CoreLibrary.colors.ColorAccent;

        // Set color to red if pin is in error condition
        if (pinError) color = CoreLibrary.colors.ColorDanger;

        if (showPin) {
            // if showPin, then show the numbers
            char[] pinchar = pin.toCharArray();

            for(int i=0; i<pins.size(); i++) {
                TextView currPin = pins.get(i);
                if (pin.length() > i) {
                    currPin.setText(String.valueOf(pinchar[i]));
                    currPin.setTextColor(Color.parseColor(color));
                } else {
                    // unset pin positions will stay as bullets
                    currPin.setText("\u25CF");
                    currPin.setTextColor(getResources().getColor(R.color.colorLightGrey));
                }
            }

        } else {
            // if showPin is false, then show as bullets
            for(int i=0; i<pins.size(); i++) {
                TextView currPin = pins.get(i);
                currPin.setText("\u25CF");
                if (pin.length() > i) {
                    currPin.setTextColor(Color.parseColor(color));
                } else {
                    currPin.setTextColor(getResources().getColor(R.color.colorLightGrey));
                }

            }
        }
    }

    // Called when keyboard is pressed
    public void onPressKey( View v ) {
        Button b = (Button)v;
        String t = b.getText().toString();
        pin = pin + t;
        setBullet();
        if (pin.length() > 5) {
            onSubmit();
        }
    }

    // called when delete key is pressed
    public void onDeleteKey(View v) {
        if (pin.length() > 0) {
            pin = pin.substring(0, pin.length()-1);
        }

        // if pin is in error condition, reset pin to ""
        // ALso hide textError and show "Show Pin"
        if (pinError) {
            pin = "";
            pinError = false;
            if (pin.length() < 6) {
                textError.setText("");
            }
            textShow.setVisibility(View.VISIBLE);
            textError.setVisibility(View.GONE);
            setShowPin(false);
        }
        setBullet();
    }

    // Toggle showing pin or not
    public void onToggleShowPin(View v) {
        if (!showPin) {
            textShow.setText(CoreLibrary.strings.PinEnrollmentReEnterPin.get(Strings.HidePin));
        } else {
            textShow.setText(CoreLibrary.strings.PinEnrollmentReEnterPin.get(Strings.ShowPin));
        }
        showPin = !showPin;
        setBullet();
    }

    // Set showPin to a certain value
    public void setShowPin(boolean show) {
        if (!show) {
            textShow.setText(CoreLibrary.strings.PinEnrollmentReEnterPin.get(Strings.ShowPin));
        } else {
            textShow.setText(CoreLibrary.strings.PinEnrollmentReEnterPin.get(Strings.HidePin));
        }
        showPin = show;
        setBullet();
    }



    //    HELPER FUNCTIONS
    // Help shake a container
    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(300);
        shake.setInterpolator(new CycleInterpolator(3));
        return shake;
    }
}