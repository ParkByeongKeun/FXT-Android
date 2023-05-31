package com.example.fxt.ble.api;


import com.example.fxt.CustomApplication;
import com.example.fxt.ble.api.callback.BleConnectionCallBack;
import com.example.fxt.ble.api.callback.BleScanCallback;
import com.example.fxt.ble.api.core.BleWrapper;

/**
 * 单例模式
 */
public class BleAPI {

    private BleAPI() {}

    /**
     * ble的功能封装类
     */
    private static BleWrapper mBleWrapper = null;

    /**
     * 本次蓝牙操作接收到的指令
     */
    private static String acceptBleCommand = "";

    /**
     * 清除监听
     */
    public static void removeCmdCallback() {
        getBleWrapper().removeCmdCallback();
    }

    /**
     * 开始扫描
     */
    public static void startScan(BleScanCallback bleScanCallback) {
        if (bleScanCallback == null) {
            throw new IllegalArgumentException("this ScanCallback is Null!");
        }
        getBleWrapper().startScanLeDevice(60*60 * 1000L, bleScanCallback);
    }

    /**
     * 停止扫描
     */
    public static void stopScan() {
        getBleWrapper().stopScanLeDevice();
    }

    /**
     * 断开蓝牙连接
     */
    public static void disconnectBle(){
        getBleWrapper().disConnect();
    }

    /**
     * 判断蓝牙是否连接
     * @return boolean
     */
    public static boolean bleIsConnected(){
        return getBleWrapper().isConnected();
    }

    /**
     * 开始连接蓝牙
     * @param address 蓝牙地址
     * @param bleConnectionCallBack callback
     */
    public static void startConnectBle(String address, BleConnectionCallBack bleConnectionCallBack){
        getBleWrapper().connectBle(address, bleConnectionCallBack);
    }


    /**
     * 得到ble分装类对象
     */
    public static BleWrapper getBleWrapper() {
        if (mBleWrapper == null) {
            mBleWrapper = new BleWrapper(CustomApplication.getCurrentContext());
        }
        return mBleWrapper;
    }

    public static String getAcceptBleCommand() {
        if (acceptBleCommand == null){
            acceptBleCommand = "";
        }
        return acceptBleCommand;
    }

    public static void setAcceptBleCommand(String command) {
        acceptBleCommand = command;
    }
}