package com.lz.proxytestdemo.sdlapp;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.proxy.RPCRequestFactory;
import com.smartdevicelink.proxy.rpc.AddCommand;
import com.smartdevicelink.proxy.rpc.OnButtonEvent;
import com.smartdevicelink.proxy.rpc.OnCommand;
import com.smartdevicelink.proxy.rpc.OnHMIStatus;
import com.smartdevicelink.proxy.rpc.SetMediaClockTimer;
import com.smartdevicelink.proxy.rpc.Show;
import com.smartdevicelink.proxy.rpc.SoftButton;
import com.smartdevicelink.proxy.rpc.StartTime;
import com.smartdevicelink.proxy.rpc.SubscribeButton;
import com.smartdevicelink.proxy.rpc.enums.ButtonEventMode;
import com.smartdevicelink.proxy.rpc.enums.ButtonName;
import com.smartdevicelink.proxy.rpc.enums.SdlDisconnectedReason;
import com.smartdevicelink.proxy.rpc.enums.SoftButtonType;
import com.smartdevicelink.proxy.rpc.enums.SystemAction;
import com.smartdevicelink.proxy.rpc.enums.UpdateMode;
import com.smartdevicelink.util.CorrelationIdGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

/**
 * Created by Administrator on 2017/11/23.
 */

public class MediaSdlApp extends LogSdlApp {

    private static final String TAG = LogHelper.makeLogTag(MediaSdlApp.class.getSimpleName());

    private int mCmdCounter = 1;
    private final int CMD_ID_PLAY = mCmdCounter++;
    private final int CMD_ID_PAUSE = mCmdCounter++;
    private final int CMD_ID_LEFT = mCmdCounter++;
    private final int CMD_ID_RIGHT = mCmdCounter++;

    private final int SB_ID_BUTTON1 = mCmdCounter++;
    private final int SB_ID_BUTTON2 = mCmdCounter++;
    private final int SB_ID_BUTTON3 = mCmdCounter++;
    private final int SB_ID_BUTTON4 = mCmdCounter++;


    ButtonName[] mButtonNames = {ButtonName.OK, ButtonName.SEEKLEFT, ButtonName.SEEKRIGHT};
    private final ArrayList<SoftButton> mSoftBtns = new ArrayList<>();
    private final AddCommand[] mCmds = {
            RPCRequestFactory.buildAddCommand(CMD_ID_PLAY, "Command 1", null, null,
                    new Vector<>(Arrays.asList("Command 1")), null, null),
            RPCRequestFactory.buildAddCommand(CMD_ID_PAUSE, "Command 2", null, null,
                    new Vector<>(Arrays.asList("Command 2")), null, null),
            RPCRequestFactory.buildAddCommand(CMD_ID_RIGHT, "Command 3", null, null,
                    new Vector<>(Arrays.asList("Command 3")), null, null),
            RPCRequestFactory.buildAddCommand(CMD_ID_LEFT, "Command 4", null, null,
                    new Vector<>(Arrays.asList("Command 4")), null, null),
    };

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private MockMediaPlayer mMediaPlayer = new MockMediaPlayer();

    protected MediaSdlApp(Context context) {
        super(context);
    }

    private void subscribeButton(){
        for (ButtonName bn : mButtonNames) {
            SubscribeButton sb = new SubscribeButton();
            sb.setButtonName(bn);
            sendRpcMsg(sb);
        }
    }

    private void addCommand(){
        for (AddCommand cmd : mCmds){
            sendRpcMsg(cmd);
        }
    }

    private void firstShow(){

//        int[] sb_id = {SB_ID_BUTTON1, SB_ID_BUTTON2, SB_ID_BUTTON3, SB_ID_BUTTON4};
//        String[] sb_text = {"Button 1", "Button 2", "Button 3", "Button 4"};
        List<MockMediaPlayer.SongList> sl = mMediaPlayer.getSongList();
        for (int i = 0; i < sl.size(); i++){
            SoftButton sb = new SoftButton();
            sb.setSoftButtonID(mCmdCounter++);
            sb.setText(sl.get(i).mName);
            sb.setType(SoftButtonType.SBT_TEXT);
            sb.setSystemAction(SystemAction.DEFAULT_ACTION);
            mSoftBtns.add(sb);
        }

        Show msg = new Show();
        msg.setCorrelationID(CorrelationIdGenerator.generateId());
        msg.setMainField1("Media Sdl App");
        msg.setMainField2("Show");
        msg.setMediaTrack("MediaTrack");
        msg.setSoftButtons(mSoftBtns);

        sendRpcMsg(msg);
    }

    private boolean checkMediaClockTimer(Integer startTime, Integer endTime, UpdateMode updateMode){
        int max = 59 * 60 * 60;
        if(updateMode == UpdateMode.COUNTDOWN || updateMode == UpdateMode.COUNTUP){
            if(startTime == null){
                LogHelper.e(TAG, "start time is needed");
                return false;
            }else if(startTime < 0 || startTime > max){
                LogHelper.e(TAG, "start time out of range");
                return false;
            }

            if(endTime != null && endTime >= 0 && endTime <= max){
                if(((startTime < endTime) && updateMode ==UpdateMode.COUNTDOWN)
                        || ((startTime > endTime) && updateMode ==UpdateMode.COUNTUP)){
                    LogHelper.e(TAG, "invalid data");
                    return false;
                }
            }
        }

        if(updateMode == UpdateMode.PAUSE){
            if(endTime != null && (endTime < 0 || endTime > max)){
                LogHelper.e(TAG, "end time out of range");
                return false;
            }
        }

        return true;
    }

    private void setHmiMediaClockTimer(Integer startTime, Integer endTime, UpdateMode updateMode){
        if(!checkMediaClockTimer(startTime, endTime, updateMode)) return;

        SetMediaClockTimer msg = new SetMediaClockTimer();
        msg.setUpdateMode(updateMode);

        if(startTime != null) {
            StartTime st = new StartTime();
            st.setHours(startTime / 60 / 60);
            st.setMinutes((startTime / 60) % 60);
            st.setSeconds(startTime % 60);
            msg.setStartTime(st);
        }

        if(endTime != null){
            StartTime et = new StartTime();
            et.setHours(endTime / 60 / 60);
            et.setMinutes((endTime / 60) % 60);
            et.setSeconds(endTime % 60);
            msg.setEndTime(et);
        }
        sendRpcMsg(msg);
    }

    private void setHmiMediaStatus(String title, String msg, Integer startTime, Integer endTime, UpdateMode updateMode){
        show(title, msg, null);
        setHmiMediaClockTimer(startTime, endTime, updateMode);
    }

    private void showToast(final Context context, final String msg, final int duration){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, duration).show();
            }
        });
    }

    public class MediaSdlAppProxyListener extends LogSdlAppProxyListener{

        @Override
        public void onFirstRun(OnHMIStatus notification) {
            mMediaPlayer.start();
            firstShow();
            subscribeButton();
            addCommand();
        }

        @Override
        public void onOnHMIStatus(OnHMIStatus notification) {
            super.onOnHMIStatus(notification);

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
                    if(mMediaPlayer.getMediaPlayerStatus() == MediaPlayerStatus.Pausing) {
                        mMediaPlayer.setMediaPlayerStatus(MediaPlayerStatus.Resume);
                    }else if(mMediaPlayer.getMediaPlayerStatus() == MediaPlayerStatus.Playing){
//                        mMediaPlayer.setMediaPlayerStatus(MediaPlayerStatus.Resume);
                    }else if(mMediaPlayer.getMediaPlayerStatus() == MediaPlayerStatus.None){
                        mMediaPlayer.setMediaPlayerStatus(MediaPlayerStatus.Playing);
                    }
                    break;
                case ATTENUATED:
                case NOT_AUDIBLE:
                    if(mMediaPlayer.getMediaPlayerStatus() != MediaPlayerStatus.Pausing) {
                        mMediaPlayer.setMediaPlayerStatus(MediaPlayerStatus.Pausing);
                    }
                    break;
                default:
                    return;
            }
            showToast(getAppContext(),
                    getAppName() + " " + notification.getAudioStreamingState().name(),
                    Toast.LENGTH_SHORT);

            switch (notification.getHmiLevel()) {
                case HMI_FULL:
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

        @Override
        public void onOnButtonEvent(OnButtonEvent notification) {
            super.onOnButtonEvent(notification);

            for (final ButtonName bn : mButtonNames){
                if(notification.getButtonEventMode() == ButtonEventMode.BUTTONUP
                        && bn == notification.getButtonName()){
                    if(bn == ButtonName.OK){
                        if(mMediaPlayer.getMediaPlayerStatus() == MediaPlayerStatus.Pausing) {
                            mMediaPlayer.setMediaPlayerStatus(MediaPlayerStatus.Resume);
                        }else if(mMediaPlayer.getMediaPlayerStatus() == MediaPlayerStatus.None){
//                            mMediaPlayer.setMediaPlayerStatus(MediaPlayerStatus.Pausing);
                        }else{
                            mMediaPlayer.setMediaPlayerStatus(MediaPlayerStatus.Pausing);
                        }
                    }else if(bn == ButtonName.SEEKLEFT){
                        mMediaPlayer.seekLeft();
                    }else if(bn == ButtonName.SEEKRIGHT){
                        mMediaPlayer.seekRight();
                    }
                    showToast(getAppContext(),
                            getAppName() + " " + bn.name() + " clicked",
                            Toast.LENGTH_SHORT);
                    return;
                }
            }

            for (final SoftButton sb : mSoftBtns){
                if(notification.getButtonEventMode() == ButtonEventMode.BUTTONUP
                        && sb.getSoftButtonID() == notification.getCustomButtonID()){
                    mMediaPlayer.changeCurrentSongList(sb.getText());
                    showToast(getAppContext(),
                            getAppName() + " " + sb.getText() + " clicked",
                            Toast.LENGTH_SHORT);
                    return;
                }
            }
        }

        @Override
        public void onOnCommand(OnCommand notification) {
            super.onOnCommand(notification);
            for (AddCommand ac : mCmds){
                if(ac.getCmdID() == notification.getCmdID()){
                    showToast(getAppContext(),
                            getAppName() + " " + ac.getVrCommands() + " fired",
                            Toast.LENGTH_SHORT);
                }
            }
        }

        @Override
        public void onProxyClosed(String info, Exception e, SdlDisconnectedReason reason) {
            super.onProxyClosed(info, e, reason);
            mMediaPlayer.stop();
        }
    }

    enum MediaPlayerStatus{
        Playing,
        Resume,
        Pausing,
        Loading,
        None
    }

    class MockMediaPlayer implements Runnable{

        private class SongList{
            String mName;
            List<Song> mSongList;

            public SongList(String name, List<Song> list){
                mName = name;
                mSongList = list;
            }
        }

        private class Song{

            String mName;
            int mDuration;

            Song(String name, int duration){
                mName = name;
                mDuration = duration;
            }
        }

        private Thread mThread;
        private MediaPlayerStatus mMediaPlayerStatus = MediaPlayerStatus.None;
        private boolean mStatusChanged = false;
        private Object mTimerLock = new Object();
        private Object mStatusLock = new Object();
        private Object mSongLock = new Object();
        private Object mLoadLock = new Object();
        private boolean mCanceled = false;
        private int mCounter = 0;
        private int[] mCurrentSong = {0, 0};
        private List<SongList> mSongLists = Arrays.asList(
                new SongList("List 1", Arrays.asList(
                        new Song("Song 1", 200),
                        new Song("Song 2", 300),
                        new Song("Song 3", 150),
                        new Song("Song 4", 100),
                        new Song("Song 5", 350))),
                new SongList("List 2", Arrays.asList(
                        new Song("Song 6", 200),
                        new Song("Song 7", 300),
                        new Song("Song 8", 150),
                        new Song("Song 9", 100),
                        new Song("Song 10", 350))),
                new SongList("List 3", Arrays.asList(
                        new Song("Song 11", 20),
                        new Song("Song 12", 30),
                        new Song("Song 13", 15),
                        new Song("Song 14", 10),
                        new Song("Song 15", 35)))
        );

        public void setMediaPlayerStatus(MediaPlayerStatus status){
            LogHelper.v(TAG, LogHelper._FUNC_(), status.name());
            synchronized (mStatusLock) {
                MediaPlayerStatus previousStatus = mMediaPlayerStatus;
                mMediaPlayerStatus = status;
                mStatusChanged = true;
                synchronized (mTimerLock) {
                    mTimerLock.notifyAll();
                }

                if(previousStatus == MediaPlayerStatus.Loading && mMediaPlayerStatus == MediaPlayerStatus.Loading){
                    synchronized (mLoadLock){
                        mLoadLock.notifyAll();
                    }
                }
            }
        }

        public MediaPlayerStatus getMediaPlayerStatus(){
            return mMediaPlayerStatus;
        }

        public void start(){
            if(mThread == null){
                mThread = new Thread(this);
                mThread.start();
            }
        }

        public void stop(){
            if(mThread != null){
                mCanceled = true;
                mThread.interrupt();
                mThread = null;
            }
        }

        public void seekLeft(){
            setCurrentSong(mCurrentSong[0], mCurrentSong[1] - 1);
        }

        public void seekRight(){
            setCurrentSong(mCurrentSong[0], mCurrentSong[1] + 1);
        }

        public void changeCurrentSongList(String name){
            LogHelper.v(TAG, LogHelper._FUNC_(), name);
            for (int i = 0; i < mSongLists.size(); i++){
                if(mSongLists.get(i).mName.equals(name)){
                    setCurrentSong(i, 0);
                    return;
                }
            }
        }

        public List<SongList> getSongList(){
            return mSongLists;
        }

        private void setCurrentSong(int list, int song){
            LogHelper.v(TAG, LogHelper._FUNC_(), list, song);
            synchronized (mSongLock){
                if(list == mCurrentSong[0] && song == mCurrentSong[1]){
                    LogHelper.w(TAG, "this song is playing");
                    return;
                }

                try {
                    SongList songList = mSongLists.get(list);
                    if(song < 0){
                        song = songList.mSongList.size() - 1;
                    }else if(song > songList.mSongList.size() - 1){
                        song = 0;
                    }

                    mCurrentSong[0] = list;
                    mCurrentSong[1] = song;
                }catch (ArrayIndexOutOfBoundsException e){
                    LogHelper.w(TAG, e.getMessage());
                    return;
                }
            }
            setMediaPlayerStatus(MediaPlayerStatus.Loading);
        }

        private Song getCurrentSong(){
            Song song = null;
            synchronized (mSongLock){
                song = mSongLists.get(mCurrentSong[0]).mSongList.get(mCurrentSong[1]);
            }

            return song;
        }

        private void resetStatus(){
            mCanceled = false;
            mMediaPlayerStatus = MediaPlayerStatus.None;
        }

        @Override
        public void run(){
            resetStatus();
            while (!mCanceled){
                if(mStatusChanged){
                    mStatusChanged = false;
                    Song song = getCurrentSong();
                    String title = song.mName;
                    int duration = song.mDuration;
                    if(mMediaPlayerStatus == MediaPlayerStatus.Playing){
                        setHmiMediaStatus(title,
                                mMediaPlayerStatus.name(),
                                0,
                                duration, UpdateMode.COUNTUP);
                    }else if(mMediaPlayerStatus == MediaPlayerStatus.Resume){
                        setHmiMediaStatus(title, mMediaPlayerStatus.name(), null, null, UpdateMode.RESUME);
                    } else if(mMediaPlayerStatus == MediaPlayerStatus.Pausing){
                        setHmiMediaStatus(title, mMediaPlayerStatus.name(), null, null, UpdateMode.PAUSE);
                    }else if(mMediaPlayerStatus == MediaPlayerStatus.Loading){
                        mCounter = 0;
//                        setHmiMediaStatus(title, mMediaPlayerStatus.name(), null, null, UpdateMode.CLEAR);
                        int sleepTime = new Random().nextInt(10) + 1;
                        setHmiMediaStatus(title, mMediaPlayerStatus.name(),
                                sleepTime - 1,
                                0,
                                UpdateMode.COUNTDOWN);
                        LogHelper.v(TAG, "loading wait: " + sleepTime * 1000);
                        synchronized (mLoadLock) {
                            try {
                                mLoadLock.wait(sleepTime * 1000);
                            } catch (InterruptedException e) {
                                continue;
                            }
                        }
                        if(mMediaPlayerStatus == MediaPlayerStatus.Pausing){
                            setHmiMediaStatus(title,
                                    mMediaPlayerStatus.name(),
                                    0,
                                    duration, UpdateMode.COUNTUP);
                            setMediaPlayerStatus(MediaPlayerStatus.Pausing);
                        }else {
                            setMediaPlayerStatus(MediaPlayerStatus.Playing);
                        }
                    }else {
                        setHmiMediaStatus("Media Sdl App", mMediaPlayerStatus.name(), null, null, UpdateMode.CLEAR);
                    }
                }else{
                    synchronized (mTimerLock){
                        try {
                            mTimerLock.wait(1000);
                            if(mMediaPlayerStatus == MediaPlayerStatus.Playing ||
                                    mMediaPlayerStatus == MediaPlayerStatus.Resume){
                                mCounter++;
                            }
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }
                    if(mStatusChanged){
                        continue;
                    }
                    Song song = getCurrentSong();
                    if (mCounter > song.mDuration){
                        setCurrentSong(mCurrentSong[0], mCurrentSong[1] + 1);
                    }
                }
            }
        }
    }
}
