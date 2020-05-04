package com.cotter.app;

public interface CotterBiometricCallback {
    void onSuccess(boolean biometricEnabled);
    void onCanceled();
    void onError(String error);
    void onLoading(boolean loading);
}
