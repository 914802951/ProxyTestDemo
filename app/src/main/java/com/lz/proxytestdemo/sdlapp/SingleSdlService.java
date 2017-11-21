package com.lz.proxytestdemo.sdlapp;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.util.LogHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SingleSdlService extends Service {

    private static final String TAG = LogHelper.makeLogTag(SingleSdlService.class.getSimpleName());
    private static final int FOREGROUND_SERVICE_ID = Integer.MAX_VALUE / 9;

    private static List<SdlApp> SdlAppList = new ArrayList<>();
    private boolean isForeground;

    public class ServiceBinder extends Binder {
        public SingleSdlService getService(){
            return SingleSdlService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.v(TAG, LogHelper._FUNC_());
        return new ServiceBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogHelper.v(TAG, LogHelper._FUNC_());
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogHelper.v(TAG, LogHelper._FUNC_());

        enterForeground();
        //use START_STICKY because we want the SDLService to be explicitly started and stopped as needed.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.v(TAG, LogHelper._FUNC_());
        super.onDestroy();
        for (SdlApp app : SdlAppList){
            app.releaseApp();
        }
    }

    public static List<SdlApp> getSdlAppList(){
        return SdlAppList;
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
