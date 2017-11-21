package com.lz.proxytestdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.sdlapp.SdlReceiver;
import com.lz.proxytestdemo.util.LogHelper;

/**
 * Created by Administrator on 2017/11/21.
 */

public class MultiMainActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(MultiMainActivity.class.getSimpleName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, LogHelper._FUNC_());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_main);

        //If we are connected to a module we want to start our MultiSdlService
        SdlReceiver.queryForConnectedService(this);
    }
}
