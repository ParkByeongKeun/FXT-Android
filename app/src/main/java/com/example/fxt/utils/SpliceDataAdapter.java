package com.example.fxt.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fxt.R;
import com.example.fxt.ble.device.splicer.bean.SpliceDataBean;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 熔接记录列表 适配器
 */
public class SpliceDataAdapter extends BaseAdapter {
    private List<SpliceDataBean> mSpliceDataBeanList;
    // 上下文环境
    private Context mContext;
    ViewHolder holder;
    boolean isCheckBox;

    public SpliceDataAdapter(List<SpliceDataBean> mSpliceDataBeanList, Context mContext) {
        this.mSpliceDataBeanList = mSpliceDataBeanList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mSpliceDataBeanList.size();
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
        // 避免每次都从xml中加载布局的消耗，convertView为空，没有缓存
        if(convertView == null){
            convertView = View.inflate(mContext, R.layout.adapter_splice_data_list_item,null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        if(isCheckBox) {
            holder.checkBox.setVisibility(View.VISIBLE);
        }else {
            holder.checkBox.setVisibility(View.INVISIBLE);
        }

        holder.ivPdf.setOnClickListener(v -> {
            Toast.makeText(mContext, "Long click to share",Toast.LENGTH_SHORT).show();
        });
        holder.ivExcel.setOnClickListener(v -> {
            Toast.makeText(mContext, "Long click to share",Toast.LENGTH_SHORT).show();
        });
        holder.textId.setText(String.valueOf(position + 1));
        if(mSpliceDataBeanList.get(position).getUpdateTime() == null) {
            return convertView;
        }
        if(mSpliceDataBeanList.get(position).getFpgaVer().equals("0")) {
            holder.ivPdf.setVisibility(View.VISIBLE);
            holder.ivExcel.setVisibility(View.INVISIBLE);
        }else if(mSpliceDataBeanList.get(position).getFpgaVer().equals("1")) {
            holder.ivPdf.setVisibility(View.INVISIBLE);
            holder.ivExcel.setVisibility(View.VISIBLE);
        }else if(mSpliceDataBeanList.get(position).getFpgaVer().equals("2")) {
            holder.ivPdf.setVisibility(View.VISIBLE);
            holder.ivExcel.setVisibility(View.VISIBLE);
        }else {
            holder.ivPdf.setVisibility(View.INVISIBLE);
            holder.ivExcel.setVisibility(View.INVISIBLE);
        }
        String date = "";
        String time = "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String strNowDate = simpleDateFormat.format(mSpliceDataBeanList.get(position).getUpdateTime());
        String[] spl = strNowDate.split(" ");
        date = spl[0];
        time = spl[1];
        holder.textUpdateDate.setText(date);
        holder.textUpdateTime.setText(time);
        return convertView;
    }

    public void setCheckBoxVisible(Boolean isShow) {
        isCheckBox = isShow;
    }

    public boolean getCheckBox() {
        return this.isCheckBox;
    }
    static class ViewHolder{
        TextView textId;
        TextView textUpdateDate;
        TextView textUpdateTime;
        ImageView ivPdf;
        ImageView ivExcel;
        CheckBox checkBox;

        ViewHolder(View convertView) {
            textId = convertView.findViewById(R.id.adapter_splice_data_list_item_tv_id);
            textUpdateDate = convertView.findViewById(R.id.date);
            textUpdateTime = convertView.findViewById(R.id.time);
            ivPdf = convertView.findViewById(R.id.ivPdf);
            ivExcel = convertView.findViewById(R.id.ivExcel);
            checkBox = convertView.findViewById(R.id.checkBox1);
            ivPdf.setVisibility(View.INVISIBLE);
            ivExcel.setVisibility(View.INVISIBLE);
        }
    }
}