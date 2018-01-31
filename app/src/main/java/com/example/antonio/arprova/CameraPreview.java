package com.example.antonio.arprova;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * Created by Antonio on 19/01/2018.
 * A basic Camera preview class
 */


// for better camera size see
// https://github.com/pikanji/CameraPreviewSample/blob/master/src/net/pikanji/camerapreviewsample/CameraPreview.java
@SuppressLint("ViewConstructor")//da verificare, per ora non serve
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";
    private SurfaceHolder mHolder;
    private Context context;
    private Camera mCamera;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.context = context;
        // Do not initialise if no camera has been set
        if (camera == null) {
            return;
        }
        mCamera = camera;
        Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, mCameraInfo);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        //noinspection deprecation
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
                Toast.makeText(context.getApplicationContext(), R.string.toast_cameraError, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context.getApplicationContext(), R.string.toast_cameraNotFound, Toast.LENGTH_SHORT).show();
        }

        if (null != c) {
            Log.d(TAG, "getCameraInstance: camera NOT null, setting parameters..");
            //forse inutile
            // get Camera parameters
            Camera.Parameters params = c.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // Autofocus mode is supported
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                // set Camera parameters
                c.setParameters(params);
            }
            //fine forse inutile
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

    /**
     * Calculate the correct orientation for a {@link Camera} preview that is displayed on screen.
     * <p>
     * Implementation is based on the sample code provided in
     * {@link Camera#setDisplayOrientation(int)}.
     */
    public static int calculatePreviewOrientation(Context context) {
        int degrees = 0;

        // Get the rotation of the screen to adjust the preview image accordingly.
        int mDisplayOrientation = ((Activity) context).getWindowManager().getDefaultDisplay()
                .getRotation();

        Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(0, mCameraInfo);

        switch (mDisplayOrientation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;

        if (mCameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCameraInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing*/
            result = (mCameraInfo.orientation - degrees + 360) % 360;
        }
        return result;
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Log.d("CameraPreview: ", "Surface creating..");
        try {
            if (mCamera == null) {
                mCamera = getCameraInstance(getContext());
                Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
                Camera.getCameraInfo(0, mCameraInfo);
            }
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d("CameraPreview: ", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // was empty. care of releasing the Camera preview in your activity.
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
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                mCamera = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
                Log.d(TAG, "preview size: " + bestw + "w, " + besth + "h");
            } catch (Exception e) {
                parameters.setPreviewSize(480, 320);
            }
            mCamera.setParameters(parameters);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //TODO check if exception in silvcell e emu
        int orientation = calculatePreviewOrientation(context);
        mCamera.setDisplayOrientation(orientation);
        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d("CameraPreview: ", "Error starting camera preview: " + e.getMessage());
        }
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
        mHolder.removeCallback(this);
    }
}