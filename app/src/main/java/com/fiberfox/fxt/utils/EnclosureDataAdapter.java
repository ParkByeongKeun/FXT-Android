package com.fiberfox.fxt.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.ble.device.splicer.bean.EnclosureDataBean;
import com.fiberfox.fxt.ble.device.splicer.bean.OFIDataBean;

import java.util.List;

/**
 * 熔接记录列表 适配器
 */
public class EnclosureDataAdapter extends BaseAdapter {
    private List<EnclosureDataBean> mOFIDataBeanList;
    // 上下文环境
    private Context mContext;
    ViewHolder holder;
    boolean isCheckBox;

    public EnclosureDataAdapter(List<EnclosureDataBean> mOFIDataBeanList, Context mContext) {
        this.mOFIDataBeanList = mOFIDataBeanList;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mOFIDataBeanList.size();
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
        holder.textId.setText(String.valueOf(mOFIDataBeanList.size() - position));
        if(mOFIDataBeanList.get(position).getDataTime() == null) {
            return convertView;
        }
        if(mOFIDataBeanList.get(position).getNote().equals("0")) {
            holder.ivPdf.setVisibility(View.VISIBLE);
            holder.ivExcel.setVisibility(View.INVISIBLE);
        }else if(mOFIDataBeanList.get(position).getNote().equals("1")) {
            holder.ivPdf.setVisibility(View.INVISIBLE);
            holder.ivExcel.setVisibility(View.VISIBLE);
        }else if(mOFIDataBeanList.get(position).getNote().equals("2")) {
            holder.ivPdf.setVisibility(View.VISIBLE);
            holder.ivExcel.setVisibility(View.VISIBLE);
        }else {
            holder.ivPdf.setVisibility(View.INVISIBLE);
            holder.ivExcel.setVisibility(View.INVISIBLE);
        }

        String date = "";
        String time = "";
        String[] spl = mOFIDataBeanList.get(position).getDataTime().split(" ");
        date = spl[0];
        time = spl[1];
        holder.textUpdateDate.setText(date);
        holder.textUpdateTime.setText(time);

//        holder.textUpdateTime.setText(mOFIDataBeanList.get(position).getDataTime());
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
            textUpdateTime = convertView.findViewById(R.id.time);
            textUpdateDate = convertView.findViewById(R.id.date);
            ivPdf = convertView.findViewById(R.id.ivPdf);
            ivExcel = convertView.findViewById(R.id.ivExcel);
            checkBox = convertView.findViewById(R.id.checkBox1);
            ivPdf.setVisibility(View.INVISIBLE);
            ivExcel.setVisibility(View.INVISIBLE);
        }
    }
}