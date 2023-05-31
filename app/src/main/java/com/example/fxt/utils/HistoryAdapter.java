package com.example.fxt.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.fxt.R;

import java.util.ArrayList;

public class HistoryAdapter extends ArrayAdapter<CustomHistoryList> {

    private ArrayList<CustomHistoryList> items;
    Context context;

    public HistoryAdapter(Context context, int textViewResourceId, ArrayList<CustomHistoryList> items) {
        super(context, textViewResourceId, items);
        this.context = context;
        this.items = items;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.vw_history_item, null);
        }
        CustomHistoryList p = items.get(position);
        if (p != null) {

            TextView title1 = v.findViewById(R.id.tvTitle);
            TextView title2 = v.findViewById(R.id.tvTop1);

            title1.setText(p.getTitle1());
            title2.setText(p.getTitle2());

        }
        return v;
    }
}
