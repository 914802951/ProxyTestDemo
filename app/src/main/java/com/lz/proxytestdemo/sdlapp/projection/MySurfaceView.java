package com.lz.proxytestdemo.sdlapp.projection;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.lz.proxytestdemo.util.LogHelper;

import java.io.IOException;

public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = LogHelper.makeLogTag(MySurfaceView.class.getSimpleName());

    private static SurfaceHolder mHolder;
    private Camera mCamera;

    public MySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        if (mCamera == null) {
            mCamera = Camera.open();
            try {
                mCamera.setPreviewDisplay(mHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

}