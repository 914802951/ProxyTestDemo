package com.lz.proxytestdemo.sdlapp.navigation;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import com.lz.proxytestdemo.sdlapp.LogSdlApp;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.enums.AppHMIType;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;

import java.io.IOException;

/**
 * Created by Administrator on 2017/11/27.
 */

public class NavigationSdlApp extends LogSdlApp {

    private static final String TAG = LogHelper.makeLogTag(NavigationSdlApp.class.getSimpleName());

    private int mFPS = 30;
    private int mWidth = 800;
    private int mHeight = 480;
    private int mBitrate = 1 * 1000 * 1000;
    private int mKeyFrameInterval = 5;
    private boolean isEncryptedVideo = false;

    private MockNavigation mNavigation = new MockNavigation(getAppContext(), mFPS, mWidth, mHeight, mBitrate, isEncryptedVideo);

    protected NavigationSdlApp(Context context) {
        super(context);
        mIsMediaApp = false;
        mAppHMIType = AppHMIType.NAVIGATION;
    }

    public class NavigationSdlAppProxyListener extends LogSdlAppProxyListener {

        @Override
        public void onFirstRun(OnHMIStatus notification) {
            show("Navigation Sdl App", "Show", "MediaTrack");
            mNavigation.start();
        }

        @Override
        public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
            super.onProxyClosed(info, e, reason);
            mNavigation.stop();
        }

    }

    class MockNavigation implements Runnable {
        /**
         * Record video from the camera preview and encode it as an MP4 file.
         * Demonstrates the use of MediaCodec with Camera input. Does not record
         * audio.
         * <p>
         * Generally speaking, it's better to use MediaRecorder for this sort of
         * thing. This example demonstrates one possible advantage: editing of video
         * as it's being encoded. A GLES 2.0 fragment shader is used to perform a
         * silly color tweak every 15 frames.
         * <p>
         * This uses various features first available in Android "Jellybean" 4.3
         * (API 18). There is no equivalent functionality in previous releases. (You
         * can send the Camera preview to a byte buffer with a fully-specified
         * format, but MediaCodec encoders want different input formats on different
         * devices, and this use case wasn't well exercised in CTS pre-4.3.)
         */

        private final String TAG = LogHelper.makeLogTag(MockNavigation.class.getSimpleName());
        private static final boolean VERBOSE = false; // lots of logging

        // parameters for the encoder
        private static final int IFRAME_INTERVAL = 5;

        // encoder state
        private InputSurface mInputSurface;

        private Thread mThread;
        // camera state
        private Camera mCamera;
        private int mFPS;
        private int mWidth;
        private int mHeight;
        private int mBitrate;
        private int mKeyFrameInterval;
        private boolean isEncryptedVideo;
        SdlProxyALM proxy;
        Context mContext;

        private boolean mIsStartStream = false;

        MockNavigation(Context context, int fps, int width, int height, int bitrate, int keyFrameInterval, boolean isEncryptedVideo) {
            mFPS = fps;
            mWidth = width;
            mHeight = height;
            mBitrate = bitrate;
            mKeyFrameInterval = keyFrameInterval;
            this.isEncryptedVideo = isEncryptedVideo;
            this.mContext = context;
        }

        MockNavigation(Context context, int fps, int width, int height, int bitrate, boolean isEncryptedVideo) {
            this(context, fps, width, height, bitrate, IFRAME_INTERVAL, isEncryptedVideo);
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
        }

        @Override
        public void run() {
            try {
                this.encodeCameraToMpeg(mFPS, mWidth, mHeight, mBitrate, mKeyFrameInterval);
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }

        /**
         * Tests encoding of AVC video from Camera input. The output is saved as an
         * MP4 file.
         */
        private void encodeCameraToMpeg(int fps, int width, int height, int bitrate, int keyFrameInterval) {
            LogHelper.d(TAG, "Output " + width + "x" + height + " @" + bitrate);

            try {
                prepareCamera(width, height);
                Surface surface;
                if(mSdlProxy != null) {
                    proxy = mSdlProxy;
                    surface = proxy.createOpenGLInputSurface(fps,
                            keyFrameInterval, width, height, bitrate, isEncryptedVideo);
                    if (surface != null) {
                        mIsStartStream = true;
                        mInputSurface = new InputSurface(surface, mContext);
                        mInputSurface.surfaceChanged(width, height);
                        proxy.startEncoder();
                    }else{
                        return;
                    }
                }

                TextureView tv = mInputSurface.getTextureView();
                SurfaceTexture st = tv.getSurfaceTexture();
                try {
                    mCamera.setPreviewTexture(st);
                } catch (IOException ioe) {
                    throw new RuntimeException("setPreviewTexture failed", ioe);
                }

                // mMediaPlayer.start();

                mCamera.startPreview();

                long startWhen = System.nanoTime();

                while (!Thread.interrupted()) {
                    proxy.drainEncoder(false);

                    // Acquire a new frame of input, and render it to the Surface.
                    // If we had a
                    // GLSurfaceView we could switch EGL contexts and call
                    // drawImage() a second
                    // time to render it on screen. The texture can be shared
                    // between contexts by
                    // passing the GLSurfaceView's EGLContext as
                    // eglCreateContext()'s share_context
                    // argument.

                    mInputSurface.awaitNewImage();
                    mInputSurface.drawImage();

                    if (VERBOSE) {
                        Log.d(TAG, "present: "
                                + ((st.getTimestamp() - startWhen) / 1000000.0)
                                + "ms");
                    }
                    mInputSurface.setPresentationTime(st.getTimestamp());

                    // Submit it to the encoder. The eglSwapBuffers call will block
                    // if the input
                    // is full, which would be bad if it stayed full until we
                    // dequeued an output
                    // buffer (which we can't do, since we're stuck here). So long
                    // as we fully drain
                    // the encoder before supplying additional input, the system
                    // guarantees that we
                    // can supply another frame without blocking.
                    if (VERBOSE)
                        Log.d(TAG, "sending frame to encoder");
                    mInputSurface.swapBuffers();
                }
            } catch (Exception ex) {
                addLogData(new LogDataBean(ex));
            } finally {
                releaseCamera();
                releaseEncoder();
                releaseSurfaceTexture();
                endStream();
            }
        }

        /**
         * Configures Camera for video capture. Sets mCamera.
         * <p>
         * Opens a Camera and sets parameters. Does not start preview.
         */
        private void prepareCamera(int encWidth, int encHeight) {
            if (mCamera != null) {
                throw new RuntimeException("camera already initialized");
            }

            mCamera = Camera.open(0);

            if (mCamera == null) {
                throw new RuntimeException("Unable to open camera");
            }

            Camera.Parameters parms = mCamera.getParameters();

            choosePreviewSize(parms, encWidth, encHeight);
            // leave the frame rate set to default
            mCamera.setParameters(parms);

            Camera.Size size = parms.getPreviewSize();
            Log.d(TAG, "Camera preview size is " + size.width + "x" + size.height);
        }

        /**
         * Attempts to find a preview size that matches the provided width and
         * height (which specify the dimensions of the encoded video). If it fails
         * to find a match it just uses the default preview size.
         */
        private void choosePreviewSize(Camera.Parameters parms, int width,
                                              int height) {
            // We should make sure that the requested MPEG size is less than the
            // preferred
            // size, and has the same aspect ratio.
            Camera.Size ppsfv = parms.getPreferredPreviewSizeForVideo();
            if (VERBOSE && ppsfv != null) {
                LogHelper.d(TAG, "Camera preferred preview size for video is "
                        + ppsfv.width + "x" + ppsfv.height);
            }

            for (Camera.Size size : parms.getSupportedPreviewSizes()) {
                if (size.width == width && size.height == height) {
                    parms.setPreviewSize(width, height);
                    return;
                }
            }

            Log.w(TAG, "Unable to set preview size to " + width + "x" + height);
            if (ppsfv != null) {
                parms.setPreviewSize(ppsfv.width, ppsfv.height);
            }
        }

        private void endStream(){
            if(mSdlProxy != null) {
                proxy = mSdlProxy;
                if (mIsStartStream) {
                    proxy.endH264();
                }
            }
        }

        /**
         * Stops camera preview, and releases the camera to the system.
         */
        private void releaseCamera() {
            if (VERBOSE)
                LogHelper.d(TAG, "releasing camera");
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }

        /**
         * Releases the SurfaceTexture.
         */
        private void releaseSurfaceTexture() {
            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
        }

        /**
         * Releases encoder resources.
         */
        private void releaseEncoder() {
            if (VERBOSE)
                Log.d(TAG, "releasing encoder objects");
            if(mSdlProxy != null) {
                proxy = mSdlProxy;
                proxy.releaseEncoder();
            }
            if (mInputSurface != null) {
                mInputSurface.release();
                mInputSurface = null;
            }
        }

        public void onTouchEvent (OnTouchEvent notification) {
            if (mInputSurface != null) {
                mInputSurface.onTouchEvent(notification);
            }
        }
    }
}
