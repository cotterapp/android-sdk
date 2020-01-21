package com.cotter.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PinEnrollmentEnterPinActivity extends AppCompatActivity {
    public static String name = ScreenNames.PinEnrollmentEnterPin;
    private static String pin;
    private TextView pin1;
    private TextView pin2;
    private TextView pin3;
    private TextView pin4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_enrollment_enter_pin);
        pin = "";
        pin1 = findViewById(R.id.input_1);
        pin2 = findViewById(R.id.input_2);
        pin3 = findViewById(R.id.input_3);
        pin4 = findViewById(R.id.input_4);
    }

    @Override
    protected void onResume() {
        super.onResume();
        pin = "";
        setBullet();
    }


    public void onContinue() {
        Class nextScreen = CoreLibrary.PinEnrollment.nextStep(name);
        Intent in = new Intent(this, nextScreen);
        in.putExtra("pin", pin);
        startActivity(in);
        finish();
    }

    public void setBullet() {
        if (pin.length() > 0) {
            pin1.setText("\u25CF");
        } else {
            pin1.setText("\u25CB");
        }
        if (pin.length() > 1) {
            pin2.setText("\u25CF");
        } else {
            pin2.setText("\u25CB");
        }
        if (pin.length() > 2) {
            pin3.setText("\u25CF");
        } else {
            pin3.setText("\u25CB");
        }
        if (pin.length() > 3) {
            pin4.setText("\u25CF");
        } else {
            pin4.setText("\u25CB");
        }
    }
    public void onPressKey( View v ) {
        Button b = (Button)v;
        String t = b.getText().toString();
        pin = pin + t;
        setBullet();
        if (pin.length() > 3) {
            onContinue();
        }
    }

    public void onDeleteKey(View v) {
        if (pin.length() > 0) {
            pin = pin.substring(0, pin.length()-1);
        }
        setBullet();
    }
}
