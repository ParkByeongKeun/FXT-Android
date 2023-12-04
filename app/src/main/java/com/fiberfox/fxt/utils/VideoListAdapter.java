package com.fiberfox.fxt.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fiberfox.fxt.R;

import java.util.ArrayList;

public class VideoListAdapter extends ArrayAdapter<CustomVideoList> {

    private ArrayList<CustomVideoList> items;
    Context context;

    public VideoListAdapter(Context context, int textViewResourceId, ArrayList<CustomVideoList> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.vw_video_list_item, null);
        }
        CustomVideoList p = items.get(position);
        if (p != null) {
            ImageView img_title = v.findViewById(R.id.iv_title);
            TextView title1 = v.findViewById(R.id.tvTitle);
            TextView title2 = v.findViewById(R.id.tvTop1);

            Glide.with(context).load(p.getImgName()).into(img_title);
            title1.setText(p.getTitle1());
            title2.setText(p.getTitle2());

        }
        return v;
    }
}
