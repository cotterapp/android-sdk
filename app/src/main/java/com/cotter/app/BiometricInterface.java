package com.cotter.app;

import androidx.biometric.BiometricPrompt;

import java.util.concurrent.Executor;

public interface BiometricInterface {

    public void setBiometricPrompt(BiometricPrompt bp);
    public BiometricPrompt getBiometricPrompt();
    public void setExecutor(Executor ex);
    public Executor getExecutor();
    public void setPromptInfo(BiometricPrompt.PromptInfo pi);
    public BiometricPrompt.PromptInfo getPromptInfo();
    public String getStringToSign();

    // called to submit request (enroll biometric or verify event)
    // signature is optional, only for verification
    public void onSubmitBio(String signature);
}
