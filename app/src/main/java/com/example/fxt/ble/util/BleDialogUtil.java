package com.example.fxt.ble.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Gravity;

/**
 * 用于蓝牙的dialog工具
 */
public class BleDialogUtil {

    private BleDialogUtil(){}

    public static ProgressDialog createProgressDialog(Activity activity){
        //实例化进度条对话框（ProgressDialog）
        ProgressDialog pd = new ProgressDialog(activity);
        //设置对话进度条显示在屏幕顶部
        pd.getWindow().setGravity(Gravity.TOP);
        //外部点击不会取消
        pd.setCanceledOnTouchOutside(false);
        //不显示右下角包数
        pd.setProgressNumberFormat("");
        return pd;
    }
}
