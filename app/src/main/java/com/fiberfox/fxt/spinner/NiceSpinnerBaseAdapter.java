package com.fiberfox.fxt.spinner;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.fiberfox.fxt.CustomApplication;
import com.fiberfox.fxt.R;

/*
 * Copyright (C) 2015 Angelo Marchesin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@SuppressWarnings("unused")
public abstract class NiceSpinnerBaseAdapter<T> extends BaseAdapter {

    private final PopUpTextAlignment horizontalAlignment;
    private final SpinnerTextFormatter spinnerTextFormatter;

    private int textColor;
    private int backgroundSelector;

    int selectedIndex;
    CustomApplication customApplication;

    NiceSpinnerBaseAdapter(
            Context context,
            int textColor,
            int backgroundSelector,
            SpinnerTextFormatter spinnerTextFormatter,
            PopUpTextAlignment horizontalAlignment
    ) {
        this.spinnerTextFormatter = spinnerTextFormatter;
        this.backgroundSelector = backgroundSelector;
        this.textColor = textColor;
        this.horizontalAlignment = horizontalAlignment;
        customApplication = (CustomApplication) context.getApplicationContext();
    }

    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        Context context = parent.getContext();
        TextView textView;
        ImageView ivCore;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.spinner_list_item, null);
            textView = convertView.findViewById(R.id.text_view_spinner);
            ivCore = convertView.findViewById(R.id.ivCore);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                textView.setBackground(ContextCompat.getDrawable(context, R.color.dark_gray));
            }
            convertView.setTag(new ViewHolder(textView,ivCore));
        } else {
            textView = ((ViewHolder) convertView.getTag()).textView;
            ivCore = ((ViewHolder) convertView.getTag()).ivCore;
        }
        if(spinnerTextFormatter.format(getItem(position)).toString().equals("391R393") ||
                spinnerTextFormatter.format(getItem(position)).toString().equals("526459930") ||
                spinnerTextFormatter.format(getItem(position)).toString().equals("100001239183") ||
                spinnerTextFormatter.format(getItem(position)).toString().equals("601185866") ||
                spinnerTextFormatter.format(getItem(position)).toString().contains("UNIT#")) {
            textColor = context.getResources().getColor(R.color.white);
        }else {
            textColor = context.getResources().getColor(R.color.red);
        }
        for(int i = 0 ; i < customApplication.fnmsDataList.size() ; i ++) {
            if(customApplication.fnmsDataList.get(i).getLeft().equals(spinnerTextFormatter.format(getItem(position)).toString())) {
                if(customApplication.fnmsDataList.get(i).getRight().contains("144C")) {
                    textColor = context.getResources().getColor(R.color.white);

                }
            }
            if(customApplication.fnmsDataList.get(i).getRight().equals(spinnerTextFormatter.format(getItem(position)).toString())) {
                if(customApplication.fnmsDataList.get(i).getLeft().contains("72C")) {
                    textColor = context.getResources().getColor(R.color.white);
                }
            }
        }

        textView.setText(spinnerTextFormatter.format(getItem(position)));
        textView.setTextColor(textColor);

        String[] splStr = getItem(position).toString().split("C");

        if(splStr.length <= 1) {
            ivCore.setImageDrawable(null);
            return convertView;
        }

        String temp = "";
        if(Integer.parseInt(splStr[1]) > 12) {
            temp = String.valueOf(Integer.parseInt(splStr[1]) - ((Integer.parseInt(splStr[1])/12) * 12));
            if(Integer.parseInt(splStr[1]) - ((Integer.parseInt(splStr[1])/12) * 12) == 0) {
                temp = "12";
            }
            String resourceName = "core_"+temp;
            int resourceId = convertView.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            Drawable drawable = convertView.getResources().getDrawable(resourceId);
            ivCore.setImageDrawable(drawable);
            ivCore.setVisibility(View.VISIBLE);
        }else {
            temp = String.valueOf(Integer.parseInt(splStr[1]));
            if(Integer.parseInt(splStr[1]) == 0) {
                temp = "12";
            }
            String resourceName = "core_"+temp;
            int resourceId = convertView.getResources().getIdentifier(resourceName, "drawable", context.getPackageName());
            Drawable drawable = convertView.getResources().getDrawable(resourceId);
            ivCore.setImageDrawable(drawable);
            ivCore.setVisibility(View.VISIBLE);
        }
        setTextHorizontalAlignment(textView);

        return convertView;
    }

    private void setTextHorizontalAlignment(TextView textView) {
        switch (horizontalAlignment) {
            case START:
                textView.setGravity(Gravity.START);
                break;
            case END:
                textView.setGravity(Gravity.END);
                break;
            case CENTER:
                textView.setGravity(Gravity.CENTER_HORIZONTAL);
                break;
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    void setSelectedIndex(int index) {
        selectedIndex = index;
    }

    public abstract T getItemInDataset(int position);

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public abstract T getItem(int position);

    @Override
    public abstract int getCount();

    static class ViewHolder {
        TextView textView;
        ImageView ivCore;
        ViewHolder(TextView textView,ImageView ivCore) {
            this.textView = textView;
            this.ivCore = ivCore;
        }
    }
}
