package com.fiberfox.fxt.widget;


import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.fiberfox.fxt.R;


/**
 * 自定义加载弹出框
 */
public class LoadingDialog extends AlertDialog {
    private Context mContext;  //上下文环境

    @SuppressLint("StaticFieldLeak")
    private static LoadingDialog instance;


    public static LoadingDialog getInstance(Context context){
        if (instance == null){
            instance = new LoadingDialog(context,0);
        }
        return instance;
    }

    public static void setInstance(LoadingDialog instance){
        LoadingDialog.instance = instance;
    }

//    private LoadingDialog(@NonNull Context context) {
//        this(context,R.style.LoadingDialog);
//    }

    private LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.mContext=context;
        initView();
    }

    protected LoadingDialog(@NonNull Context context, boolean cancelable, @Nullable SearchManager.OnCancelListener cancelListener) {
        super(context, cancelable, (OnCancelListener) cancelListener);
    }

    private void initView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_loading_view,null);
        this.setView(view);
        this.setCancelable(false);
    }
}
