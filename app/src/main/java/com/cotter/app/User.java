package com.cotter.app;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONObject;

public class User {
    public String ID;
    public String issuer;
    public String client_user_id;
    public String[] enrolled = new String[]{};
    public String default_method;

    public String name;
    public String sendingDestination;
    public String sendingMethod;

    private static User instance;

    public static User getInstance(Context context, AuthRequest authRequest) {
        if (instance == null) {
            instance = new User();
            instance.issuer = Cotter.ApiKeyID;
            instance.client_user_id = Cotter.UserID;
            authRequest.GetUser(context, new Callback(){
                public void onSuccess(JSONObject response){
                    Gson gson = new Gson();
                    User updatedUser = gson.fromJson(response.toString(), User.class);
                    instance = updateUser(updatedUser);
                    Cotter.setUser(instance);
                    Log.i("Init User Success", response.toString());
                }
                public void onError(String error){
                    Log.e("Init User Error", error);
                }
            });
        }
        Cotter.setUser(instance);
        return instance;
    }

    public static User refetchUser(Context context, AuthRequest authRequest) {
        authRequest.GetUser(context, new Callback(){
            public void onSuccess(JSONObject response){
                Gson gson = new Gson();
                User updatedUser = gson.fromJson(response.toString(), User.class);
                instance = updateUser(updatedUser);
                Cotter.setUser(instance);
                Log.i("Init User Success", response.toString());
            }
            public void onError(String error){
                Log.e("Init User Error", error);
            }
        });
        Cotter.setUser(instance);
        return instance;
    }

    public static User updateUser(User updatedUser) {
        if (instance == null) {
            instance = updatedUser;
        } else {
            instance.ID = updatedUser.ID;
            instance.issuer = updatedUser.issuer;
            instance.client_user_id = updatedUser.client_user_id;
            instance.enrolled = updatedUser.enrolled;
            instance.default_method = updatedUser.default_method;
        }
        Cotter.setUser(instance);
        return instance;
    }

    public void setUserInformation(String Name, String SendingDestination, String SendingMethod) {
        name = Name;
        sendingDestination = SendingDestination;
        sendingMethod = SendingMethod;
    }
}
