package com.cotter.app;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

public class User {
    public String ID;
    public String issuer;
    public String client_user_id;
    public String[] enrolled;
    public String default_method;

    private static User instance;

    public static User getInstance(Context context) {
        if (instance == null) {
            AuthRequest.GetUser(context, new Callback(){
                public void onSuccess(JSONObject response){
                    Gson gson = new Gson();
                    instance = gson.fromJson(response.toString(), User.class);
                    Log.i("Init User Success", response.toString());
                }
                public void onError(String error){
                    Log.e("Init User Error", error);
                }
            });
        }
        return instance;
    }
}
