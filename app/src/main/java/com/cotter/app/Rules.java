package com.cotter.app;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Map;

public class Rules {
    public String CompanyID;
    public String on_premise_url;
    public String[] allowed_methods;
    public Map<String, Boolean> on_premise_deployment;

    private static Rules instance;

    public static Rules getInstance(Context context) {
        if (instance == null) {
            AuthRequest.GetRules(context, new Callback(){
                public void onSuccess(JSONObject response){
                    Gson gson = new Gson();
                    instance = gson.fromJson(response.toString(), Rules.class);
                    Log.i("Init Rules Success", response.toString());
                }
                public void onError(String error){
                    Log.e("Init Rules Error", error);
                }
            });
        }
        return instance;
    }
}
