package com.cotter.app;

import android.view.View;

public interface PinInterface {

    // GETTER AND SETTER
    public void setPin(String pin);
    public String getPin();
    public void setPinError(boolean pinErr);
    public boolean getPinError();
    public void setShowPin(boolean show);
    public boolean getShowPin();

    // when pin reaches 6 digits, onSubmit will be invoked
    public void onSubmitPin();
    // onContinue goes to the next page, called by onSubmit
    public void onContinue();

    // Set the bullets to the correct color based on entered pin length,
    // and decide to show bullets or numbers based on showPin
    public void setBullet();

    // Called when keyboard is pressed
    public void onPressKey( View v );

    // called when delete key is pressed
    public void onDeleteKey(View v);

    // if PIN is weak, this is invoked
    public void invalidPin();

    // toggle Show Pin
    public void onToggleShowPin(View v);

    // set Show Pin to a certain value
    public void setShowPinText(boolean show);
}
