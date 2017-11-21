package com.lz.proxytestdemo.activity;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.adapter.AppListAdapter;
import com.lz.proxytestdemo.sdlapp.LogSdlApp;
import com.lz.proxytestdemo.sdlapp.SdlApp;
import com.lz.proxytestdemo.sdlapp.SingleSdlService;
import com.lz.proxytestdemo.util.LogHelper;
import com.smartdevicelink.transport.BTTransportConfig;
import com.smartdevicelink.transport.BaseTransportConfig;
import com.smartdevicelink.transport.MultiplexTransportConfig;
import com.smartdevicelink.transport.TCPTransportConfig;
import com.smartdevicelink.transport.USBTransportConfig;

public class SingleMainActivity extends AppCompatActivity {

    private static final String TAG = LogHelper.makeLogTag(SingleMainActivity.class.getSimpleName());

    private static Integer Counter = 0;

    public SingleSdlService mMultiSdlService;
    private GridView mAppListGridView;
    private AppListAdapter mAppListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.v(TAG, LogHelper._FUNC_());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_main);

        initView();
        initListener();
    }

    private void initView(){
        mAppListGridView = (GridView) findViewById(R.id.app_list_gv);
        mAppListAdapter = new AppListAdapter(this, SingleSdlService.getSdlAppList());
        mAppListGridView.setAdapter(mAppListAdapter);

//        for (int i = 0; i < 10; i++){
//            SdlApp.Builder builder = new SdlApp.Builder(this);
//            mAppListAdapter.add(builder.build());
//        }
    }

    private void initListener(){
        findViewById(R.id.new_sdl_app_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(SingleMainActivity.this, LogSdlAppActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(LogSdlAppActivity.Log_SDL_APP, MultiSdlService.mApp);
//                intent.putExtras(bundle);
//                startActivity(intent);

//                SdlApp.Builder builder = new SdlApp.Builder(SingleMainActivity.this);
//                mAppListAdapter.add(builder.build());
//                mAppListAdapter.notifyDataSetChanged();

                showNewSdlAppDialog();
            }
        });

        mAppListGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SdlApp app = SingleSdlService.getSdlAppList().get(position);
                if(!(app instanceof LogSdlApp)){
                    Toast.makeText(SingleMainActivity.this, "No log", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(SingleMainActivity.this, LogSdlAppActivity.class);
                intent.putExtra(LogSdlAppActivity.Log_SDL_APP, position);
                startActivity(intent);
            }
        });

        mAppListGridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteSdlAppDialog(position);
                return true;
            }
        });
    }

    private void startSingleService(){
        Intent intent = new Intent(this, SingleSdlService.class);
        startService(intent);
    }

    private void showDeleteSdlAppDialog(final int position){
        final SdlApp app = SingleSdlService.getSdlAppList().get(position);
        AlertDialog.Builder normalDialog = new AlertDialog.Builder(SingleMainActivity.this);
        normalDialog.setIcon(app.getAppIconId());
        normalDialog.setTitle("Close App: " + app.getAppName() + "?");
        normalDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        app.releaseApp();
                        mAppListAdapter.remove(position);
                    }
                });
        normalDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        normalDialog.show();
    }

    private void showNewSdlAppDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SingleMainActivity.this);

        final View dialogView = LayoutInflater.from(SingleMainActivity.this)
                .inflate(R.layout.dialog_new_sdl_app,null);
        final EditText appNameEt = (EditText) dialogView.findViewById(R.id.app_name_et);
        final EditText appIdEt = (EditText) dialogView.findViewById(R.id.app_id_et);
        final EditText tcpIpEt = (EditText) dialogView.findViewById(R.id.tcp_ip_et);
        final EditText tcpPortEt = (EditText) dialogView.findViewById(R.id.tcp_port_et);
        final RadioButton btRb = (RadioButton) dialogView.findViewById(R.id.transport_type_bt_rb);
        final RadioButton usbRb = (RadioButton) dialogView.findViewById(R.id.transport_type_usb_rb);
        final RadioButton tcpRb = (RadioButton) dialogView.findViewById(R.id.transport_type_tcp_rb);
        RadioGroup typeRg = (RadioGroup) dialogView.findViewById(R.id.transport_type_rg);
        appNameEt.setText(SdlApp.APP_NAME + Counter);
        appIdEt.setText(String.valueOf(SdlApp.APP_ID + Counter));
        typeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.transport_type_tcp_rb){
                    dialogView.findViewById(R.id.tcp_set_ly).setVisibility(View.VISIBLE);
                }else{
                    dialogView.findViewById(R.id.tcp_set_ly).setVisibility(View.GONE);
                }
            }
        });

        dialogBuilder.setTitle("New Sdl App");
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSingleService();
                        try {
                            SdlApp.Builder builder = new SdlApp.Builder(SingleMainActivity.this);
                            builder.mAppName = appNameEt.getText().toString();
                            builder.mAppId = Integer.valueOf(appIdEt.getText().toString());

                            BaseTransportConfig config;
                            if (btRb.isChecked()) {
                                config = new BTTransportConfig();
                            } else if (usbRb.isChecked()) {
                                config = new USBTransportConfig(SingleMainActivity.this);
                            } else if (tcpRb.isChecked()) {
                                config = new TCPTransportConfig(
                                        Integer.valueOf(tcpPortEt.getText().toString()),
                                        tcpIpEt.getText().toString(), true);
                            } else {
                                config = new MultiplexTransportConfig(SingleMainActivity.this, builder.mAppId.toString());
                            }

                            builder.mTransportConfig = config;
                            LogSdlApp sdlApp = builder.build(LogSdlApp.class, LogSdlApp.LogSdlAppProxyListener.class);
                            Counter++;
                            sdlApp.addOnDataChangedListener(mSdlAppListener);
                            mAppListAdapter.add(sdlApp);
                            mAppListAdapter.notifyDataSetChanged();
                        }catch (Exception e){
                            Toast.makeText(SingleMainActivity.this, "Parameters error", Toast.LENGTH_LONG).show();
                        }
                    }
                });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialogBuilder.show();
    }

    private ServiceConnection mSdlServiceConnection = new ServiceConnection() {

        private final String TAG = LogHelper.makeLogTag(this.getClass().getSimpleName());

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LogHelper.v(TAG, LogHelper._FUNC_());
            mMultiSdlService = ((SingleSdlService.ServiceBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogHelper.v(TAG, LogHelper._FUNC_());
            mMultiSdlService = null;
        }
    };

    private  LogSdlApp.OnDataChangedListener mSdlAppListener = new LogSdlApp.OnDataChangedListener() {
        @Override
        public void onDataChanged(final LogSdlApp.LogDataBean data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAppListAdapter.notifyDataSetChanged();
                }
            });
        }
    };
}
