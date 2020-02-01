package com.cotter.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class PinEnrollmentEnterPinActivity extends AppCompatActivity {
    public static String name = ScreenNames.PinEnrollmentEnterPin;
    private static String pin;
    private List<TextView> pins = new ArrayList<TextView>();

    private static boolean pinError = false;
    private static boolean showPin = false;

    private TextView textTitle;
    private TextView textShow;
    private TextView textError;
    private LinearLayout bullet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_enrollment_enter_pin);
        pin = "";
        // set pins objects
        pins.add((TextView)findViewById(R.id.input_1));
        pins.add((TextView)findViewById(R.id.input_2));
        pins.add((TextView)findViewById(R.id.input_3));
        pins.add((TextView)findViewById(R.id.input_4));
        pins.add((TextView)findViewById(R.id.input_5));
        pins.add((TextView)findViewById(R.id.input_6));

        // set bullet obj
        bullet = findViewById(R.id.bullet);

        // get views for texts
        textTitle = findViewById(R.id.text_title);
        textShow = findViewById(R.id.text_show);
        textError = findViewById(R.id.text_error);

        // Set strings
        textTitle.setText(CoreLibrary.strings.PinEnrollmentEnterPin.get(Strings.Title));
        textShow.setText(CoreLibrary.strings.PinEnrollmentEnterPin.get(Strings.ShowPin));
        textShow.setTextColor(Color.parseColor(CoreLibrary.colors.ColorPrimary));
        textError.setTextColor(Color.parseColor(CoreLibrary.colors.ColorDanger));

        // Set up and show toolbar
        setupToolBar();
    }

    // Set up and show toolbar
    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(CoreLibrary.strings.Headers.get(name));
        toolbar.setTitle(CoreLibrary.strings.Headers.get(name));

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

    // What to do when user "backed" to this page
    @Override
    protected void onResume() {
        super.onResume();
        pin = "";
        setBullet();
    }


    // when pin reaches 6 digits, onSubmit will be invoked
    public void onContinue() {
        // If PIN is weak, don't let it pass
        if (pinIsWeak()) {
            invalidPin();
            return;
        }
        // Otherwise continue to next screen
        Class nextScreen = CoreLibrary.PinEnrollment.nextStep(name);
        Intent in = new Intent(this, nextScreen);
        in.putExtra("pin", pin);
        startActivity(in);
        finish();
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
            onContinue();
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

    // check if PIN is weak
    public boolean pinIsWeak() {
        char[] pinchar = pin.toCharArray();
        // Check repeating digits
        int count = 0;
        for (int i = 0; i < pin.length()-1; i++) {
            if (pinchar[i] == pinchar[i+1]) {
                count = count + 1;
            }
        }
        if (count >= 5) {
            return true;
        }
        // Check increasing digits
        count = 0;
        for (int i = 0; i < pin.length()-1; i++) {
            if (pinchar[i] == pinchar[i+1]+1) {
                count = count + 1;
            }
        }
        if (count >= 5) {
            return true;
        }
        // Check decreasing digits
        count = 0;
        for (int i = 0; i < pin.length()-1; i++) {
            if (pinchar[i] == pinchar[i+1]-1) {
                count = count + 1;
            }
        }
        if (count >= 5) {
            return true;
        }
        return false;
    }

    // if PIN is weak, this is invoked
    public void invalidPin() {
        // Shake the bullet container and the phone
        bullet.startAnimation(shakeError());
        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);

        // set pin color to red
        for(int i=0; i<pins.size(); i++) {
            TextView currPin = pins.get(i);
            currPin.setTextColor(Color.parseColor(CoreLibrary.colors.ColorDanger));
        }

        // hide textShow and show error text
        pinError = true;
        textShow.setVisibility(View.GONE);
        textError.setVisibility(View.VISIBLE);
        textError.setText(CoreLibrary.strings.PinEnrollmentEnterPin.get(Strings.ErrorCombination));
    }

    // toggle Show Pin
    public void onToggleShowPin(View v) {
        if (!showPin) {
            textShow.setText(CoreLibrary.strings.PinEnrollmentEnterPin.get(Strings.HidePin));
        } else {
            textShow.setText(CoreLibrary.strings.PinEnrollmentEnterPin.get(Strings.ShowPin));
        }
        showPin = !showPin;
        setBullet();
    }

    // set Show Pin to a certain value
    public void setShowPin(boolean show) {
        if (!show) {
            textShow.setText(CoreLibrary.strings.PinEnrollmentEnterPin.get(Strings.ShowPin));
        } else {
            textShow.setText(CoreLibrary.strings.PinEnrollmentEnterPin.get(Strings.HidePin));
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
