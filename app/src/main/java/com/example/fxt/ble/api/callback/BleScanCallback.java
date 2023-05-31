package com.example.fxt.ble.api.callback;


import com.example.fxt.ble.api.bean.BleScanBean;

import java.util.List;

/**
 * 蓝牙设备扫描时的回调
 */
public interface BleScanCallback {
    void onStart(List<BleScanBean> bleScanBeanList);

    void onStop(List<BleScanBean> bleScanBeanList);

    void onDeviceFound(BleScanBean bleScanBean, List<BleScanBean> bleScanBeanList);
}
