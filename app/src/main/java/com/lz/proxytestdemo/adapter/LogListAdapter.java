package com.lz.proxytestdemo.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.sdlapp.LogSdlApp;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.RPCResponse;
import com.smartdevicelink.proxy.constants.Names;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Administrator on 2017/9/28.
 */

public class LogListAdapter extends BaseAdapter{

    static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss SSS");

    private List<LogSdlApp.LogDataBean> mList;
    private LayoutInflater inflater;
    private Context mContext;

    public LogListAdapter(Context context, LogSdlApp sdlApp) {
        this.mContext = context;
        this.mList = sdlApp.getLogList();
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public LogSdlApp.LogDataBean getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_log_list, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        LogSdlApp.LogDataBean item = mList.get(position);
        StringBuilder sb = new StringBuilder();
        if(item.getThrowable() != null){
            sb.append(item.getThrowable().getMessage());
            sb.append("\n");
        }
        if(item.getRpcMessage() != null){
            RPCMessage msg = item.getRpcMessage();
            String head = msg.getFunctionName() + "(" +
                    msg.getMessageType() + ")";
            sb.append(head);
            sb.append("\n");
        }
        if(item.getObjects() != null){
            for (Object o : item.getObjects()){
                sb.append(o).append(" ");
            }
        }
        holder.mLogHeadTv.setText(sb.substring(0, sb.length() - 1));
        holder.mLogTimeTv.setText(formatter.format(item.getDate()));

        if(item.getRpcMessage() != null){
            String type = item.getRpcMessage().getMessageType();
            if (type.equals(Names.request)) {
                holder.mLogHeadTv.setTextColor(Color.CYAN);
            } else if (type.equals(Names.notification)) {
                holder.mLogHeadTv.setTextColor(Color.argb(0xff, 0xff, 0xb0, 0x00));
            } else if (type.equals(Names.response)) {
                if(((RPCResponse)item.getRpcMessage()).getSuccess()) {
                    holder.mLogHeadTv.setTextColor(Color.argb(0xff, 0x20, 0xa1, 0x20));
                }else{
                    holder.mLogHeadTv.setTextColor(Color.argb(0xff, 0xed, 0x1c, 0x24));
                }
            }
        }

        if(item.getThrowable() != null){
            holder.mLogHeadTv.setTextColor(Color.argb(0xff, 0xed, 0x1c, 0x24));
        }

        return convertView;
    }

    public void add(LogSdlApp.LogDataBean data){
        mList.add(data);
        notifyDataSetChanged();
    }

    public void clear(){
        mList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder {
        TextView mLogHeadTv;
        TextView mLogBodyTv;
        TextView mLogTimeTv;

        public ViewHolder(View itemView) {
            mLogHeadTv = (TextView) itemView.findViewById(R.id.log_head_tv);
            mLogBodyTv = (TextView) itemView.findViewById(R.id.log_body_tv);
            mLogTimeTv = (TextView) itemView.findViewById(R.id.lg_time_tv);
        }
    }
}

