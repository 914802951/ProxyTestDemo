package com.lz.proxytestdemo.sdlapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.CallSuper;

import com.lz.proxytestdemo.BuildConfig;
import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.exception.SdlException;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.SdlProxyALM;
import com.smartdevicelink.proxy.TTSChunkFactory;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
import com.smartdevicelink.proxy.interfaces.IProxyListenerALM;
import com.smartdevicelink.proxy.rpc.AddCommandResponse;
import com.smartdevicelink.proxy.rpc.AddSubMenuResponse;
import com.smartdevicelink.proxy.rpc.AlertManeuverResponse;
import com.smartdevicelink.proxy.rpc.AlertResponse;
import com.smartdevicelink.proxy.rpc.ButtonPressResponse;
import com.smartdevicelink.proxy.rpc.ChangeRegistrationResponse;
import com.smartdevicelink.proxy.rpc.CreateInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteCommandResponse;
import com.smartdevicelink.proxy.rpc.DeleteFileResponse;
import com.smartdevicelink.proxy.rpc.DeleteInteractionChoiceSetResponse;
import com.smartdevicelink.proxy.rpc.DeleteSubMenuResponse;
import com.smartdevicelink.proxy.rpc.DiagnosticMessageResponse;
import com.smartdevicelink.proxy.rpc.DialNumberResponse;
import com.smartdevicelink.proxy.rpc.EndAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.GenericResponse;
import com.smartdevicelink.proxy.rpc.GetDTCsResponse;
import com.smartdevicelink.proxy.rpc.GetInteriorVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetSystemCapabilityResponse;
import com.smartdevicelink.proxy.rpc.GetVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.GetWayPointsResponse;
import com.smartdevicelink.proxy.rpc.ListFilesResponse;
import com.smartdevicelink.proxy.rpc.OnAudioPassThru;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnButtonPress;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnDriverDistraction;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.OnHashChange;
import com.smartdevicelink.proxy.rpc.OnInteriorVehicleData;
import com.smartdevicelink.proxy.rpc.OnKeyboardInput;
import com.smartdevicelink.proxy.rpc.OnLanguageChange;
import com.smartdevicelink.proxy.rpc.OnLockScreenStatus;
import com.smartdevicelink.proxy.rpc.OnPermissionsChange;
import com.smartdevicelink.proxy.rpc.OnStreamRPC;
import com.smartdevicelink.proxy.rpc.OnSystemRequest;
import com.smartdevicelink.proxy.rpc.OnTBTClientState;
import com.smartdevicelink.proxy.rpc.OnTouchEvent;
import com.smartdevicelink.proxy.rpc.OnVehicleData;
import com.smartdevicelink.proxy.rpc.OnWayPointChange;
import com.smartdevicelink.proxy.rpc.PerformAudioPassThruResponse;
import com.smartdevicelink.proxy.rpc.PerformInteractionResponse;
import com.smartdevicelink.proxy.rpc.PutFile;
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SdlMsgVersion;
import com.smartdevicelink.proxy.rpc.SendHapticDataResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIcon;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetInteriorVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.TTSChunk;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.enums.AppHMIType;
import com.smartdevicelink.proxy.rpc.enums.FileType;
import com.smartdevicelink.proxy.rpc.enums.Language;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.SpeechCapabilities;
import com.smartdevicelink.proxy.rpc.listeners.OnRPCResponseListener;
import com.smartdevicelink.transport.BaseTransportConfig;
import com.smartdevicelink.transport.MultiplexTransportConfig;
import com.smartdevicelink.transport.enums.TransportType;
import com.smartdevicelink.util.CorrelationIdGenerator;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.Vector;

/**
 * Created by Administrator on 2017/11/15.
 */

public class SdlApp{
    private static final String TAG = LogHelper.makeLogTag(SdlApp.class.getSimpleName());

    public static final String APP_NAME = BuildConfig.APP_NAME;
    public static final String APP_ICON_NAME = BuildConfig.APP_NAME;
    public static final Integer APP_ID = BuildConfig.APP_ID;

    protected Context mContext;
    protected SdlProxyALM mSdlProxy;
    protected SdlAppProxyListener mProxyListener;
    protected boolean mForceConnect = false;

    protected Status mStatus = Status.DISCONNECT;
    protected boolean mIconSended = false;
    protected Integer mAppId;
    protected String mAppName;
    protected Integer mAppIconId;
    protected Boolean mIsMediaApp;
    protected AppHMIType mAppHMIType;
    protected BaseTransportConfig mTransportConfig;

    protected boolean mIsRestartApp = true;

    public static class Builder implements Serializable {

        private static Integer Counter = 0;

        private Context mContext;

        public SdlAppProxyListener mProxyListener;
        public boolean mForceConnect = false;
        public Integer mAppId;
        public String mAppName;
        public Integer mAppIconId;
        public BaseTransportConfig mTransportConfig;
        public boolean mIsMediaApp = true;
        public AppHMIType mAppHMIType = AppHMIType.MEDIA;

        public Builder(Context context){
            if(context == null){
                throw new NullPointerException();
            }

            mContext = context.getApplicationContext();
        }

        public SdlApp build(){
            return build(SdlApp.class, SdlAppProxyListener.class);
        }

        public <T extends SdlApp> T build(Class<? extends SdlApp> sdlAppClass, Class<? extends SdlAppProxyListener> listenerClass){
            T app;
            try {
                Constructor constructor = sdlAppClass.getDeclaredConstructor(Context.class);
                constructor.setAccessible(true);
                app = (T) constructor.newInstance(mContext);
                constructor.setAccessible(false);
                constructor = listenerClass.getConstructor(sdlAppClass);
                mProxyListener = (SdlAppProxyListener) constructor.newInstance(app);
            } catch (Exception e) {
                e.printStackTrace();
                LogHelper.e(TAG, "Unable to create Presentation Class");
                return null;
            }

            if(mAppId == null){
                mAppId = APP_ID + Counter;
            }
            if(mAppName == null){
                mAppName = APP_NAME + Counter;
            }
            if(mAppIconId == null){
                int resourcesIncluded = mContext.getResources().getIdentifier("ic_sdl", "drawable", mContext.getPackageName());
                if(resourcesIncluded != 0) {
                    mAppIconId = R.drawable.ic_sdl;
                }else{
                    mAppIconId = android.R.drawable.sym_def_app_icon;
                }
            }
            if(mTransportConfig == null){
                mTransportConfig = new MultiplexTransportConfig(mContext, mAppId.toString());
            }

            Counter++;

            app.mForceConnect = mForceConnect;
            app.mAppId = mAppId;
            app.mAppName = mAppName;
            app.mStatus = Status.CONNECTING;
            app.mProxyListener = mProxyListener;
            app.mAppIconId = mAppIconId;
            app.mTransportConfig = mTransportConfig;
            if(app.mIsMediaApp == null) {
                app.mIsMediaApp = mIsMediaApp;
            }
            if(app.mAppHMIType == null) {
                app.mAppHMIType = mAppHMIType;
            }
            app.initProxy();

            return app;
        }
    }

    public enum Status{
        DISCONNECT,
        CONNECTING,
        CONNECTED;
    }

    public Integer getAppIconId() {
        return mAppIconId;
    }

    public String getAppName() {
        return mAppName;
    }

    public Status getStatus(){
        return mStatus;
    }

    public BaseTransportConfig getTransportConfig(){
        return mTransportConfig;
    }

    public Context getAppContext(){
        return mContext;
    }

    @CallSuper
    public void releaseApp(){
        LogHelper.v(TAG, LogHelper._FUNC_() + " on thread: " + Thread.currentThread().getName());
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    releaseApp();
                    return null;
                }
            }.execute();
        } else {
            mIsRestartApp = false;
            disposeProxy();
        }
    }

    @CallSuper
    public void resetApp(){
        LogHelper.v(TAG, LogHelper._FUNC_() + " on thread: " + Thread.currentThread().getName());
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    resetApp();
                    return null;
                }
            }.execute();
        } else {
            resetProxy();
        }

    }

    protected SdlApp(Context context){
        if(context == null){
            throw new NullPointerException();
        }

        mContext = context;
    }

    @CallSuper
    protected void resetProxy(){
        LogHelper.v(TAG, LogHelper._FUNC_());
        disposeProxy();
        initProxy();
    }

    @CallSuper
    protected void initProxy() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    initProxy();
                    return null;
                }
            }.execute();
        } else {
            LogHelper.v(TAG, LogHelper._FUNC_());
            if (mSdlProxy == null) {
                try {
                    //Create a new mSdlProxy using Bluetooth transport
                    //The listener, app name,
                    //whether or not it is a media app and the applicationId are supplied.
//                mSdlProxy = new SdlProxyALM(mContext, mProxyListener, mAppName, true, mAppId.toString());
                    Vector<TTSChunk> chunks = new Vector<TTSChunk>();
                    TTSChunk ttsChunks = TTSChunkFactory.createChunk(SpeechCapabilities.TEXT, mAppName);
                    chunks.add(ttsChunks);
                    SdlMsgVersion SdlMsgVersion = new SdlMsgVersion();
                    SdlMsgVersion.setMajorVersion(2);
                    SdlMsgVersion.setMinorVersion(2);
                    Vector<AppHMIType> vrAppHMITypes = new Vector<AppHMIType>();
                    vrAppHMITypes.add(mAppHMIType);
                    mSdlProxy = new SdlProxyALM(mProxyListener,
						/*Sdl proxy configuration resources*/null,
						/*enable advanced lifecycle management true,*/
                            mAppName,
                            chunks,
						/*ngn media app*/null,
						/*vr synonyms*/null,
						/*is media app*/mIsMediaApp,
						/*SdlMsgVersion*/SdlMsgVersion,
						/*language desired*/Language.EN_US,
						/*HMI Display Language Desired*/Language.EN_US,
						/*AppHMIType*/ vrAppHMITypes,
						/*App ID*/mAppId.toString(),
						/*autoActivateID*/null,
						/*callbackToUIThre1ad*/ false,
						/*preRegister*/ false,
						/*app resuming*/ null,
                            mTransportConfig);
                } catch (SdlException e) {
                    //There was an error creating the mSdlProxy
                    if (mSdlProxy == null) {
                        //Stop the MultiSdlService
                        releaseApp();
                    }
                }
            } else if (mForceConnect) {
                mSdlProxy.forceOnConnected();
            }
        }
    }

    @CallSuper
    protected void disposeProxy() {
        LogHelper.v(TAG, LogHelper._FUNC_());
        resetStatus();
        if (mSdlProxy != null) {
            try {
                mSdlProxy.dispose();
            } catch (SdlException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }finally {
                mSdlProxy = null;
                //We will not receive any news when proxy disposed, so do it ourself
                mProxyListener.onDisposeProxy();
            }
        }
    }

    @CallSuper
    protected void resetStatus(){
        LogHelper.v(TAG, LogHelper._FUNC_());
        mIconSended = false;
        mStatus = Status.DISCONNECT;
    }

    protected void sendRpcMsg(RPCRequest rpc){
        if(mSdlProxy != null){
            try {
                mSdlProxy.sendRPCRequest(rpc);
            } catch (SdlException e) {
                e.printStackTrace();
            }
        }
    }

    protected void putIconFile(Integer iconId){
        LogHelper.v(TAG, LogHelper._FUNC_());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), iconId);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        byte[] array= out.toByteArray();

        PutFile msg = new PutFile();
        msg.setFileData(array);
        msg.setFileType(FileType.GRAPHIC_PNG);
        msg.setSdlFileName(APP_ICON_NAME);
        msg.setOnRPCResponseListener(new OnRPCResponseListener() {
            @Override
            public void onResponse(int correlationId, RPCResponse response) {
                LogHelper.d(TAG, "PutFile:", response.getSuccess(), "Info:", response.getInfo());
                if(response.getSuccess()){
                    setIcon(APP_ICON_NAME);
                }else{

                }
            }
        });

        sendRpcMsg(msg);
    }

    protected void setIcon(String iconName){
        SetAppIcon msg = new SetAppIcon();
        msg.setSdlFileName(iconName);
        msg.setOnRPCResponseListener(new OnRPCResponseListener() {
            @Override
            public void onResponse(int correlationId, RPCResponse response) {
                LogHelper.d(TAG, "SetAppIcon:", response.getSuccess(), "Info:", response.getInfo());
            }
        });

        sendRpcMsg(msg);
    }

    protected void show(String mainField1, String mainField2, String mediaTrack){
        Show msg = new Show();
        msg.setCorrelationID(CorrelationIdGenerator.generateId());
        msg.setMainField1(mainField1);
        msg.setMainField2(mainField2);
        msg.setMediaTrack(mediaTrack);

        sendRpcMsg(msg);
    }

    public class SdlAppProxyListener extends MyProxyListenerALM{

        public void onFirstRun(OnHMIStatus notification){
            show("Base Sdl App", "Show", "MediaTrack");
        }

        public void onDisposeProxy(){

        }

        @CallSuper
        @Override
        public void onOnHMIStatus(OnHMIStatus notification) {
            LogHelper.v(TAG, LogHelper._FUNC_());
            LogHelper.v(TAG, "OnHMIStatus first run: " + notification.getHmiLevel() + ", " + notification.getFirstRun());

            mStatus = Status.CONNECTED;

            if(!mIconSended){
                mIconSended = true;
                putIconFile(mAppIconId);
            }

            switch (notification.getSystemContext()) {
                case SYSCTXT_MAIN:
                    break;
                case SYSCTXT_VRSESSION:
                    break;
                case SYSCTXT_MENU:
                    break;
                default:
                    return;
            }

            switch (notification.getAudioStreamingState()) {
                case AUDIBLE:
                    break;
                case NOT_AUDIBLE:
                    break;
                case ATTENUATED:
                    break;
                default:
                    return;
            }

            switch (notification.getHmiLevel()) {
                case HMI_FULL:
                    if (notification.getFirstRun()) {
                        try {
                            onFirstRun(notification);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case HMI_LIMITED:
                    break;
                case HMI_BACKGROUND:
                    break;
                case HMI_NONE:
                    break;
                default:
                    return;
            }
        }

        @CallSuper
        @Override
        public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
            LogHelper.v(TAG, LogHelper._FUNC_());
//            LogHelper.e(TAG, e, info);
            if(!mIsRestartApp || mTransportConfig.getTransportType().equals(TransportType.MULTIPLEX)) {
                releaseApp();
            }else{
                if(reason.equals(SdlDisconnectedReason.BLUETOOTH_DISABLED)){
                    LogHelper.w(TAG, "Proxy closed with bluetooth disabled, do not reset app");
                    disposeProxy();
                }else if(reason.equals(SdlDisconnectedReason.BLUETOOTH_ADAPTER_ERROR)) {
                    LogHelper.w(TAG, "Proxy closed with bluetooth adapter error, do not reset app");
                    disposeProxy();
                }else if(reason.equals(SdlDisconnectedReason.GENERIC_ERROR)) {
                    LogHelper.w(TAG, "Cycling the proxy failed.");
                    resetApp();
                } else {
                    LogHelper.w(TAG, "Proxy closed reason: " + reason);
                    resetApp();
                }
            }
        }

        @CallSuper
        @Override
        public void onError(String info, Exception e) {
            LogHelper.v(TAG, LogHelper._FUNC_());
            LogHelper.e(TAG, e, info);
        }
    }
}

class MyProxyListenerALM implements IProxyListenerALM {
    @Override
    public void onOnHMIStatus(OnHMIStatus notification) {
    }

    @Override
    public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
    }

    @Override
    public void onServiceEnded(OnServiceEnded serviceEnded) {

    }

    @Override
    public void onServiceNACKed(OnServiceNACKed serviceNACKed) {

    }

    @Override
    public void onOnStreamRPC(OnStreamRPC notification) {

    }

    @Override
    public void onStreamRPCResponse(StreamRPCResponse response) {

    }

    @Override
    public void onError(String info, Exception e) {

    }

    @Override
    public void onGenericResponse(GenericResponse response) {

    }

    @Override
    public void onOnCommand(OnCommand notification) {

    }

    @Override
    public void onAddCommandResponse(AddCommandResponse response) {

    }

    @Override
    public void onAddSubMenuResponse(AddSubMenuResponse response) {

    }

    @Override
    public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {

    }

    @Override
    public void onAlertResponse(AlertResponse response) {

    }

    @Override
    public void onDeleteCommandResponse(DeleteCommandResponse response) {

    }

    @Override
    public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {

    }

    @Override
    public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {

    }

    @Override
    public void onPerformInteractionResponse(PerformInteractionResponse response) {

    }

    @Override
    public void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse response) {

    }

    @Override
    public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {

    }

    @Override
    public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {

    }

    @Override
    public void onShowResponse(ShowResponse response) {

    }

    @Override
    public void onSpeakResponse(SpeakResponse response) {

    }

    @Override
    public void onOnButtonEvent(OnButtonEvent notification) {

    }

    @Override
    public void onOnButtonPress(OnButtonPress notification) {

    }

    @Override
    public void onSubscribeButtonResponse(SubscribeButtonResponse response) {

    }

    @Override
    public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {

    }

    @Override
    public void onOnPermissionsChange(OnPermissionsChange notification) {

    }

    @Override
    public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {

    }

    @Override
    public void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse response) {

    }

    @Override
    public void onGetVehicleDataResponse(GetVehicleDataResponse response) {

    }

    @Override
    public void onOnVehicleData(OnVehicleData notification) {

    }

    @Override
    public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {

    }

    @Override
    public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {

    }

    @Override
    public void onOnAudioPassThru(OnAudioPassThru notification) {

    }

    @Override
    public void onPutFileResponse(PutFileResponse response) {

    }

    @Override
    public void onDeleteFileResponse(DeleteFileResponse response) {

    }

    @Override
    public void onListFilesResponse(ListFilesResponse response) {

    }

    @Override
    public void onSetAppIconResponse(SetAppIconResponse response) {

    }

    @Override
    public void onScrollableMessageResponse(ScrollableMessageResponse response) {

    }

    @Override
    public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {

    }

    @Override
    public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {

    }

    @Override
    public void onOnLanguageChange(OnLanguageChange notification) {

    }

    @Override
    public void onOnHashChange(OnHashChange notification) {

    }

    @Override
    public void onSliderResponse(SliderResponse response) {

    }

    @Override
    public void onOnDriverDistraction(OnDriverDistraction notification) {

    }

    @Override
    public void onOnTBTClientState(OnTBTClientState notification) {

    }

    @Override
    public void onOnSystemRequest(OnSystemRequest notification) {

    }

    @Override
    public void onSystemRequestResponse(SystemRequestResponse response) {

    }

    @Override
    public void onOnKeyboardInput(OnKeyboardInput notification) {

    }

    @Override
    public void onOnTouchEvent(OnTouchEvent notification) {

    }

    @Override
    public void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {

    }

    @Override
    public void onReadDIDResponse(ReadDIDResponse response) {

    }

    @Override
    public void onGetDTCsResponse(GetDTCsResponse response) {

    }

    @Override
    public void onOnLockScreenNotification(OnLockScreenStatus notification) {

    }

    @Override
    public void onDialNumberResponse(DialNumberResponse response) {

    }

    @Override
    public void onSendLocationResponse(SendLocationResponse response) {

    }

    @Override
    public void onShowConstantTbtResponse(ShowConstantTbtResponse response) {

    }

    @Override
    public void onAlertManeuverResponse(AlertManeuverResponse response) {

    }

    @Override
    public void onUpdateTurnListResponse(UpdateTurnListResponse response) {

    }

    @Override
    public void onServiceDataACK(int dataSize) {

    }

    @Override
    public void onGetWayPointsResponse(GetWayPointsResponse response) {

    }

    @Override
    public void onSubscribeWayPointsResponse(SubscribeWayPointsResponse response) {

    }

    @Override
    public void onUnsubscribeWayPointsResponse(UnsubscribeWayPointsResponse response) {

    }

    @Override
    public void onOnWayPointChange(OnWayPointChange notification) {

    }

    @Override
    public void onGetSystemCapabilityResponse(GetSystemCapabilityResponse response) {

    }

    @Override
    public void onGetInteriorVehicleDataResponse(GetInteriorVehicleDataResponse response) {

    }

    @Override
    public void onButtonPressResponse(ButtonPressResponse response) {

    }

    @Override
    public void onSetInteriorVehicleDataResponse(SetInteriorVehicleDataResponse response) {

    }

    @Override
    public void onOnInteriorVehicleData(OnInteriorVehicleData notification) {

    }

    @Override
    public void onSendHapticDataResponse(SendHapticDataResponse response) {

    }

}
