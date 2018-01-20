package com.example.antonio.arprova;

/**
 * Created by Antonio on 19/01/2018.
 */

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;

import static android.content.ContentValues.TAG;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(Context context) {
        Camera c = null;
        if (checkCameraHardware(context)) {
            try {
                c = Camera.open(); // attempt to get a Camera instance
            } catch (Exception e) {
                // Camera is not available (in use or does not exist)
                Toast.makeText(context, R.string.toast_cameraError, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, R.string.toast_cameraNotFound, Toast.LENGTH_SHORT).show();
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Check if this device has a camera
     */
    private static boolean checkCameraHardware(Context context) {
        // true if device has a camera, false otherwise.
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.d("CameraPreview: ", "Surface creating..");
        try {
            if (mCamera == null) {
                mCamera = getCameraInstance(getContext());
            }
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // was empty. Take care of releasing the Camera preview in your activity.
        //fatto
        Log.d("CameraPreview: ", "Surface destroying..");
        try {
            if (mCamera != null) {
                try {
                    mCamera.stopPreview();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                mCamera.setPreviewCallback(null);
                try {
                    mCamera.release();
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
                mCamera = null;
            }
        } catch (Exception ex22) {
            ex22.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        Log.d("CameraPreview: ", "Surface changing..");
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            //per evitare stretch della preview
            try {
                float ff = ((float) w) / ((float) h);
                float bff = 0.0f;
                int bestw = 0;
                int besth = 0;
                for (Camera.Size element : parameters.getSupportedPreviewSizes()) {
                    float cff = ((float) element.width) / ((float) element.height);
                    if (ff - cff <= ff - bff && element.width <= w && element.width >= bestw) {
                        bff = cff;
                        bestw = element.width;
                        besth = element.height;
                    }
                }
                if (bestw == 0 || besth == 0) {
                    bestw = 480;
                    besth = 320;
                }
                parameters.setPreviewSize(bestw, besth);
            } catch (Exception e) {
                parameters.setPreviewSize(480, 320);
            }
            mCamera.setParameters(parameters);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
}