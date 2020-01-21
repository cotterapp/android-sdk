package com.cotter.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class PinEnrollmentReEnterPinActivity extends AppCompatActivity {
    public static String name = ScreenNames.PinEnrollmentReEnterPin;
    private static String pin;
    private static String originalPin;
    private static int wrong;
    private TextView pin1;
    private TextView pin2;
    private TextView pin3;
    private TextView pin4;
    private LinearLayout bullet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_enrollment_re_enter_pin);
        pin = "";
        wrong = 0;
        pin1 = findViewById(R.id.input_1);
        pin2 = findViewById(R.id.input_2);
        pin3 = findViewById(R.id.input_3);
        pin4 = findViewById(R.id.input_4);
        bullet = findViewById(R.id.bullet);
        Intent intent = getIntent();
        originalPin = intent.getExtras().getString("pin");
    }

    @Override
    protected void onResume() {
        super.onResume();
        pin = "";
        wrong = 0;
        setBullet();
    }


    public void onSubmit() {
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

    public void onContinue() {
        Class nextScreen = CoreLibrary.PinEnrollment.nextStep(name);
        Intent in = new Intent(this, nextScreen);
        startActivity(in);
        finish();
    }

    public void invalidPin() {
        wrong = wrong + 1;
        if (wrong >= 3) {
            finish();
            return;
        }
        bullet.startAnimation(shakeError());

        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);
        pin = "";
        setBullet();
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
            onSubmit();
        }
    }

    public void onDeleteKey(View v) {
        if (pin.length() > 0) {
            pin = pin.substring(0, pin.length()-1);
        }
        setBullet();
    }


    //    HELPER FUNCTIONS
    public TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(300);
        shake.setInterpolator(new CycleInterpolator(3));
        return shake;
    }
}