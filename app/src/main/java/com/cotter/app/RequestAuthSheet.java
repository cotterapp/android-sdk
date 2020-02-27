package com.cotter.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.json.JSONObject;

import java.util.Map;

public class RequestAuthSheet extends BottomSheetDialogFragment {
    public static final String TAG = "ActionBottomDialog";
    private TextView title;
    private TextView subtitle;
    private ImageView tapDevice;

    private String eventID;
    private Class callbackClass;
    private Callback callback;
    private Handler handler;

    public Map<String, String> ActivityStrings;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        handler = new Handler();

        View v = inflater.inflate(R.layout.request_auth_sheet, container, false);
        title = v.findViewById(R.id.title);
        subtitle = v.findViewById(R.id.subtitle);
        tapDevice = v.findViewById(R.id.tap_device_image);

        ActivityStrings = Cotter.strings.RequestAuth;

        title.setText(ActivityStrings.get(Strings.DialogTitle));
        subtitle.setText(ActivityStrings.get(Strings.DialogSubtitle));
        tapDevice.setImageResource(Cotter.colors.Tap);

        pollingEvent(eventID);

        return v;
    }

    public static RequestAuthSheet newInstance() {
        RequestAuthSheet req = new RequestAuthSheet();
        return req;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
    public void setCallbackClass(Class callbackClass) {
        this.callbackClass = callbackClass;
    }
    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        handler.removeCallbacksAndMessages(null);
        super.onDismiss(dialog);
    }

    public void pollingEvent(String eventID) {
        Context ctx = this.getContext();
        Callback cb = new Callback(){
            public void onSuccess(JSONObject response){
                Boolean approved;
                try {
                    approved = response.getBoolean("approved");
                    Log.i("COTTER_TRUSTED_DEV", "pollingEvent > onSuccess > Response Success: " + response.toString());

                    // if approved, move on to the callbackClass
                    if (approved) {
                        callback.onSuccess(response);
                        Intent in = new Intent(ctx, callbackClass);
                        in.putExtra(TrustedDeviceHelper.EVENT_KEY, response.toString());
                        startActivity(in);
                        dismiss();
                    } else {
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                pollingEvent(eventID);
                            }
                        }, 1000);
                    }
                } catch (Exception e) {
                    approved = false;
                    Log.e("COTTER_TRUSTED_DEV", "pollingEvent > onSuccess > Response ERROR: " + response.toString() + ", Exception: " + e.toString());
                    callback.onError(e.toString());
                }
            }
            public void onError(String error){
                Log.e("COTTER_TRUSTED_DEV", "pollingEvent > onError: " + error);
                callback.onError(error);
            }
        };

        Cotter.authRequest.GetEvent(this.getContext(), eventID, cb);
    }

}
