package com.cotter.app;

import android.content.Context;
import android.content.Intent;

import java.util.Arrays;

public class Flow {
    String[] listFlow;
    Class callBack;

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

    public void startFlow(Context ctx, Class callBackIntent, String event) {
        this.callBack = callBackIntent;
        Class intent = ScreenNames.getClassFromName(listFlow[0]);
        Intent in = new Intent(ctx, intent);
        in.putExtra("event", event);
        ctx.startActivity(in);
    }
}
