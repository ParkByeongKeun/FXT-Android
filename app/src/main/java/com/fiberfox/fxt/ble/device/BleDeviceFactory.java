package com.fiberfox.fxt.ble.device;

import android.app.Activity;

import com.fiberfox.fxt.ble.device.splicer.BleSplicerDevice;

import java.util.HashMap;
import java.util.Map;


public class BleDeviceFactory {

    private BleDeviceFactory(){}

    /**
     * 设备单例Map
     * key: 设备地址
     * value:T extends BleDevice
     */
    private static Map<String, BleSplicerDevice> mBleDeviceMap = new HashMap<>();

    /**
     * 获取设备对象
     */
    public static BleSplicerDevice getSplicerDevice(Activity activity, String address, String channelCode) {
        BleSplicerDevice lockDevice = mBleDeviceMap.get(address);
        if (lockDevice == null) {
            lockDevice = new BleSplicerDevice(activity,address,channelCode);
            mBleDeviceMap.put(address,lockDevice);
        }
        lockDevice.setActivity(activity);
        lockDevice.setmChannelCode(channelCode);
        return lockDevice;
    }
}
