package com.cotter.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;

public class Flow {
    String[] listFlow;
    Class callBack;
    boolean changePin = false;
    ArrayList<Activity> activities = new ArrayList<>();

    public Flow(String[] flow ) {

        this.listFlow = flow;
    }


    public Class nextStep(String screenName) {
        int index = Arrays.asList(listFlow).indexOf(screenName);
        if (index != listFlow.length - 1) {
            String nextScreen = listFlow[index + 1];
            return ScreenNames.getClassFromName(nextScreen);
        }
        return callBack;
    }


    public Class goToCallback() {
        return callBack;
    }

    public void startFlow(View view, Class callBackIntent, String event) {
        this.callBack = callBackIntent;
        Class intent = ScreenNames.getClassFromName(listFlow[0]);
        Context ctx = view.getContext();
        Intent in = new Intent(ctx, intent);
        in.putExtra("event", event);
        in.putExtra("change_pin", false);
        ctx.startActivity(in);
    }
    public void startFlow(Context ctx, Class callBackIntent, String event) {
        this.callBack = callBackIntent;
        Class intent = ScreenNames.getClassFromName(listFlow[0]);
        Intent in = new Intent(ctx, intent);
        in.putExtra("event", event);
        in.putExtra("change_pin", false);
        ctx.startActivity(in);
    }

    public Intent startFlowWithIntent(Context ctx, Class callBackIntent, String event) {
        this.callBack = callBackIntent;
        Class intent = ScreenNames.getClassFromName(listFlow[0]);
        Intent in = new Intent(ctx, intent);
        in.putExtra("event", event);
        in.putExtra("change_pin", false);
        return in;
    }

    public void addActivityStack(Activity act) {
        activities.add(act);
    }

    public void removeAllActivityStack() {
        for (Activity act : activities) {
            act.finish();
        }
    }
}
