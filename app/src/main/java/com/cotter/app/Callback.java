package com.cotter.app;


import org.json.JSONObject;

public interface Callback {
    void onSuccess(JSONObject result);
    void onError(String error);
}
