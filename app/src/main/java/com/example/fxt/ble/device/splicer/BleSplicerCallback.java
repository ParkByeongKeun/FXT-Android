package com.example.fxt.ble.device.splicer;


import com.example.fxt.ble.api.bean.BleResultBean;


public interface BleSplicerCallback<T extends BleResultBean> {
    void onSuccess(T resultBean);

    void onReceiveSuccess(T resultBean);

    void onFailed(int code, String msg);
}
