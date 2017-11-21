package com.lz.proxytestdemo.sdlapp;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.transport.TransportConstants;

/**
 * Created by Administrator on 2017/11/15.
 */

public class MultiSdlService extends Service {

    private static final String TAG = LogHelper.makeLogTag(MultiSdlService.class.getSimpleName());
    private static final int FOREGROUND_SERVICE_ID = Integer.MAX_VALUE / 9;

    public static SdlApp mApp;

    private boolean isForeground;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.v(TAG, LogHelper._FUNC_());
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogHelper.v(TAG, LogHelper._FUNC_());
        boolean forceConnect = intent !=null && intent.getBooleanExtra(TransportConstants.FORCE_TRANSPORT_CONNECTED, false);

        if (mApp == null){
            LogHelper.d(TAG, "SdlApp is null");
        }else{
            LogHelper.d(TAG, "SdlApp status:", mApp.getStatus());
        }

        if (mApp == null){
            SdlApp.Builder builder = new SdlApp.Builder(this);
            builder.mForceConnect = forceConnect;
            mApp = builder.build(LogSdlApp.class, LogSdlApp.LogSdlAppProxyListener.class);
        }else if (mApp.getStatus() == SdlApp.Status.DISCONNECT){
            mApp.resetApp();
        }

        enterForeground();
        //use START_STICKY because we want the SDLService to be explicitly started and stopped as needed.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, LogHelper._FUNC_());
        super.onDestroy();
        if (mApp != null){
            mApp.releaseApp();
            mApp = null;
        }
    }

    private void enterForeground() {
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getPackageName());
        builder.setContentText(getPackageName());

        builder.setLargeIcon(icon);
        builder.setOngoing(true);

        Notification notification;
//        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O) {
//            //Now we need to add a notification channel
//            NotificationManager notificationManager =(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            String channelId = SDL_NOTIFICATION_CHANNEL_ID;
//            CharSequence channelName = SDL_NOTIFICATION_CHANNEL_NAME;
//            int importance = NotificationManager.IMPORTANCE_DEFAULT;
//            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
//            notificationChannel.enableLights(false);
//            notificationChannel.enableVibration(false);
//            notificationManager.createNotificationChannel(notificationChannel);
//            builder.setChannelId(channelId);
//
//        }
        notification = builder.build();
        if(notification == null){
            LogHelper.e(TAG, "Notification was null");
            return;
        }
        startForeground(FOREGROUND_SERVICE_ID, notification);
        isForeground = true;
    }

    private void exitForeground(){
        if(isForeground){
//            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.deleteNotificationChannel(TransportConstants.SDL_NOTIFICATION_CHANNEL_ID);
//            }

            this.stopForeground(true);
        }
    }
}
