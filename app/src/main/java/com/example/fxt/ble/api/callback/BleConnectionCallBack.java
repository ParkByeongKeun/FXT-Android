package com.example.fxt.ble.api.callback;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

/**
 * 蓝牙连接时的回调
 */
public interface BleConnectionCallBack {
    void onReceive(BluetoothGattCharacteristic data_char);

    void onConnectFail(final String errMsg);

    void onConnectSuccess(BluetoothGatt bluetoothGatt);

    // isActive 是否主动断开设备连接
    void onDisconnect(boolean isActive);
}
