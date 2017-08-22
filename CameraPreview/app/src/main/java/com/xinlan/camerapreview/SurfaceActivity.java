package com.xinlan.camerapreview;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;
import java.util.List;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;

public class SurfaceActivity extends AppCompatActivity implements View.OnClickListener, SurfaceHolder.Callback {
    public static final int PERMISSON_CAMERA = 13;
    private SurfaceView mSurfaceView;

    private ImageView mImageView;
    private View mStartBtn;
    private View mEndBtn;
    private View mPauseBtn;

    private Camera mCamera;
    private Camera.Size mPreviewSize;
    private VideoRecord mRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surface);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA , Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSON_CAMERA);
            }
        }

        mSurfaceView = (SurfaceView) findViewById(R.id.texture_view);
        mStartBtn = findViewById(R.id.btn_start);
        mEndBtn = findViewById(R.id.btn_end);
        mPauseBtn = findViewById(R.id.btn_pause);

        mSurfaceView.getHolder().addCallback(this);

        mStartBtn.setOnClickListener(this);
        mEndBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mRecord = new VideoRecord();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_start:
                try {
                    mRecord.startRecord(VideoRecord.getSaveFile(mPreviewSize.width+"x"+mPreviewSize.height));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_end:
                mRecord.stopRecord(this);
                break;
            case R.id.btn_pause:
                break;
        }//end switch
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        releaseCamera();
        mCamera = Camera.open();
        setCameraParameters();
        setCameraDisplayOrientation(this, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            mCamera.setPreviewCallbackWithBuffer(getPreviewCallback());
            mCamera.addCallbackBuffer(new byte[calculateFrameSize(ImageFormat.NV21)]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Camera.PreviewCallback getPreviewCallback() {
        return new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, final Camera camera) {
                if (data != null) {
                    mRecord.putVideoData(data);
                }
                camera.addCallbackBuffer(data);
            }
        };
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
        }
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
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
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void setCameraParameters(){
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : supportedPreviewSizes) {
            mPreviewSize = size;
            break;
        }//end for each

        if (mPreviewSize == null) {
            throw new RuntimeException("find previewSize error");
        }

        parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);

        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        for (int i = 0; null != supportedFocusModes && i < supportedFocusModes.size(); i++) {
            if (FOCUS_MODE_AUTO.equals(supportedFocusModes.get(i))) {
                parameters.setFocusMode(FOCUS_MODE_AUTO);
                break;
            }
        }//end for i

        parameters.setPreviewFormat(ImageFormat.NV21);
        mCamera.setParameters(parameters);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private int calculateFrameSize(int format) {
        return mPreviewSize.width * mPreviewSize.height * ImageFormat.getBitsPerPixel(format) / 8;
    }
}//end class
