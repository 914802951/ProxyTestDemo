package com.lz.proxytestdemo.broadcast;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.lz.proxytestdemo.sdlapp.SdlApp;
import com.lz.proxytestdemo.sdlapp.SingleSdlService;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.transport.enums.TransportType;

import java.util.List;

/**
 * Created by Administrator on 2017/12/5.
 */

public class StaticBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LogHelper.makeLogTag(StaticBroadcastReceiver.class.getSimpleName());

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    LogHelper.d(TAG, "Bluetooth adapter off");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    LogHelper.d(TAG, "Bluetooth adapter turning off");
                    break;
                case BluetoothAdapter.STATE_ON:
                    LogHelper.d(TAG, "Bluetooth adapter on");
                    resetBtApp();
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    LogHelper.d(TAG, "Bluetooth adapter turning on");
                    break;
                default:
                    LogHelper.d(TAG, "Bluetooth adapter " + state);
                    break;
            }
        }
    }

    private void resetBtApp(){
        List<SdlApp> appList = SingleSdlService.getSdlAppList();
        for (SdlApp app : appList){
            if (app.getTransportConfig().getTransportType().equals(TransportType.BLUETOOTH)
                    && !app.getStatus().equals(SdlApp.Status.CONNECTED)){
                app.resetApp();
            }
        }
    }
}
