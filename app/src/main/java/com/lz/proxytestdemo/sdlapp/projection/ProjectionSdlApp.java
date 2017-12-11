package com.lz.proxytestdemo.sdlapp.projection;

import android.content.Context;
import android.widget.Toast;

import com.lz.proxytestdemo.sdlapp.LogSdlApp;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.protocol.enums.SessionType;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.rpc.ImageResolution;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.enums.AppHMIType;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.streaming.video.SdlRemoteDisplay;
import com.smartdevicelink.streaming.video.VideoStreamingParameters;

/**
 * Created by Administrator on 2017/11/27.
 */

public class ProjectionSdlApp extends LogSdlApp {

    private static final String TAG = LogHelper.makeLogTag(ProjectionSdlApp.class.getSimpleName());

    private int mFPS = 30;
    private int mWidth = 800;
    private int mHeight = 480;
    private int mBitrate = 1 * 1000 * 1000;
    private int mKeyFrameInterval = 5;
    private boolean isEncryptedVideo = false;
    private MockProjection mProjection;

    protected ProjectionSdlApp(Context context) {
        super(context);

        mIsMediaApp = false;
//        mAppHMIType = AppHMIType.PROJECTION;
        //AppHMIType.PROJECTION may be not supported
        mAppHMIType = AppHMIType.NAVIGATION;

        VideoStreamingParameters desired = new VideoStreamingParameters();
        desired.setFrameRate(mFPS);
        desired.setInterval(mKeyFrameInterval);
        desired.setBitrate(mBitrate);
        ImageResolution resolution = new ImageResolution();
        resolution.setResolutionWidth(mWidth);
        resolution.setResolutionHeight(mHeight);
        desired.setResolution(resolution);

        mProjection = new MockProjection(
                getAppContext(),
                MyRemoteDisplay.class,
                desired,
                isEncryptedVideo);
    }

    public class ProjectionSdlAppProxyListener extends LogSdlAppProxyListener {

        @Override
        public void onFirstRun(OnHMIStatus notification) {
            show("Projection Sdl App", "Show", "MediaTrack");
            mProjection.start();
        }

        @Override
        public void onDisposeProxy(){
            mProjection.stop();
        }

        @Override
        public void onOnHMIStatus(OnHMIStatus notification) {
            super.onOnHMIStatus(notification);

            switch (notification.getHmiLevel()) {
                case HMI_FULL:
                    mProjection.start();
                    mSdlProxy.resumeVideoStream();
                    break;
                case HMI_LIMITED:
                    mSdlProxy.pauseVideoStream();
                    break;
                case HMI_BACKGROUND:
                    mSdlProxy.pauseVideoStream();
                    break;
                case HMI_NONE:
                    mProjection.stop();
                    break;
                default:
                    return;
            }
            showToast(getAppContext(),
                    getAppName() + " " + notification.getHmiLevel().name(),
                    Toast.LENGTH_SHORT);
        }

        @Override
        public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
            super.onProxyClosed(info, e, reason);
            mProjection.stop();
        }

        @Override
        public void onServiceEnded(OnServiceEnded serviceEnded) {
            super.onServiceEnded(serviceEnded);
            if(serviceEnded.getSessionType().equals(SessionType.NAV)
                    || serviceEnded.getSessionType().equals(SessionType.RPC)) {
                mProjection.stop();
            }
        }

    }

    class MockProjection implements Runnable {

        private final String TAG = LogHelper.makeLogTag(MockProjection.class.getSimpleName());

        private Context mContext;
        private Class<? extends SdlRemoteDisplay> mRemoteDisplay;
        private VideoStreamingParameters mParameters;
        private boolean mEncrypted;
        private Thread mThread;

        public MockProjection(Context context, Class<? extends SdlRemoteDisplay> remoteDisplay){
            this(context, remoteDisplay, new VideoStreamingParameters(), false);
        }

        public MockProjection(Context context, Class<? extends SdlRemoteDisplay> remoteDisplay, VideoStreamingParameters parameters, boolean encrypted){
            mContext = context;
            mRemoteDisplay = remoteDisplay;
            mParameters = parameters;
            mEncrypted = encrypted;
        }

        public void start(){
            if(mThread == null){
                mThread = new Thread(this);
                mThread.start();
            }
        }

        public void stop(){
            if(mThread != null){
                mThread.interrupt();
                mThread = null;
            }

            if(mSdlProxy == null) {
                return;
            }else {
                SdlProxyALM proxy = mSdlProxy;
                proxy.stopRemoteDisplayStream();
                proxy.endH264();
            }
        }

        @Override
        public void run() {
            if(mSdlProxy == null) {
                LogHelper.e("Proxy is not start");
                addLogData(new LogDataBean(new Exception("Proxy is not start")));
                return;
            }else {
                SdlProxyALM proxy = mSdlProxy;
                proxy.startRemoteDisplayStream(mContext, mRemoteDisplay, mParameters, mEncrypted);
            }
        }
    }
}
