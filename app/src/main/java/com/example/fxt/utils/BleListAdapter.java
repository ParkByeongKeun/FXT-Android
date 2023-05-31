package com.example.fxt.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.fxt.R;
import com.example.fxt.ble.api.bean.BleScanBean;

import java.util.List;

public class BleListAdapter extends BaseAdapter {
    private List<BleScanBean> mBleScanList;
    // 上下文环境
    private Context mContext;

    public BleListAdapter(List<BleScanBean> mBleScanList, Context mContext) {
        this.mBleScanList = mBleScanList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mBleScanList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        // 避免每次都从xml中加载布局的消耗，convertView为空，表示没有缓存
        if(convertView == null){
            convertView = View.inflate(mContext, R.layout.adapter_ble_list_item,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }

        // 绑定数据
        holder.textDeviceName.setText(mBleScanList.get(position).getName());
        holder.textAddress.setText(mBleScanList.get(position).getAddress());
        holder.textRssi.setText(String.valueOf(mBleScanList.get(position).getRssi()+ "dBm"));
        return convertView;
    }

    static class ViewHolder{
        TextView textDeviceName;
        TextView textAddress;
        TextView textRssi;

        ViewHolder(View convertView) {
            textDeviceName = convertView.findViewById(R.id.adapter_ble_list_item_tv_name);
            textAddress = convertView.findViewById(R.id.adapter_ble_list_item_tv_address);
            textRssi = convertView.findViewById(R.id.adapter_ble_list_item_tv_rssi);
        }
    }
}

