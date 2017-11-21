package com.lz.proxytestdemo.sdlapp;

import android.content.Context;
import android.content.Intent;

import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.transport.SdlBroadcastReceiver;
import com.smartdevicelink.transport.SdlRouterService;

/**
 * Created by Administrator on 2017/11/14.
 */

public class SdlReceiver extends SdlBroadcastReceiver{

    private static final String TAG = LogHelper.makeLogTag(SdlReceiver.class.getSimpleName());

    @Override
    public Class<? extends SdlRouterService> defineLocalSdlRouterClass() {
        //Return a local copy of the SdlRouterService located in your project.
        return com.lz.proxytestdemo.sdlapp.SdlRouterService.class;
    }

    @Override
    public void onSdlEnabled(Context context, Intent intent) {
        //Use the provided intent but set the class to the MultiSdlService

        LogHelper.d(TAG, "onSdlEnabled");

        intent.setClass(context, MultiSdlService.class);
        context.startService(intent);
    }
}
