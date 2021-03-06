package com.cotter.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthRequest {
    public String mainServerURL;
    public static String NETWORK_ERROR_MESSAGE = "Network Error";

    public AuthRequest(String url) {
        mainServerURL = url;
    }

    public void GetUser(final Context context, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user/" + Cotter.UserID;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                        Log.d("AUTH_REQUEST_GET_USER", "Success getting user: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(getErrorMessage(error));
                        Log.d("AUTH_REQUEST_GET_USER", "Error getting user: " + getErrorMessage(error));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void EnrollMethod(Context context, String method, String code, final Callback callback) {
        UpdateMethod(context, method, code, true, false, null, callback);
    }

    // For trusted devices
    public void EnrollMethod(Context context, String method, String code, String algorithm, final Callback callback) {
        UpdateMethod(context, method, code, true, false, null, algorithm, null, callback);
    }
    public void EnrollMethodWithCotterUserID(Context context, String method, String code, String algorithm, String cotterUserID, final Callback callback) {
        UpdateMethod(context, method, code, true, false, null, algorithm, cotterUserID, callback);
    }

    public void ChangeMethod(Context context, String method, String code, String currentCode, final Callback callback) {
        UpdateMethod(context, method, code, true, true, currentCode, callback);
    }

    public void DeleteMethod(Context context, String method, String code, final Callback callback) {
        UpdateMethod(context, method, code, false, false, null, callback);
    }

    public void UpdateMethod(Context context, String method, String code, boolean enrolled, boolean changeCode,
                             String currentCode, final Callback callback) {
        UpdateMethod(context, method, code, enrolled, changeCode, currentCode, null, null, callback);
    }

    public void UpdateMethod(Context context, String method, String code, boolean enrolled, boolean changeCode,
            String currentCode, String algorithm, String cotterUserID, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user/" + Cotter.UserID;
        if (cotterUserID != null) {
            url = mainServerURL + "/user/methods?cotter_user_id=" + cotterUserID + "&oauth_token=true";
        }

        JSONObject req = new JSONObject();

        try {
            req.put("method", method);
            req.put("enrolled", enrolled);
            req.put("code", code);
            if (changeCode) {
                req.put("change_code", changeCode);
                req.put("current_code", currentCode);
            }

            req.put("algorithm", algorithm);
            req.put("device_name", getDeviceName());
            req.put("device_type", getDeviceType());
        } catch (Exception e) {
            callback.onError(e.toString());
        }

        Log.e("COTTER URL", url);
        Log.e("COTTER REQ", req.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.PUT, url, req,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        // Update shared preferences
                        if (method == Cotter.BiometricMethod) {
                            SharedPreferences sharedPref = context.getSharedPreferences(
                                    Cotter.getSharedPreferenceFileKeyPrefix(), Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();

                            editor.putBoolean(CotterMethodHelper.BiometricEnrolledThisDeviceKey, enrolled);

                            Gson gson = new Gson();
                            User user = gson.fromJson(response.toString(), User.class);
                            List<String> enrolledMethods = Arrays.asList(user.enrolled);
                            boolean bioEnrolledAny = enrolledMethods.contains(Cotter.BiometricMethod);
                            boolean bioDefault = user.default_method != null && user.default_method.equals(Cotter.BiometricMethod);
                            editor.putBoolean(CotterMethodHelper.BiometricEnrolledAnyKey, bioEnrolledAny);
                            editor.putBoolean(CotterMethodHelper.BiometricEnrolledDefaultKey, bioDefault);


                            editor.commit();
                        }

                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Cotter.methods.biometricEnrolledForce(new CotterMethodChecker() {
                            @Override
                            public void onCheck(boolean result) {
                                Log.d("COTTER_BIO_AUTH_REQ", "Update biometric enrolled in shared pref: " + result);
                            }
                        }, true);

                        if (method == Cotter.BiometricMethod && enrolled && getErrorMessage(error).contains("already enrolled")) {
                            JSONObject enrolledResp = new JSONObject();
                            try {
                                enrolledResp.put("response", getErrorMessage(error));
                            } catch (JSONException e) {
                                callback.onError(getErrorMessage(error));
                            }
                            callback.onSuccess(enrolledResp);
                        } else {
                            callback.onError(getErrorMessage(error));
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void CheckEnrolledMethod(Context context, String method, String pubKey, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user/enrolled/" + Cotter.UserID + "/" + method;
        if (pubKey != null) {
            url = url + "/" + Base64.encodeToString(pubKey.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        }
        Log.d("COTTER_ENROLLED_URL", url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("COTTER_ENROLLED_REQ", response.toString());
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("COTTER_ENROLLED_ERR", getErrorMessage(error));
                        callback.onError(getErrorMessage(error));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void GetRules(Context context, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/rules";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                        Log.d("AUTH_REQUEST_GET_RULES", "Success getting rules: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(getErrorMessage(error));
                        Log.d("AUTH_REQUEST_GET_RULES", "Success getting rules: " + getErrorMessage(error));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public String ConstructApprovedEventMsg(String event, String timestamp, String method) {
        String[] list = { Cotter.UserID, Cotter.ApiKeyID, event, timestamp, method,
                "true" };
        return TextUtils.join("", list);
    }

    public JSONObject ConstructApprovedEventJSON(String event, String timestamp, String method, String code,
                                                 String publicKey, String algorithm, final Callback callback) {
        final JSONObject req = new JSONObject();

        try {
            req.put("client_user_id", Cotter.UserID);
            req.put("issuer", Cotter.ApiKeyID);
            req.put("event", event);
            req.put("ip", getLocalIpAddress());
            req.put("timestamp", timestamp);
            req.put("method", method);
            req.put("code", code);
            req.put("approved", true);
            req.put("public_key", publicKey);
            req.put("algorithm", algorithm);
        } catch (Exception e) {
            callback.onError(e.toString());
        }
        return req;
    }

    public JSONObject ConstructApprovedEventJSON(String event, String timestamp, String method, String code,
            String publicKey, final Callback callback) {
        final JSONObject req = new JSONObject();

        try {
            req.put("client_user_id", Cotter.UserID);
            req.put("issuer", Cotter.ApiKeyID);
            req.put("event", event);
            req.put("ip", getLocalIpAddress());
            req.put("timestamp", timestamp);
            req.put("method", method);
            req.put("code", code);
            req.put("approved", true);
            req.put("public_key", publicKey);
        } catch (Exception e) {
            callback.onError(e.toString());
        }
        return req;
    }

    public JSONObject ConstructRegisterNewDeviceJSON(String event, String timestamp, String method, String code,
                                                 String publicKey, String algorithm, String newPublicKey, String newAlgo, final Callback callback) {
        final JSONObject req = new JSONObject();

        try {
            req.put("client_user_id", Cotter.UserID);
            req.put("issuer", Cotter.ApiKeyID);
            req.put("event", event);
            req.put("ip", getLocalIpAddress());
            req.put("timestamp", timestamp);
            req.put("method", method);
            req.put("code", code);
            req.put("approved", true);
            req.put("public_key", publicKey);
            req.put("algorithm", algorithm);

            // Registered Devices
            req.put("register_new_device", true);
            req.put("new_device_public_key", newPublicKey);
            req.put("device_type", getDeviceType());
            req.put("device_name", getDeviceName());
            req.put("new_device_algorithm", newAlgo);
        } catch (Exception e) {
            callback.onError(e.toString());
        }
        return req;
    }

    public void CreateApprovedEventRequest(Context context, JSONObject req, final Callback callback) {
        String path = "/event/create?oauth_token=true";
        CreateEventRequest(context, req, path, callback);
    }


    public JSONObject ConstructEventJSON(String event, String timestamp, String method, final Callback callback) {
        final JSONObject req = new JSONObject();
        try {
            req.put("client_user_id", Cotter.UserID);
            req.put("issuer", Cotter.ApiKeyID);
            req.put("event", event);
            req.put("ip", getLocalIpAddress());
            req.put("timestamp", timestamp);
            req.put("method", method);
        } catch (Exception e) {
            callback.onError(e.toString());
        }
        return req;
    }

    public void CreatePendingEventRequest(Context context, JSONObject req, final Callback callback) {
        String path = "/event/create_pending";
        CreateEventRequest(context, req, path, callback);
    }

    public JSONObject ConstructRespondEventJSON(Event ev, String method, String signature,
                                                 String publicKey, String algorithm, boolean approved, final Callback callback) {
        final JSONObject req = new JSONObject();

        try {
            req.put("client_user_id", Cotter.UserID);
            req.put("issuer", Cotter.ApiKeyID);
            req.put("event", ev.event);
            req.put("ip", ev.ip);
            req.put("location", ev.location);
            req.put("timestamp", ev.timestamp);
            req.put("method", method);
            req.put("code", signature);
            req.put("approved", approved);
            req.put("public_key", publicKey);
            req.put("algorithm", algorithm);
        } catch (Exception e) {
            callback.onError(e.toString());
        }
        return req;
    }

    public String ConstructRespondEventMsg(String event, String timestamp, String method, boolean approved) {
        String[] list = {Cotter.UserID, Cotter.ApiKeyID, event, timestamp, method,
                approved + ""};
        return TextUtils.join("", list);
    }

    public void CreateRespondEventRequest(Context context, int eventID, JSONObject req, final Callback callback) {
        String path = "/event/respond/" + eventID;
        CreateEventRequest(context, req, path, callback);
    }

    public void CreateEventRequest(Context context, JSONObject req, String path, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + path;

        Log.e("CreateEventRequest", url);
        Log.e("CreateEventRequest req", req.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, req,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    // Checking for new un-approved event for this user
    public void GetNewEvent(Context context, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/event/new/" + Cotter.UserID;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                        Log.i("COTTER_AUTH_REQUEST", "Success checking new event: " + response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
                Log.e("COTTER_AUTH_REQUEST", "Error checking new event: " + getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    // Get event of a specific id
    public void GetEvent(Context context, String eventID, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/event/get/" + eventID + "?oauth_token=true";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                        Log.i("COTTER_AUTH_REQUEST", "Success getting event: " + response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
                Log.e("COTTER_AUTH_REQUEST", "Error getting event: " + getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void RequestIdentityToken(Context context, String codeVerifier, String authCode, int challengeID,
            String redirectURL, final Callback callback) {

        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/verify/get_identity";

        JSONObject req = new JSONObject();

        try {
            req.put("code_verifier", codeVerifier);
            req.put("authorization_code", authCode);
            req.put("challenge_id", challengeID);
            req.put("redirect_url", redirectURL);
        } catch (Exception e) {
            callback.onError(e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, req,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        callback.onError(getErrorMessage(error));
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    // DEPRECATED
    public void ResetStart(Context context, String method, String sendingMethod, String sendingDestination, String name, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user/reset/start/" + Cotter.UserID;

        JSONObject req = new JSONObject();
        try {
            req.put("method", method);
            req.put("sending_method", sendingMethod);
            req.put("sending_destination", sendingDestination);
            req.put("name", name);
        } catch (Exception e) {
            callback.onError(e.toString());
        }

        Log.e("COTTER RESET REQ", req.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, req,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }


    public void ResetVerify(Context context, String method, String resetCode, int challengeID, String challenge, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user/reset/verify/" + Cotter.UserID;
        Log.d("COTTER AUTH REQ", "Reset Verify: " + url);

        JSONObject req = new JSONObject();
        try {
            req.put("method", method);
            req.put("reset_code", resetCode);
            req.put("challenge_id", challengeID);
            req.put("challenge", challenge);
        } catch (Exception e) {
            callback.onError(e.toString());
        }

        Log.e("COTTER RESET VERIFY", req.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, req,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public void ResetRespond(Context context, String method, String resetCode, int challengeID, String challenge, String newCode, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user/reset/respond/" + Cotter.UserID;

        JSONObject req = new JSONObject();
        try {
            req.put("method", method);
            req.put("reset_code", resetCode);
            req.put("challenge_id", challengeID);
            req.put("challenge", challenge);
            req.put("new_code", newCode);
        } catch (Exception e) {
            callback.onError(e.toString());
        }

        Log.e("COTTER RESET RESP", req.toString());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, req,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);
                params.put("API_SECRET_KEY", Cotter.ApiSecretKey);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    // Register user to Cotter
    public void RegisterUserToCotter(Context context, String identifier, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user/create";


        JSONObject req = new JSONObject();
        try {
            req.put("identifier", identifier);
        } catch (Exception e) {
            callback.onError(e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, req,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    // Get user by identifier
    public void GetUserByIdentifier(Context context, String identifier, final Callback callback) {
        if (!networkIsAvailable(context)) {
            showNetworkErrorDialogIfNecessary(context, callback);
            return;
        }

        String url = mainServerURL + "/user?identifier=" + Uri.encode(identifier);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccess(response);
                        Log.i("COTTER GetByIdentifier", "Success getting user: " + response.toString());
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onError(getErrorMessage(error));
                Log.e("COTTER GetByIdentifier", "Error getting user: " + getErrorMessage(error));
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("API_KEY_ID", Cotter.ApiKeyID);

                return params;
            }
        };

        // Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getLocation(Activity act) {
        final List<Location> locContainer = new ArrayList<>();
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(act);
        fusedLocationClient.getLastLocation().addOnSuccessListener(act, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    locContainer.add(location);
                }
            }
        });
        Location loc = locContainer.get(0);
        return loc.getLatitude() + " " + loc.getLongitude();
    }

    public static String getErrorMessage(VolleyError error) {
        String statusCode = null;
        String body = null;

        if (error.networkResponse != null) {
            // get status code here
            statusCode = String.valueOf(error.networkResponse.statusCode);

            // get response body and parse with appropriate encoding
            if (error.networkResponse.data != null) {
                try {
                    body = new String(error.networkResponse.data, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return "Status Code: " + statusCode + " Body: " + body;
    }

    public static void showNetworkErrorDialogIfNecessary(Context context, Callback callback) {
        callback.onError(NETWORK_ERROR_MESSAGE);
        if (context == null || Cotter.strings == null) {
            Log.e("COTTER_NETWORK_ERROR", "Cotter cannot be initialized because there is no connection");
            return;
        } else {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme)
                        .setTitle(Cotter.strings.NetworkError.get(Strings.DialogTitle))
                        .setMessage(Cotter.strings.NetworkError.get(Strings.DialogSubtitle))
                        .setIcon(Cotter.colors.NetworkErrorImage)
                        .setPositiveButton(Cotter.strings.NetworkError.get(Strings.DialogPositiveButton),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
            } catch (Exception e) {
                Log.e("COTTER_NETWORK_ERROR", e.toString());
            }
        }
    }

    private void showHttpErrorDialog(Context context) {
        if (context == null || Cotter.strings == null) {
            Log.e("COTTER_HTTP_ERROR", "Cotter cannot be initialized because there is an http error");
            return;
        } else {
            try {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialogTheme)
                        .setTitle(Cotter.strings.HttpError.get(Strings.DialogTitle))
                        .setMessage(Cotter.strings.HttpError.get(Strings.DialogSubtitle))
                        .setIcon(Cotter.colors.HttpErrorImage)
                        .setPositiveButton(Cotter.strings.HttpError.get(Strings.DialogPositiveButton),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });

                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setAllCaps(false);
            } catch (Exception e) {
                Log.e("COTTER_HTTP_ERROR", e.toString());
            }
        }
    }

    public static boolean networkIsAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String getDeviceType() {
        return android.os.Build.MANUFACTURER;
    }

    public static String getDeviceName() {
        return android.os.Build.MODEL + " " + android.os.Build.VERSION.RELEASE;
    }
}
