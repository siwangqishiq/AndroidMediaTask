package com.xinlan.gldraw.nat;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by panyi on 2017/8/28.
 */
public class NativeOpenGlActivity extends AppCompatActivity {
    private GLSurfaceView mRenderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRenderView = new RenderSurface(this);
        setContentView(mRenderView);
    }


    @Override
    protected void onPause() {
        super.onPause();
        mRenderView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRenderView.onResume();
    }
}//end class
