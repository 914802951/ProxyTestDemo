package com.lz.proxytestdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.adapter.LogListAdapter;
import com.lz.proxytestdemo.sdlapp.LogSdlApp;
import com.lz.proxytestdemo.sdlapp.SdlApp;
import com.lz.proxytestdemo.sdlapp.SingleSdlService;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.proxy.RPCMessage;

/**
 * Created by Administrator on 2017/11/20.
 */

public class LogSdlAppActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(LogSdlAppActivity.class.getSimpleName());

    public static final String Log_SDL_APP = "log_sdl_app";

    private ListView mLogListView;
    private LogSdlApp mSdlApp;
    private LogListAdapter mLogListAdapter;
    private boolean mIsScrollToBottom = true;
    private LogSdlApp.OnDataChangedListener mSdlAppListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, LogHelper._FUNC_());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_sdl_app);

        initData();
        initView();
        initListener();

    }

    private void initData(){
        int position = getIntent().getIntExtra(Log_SDL_APP, -1);
        if(position >= 0){
            SdlApp app = SingleSdlService.getSdlAppList().get(position);
            if(app instanceof LogSdlApp) {
                mSdlApp = (LogSdlApp) app;
                return;
            }
        }

        Toast.makeText(this, "Sdl app error", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void initView(){
        mLogListView = (ListView) findViewById(R.id.log_list_view);
        mLogListAdapter = new LogListAdapter(this, mSdlApp);
        mLogListView.setAdapter(mLogListAdapter);
    }

    private void initListener(){
        mSdlAppListener = new LogSdlApp.OnDataChangedListener() {
            @Override
            public void onDataChanged(final LogSdlApp.LogDataBean data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLogListAdapter.add(data);
                        if(mIsScrollToBottom) {
                            mLogListView.setSelection(mLogListAdapter.getCount());
                        }
                    }
                });
            }
        };
        mSdlApp.addOnDataChangedListener(mSdlAppListener);
        mLogListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                if (firstVisibleItem == 0) {
//                    View firstVisibleItemView = mLogListView.getChildAt(0);
//                    if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
//                    }
//                } else
                if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
                    mIsScrollToBottom = true;
                }else{
                    mIsScrollToBottom = false;
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
        });
        mLogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showSdlAppLogDialog(position);
            }
        });
    }

    private void showSdlAppLogDialog(int position) {

        LogSdlApp.LogDataBean item = mLogListAdapter.getItem(position);
        StringBuilder sb = new StringBuilder();
        if(item.getThrowable() != null){
            sb.append(item.getThrowable().getMessage());
            sb.append("\n");
            sb.append(Log.getStackTraceString(item.getThrowable()));
            sb.append("\n\n");
        }
        if(item.getRpcMessage() != null){
            RPCMessage msg = item.getRpcMessage();
            String head = msg.getFunctionName() + "(" +
                    msg.getMessageType() + ")";
            sb.append(head);
            sb.append("\n");
            sb.append(mSdlApp.serializeJSON(item.getRpcMessage()));
            sb.append("\n\n");
        }
        if(item.getObjects() != null){
            for (Object o : item.getObjects()){
                sb.append(o).append("\n");
            }
        }

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LogSdlAppActivity.this);

        final View dialogView = LayoutInflater.from(LogSdlAppActivity.this)
                .inflate(R.layout.dialog_app_log,null);
        final TextView appLogTv = (TextView) dialogView.findViewById(R.id.app_log_tv);
        appLogTv.setText(sb.toString().trim());
        appLogTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Ok", null);
        dialogBuilder.show();

    }
}
