package com.cotter.app;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

public class RegisterDeviceQRShowActivity extends AppCompatActivity {
    public static String name = ScreenNames.RegisterDeviceQRShow;
    public int QRcodeWidth;
    Bitmap bitmap;

    TextView title;
    TextView subtitle;
    ImageView qrCodeImage;

    private Handler handler;

    public Map<String, String> ActivityStrings;

    private int MILLI_SECONDS_TO_ERROR = 60000 * 3;
    private int MILLI_SECONDS_TO_RETRY = 3000;
    private boolean stopPolling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device_qrshow);

        handler = new Handler();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        QRcodeWidth = Math.min(500, (int) (size.x-120));

        // setting elements
        qrCodeImage = findViewById(R.id.qr_code);
        title = findViewById(R.id.title);
        subtitle = findViewById(R.id.subtitle);

        ActivityStrings = Cotter.strings.QRCodeShow;
        title.setText(ActivityStrings.get(Strings.Title));
        subtitle.setText(ActivityStrings.get(Strings.Subtitle));

        String qrCode = TrustedDeviceHelper.getPublicKey(this) + ":" + TrustedDeviceHelper.getAlgorithm(this);

        try {
            bitmap = TextToImageEncode(qrCode);
            qrCodeImage.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
            Log.e("COTTER_TRUST_DEV", "Error showing QR Code, " + e.toString());
        }
        pollingEvent();

        Handler stopHandler = new Handler();
        stopHandler.postDelayed(new Runnable() {
            public void run() {
                stopPolling();
            }
        }, MILLI_SECONDS_TO_ERROR);

        setupToolBar();
    }


    // Set up and show toolbar
    private void setupToolBar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(Cotter.strings.Headers.get(name));

        if (toolbar == null) return;

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_black_24dp);
    }

    // Handle back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }

        return super.onOptionsItemSelected(item);
    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.colorBlack):getResources().getColor(R.color.colorWhite);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    public void pollingEvent() {
        Cotter.methods.trustedDeviceEnrolled(new CotterMethodChecker() {
            @Override
            public void onCheck(boolean enrolled) {
                // Check if TrustedDevice available and enabled
                if (enrolled) {
                    qrCodeImage.setImageResource(Cotter.colors.SuccessImage);

                    handler.postDelayed(new Runnable() {
                        public void run() {
                            finish();
                        }
                    }, MILLI_SECONDS_TO_RETRY);
                } else {
                     if (!stopPolling){
                         handler.postDelayed(new Runnable() {
                             public void run() {
                                 pollingEvent();
                             }
                         }, MILLI_SECONDS_TO_RETRY);
                    } else {
                        handler.removeCallbacksAndMessages(null);
                    }
                }
            }
        });
    }



    public void stopPolling() {
        handler.removeCallbacksAndMessages(null);
        stopPolling = true;
        // Show error message
        ActivityStrings = Cotter.strings.QRCodeShowError;

        title.setText(ActivityStrings.get(Strings.Title));
        subtitle.setText(ActivityStrings.get(Strings.Subtitle));
        qrCodeImage.requestLayout();
        qrCodeImage.getLayoutParams().width = 200;
        qrCodeImage.getLayoutParams().height = 200;
        qrCodeImage.setImageResource(Cotter.colors.ErrorImage);
    }
}
