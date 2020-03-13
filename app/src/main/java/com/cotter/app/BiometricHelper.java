package com.cotter.app;

import android.app.Activity;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.concurrent.Executor;

public class BiometricHelper {
    private static String androidKeyStore = "AndroidKeyStore";
    private static String keyStoreAlias = "COTTER_USER_KEY";
    private static String signatureAlgo = "SHA256withECDSA";

    public static String getKeyStoreAlias() {
        Log.i("COTTER_BIOMETRIC", "Keystore Alias: " + keyStoreAlias + Cotter.ApiKeyID + Cotter.UserID);
        return keyStoreAlias + Cotter.ApiKeyID + Cotter.UserID;

    }

    public static boolean checkBiometricAvailable(Context context) {
        // Check that biometric is enabled
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d("COTTER_BIOMETRIC", "App can authenticate using biometrics.");
                return true;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e("COTTER_BIOMETRIC", "No biometric features available on this device.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e("COTTER_BIOMETRIC", "Biometric features are currently unavailable.");
                return false;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e("COTTER_BIOMETRIC", "The user hasn't associated " +
                        "any biometric credentials with their account.");
                return false;
        }
        return false;
    }

    // Handler on Auth Success
    public static void setupEnrollBiometricHandler(final BiometricInterface biometricInterface, final Context context, FragmentActivity fragmentActivity, final Activity activity, final CotterBiometricCallback callback) {
        // Auth 1
        Executor ex = ContextCompat.getMainExecutor(context);
        BiometricPrompt bp = new BiometricPrompt(fragmentActivity,
                ex, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                if (errorCode == 13 && callback != null) {
                    callback.onCanceled();
                }
                Log.d("COTTER_BIOMETRIC_HELPER", "onAuthenticationError: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                try {
                      biometricInterface.onSubmitBio(null);
                }    catch (Exception e) {
                    Log.d("onAuthSucceeded", e.toString());
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (callback != null) {
                    callback.onError("Biometric authentication failed: onAuthenticationFailed");
                }
                Log.d("COTTER_BIOMETRIC_HELPER", "onAuthenticationFailed");
            }
        });
        biometricInterface.setBiometricPrompt(bp);
        biometricInterface.setExecutor(ex);
    }


    // Generate KeyPair
    public static String generateKeyPair() {
        try {
            KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                    getKeyStoreAlias(),
                    KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setDigests(KeyProperties.DIGEST_SHA256,
                            KeyProperties.DIGEST_SHA512)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
                    .build();

            KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_EC, androidKeyStore);
            kpg.initialize(keyGenParameterSpec);

            KeyPair kp = kpg.generateKeyPair();
            return Base64.encodeToString(kp.getPublic().getEncoded(), Base64.DEFAULT);
        }   catch (Exception e) {
            Log.d("generateKeyPair", e.toString());
        }
        return null;
    }


    // Get Private Key
    public static PrivateKey getPrivateKey() {
        try {

            KeyStore keyStore = KeyStore.getInstance(androidKeyStore);
            // Before the keystore can be accessed, it must be loaded.
            keyStore.load(null);


            PrivateKey privateKey = (PrivateKey) keyStore.getKey(getKeyStoreAlias(), null);
            // PublicKey publicKey = keyStore.getCertificate("alias").getPublicKey();

            return privateKey;
        } catch (Exception e) {
            Log.d("getPrivateKey", e.toString());
        }
        return null;
    }

    // Get Public Key
    public static String getPublicKey() {
        try {

            KeyStore keyStore = KeyStore.getInstance(androidKeyStore);
            keyStore.load(null);

             PublicKey publicKey = keyStore.getCertificate(getKeyStoreAlias()).getPublicKey();
            return Base64.encodeToString(publicKey.getEncoded(), Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("getPublicKey", e.toString());
        }
        return null;
    }


    // Open Biometric Prompt
    public static void PromptBiometric(BiometricInterface biometricInterface) {
        try {
            BiometricPrompt biometricPrompt = biometricInterface.getBiometricPrompt();
            BiometricPrompt.PromptInfo promptInfo = biometricInterface.getPromptInfo();

            Signature s = Signature.getInstance(signatureAlgo);
            PrivateKey pk = BiometricHelper.getPrivateKey();
            s.initSign(pk);
            biometricPrompt.authenticate(promptInfo,
                    new BiometricPrompt.CryptoObject(s));
        }  catch (Exception e) {
            Log.d("PromptBiometric", e.toString());
        }
    }


    // Handler on Auth Success
    public static void setupVerifyBiometricHandler(final BiometricInterface biometricInterface, Context context, FragmentActivity fragmentActivity, final Activity activity) {
        // Auth 1
        Executor ex = ContextCompat.getMainExecutor(context);
        BiometricPrompt bp = new BiometricPrompt(fragmentActivity,
                ex, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.d("COTTER_BIOMETRIC_HELPER", "onAuthenticationError: " + errString);
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                try {
                    String strToSign = biometricInterface.getStringToSign();
                    Signature s = result.getCryptoObject().getSignature();
                    s.update(strToSign.getBytes());
                    byte[] signature = s.sign();

                    biometricInterface.onSubmitBio(Base64.encodeToString(signature, Base64.DEFAULT));

                    Log.d("Message", strToSign);
                    Log.d("Signature", Base64.encodeToString(signature, Base64.DEFAULT));
                }    catch (Exception e) {
                    Log.d("onAuthSucceeded", e.toString());
                }
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();

                Log.d("COTTER_BIOMETRIC_HELPER", "onAuthenticationFailed");
            }
        });
        biometricInterface.setBiometricPrompt(bp);
        biometricInterface.setExecutor(ex);
    }
}
