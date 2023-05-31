package com.example.fxt.utils;

import android.content.Context;
import android.widget.Toast;


public class ToastUtil {
    private static Toast mToast = null;

    /**
     * 长时间显示Toast
     * @param context 上下文
     * @param message 信息
     */
    public static void showToastLong(Context context, CharSequence message) {
        if (mToast == null) {
            mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    /**
     * 短时间显示Toast
     * @param context 上下文
     * @param message 信息
     */
    public static void showToast(Context context, CharSequence message) {
        if (mToast == null) {
            mToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(message);
        }
        mToast.show();
    }

    /**
     * 短时间显示Toast
     * @param context 上下文
     * @param resId 资源ID:getResources().getString(R.string.xxxxxx);
     */
    public static void showToast(Context context, int resId) {
        if (mToast == null) {
            mToast = Toast.makeText(context, resId, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(resId);
        }
        mToast.show();
    }
}
