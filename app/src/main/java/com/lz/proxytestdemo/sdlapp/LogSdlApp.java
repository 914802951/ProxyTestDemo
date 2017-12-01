package com.lz.proxytestdemo.sdlapp;

import android.content.Context;
import android.support.annotation.CallSuper;

import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.callbacks.OnServiceEnded;
import com.smartdevicelink.proxy.callbacks.OnServiceNACKed;
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
import com.smartdevicelink.proxy.rpc.PutFileResponse;
import com.smartdevicelink.proxy.rpc.ReadDIDResponse;
import com.smartdevicelink.proxy.rpc.ResetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.ScrollableMessageResponse;
import com.smartdevicelink.proxy.rpc.SendHapticDataResponse;
import com.smartdevicelink.proxy.rpc.SendLocationResponse;
import com.smartdevicelink.proxy.rpc.SetAppIconResponse;
import com.smartdevicelink.proxy.rpc.SetDisplayLayoutResponse;
import com.smartdevicelink.proxy.rpc.SetGlobalPropertiesResponse;
import com.smartdevicelink.proxy.rpc.SetInteriorVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimerResponse;
import com.smartdevicelink.proxy.rpc.ShowConstantTbtResponse;
import com.smartdevicelink.proxy.rpc.ShowResponse;
import com.smartdevicelink.proxy.rpc.SliderResponse;
import com.smartdevicelink.proxy.rpc.SpeakResponse;
import com.smartdevicelink.proxy.rpc.StreamRPCResponse;
import com.smartdevicelink.proxy.rpc.SubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.SubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.SubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.SystemRequestResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeButtonResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeVehicleDataResponse;
import com.smartdevicelink.proxy.rpc.UnsubscribeWayPointsResponse;
import com.smartdevicelink.proxy.rpc.UpdateTurnListResponse;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/20.
 */

public class LogSdlApp extends SdlApp {

    private static final String TAG = LogHelper.makeLogTag(LogSdlApp.class.getSimpleName());
    private static final int MAX_LOG_LIST_SIZE = 1000;

    public interface OnDataChangedListener{
        void onDataChanged(LogDataBean data);
    }

    private List<LogDataBean> mLogList = Collections.synchronizedList(new LinkedList<LogDataBean>());
    private LinkedList<WeakReference<OnDataChangedListener>> mLogListenerSet = new LinkedList<>();

    protected LogSdlApp(Context context) {
        super(context);
    }

    @Override
    protected void sendRpcMsg(RPCRequest rpc){
        super.sendRpcMsg(rpc);
        addLogData(new LogDataBean(rpc));
    }

    public List<LogDataBean> getLogList(){
        List<LogDataBean> list = new ArrayList<>();
        list.addAll(mLogList);
        return list;
    }

    public void addLogData(LogDataBean data){
        while (mLogList.size() >= MAX_LOG_LIST_SIZE){
            mLogList.remove(0);
        }
        mLogList.add(data);

        synchronized (mLogListenerSet) {
            Iterator<WeakReference<OnDataChangedListener>> it = mLogListenerSet.iterator();
            while (it.hasNext()) {
                WeakReference<OnDataChangedListener> l = it.next();
                if (l.get() != null) {
                    l.get().onDataChanged(data);
                } else {
                    it.remove();
                }
            }
        }
    }

    public void addOnDataChangedListener(OnDataChangedListener listener){
        synchronized (mLogListenerSet) {
            Iterator<WeakReference<OnDataChangedListener>> it = mLogListenerSet.iterator();
            while (it.hasNext()) {
                OnDataChangedListener l = it.next().get();
                if(l != null && equals(listener)){
                    return;
                }
            }
            mLogListenerSet.add(new WeakReference<>(listener));
        }
    }

    public String serializeJSON(RPCMessage msg) {
        if (mSdlProxy != null){
            return mSdlProxy.serializeJSON(msg);
        }
        return null;
    }

    public static class LogDataBean{

        private RPCMessage mRpcMessage;
        private Throwable mThrowable;
        private Object[] mObjects;
        Date mDate;

        public LogDataBean(Object... objects){
            this(null, null, objects);
        }

        public LogDataBean(RPCMessage msg, Object... objects){
            this(null, msg, objects);
        }

        public LogDataBean(Throwable throwable, Object... objects){
            this(throwable, null, objects);
        }

        public LogDataBean(Throwable throwable, RPCMessage msg, Object... objects){
            mRpcMessage = msg;
            mThrowable = throwable;
            mObjects = objects;
            mDate = new Date();
        }

        public Throwable getThrowable() {
            return mThrowable;
        }

        public RPCMessage getRpcMessage(){
            return mRpcMessage;
        }

        public Object[] getObjects() {
            return mObjects;
        }

        public Date getDate(){
            return mDate;
        }
    }

    public class LogSdlAppProxyListener extends SdlAppProxyListener{

        @Override
        public void onFirstRun(OnHMIStatus notification) {
            show("Log Sdl App", "Show", "MediaTrack");
        }

        @CallSuper
        @Override
        public void onOnHMIStatus(OnHMIStatus notification) {
            addLogData(new LogDataBean(notification));
            super.onOnHMIStatus(notification);
        }

        @CallSuper
        @Override
        public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
            super.onProxyClosed(info, e, reason);
            addLogData(new LogDataBean(e, info, reason));
        }

        @CallSuper
        @Override
        public void onError(String info, Exception e) {
            super.onError(info, e);
            addLogData(new LogDataBean(e, info));
        }

        @CallSuper
        @Override
        public void onServiceEnded(OnServiceEnded serviceEnded) {
            addLogData(new LogDataBean(serviceEnded, serviceEnded.getFunctionName(), serviceEnded.getSessionType().getName()));
        }

        @CallSuper
        @Override
        public void onServiceNACKed(OnServiceNACKed serviceNACKed) {
            addLogData(new LogDataBean(serviceNACKed, serviceNACKed.getFunctionName(), serviceNACKed.getSessionType().getName()));
        }

        @CallSuper
        @Override
        public void onOnStreamRPC(OnStreamRPC notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onStreamRPCResponse(StreamRPCResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onGenericResponse(GenericResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnCommand(OnCommand notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onAddCommandResponse(AddCommandResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onAddSubMenuResponse(AddSubMenuResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onCreateInteractionChoiceSetResponse(CreateInteractionChoiceSetResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onAlertResponse(AlertResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onDeleteCommandResponse(DeleteCommandResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onDeleteInteractionChoiceSetResponse(DeleteInteractionChoiceSetResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onDeleteSubMenuResponse(DeleteSubMenuResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onPerformInteractionResponse(PerformInteractionResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onResetGlobalPropertiesResponse(ResetGlobalPropertiesResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSetGlobalPropertiesResponse(SetGlobalPropertiesResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSetMediaClockTimerResponse(SetMediaClockTimerResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onShowResponse(ShowResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSpeakResponse(SpeakResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnButtonEvent(OnButtonEvent notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onOnButtonPress(OnButtonPress notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onSubscribeButtonResponse(SubscribeButtonResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onUnsubscribeButtonResponse(UnsubscribeButtonResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnPermissionsChange(OnPermissionsChange notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onSubscribeVehicleDataResponse(SubscribeVehicleDataResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onUnsubscribeVehicleDataResponse(UnsubscribeVehicleDataResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onGetVehicleDataResponse(GetVehicleDataResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnVehicleData(OnVehicleData notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onPerformAudioPassThruResponse(PerformAudioPassThruResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onEndAudioPassThruResponse(EndAudioPassThruResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnAudioPassThru(OnAudioPassThru notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onPutFileResponse(PutFileResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onDeleteFileResponse(DeleteFileResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onListFilesResponse(ListFilesResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSetAppIconResponse(SetAppIconResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onScrollableMessageResponse(ScrollableMessageResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onChangeRegistrationResponse(ChangeRegistrationResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSetDisplayLayoutResponse(SetDisplayLayoutResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnLanguageChange(OnLanguageChange notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onOnHashChange(OnHashChange notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onSliderResponse(SliderResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnDriverDistraction(OnDriverDistraction notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onOnTBTClientState(OnTBTClientState notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onOnSystemRequest(OnSystemRequest notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onSystemRequestResponse(SystemRequestResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnKeyboardInput(OnKeyboardInput notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onOnTouchEvent(OnTouchEvent notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onDiagnosticMessageResponse(DiagnosticMessageResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onReadDIDResponse(ReadDIDResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onGetDTCsResponse(GetDTCsResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnLockScreenNotification(OnLockScreenStatus notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onDialNumberResponse(DialNumberResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSendLocationResponse(SendLocationResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onShowConstantTbtResponse(ShowConstantTbtResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onAlertManeuverResponse(AlertManeuverResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onUpdateTurnListResponse(UpdateTurnListResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onServiceDataACK(int dataSize) {
//            addLogData(new LogDataBean(dataSize));
        }

        @CallSuper
        @Override
        public void onGetWayPointsResponse(GetWayPointsResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSubscribeWayPointsResponse(SubscribeWayPointsResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onUnsubscribeWayPointsResponse(UnsubscribeWayPointsResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnWayPointChange(OnWayPointChange notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onGetSystemCapabilityResponse(GetSystemCapabilityResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onGetInteriorVehicleDataResponse(GetInteriorVehicleDataResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onButtonPressResponse(ButtonPressResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onSetInteriorVehicleDataResponse(SetInteriorVehicleDataResponse response) {
            addLogData(new LogDataBean(response));
        }

        @CallSuper
        @Override
        public void onOnInteriorVehicleData(OnInteriorVehicleData notification) {
            addLogData(new LogDataBean(notification));
        }

        @CallSuper
        @Override
        public void onSendHapticDataResponse(SendHapticDataResponse response) {
            addLogData(new LogDataBean(response));
        }
    }
}
