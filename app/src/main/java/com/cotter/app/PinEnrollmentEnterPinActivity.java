package com.cotter.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PinEnrollmentEnterPinActivity extends AppCompatActivity implements PinInterface {
    public static String name = ScreenNames.PinEnrollmentEnterPin;
    private String pin;
    private List<TextView> pins = new ArrayList<TextView>();

    private boolean pinError = false;
    private boolean showPin = false;

    private TextView textTitle;
    private TextView textShow;
    private TextView textError;
    private ConstraintLayout container;
    private LinearLayout bullet;

    public Map<String, String> ActivityStrings;

    private boolean changePin = false;
    private String currentPin = "";

    public static PinEnrollmentEnterPinActivity instance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_enrollment_enter_pin);

        name = ScreenNames.PinEnrollmentEnterPin;
        ActivityStrings = Cotter.strings.PinEnrollmentEnterPin;
        instance = this;

        // Checking if this was called from Change Pin or Enroll Pin
        Intent intent = getIntent();
        changePin = intent.getExtras().getBoolean("change_pin");
        currentPin = intent.getExtras().getString("current_pin");
        if (changePin) {
            name = ScreenNames.PinChangeEnterPin;
            ActivityStrings = Cotter.strings.PinChangeEnterPin;
        }

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
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
    }

    // Handle back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomDialogTheme)
                    .setTitle(ActivityStrings.get(Strings.DialogTitle))
                    .setMessage(ActivityStrings.get(Strings.DialogSubtitle))
                    .setPositiveButton(ActivityStrings.get(Strings.DialogPositiveButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .setNegativeButton(ActivityStrings.get(Strings.DialogNegativeButton), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Continue to callback class if PinEnrollment
                            // otherwise if PinChange, go back
                            if (!changePin) {
                                Class nextScreen = Cotter.PinEnrollment.goToCallback();
                                Intent in = new Intent(getApplicationContext(), nextScreen);
                                startActivity(in);
                            }
                            finish();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setAllCaps(false);
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


    // when pin reaches 6 digits, onSubmitPin will be invoked
    public void onSubmitPin() {
        // If PIN is weak, don't let it pass
        if (PinHelper.pinIsWeak(pin)) {
            invalidPin();
            return;
        }
        onContinue();
    }

    public void onContinue() {
        // Otherwise continue to next screen
        Class nextScreen = Cotter.PinEnrollment.nextStep(name);
        if (changePin) {
            nextScreen = Cotter.PinChange.nextStep(name);
        }
        Intent in = new Intent(this, nextScreen);
        in.putExtra("pin", pin);
        if (changePin) {
            in.putExtra("current_pin", currentPin);
            in.putExtra("change_pin", true);
        }
        startActivity(in);
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
        PinHelper.setBullet(pinError, showPin, pin, pins, this);
    }

    // Called when keyboard is pressed
    public void onPressKey( View v ) { PinHelper.onPressKey(v, this); }

    // called when delete key is pressed
    public void onDeleteKey(View v) {
        PinHelper.onDeleteKey(textError, textShow, this);
    }

    // if PIN is weak, this is invoked
    public void invalidPin() {
        String errorString = ActivityStrings.get(Strings.ErrorCombination);
        PinHelper.shakePin(bullet, pins, errorString, textShow, textError, this, this);
    }

    // toggle Show Pin
    public void onToggleShowPin(View v) {
        setShowPinText(!showPin);
        setBullet();
    }

    // set Show Pin to a certain value
    public void setShowPinText(boolean show) {
        PinHelper.setShowPinText(show, textShow, ActivityStrings.get(Strings.ShowPin), ActivityStrings.get(Strings.HidePin), this);
    }

}
