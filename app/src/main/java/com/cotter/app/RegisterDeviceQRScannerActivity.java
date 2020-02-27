package com.cotter.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.json.JSONObject;

import java.io.IOException;

public class RegisterDeviceQRScannerActivity extends AppCompatActivity {
    SurfaceView surfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private boolean barcodeDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device_qrscanner);

        initViews();
    }

    private void initViews() {
        surfaceView = findViewById(R.id.surfaceView);
    }

    private void initialiseDetectorsAndSources() {

        Log.i("COTTER_TRUST_DEV", "QRCode scanner started");

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(RegisterDeviceQRScannerActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(RegisterDeviceQRScannerActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                Log.i("COTTER_TRUST_DEV", "QRCode scanner released");
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() > 0 && !barcodeDetected) {
                    surfaceView.post(new Runnable() {
                        @Override
                        public void run() {
                            release();
                            barcodeDetected = true;

                            TrustedDeviceHelper.enrollOtherDevice(getApplicationContext(), barcodes.valueAt(0).displayValue, new Callback() {
                                @Override
                                public void onSuccess(JSONObject result) {
                                    SuccessSheet successSheet = SuccessSheet.newInstance(Cotter.strings.SuccessSheet, Cotter.colors.SuccessImage);
                                    successSheet.setRunnableOnDismiss(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    });
                                    successSheet.show(getSupportFragmentManager(), SuccessSheet.TAG);
                                }

                                @Override
                                public void onError(String error) {
                                    SuccessSheet successSheet = SuccessSheet.newInstance(Cotter.strings.SuccessSheetError, Cotter.colors.ErrorImage);
                                    successSheet.setRunnableOnDismiss(new Runnable() {
                                        @Override
                                        public void run() {
                                            finish();
                                        }
                                    });
                                    successSheet.show(getSupportFragmentManager(), SuccessSheet.TAG);
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}
