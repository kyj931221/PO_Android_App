package com.kimabilty.myflash;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowInsets;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    ImageView img;
    SensorManager manager;
    CameraManager cam;
    String cameraId;
    boolean isFlashOn = false;
    Handler handler = new Handler();
    private static final int FLASH_INTERVAL = 300; // 500ms = 0.5 seconds
    private static final int LUX_THRESHOLD = 100; // Adjust this threshold as needed

    private boolean isTorchCurrentlyOn = false; // Track torch state manually

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the status bar and navigation bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            getWindow().getInsetsController().hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
        } else {
            // For older Android versions
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }

        img = findViewById(R.id.imageView3);
        manager = (SensorManager) getSystemService(SENSOR_SERVICE);
        cam = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = cam.getCameraIdList()[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        manager.registerListener(listener, manager.getDefaultSensor(Sensor.TYPE_LIGHT),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    SensorEventListener listener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                int lux = (int) event.values[0];
                if (lux < LUX_THRESHOLD) {
                    img.setImageResource(R.drawable.light);
                    startFlashing();
                } else {
                    img.setImageResource(R.drawable.dark);
                    stopFlashing();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void startFlashing() {
        if (!isFlashOn) {
            handler.postDelayed(flashRunnable, FLASH_INTERVAL);
            isFlashOn = true;
        }
    }

    private void stopFlashing() {
        if (isFlashOn) {
            handler.removeCallbacks(flashRunnable);
            turnOffFlash();
            isFlashOn = false;
        }
    }

    private Runnable flashRunnable = new Runnable() {
        @Override
        public void run() {
            if (isFlashOn) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        // Toggle torch state and update isTorchCurrentlyOn
                        isTorchCurrentlyOn = !isTorchCurrentlyOn;
                        cam.setTorchMode(cameraId, isTorchCurrentlyOn);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handler.postDelayed(this, FLASH_INTERVAL);
            }
        }
    };

    private void turnOffFlash() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                cam.setTorchMode(cameraId, false);
                isTorchCurrentlyOn = false; // Update torch state
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}