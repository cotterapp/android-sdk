package com.cotter.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

public class TrustedDeviceHelper {
    private static String androidKeyStore = "AndroidKeyStore";
    private static String keyStoreAlias = "COTTER_TRUSTED_DEVICE_KEY";
    private static String signatureAlgo = "SHA256withECDSA";
    private static String signatureAlgoRSA = "SHA256withRSA";
    public static String EVENT_KEY = "EVENT";

    public static String getKeyStoreAlias() {
        Log.i("COTTER_TRUSTDEV", "Keystore Alias: " + keyStoreAlias + Cotter.ApiKeyID + Cotter.UserID);
        return keyStoreAlias + Cotter.ApiKeyID + Cotter.UserID;
    }


    // Generate KeyPair
    public static String generateKeyPair(Context ctx) {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(
                        getKeyStoreAlias(),
                        KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256,
                                KeyProperties.DIGEST_SHA512)
                        .build();

                KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_EC, androidKeyStore);
                kpg.initialize(keyGenParameterSpec);

                KeyPair kp = kpg.generateKeyPair();
                return Base64.encodeToString(kp.getPublic().getEncoded(), Base64.DEFAULT);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                        KeyProperties.KEY_ALGORITHM_RSA, androidKeyStore);

                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 5);

                KeyPairGeneratorSpec specs = new KeyPairGeneratorSpec.Builder(ctx)
                        .setAlias(getKeyStoreAlias())
                        .setSubject(new X500Principal("CN=" + getKeyStoreAlias())) // TODO: Find out what this does
                        .setSerialNumber(BigInteger.valueOf(Math.abs(getKeyStoreAlias().hashCode()))) // TODO: Find out what this does
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .build();

                keyPairGenerator.initialize(specs);
                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                return Base64.encodeToString(keyPair.getPublic().getEncoded(), Base64.DEFAULT);
            } else {
                Log.e("COTTER_TRUSTED_DEV", "API Level lower than 18 not supported for Trusted Devices");
            }
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
            Log.e("COTTER_TRUSTED_DEV", "getPrivateKey error: " + e.toString());
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
            Log.e("COTTER_TRUSTED_DEV", "getPublicKey error: " + e.toString());
        }
        return null;
    }

    // Get algorithm used
    public static String getAlgorithm() {
        try {
            KeyStore keyStore = KeyStore.getInstance(androidKeyStore);
            keyStore.load(null);

            PublicKey publicKey = keyStore.getCertificate(getKeyStoreAlias()).getPublicKey();
            return publicKey.getAlgorithm();
        } catch (Exception e) {
            Log.e("COTTER_TRUSTED_DEV", "getAlgorithm error: " + e.toString());
        }
        return null;
    }

    // Get signature
    public static String sign(String stringToSign) {
        try {
            Signature s;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                s = Signature.getInstance(signatureAlgo);
            } else {
                s = Signature.getInstance(signatureAlgoRSA);
            }
            PrivateKey pk = getPrivateKey();
            s.initSign(pk);
            s.update(stringToSign.getBytes());
            byte[] signature = s.sign();

            Log.d("Message", stringToSign);
            Log.d("Signature", Base64.encodeToString(signature, Base64.DEFAULT));

            return Base64.encodeToString(signature, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e("COTTER_TRUSTED_DEV", "sign error: " + e.toString());
        }
        return null;
    }

    // TODO: PROVIDE CALLBACK METHOD
    public static void enrollDevice(Context ctx) {

        String publicKey = getPublicKey();
        if (publicKey == null) {
            // Generate keypair that can only be accessed by biometrics
            publicKey = generateKeyPair(ctx);
        }

        // Enroll Trusted Device
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.e("COTTER_TRUSTED_DEV", "Enroll Success: " + response.toString());
                // callback.onSuccess(true);
                User.refetchUser(ctx, Cotter.authRequest);
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "Enroll error: " + error);
                //  invalidEnrollBiometric();
                //  callback.onError(error);
                User.refetchUser(ctx, Cotter.authRequest);
            }
        };

        Cotter.authRequest.EnrollMethod(ctx, Cotter.TrustedDeviceMethod, publicKey, getAlgorithm(), cb);
    }


    public static void authorizeDevice(Context ctx, String event) {

        // Verify Device
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Boolean valid;
                try {
                    valid = response.getBoolean("approved");
                    Log.i("COTTER_TRUSTED_DEV", "verifyDevice > onSuccess > Response Success: " + response.toString());
                } catch (Exception e) {
                    valid = false;
                    Log.e("COTTER_TRUSTED_DEV", "verifyDevice > onSuccess > Response ERROR: " + response.toString());
                }
                if (valid) {
                    Log.i("COTTER_TRUSTED_DEV", "verifyDevice Approved Success: " + response.toString());
                } else {
                    Log.i("COTTER_TRUSTED_DEV", "verifyDevice Not Approved: " + response.toString());
                }
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "onSubmitBio > onError: " + error);
            }
        };


        Date now = new Date();
        long timestamp = now.getTime() / 1000L;
        String strTimestamp = Long.toString(timestamp);
        String stringToSign = Cotter.authRequest.ConstructApprovedEventMsg(event, strTimestamp, Cotter.TrustedDeviceMethod);
        String signature = sign(stringToSign);
        JSONObject req = Cotter.authRequest.ConstructApprovedEventJSON(event, strTimestamp, Cotter.TrustedDeviceMethod, signature, getPublicKey(), getAlgorithm(), cb);
        Cotter.authRequest.CreateApprovedEventRequest(ctx, req, cb);
    }


    public static void enrollOtherDevice(Context ctx, String newPublicKeyAndAlgo) {

        String[] strs = newPublicKeyAndAlgo.split(":");
        if (strs.length < 2) {
            Log.e("COTTER_TRUSTED_DEV", "enrollOtherDevice, invalid newPublicKeyAndAlgo string. Should be of format <publickey>:<algo>");
            return;
        }

        String newPublicKey = strs[0];
        String newAlgo = strs[1];
        // Verify Device
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Boolean valid;
                try {
                    valid = response.getBoolean("approved");
                    Log.i("COTTER_TRUSTED_DEV", "enrollOtherDevice > onSuccess > Response Success: " + response.toString());
                } catch (Exception e) {
                    valid = false;
                    Log.e("COTTER_TRUSTED_DEV", "enrollOtherDevice > onSuccess > Response ERROR: " + response.toString());
                }
                if (valid) {
                    Log.i("COTTER_TRUSTED_DEV", "enrollOtherDevice Approved Success: " + response.toString());
                } else {
                    Log.i("COTTER_TRUSTED_DEV", "enrollOtherDevice Not Approved signature: " + response.toString());
                }
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "enrollOtherDevice > onError: " + error);
            }
        };


        String event = "ENROLL NEW TRUSTED DEVICE";
        Date now = new Date();
        long timestamp = now.getTime() / 1000L;
        String strTimestamp = Long.toString(timestamp);
        String stringToSign = Cotter.authRequest.ConstructApprovedEventMsg(event, strTimestamp, Cotter.TrustedDeviceMethod) + newPublicKey;
        String signature = sign(stringToSign);
        JSONObject req = Cotter.authRequest.ConstructRegisterNewDeviceJSON(event, strTimestamp, Cotter.TrustedDeviceMethod, signature, getPublicKey(), getAlgorithm(), newPublicKey, newAlgo, cb);
        Cotter.authRequest.CreateApprovedEventRequest(ctx, req, cb);
    }


    public static void removeDevice(Context ctx) {
        String pubKey = getPublicKey();

        // Signature is null for enrollment
        // Enroll Biometric
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Log.e("COTTER_TRUSTED_DEV", "Delete trusted device success " + response.toString());
                User.refetchUser(ctx, Cotter.authRequest);
//                callback.onSuccess(false);
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "Delete trusted device error " + error);
//                invalidDisableBiometric();
                User.refetchUser(ctx, Cotter.authRequest);
//                callback.onError(error);
            }
        };

        Cotter.authRequest.DeleteMethod(ctx, Cotter.TrustedDeviceMethod, pubKey, cb);
    }



    public static void requestAuthFromNonTrusted(Context ctx, String event) {
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Boolean approved;
                try {
                    approved = response.getBoolean("approved");
                    Log.i("COTTER_TRUSTED_DEV", "requestAuthFromNonTrusted > onSuccess > Response Success: " + response.toString());
                } catch (Exception e) {
                    approved = false;
                    Log.e("COTTER_TRUSTED_DEV", "requestAuthFromNonTrusted > onSuccess > Response ERROR: " + response.toString());
                }
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "requestAuthFromNonTrusted > onError: " + error);
            }
        };


        Date now = new Date();
        long timestamp = now.getTime() / 1000L;
        String strTimestamp = Long.toString(timestamp);
        JSONObject req = Cotter.authRequest.ConstructEventJSON(event, strTimestamp, Cotter.TrustedDeviceMethod, cb);
        Cotter.authRequest.CreatePendingEventRequest(ctx, req, cb);
    }

    public static void getNewEvent(Context ctx, Activity act) {
        // Get new login/auth events for this user
        // CAN ONLY BE APPROVED BY A TRUSTED DEVICE
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                try {
                    Log.i("COTTER_TRUSTED_DEV", "getNewEvent > onSuccess > Response Success: " + response.toString());

                    Intent in = new Intent(ctx, ApproveRequestActivity.class);
                    in.putExtra(EVENT_KEY, response.toString());
                    ctx.startActivity(in);
                    act.overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
                    return;

                } catch (Exception e) {
                    Log.e("COTTER_TRUSTED_DEV", "getNewEvent > onSuccess > Response ERROR: " + response.toString());
                }
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "getNewEvent > onError: " + error);
            }
        };

        Cotter.authRequest.GetNewEvent(ctx, cb);
    }

    public static void approveEvent(Context ctx, String eventString, boolean approved) {
        // Get new login/auth events for this user
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                try {
                    Log.i("COTTER_TRUSTED_DEV", "getNewEvent > onSuccess > Response Success: " + response.toString());
                } catch (Exception e) {
                    Log.e("COTTER_TRUSTED_DEV", "getNewEvent > onSuccess > Response ERROR: " + response.toString());
                }
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "getNewEvent > onError: " + error);
            }
        };


        Gson gson = new Gson();
        Event ev = gson.fromJson(eventString, Event.class);

        String stringToSign = Cotter.authRequest.ConstructRespondEventMsg(ev.event, ev.timestamp, Cotter.TrustedDeviceMethod, approved);
        String signature = sign(stringToSign);
        JSONObject req = Cotter.authRequest.ConstructRespondEventJSON(ev, Cotter.TrustedDeviceMethod, signature, getPublicKey(), getAlgorithm(), approved, cb);
        Cotter.authRequest.CreateRespondEventRequest(ctx, ev.ID, req, cb);
    }
}
