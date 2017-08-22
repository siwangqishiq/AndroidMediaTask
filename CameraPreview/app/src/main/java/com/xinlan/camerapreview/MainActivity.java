package com.xinlan.camerapreview;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements
        TextureView.SurfaceTextureListener,View.OnClickListener {
    public static final int PERMISSON_CAMERA = 13;
    private TextureView mTextureView;

    private ImageView mImageView;
    private View mTakeBtn;

    private Camera mCamera;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextureView = (TextureView) findViewById(R.id.texture_view);
        mImageView = (ImageView) findViewById(R.id.image);
        mTakeBtn = findViewById(R.id.btn);
        mTakeBtn.setOnClickListener(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA},PERMISSON_CAMERA);
        }

        mTextureView.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();

        try {
            mCamera.setPreviewTexture(surface);
            mCamera.setDisplayOrientation(90);
            mCamera.startPreview();
//            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
//                @Override
//                public void onPreviewFrame(byte[] data, Camera camera) {
//
//                }
//            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        //System.out.println("mCamera release!");
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        //System.out.println("onSurfaceTextureUpdated!");
    }

    @Override
    public void onClick(View v) {
        mImageView.setImageBitmap(mTextureView.getBitmap());
    }
}//end class
