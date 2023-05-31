package com.example.fxt.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.fxt.R;

import java.util.ArrayList;

public class DeviceInfoAdapter extends ArrayAdapter<CustomDevice> {

    private ArrayList<CustomDevice> items;
    Context context;

    public DeviceInfoAdapter(Context context, int textViewResourceId, ArrayList<CustomDevice> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.vw_device_list_item, null);
        }
        CustomDevice p = items.get(position);
        if (p != null) {
//            ImageView img_title = v.findViewById(R.id.iv_title);
            TextView title1 = v.findViewById(R.id.tvTop1);
            TextView title2 = v.findViewById(R.id.tvTop2);


//            img_title.setImageResource(p.getImgName());
            title1.setText(p.getTitle1());
            title2.setText(p.getTitle2());

        }
        return v;
    }
}
