package com.fiberfox.fxt.ble.util;

import static com.fiberfox.fxt.ble.device.splicer.BleSplicerDevice.CHECK_BLUETOOTH;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;

import com.fiberfox.fxt.R;

import java.util.Random;


/**
 * 蓝牙工具类
 */
public class BleUtil {

    private BleUtil(){}

    /**
     * 判断设备是否支持ble
     * @param context ctx
     * @return boolean
     */
    public static boolean isSupportBle(Context context) {
        if (context == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) {
            return false;
        }

        final BluetoothAdapter adapter = manager.getAdapter();
        if (adapter == null) {
            return false;
        }

        return manager.getAdapter() != null;
    }

    /**
     * 判断蓝牙是否开启
     * @param context ctx
     * @return boolean
     */
    public static boolean isBleEnable(final Context context) {
        if (context == null){
            return false;
        }

        if (!isSupportBle(context)) {
            return false;
        }

        // 获取蓝牙适配器
        final BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) {
            return false;
        }

        final BluetoothAdapter adapter = manager.getAdapter();
        if (adapter == null) {
            return false;
        }

        return manager.getAdapter().isEnabled();
    }


    /**
     * 确认蓝牙环境
     * 1. 判断蓝牙是否开启，若没有开启，则请求开启蓝牙
     * 2. 确认位置权限开启，若没有，则请求位置权限
     * @param activity ctx
     * @return 蓝牙状态
     */
    public static boolean makeSureEnable(final Activity activity) {

        if (activity == null){
            return false;
        }

        // 获取蓝牙适配器
        final BluetoothManager manager = (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) {
            return false;
        }
        final BluetoothAdapter adapter = manager.getAdapter();
        if (adapter == null) {
            return false;
        }

        // 判断手机蓝牙是否被打开
        if (!adapter.isEnabled()) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, CHECK_BLUETOOTH);
            return false;
        } else if (!isLocServiceEnable(activity)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(activity.getString(R.string.open_the_application_location_permission))
                    .setMessage(activity.getString(R.string.please_open_location))
                    .setPositiveButton(activity.getString(R.string.common_ok), (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        activity.startActivityForResult(intent, 0);
                    })
                    .setNeutralButton(activity.getString(R.string.common_cancel), (dialog, which) -> dialog.dismiss()).show();
            return false;
        } else {
            return true;
        }
    }

    private static boolean isLocServiceEnable(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return gps || network;
    }

    /**
     * 得到随机加密字节
     *
     * @param byteLength 字节长度
     * @return 加密字符串
     */
    public static String getEncryptionKey(int byteLength) {

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < byteLength * 2; i++) {
            stringBuilder.append(new Random().nextInt(9));
        }
        return stringBuilder.toString();
    }

    /**
     * 整形转16进制字符串,高位补0
     * 字节长度小于转换后的长度时不做处理
     */
    public static String getIntegerToHexString(int number, int byteLength) {
        StringBuilder str = new StringBuilder();
        String strNum = Integer.toHexString(number);
        for (int i = 0; i < byteLength * 2 - strNum.length(); i++) {
            str.append("0");
        }
        str.append(strNum);
        return str.toString();
    }
}
