package com.example.fxt.ble.device.splicer;


import android.app.Activity;

import com.example.fxt.ble.device.splicer.request.AddReceiveCallback;


public class BleSplicerDevice {
    public static final int CHECK_BLUETOOTH = 5959;

    private Activity activity;

    /**
     * 蓝牙地址
     */
    private String mDeviceAddress;

    /**
     * 信道密码
     */
    private String mChannelCode;

    public BleSplicerDevice(Activity activity, String address, String channelCode) {
        this.activity = activity;
        this.mDeviceAddress = address;
        this.mChannelCode = channelCode;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setmChannelCode(String mChannelCode) {
        this.mChannelCode = mChannelCode;
    }

    public void setReceiveCallback(final BleSplicerCallback bleSplicerCallback){
        new AddReceiveCallback(mDeviceAddress, bleSplicerCallback).addReceiveCallback();
    }

}