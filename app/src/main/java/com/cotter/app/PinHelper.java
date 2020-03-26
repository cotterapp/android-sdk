package com.cotter.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class PinHelper {

    // Set the bullets to the correct color based on entered pin length,
    // and decide to show bullets or numbers based on showPin
    public static void setBullet(boolean pinError, boolean showPin, String pin, List<TextView> pins, Activity activity) {
        String color = Cotter.colors.ColorAccent;

        // Set color to red if pin is in error condition
        if (pinError) color = Cotter.colors.ColorDanger;

        if (showPin) {
            // if showPin, then show the numbers
            char[] pinChar = pin.toCharArray();

            for(int i=0; i<pins.size(); i++) {
                TextView currPin = pins.get(i);
                if (pin.length() > i) {
                    currPin.setText(String.valueOf(pinChar[i]));
                    currPin.setTextColor(Color.parseColor(color));
                } else {
                    // unset pin positions will stay as bullets
                    currPin.setText("\u25CF");
                    currPin.setTextColor(activity.getResources().getColor(R.color.colorLightGrey));
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
                    currPin.setTextColor(activity.getResources().getColor(R.color.colorLightGrey));
                }

            }
        }
    }

    // Called when keyboard is pressed
    public static void onPressKey( View v , PinInterface pinInterface) {
        Button b = (Button)v;
        String t = b.getText().toString();
        String pin = pinInterface.getPin();
        if (pin.length() > 5) {
            pin = "";
            pinInterface.onDeleteKey(v);
        }
        pin = pin + t;
        pinInterface.setPin(pin);
        pinInterface.setBullet();
        if (pin.length() > 5) {
            pinInterface.onSubmitPin();
        }
    }

    // Called when keyboard is pressed
    public static void onPressKey( View v , PinInterface pinInterface, int maxLength) {
        Button b = (Button)v;
        String t = b.getText().toString();
        String pin = pinInterface.getPin();
        if (pin.length() > maxLength-1) {
            pin = "";
            pinInterface.onDeleteKey(v);
        }
        pin = pin + t;
        pinInterface.setPin(pin);
        pinInterface.setBullet();
        if (pin.length() > maxLength-1) {
            pinInterface.onSubmitPin();
        }
    }


    // called when delete key is pressed
    public static void onDeleteKey(TextView textError, TextView textShow, PinInterface pinInterface) {
        String pin = pinInterface.getPin();
        boolean pinError = pinInterface.getPinError();

        if (pin.length() > 0) {
            pin = pin.substring(0, pin.length()-1);
            pinInterface.setPin(pin);
        }
        // if pin is in error condition, reset pin to ""
        // ALso hide textError and show "Show Pin"
        if (pinError) {
            pinInterface.setPin("");
            pinInterface.setPinError(false);
            textError.setText("");
            textShow.setVisibility(View.VISIBLE);
            textError.setVisibility(View.GONE);
            pinInterface.setShowPinText(false);
        }
        pinInterface.setBullet();
    }

    // check if PIN is weak
    public static boolean pinIsWeak(String pin) {
        char[] pinChar = pin.toCharArray();
        // Check repeating digits
        int count = 0;
        for (int i = 0; i < pin.length()-1; i++) {
            if (pinChar[i] == pinChar[i+1]) {
                count = count + 1;
            }
        }
        if (count >= 5) {
            return true;
        }
        // Check increasing digits
        count = 0;
        for (int i = 0; i < pin.length()-1; i++) {
            if (pinChar[i] == pinChar[i+1]+1) {
                count = count + 1;
            }
        }
        if (count >= 5) {
            return true;
        }
        // Check decreasing digits
        count = 0;
        for (int i = 0; i < pin.length()-1; i++) {
            if (pinChar[i] == pinChar[i+1]-1) {
                count = count + 1;
            }
        }
        if (count >= 5) {
            return true;
        }
        return false;
    }


    // if PIN is invalid
    public static void shakePin(LinearLayout bullet, List<TextView> pins, String errorString, TextView textShow, TextView textError, PinInterface pinInterface, Activity activity) {
        // Shake the bullet container and the phone
        bullet.startAnimation(shakeError());
        Vibrator v = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(400);

        // set pin color to red
        for(int i=0; i<pins.size(); i++) {
            TextView currPin = pins.get(i);
            currPin.setTextColor(Color.parseColor(Cotter.colors.ColorDanger));
        }

        // hide textShow and show error text
        pinInterface.setPinError(true);
        if (textShow != null) {
            textShow.setVisibility(View.GONE);
        }
        textError.setVisibility(View.VISIBLE);
        textError.setText(errorString);
    }

    // set Show Pin to a certain value
    public static void setShowPinText(boolean show, TextView textShow, String showPinText, String hidePinText, PinInterface pinInterface) {
        if (!show) {
            textShow.setText(showPinText);
        } else {
            textShow.setText(hidePinText);
        }
        pinInterface.setShowPin(show);
        pinInterface.setBullet();
    }

    //    HELPER FUNCTIONS
    // Help shake a container
    public static TranslateAnimation shakeError() {
        TranslateAnimation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(300);
        shake.setInterpolator(new CycleInterpolator(3));
        return shake;
    }

}
