package com.cotter.app;

import androidx.annotation.NonNull;

import com.google.gson.Gson;

public class Event {
    public int ID;
    public String client_user_id;
    public String issuer;
    public String event;
    public String ip;
    public String location;
    public String timestamp;
    public String method;
    public boolean approved;

    @NonNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Event getEventFromString(String response) {
        Gson gson = new Gson();
        return gson.fromJson(response, Event.class);
    }
}