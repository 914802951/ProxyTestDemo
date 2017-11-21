package com.lz.proxytestdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lz.proxytestdemo.R;
import com.lz.proxytestdemo.sdlapp.SdlApp;
import com.smartdevicelink.transport.enums.TransportType;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Administrator on 2017/11/21.
 */

public class AppListAdapter extends BaseAdapter {

    static SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss SSS");

    private List<SdlApp> mList;
    private LayoutInflater inflater;
    private Context mContext;

    public AppListAdapter(Context context, List<SdlApp> sdlAppList) {
        this.mContext = context;
        this.mList = sdlAppList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public SdlApp getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppListAdapter.ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_app_list, parent, false);
            holder = new AppListAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (AppListAdapter.ViewHolder) convertView.getTag();
        }
        SdlApp item = mList.get(position);
        holder.mAppIconIv.setImageResource(item.getAppIconId());
        holder.mAppNameTv.setText(item.getAppName());
        TransportType transportType = item.getTransportConfig().getTransportType();
        String type = "";
        if (transportType.equals(TransportType.MULTIPLEX)){
            type = "MBT";
        }else if (transportType.equals(TransportType.BLUETOOTH)){
            type = "BT";
        }else if (transportType.equals(TransportType.TCP)){
            type = "TCP";
        }else if (transportType.equals(TransportType.USB)){
            type = "USB";
        }
        holder.mAppStatus.setText(type + " " + item.getStatus().name());
        return convertView;
    }

    public void add(SdlApp data){
        mList.add(data);
        notifyDataSetChanged();
    }

    public void remove(int position){
        mList.remove(position);
        notifyDataSetChanged();
    }

    public void clear(){
        mList.clear();
        notifyDataSetChanged();
    }

    class ViewHolder {

        ImageView mAppIconIv;
        TextView mAppNameTv;
        TextView mAppStatus;

        public ViewHolder(View itemView) {
            mAppIconIv = (ImageView) itemView.findViewById(R.id.app_icon_iv);
            mAppNameTv = (TextView) itemView.findViewById(R.id.app_name_tv);
            mAppStatus = (TextView) itemView.findViewById(R.id.app_status_tv);
        }
    }
}
