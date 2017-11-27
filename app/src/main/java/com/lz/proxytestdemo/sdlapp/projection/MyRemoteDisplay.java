package com.lz.proxytestdemo.sdlapp.projection;

import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.SurfaceView;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.streaming.video.SdlRemoteDisplay;

public class MyRemoteDisplay extends SdlRemoteDisplay {

    private static final String TAG = LogHelper.makeLogTag(MyRemoteDisplay.class.getSimpleName());

    private SurfaceView mCameraSv;
    private TextView mCameraTv;

    public MyRemoteDisplay(Context context, Display display) {
        super(context, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.remote_display);

        initView();
    }

    private void initView(){
        mCameraSv = (SurfaceView) findViewById(R.id.camera_sv);
        mCameraTv = (TextView) findViewById(R.id.camera_tv);

        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_PARENT, 1,
                Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_PARENT, 0,
                Animation.RELATIVE_TO_PARENT, 0);
        animation.setRepeatCount(ValueAnimator.INFINITE);
        animation.setRepeatMode(ValueAnimator.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        animation.setDuration(15000);
        mCameraTv.startAnimation(animation);
    }
}
