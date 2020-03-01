package com.cotter.app;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Map;

public class SuccessSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ActionSuccessBottomDialog";
    private TextView title;
    private TextView subtitle;
    private ImageView successImg;

    private Handler handler;
    private Runnable runOnDismiss;
    public Map<String, String> ActivityStrings;
    public int ImageResource;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        handler = new Handler();

        View v = inflater.inflate(R.layout.success_sheet, container, false);
        title = v.findViewById(R.id.title);
        subtitle = v.findViewById(R.id.subtitle);
        successImg = v.findViewById(R.id.success_image);


        title.setText(ActivityStrings.get(Strings.DialogTitle));
        subtitle.setText(ActivityStrings.get(Strings.DialogSubtitle));
        successImg.setImageResource(ImageResource);

        handler.postDelayed(new Runnable() {
            public void run() {
                dismiss();
                runOnDismiss.run();
            }
        }, 3000);

        return v;
    }

    public static SuccessSheet newInstance(Map<String, String> strings, int imgResource) {
        SuccessSheet req = new SuccessSheet();
        req.setString(strings);
        req.setImage(imgResource);
        return req;
    }
    public void setString(Map<String, String> strings) {
        ActivityStrings = strings;
    }
    public void setImage(int imgResource) {
        ImageResource = imgResource;
    }
    public void setRunnableOnDismiss(Runnable runnable) {
        this.runOnDismiss = runnable;
    }
}
