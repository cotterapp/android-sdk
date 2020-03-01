package com.cotter.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Map;

public class ApproveRequestActivity extends AppCompatActivity {

    String event;

    private TextView title;
    private TextView subtitle;
    private Button buttonNo;
    private Button buttonYes;
    private ImageView logo;

    public static String name = ScreenNames.ApproveRequest;

    public Map<String, String> ActivityStrings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_request);

        ActivityStrings = Cotter.strings.ApproveRequest;

        // Get elements
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);
        buttonNo = findViewById(R.id.button_no);
        buttonYes = findViewById(R.id.button_yes);
        logo = findViewById(R.id.logo);

        // Set strings and logo
        title.setText(ActivityStrings.get(Strings.Title));
        subtitle.setText(ActivityStrings.get(Strings.Subtitle));
        buttonYes.setText(ActivityStrings.get(Strings.ButtonYes));
        buttonNo.setText(ActivityStrings.get(Strings.ButtonNo));
        logo.setImageResource(Cotter.colors.Logo);
        logo.setAdjustViewBounds(true);
        logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        event = getIntent().getExtras().getString(TrustedDeviceHelper.EVENT_KEY);
        setupToolBar();
    }

    // Set up and show toolbar
    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");

        if (toolbar == null) return;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
    }

    // Handle back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.push_down_in, R.anim.push_down_out);
    }

    public void approve(View view) {
        TrustedDeviceHelper.approveEvent(this, event, true);
        finish();
    }

    public void deny(View view) {
        TrustedDeviceHelper.approveEvent(this, event, false);
        finish();
    }
}
