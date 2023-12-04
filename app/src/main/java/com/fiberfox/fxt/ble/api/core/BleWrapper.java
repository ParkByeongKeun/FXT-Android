package com.fiberfox.fxt.ble.api.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.fiberfox.fxt.R;
import com.fiberfox.fxt.ble.api.BleAPI;
import com.fiberfox.fxt.ble.api.bean.BleCmdBean;
import com.fiberfox.fxt.ble.api.bean.BleScanBean;
import com.fiberfox.fxt.ble.api.callback.BleCmdCallback;
import com.fiberfox.fxt.ble.api.callback.BleConnectionCallBack;
import com.fiberfox.fxt.ble.api.callback.BleScanCallback;
import com.fiberfox.fxt.ble.api.event.ResultEvent;
import com.fiberfox.fxt.ble.api.event.WriteEvent;
import com.fiberfox.fxt.ble.api.event.base.BaseEvent;
import com.fiberfox.fxt.ble.api.util.ArrayUtils;
import com.fiberfox.fxt.ble.api.util.BleCmdUtil;
import com.fiberfox.fxt.ble.api.util.BleHexConvert;
import com.fiberfox.fxt.utils.StringUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class BleWrapper {

    public static final String TAG = "yot132";
    private Context mContext;
    private static Handler mTimerHandler = new Handler();

    /**
     * 默认超时时间
     */
    private static final long TIMEOUT_DEFAULT = 15 * 1000L;

    /**
     * 蓝牙指令发送回调
     */
    private BleCmdCallback mBleCmdCallback;

    /**
     * 蓝牙设备扫描回调
     */
    private BleScanCallback mBleScanCallback;

    /**
     * 蓝牙连接回调
     */
    private BleConnectionCallBack mBleConnectionCallback;

    /**
     * 当前执行中的指令
     */
    private BleCmdBean mCurrentCmdBean;

    /**
     * 临时命令对象，用来记录发送的指令
     */
    private BleCmdBean tempCmdBean;

    /**
     * 内部请求执行回调，主要用于命令拆分情况
     */
    private BleSendCallback mBleSendCallback;

    /**
     * 是否已连接
     */
    private int mConnectionState = 0;
    private boolean isActiveDisconnect = false;//是否主动断开连接

    /**
     * 设备mac地址
     */
    private String mDeviceAddress;

    /**
     * 蓝牙适配对象
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * 具体的蓝牙设备对象，可以有多个，这里记录当前的
     */
    private BluetoothDevice mBluetoothDevice = null;

    /**
     * 写数据（发命令）的特征
     */
    private BluetoothGattCharacteristic mCharacteristicWrite;

    /**
     * 返回接收的数据，可能是分条接收
     */
    private byte[] receiveBytes = null;

    /**
     * 蓝牙协议对象，在连接某个设备时产生，与设备一一对应
     */
    private BluetoothGatt mBluetoothGatt = null;

    private List<BleScanBean> mBleScanBeanList;

    /**
     * 是否在扫描中
     */
    private boolean isScanning = false;

    private int currentReceive = 1;

    private byte[] realDataTemp;

    /**
     * newBleTag S.
     */
    private ArrayList<BluetoothGattCharacteristic> arrayNtfCharList = new ArrayList<>();

    /**
     * 已写入的qpp描述符
     */
    private int writeDescriptorCount = 0;

    public void removeCmdCallback() {
        mBleCmdCallback = null;
    }

    public void setCmdCallback(BleCmdCallback bleCmdCallback) {
        mBleCmdCallback = bleCmdCallback;
    }

    private void onCmdWrite(final WriteEvent writeEvent) {
        mTimerHandler.post(() -> {
            if (mBleCmdCallback != null) {
                mBleCmdCallback.onCmdWrite(writeEvent);
            }
        });
    }

    private void onReceiveResult(final ResultEvent resultEvent) {
        mTimerHandler.post(() -> {
            if (mBleCmdCallback != null) {
                mBleCmdCallback.onReceiveResult(resultEvent);
            }
        });
    }

    /**
     * 构造函数，用在 Application
     */
    public BleWrapper(Context context) {
        this.mContext = context;
        init();
    }

    /**
     * 初始化
     */
    public void init() {
        // 设备是否支持蓝牙4.0
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(mContext, R.string.ble_support_4, Toast.LENGTH_SHORT).show();
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * 提供给外部调用 连接蓝牙
     */
    public void connectBle(String address, BleConnectionCallBack bleConnectionCallBack) {
        Log.e(TAG, "开始连接蓝牙。。。");

        if (bleConnectionCallBack != null){
            mBleConnectionCallback = bleConnectionCallBack;
        }

        setDeviceAddress(address);
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "蓝牙未初始化！");
            mBleConnectionCallback.onConnectFail("Bluetooth not initialized！");
            return;
        }

        if (mConnectionState != BluetoothProfile.STATE_CONNECTED) {
            new Thread(this::connect).start();
        } else {
            mTimerHandler.removeCallbacks(connectTimeoutRun);
            mBleConnectionCallback.onConnectSuccess(mBluetoothGatt);
        }
    }

    /**
     * 提供给外部调用 发送命令
     */
    public void sendCmd(BleCmdBean cmdBean, String address, BleCmdCallback bleCmdCallback) {
        Log.e(TAG, "开始发送指令。。。");

        setDeviceAddress(address);

        if (mBluetoothAdapter == null) {
            Log.e(TAG, "蓝牙未初始化！");
            onCmdWrite(new WriteEvent(cmdBean, BaseEvent.CODE_ERROR_NOT_INIT, mContext.getString(R.string.ble_not_init)));
            return;
        }

        if (cmdBean == null || TextUtils.isEmpty(cmdBean.getCommandStr())) {
            Log.e(TAG, "指令错误！");
            onCmdWrite(new WriteEvent(cmdBean, BaseEvent.CODE_ERROR_CMD_FORMAT, mContext.getString(R.string.ble_command_null_or_format_error)));
            return;
        }

        if (mBleCmdCallback != null){
            mBleCmdCallback = bleCmdCallback;
        }
        mCurrentCmdBean = cmdBean;
        tempCmdBean = cmdBean;

        if (mConnectionState != BluetoothProfile.STATE_CONNECTED) {
            new Thread(this::connect).start();
        } else {
            // 拆分命令，发送
            readySendCmd(cmdBean.getCommands());
        }
    }

    /**
     * 发送指令
     */
    private void readySendCmd(final String[] cmds) {
        if (mCurrentCmdBean != null) {
            new Thread(() -> sendCmds(cmds, 0)).start();
        }
    }

     /**
     * 顺序发送 拆分的命令
     *
     * @param cmds  拆分的命令
     * @param index 命令开始的下标
     */
    private void sendCmds(final String[] cmds, final int index) {
        mTimerHandler.removeCallbacks(connectTimeoutRun);
        if (cmds != null && cmds.length > 0) {
            // 启动发送指令超时定时器
            mTimerHandler.postDelayed(sendCmdTimeoutRun, TIMEOUT_DEFAULT);
            Log.e(TAG, "执行命令 sendCmd(" + (index + 1) + "/" + cmds.length + "): " + cmds[index]);
            excuteCmd(cmds[index], new BleSendCallback() {
                @Override
                public void onSuccess() {
                    if (index + 1 >= cmds.length) {
                        onCmdWrite(new WriteEvent(tempCmdBean, BaseEvent.CODE_SUCCESS, mContext.getString(R.string.ble_command_send_success)));
                        mTimerHandler.removeCallbacks(sendCmdTimeoutRun);
                        // 启动接受反馈超时定时器
//                        mTimerHandler.postDelayed(notifyTimeoutRun, TIMEOUT_DEFAULT);
                    } else {
                        // 命令未执行完
                        sendCmds(cmds, index + 1);
                    }
                }

                /**
                 * 反馈信息发送失败
                 * @param code 错误编码
                 * @param errMsg 错误信息
                 */
                @Override
                public void onFail(int code, String errMsg) {
                    Log.e(TAG, code + ":" + errMsg);
                    // 1. 反馈失败信息
                    onCmdWrite(new WriteEvent(tempCmdBean, code, errMsg));
                    // 2.及时断开连接
                    mTimerHandler.postDelayed(() -> {
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt.disconnect();
                        }
                        disConnect();
                    }, 100);
                }
            });
        } else {
            mCurrentCmdBean = null;
            onCmdWrite(new WriteEvent(tempCmdBean, BaseEvent.CODE_ERROR_CMD_FORMAT, mContext.getString(R.string.ble_command_null_or_format_error)));
        }
    }

    /**
     * 执行命令，向蓝牙设备写指令
     *
     * @param cmd             指令
     * @param bleSendCallback 回调
     */
    public void excuteCmd(final String cmd, BleSendCallback bleSendCallback) {
        mBleSendCallback = bleSendCallback;
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Have not call init()");
            onCmdWrite(new WriteEvent(tempCmdBean, BaseEvent.CODE_ERROR_NOT_INIT, "Have not call init()"));
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            // BT is not turned on - ask user to make it enabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivity(enableBtIntent);
            return;
        }
        if (mBluetoothGatt == null || mCharacteristicWrite == null) {
            Log.e(TAG, "Have not call connect()");
            onCmdWrite(new WriteEvent(tempCmdBean, BaseEvent.CODE_ERROR_NOT_INIT, "Have not call connect()"));
            return;
        }
        // 将16进制命令转换成byte[] 后发送
        mCharacteristicWrite.setValue(BleHexConvert.parseHexStringToBytes(cmd));
        mBluetoothGatt.writeCharacteristic(mCharacteristicWrite);

//        if (mCurrentCmdBean != null && mCurrentCmdBean.getCmd() == BleCmd.OLD_DEVICE_UPDATE) {
//            bleSendCallback.onSuccess();
//        }
    }

    /**
     * 连接超时处理
     */
    private Runnable connectTimeoutRun = () -> {
        BleAPI.setAcceptBleCommand("");
        if (tempCmdBean != null) {
            onCmdWrite(new WriteEvent(tempCmdBean, BaseEvent.CODE_ERROR_CONNECTION_TIMEOUT, mContext.getString(R.string.ble_connect_timeout)));
        }
        disConnect();
        mBleConnectionCallback.onConnectFail("Connection timed out");
    };

    /**
     * 指令发送超时处理
     */
    private Runnable sendCmdTimeoutRun = () -> {
        BleAPI.setAcceptBleCommand("");
        BleCmdBean temp = mCurrentCmdBean;
        mCurrentCmdBean = null;
        if (temp != null) {
            mBleSendCallback.onFail(BaseEvent.CODE_ERROR_SEND_TIMEOUT, mContext.getString(R.string.ble_command_send_timeout));
        }
    };

    /**
     * 指令反馈超时处理
     */
    private Runnable notifyTimeoutRun = () -> {
        BleAPI.setAcceptBleCommand("");
        BleCmdBean temp = mCurrentCmdBean;
        mCurrentCmdBean = null;
        if (temp != null) {
            onReceiveResult(new ResultEvent(BaseEvent.CODE_ERROR_NOTIFY_TIMEOUT, mContext.getString(R.string.ble_command_notify_timeout)));
        }
    };

    /**
     * 连接ble
     */
    private void connect() {
        // 启动连接蓝牙令超时定时器
        mTimerHandler.postDelayed(connectTimeoutRun, TIMEOUT_DEFAULT);

        if (TextUtils.isEmpty(mDeviceAddress) || mBluetoothAdapter == null) {
            return;
        }

        mDeviceAddress = mDeviceAddress.toUpperCase();
        Log.e(TAG, "第一次连接该设备，需要从搜索中连接，蓝牙地址 --> " + mDeviceAddress);

        startScanLeDevice(TIMEOUT_DEFAULT, new BleScanCallback() {

            @Override
            public void onStart(List<BleScanBean> bleScanBeanList) {
                // Do Nothing
            }

            @Override
            public void onStop(List<BleScanBean> bleScanBeanList) {
                // Do Nothing
            }

            @Override
            public void onDeviceFound(BleScanBean bleScanBean, List<BleScanBean> bleScanBeanList) {

                if (bleScanBean.getAddress().equalsIgnoreCase(mDeviceAddress)) {
                    mBluetoothDevice = bleScanBean.getBluetoothDevice();
                    stopScanLeDevice();
                    // 这里部分手机在关闭 扫描蓝牙设备的过程中需要时间
                    mTimerHandler.postDelayed(() -> {
                        mBluetoothGatt = mBluetoothDevice.connectGatt(mContext, false, mGattCallback);
                        mConnectionState = BluetoothProfile.STATE_CONNECTING;
                    }, 500);
                }
            }
        });
    }

    /**
     * 断开连接
     */
    public void disConnect() {
        Log.i(TAG, "执行 断开连接操作");
        stopScanLeDevice();
        if (mBluetoothGatt != null) {
            try {
                mBluetoothGatt.disconnect();
                mBluetoothGatt.close();
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
        mConnectionState = 0;
        mBluetoothGatt = null;
        // 清空接收到的数据
        receiveBytes = null;
    }

    /**
     * 蓝牙状态回调
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        /**
         * 连接状态改变，主要用来分析设备的连接与断开
         * @param gatt GATT
         * @param status 改变前状态
         * @param newState 改变后状态
         */
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "连接状态改变 onConnectionStateChange:" + status + "->" + newState);
            mBluetoothGatt = gatt;
            mConnectionState = newState;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "连接成功，开始设置MTU = 517");
                    // 连接成功之后设置mtu值  最大517byte
                    gatt.requestMtu(517);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.e(TAG, "服务端断开连接");
                    mTimerHandler.removeCallbacks(connectTimeoutRun);
                    disConnect();
                    isActiveDisconnect = true;
                    mBleConnectionCallback.onDisconnect(isActiveDisconnect);
                }
            } else if (status == 133) {
                new Thread(BleWrapper.this::connect).start();
            } else {
                isActiveDisconnect = false;
                // 蓝牙连接失败
                mTimerHandler.removeCallbacks(connectTimeoutRun);
                disConnect();
                mBleConnectionCallback.onDisconnect(isActiveDisconnect);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (BluetoothGatt.GATT_SUCCESS == status){
                Log.i(TAG, "MTU 设置成功,开始扫描服务: " + mtu);
                // mtu设置成功后，再去发现服务
                mBluetoothGatt.discoverServices();
            }else {
                Log.i(TAG, "onMtuChanged 失败: " + mtu);
                // 设置MTU失败
                if (mBleConnectionCallback != null) {
                    mTimerHandler.removeCallbacks(connectTimeoutRun);
                    mBleConnectionCallback.onConnectFail("Failed to set MTU");
                }
            }
        }

        /**
         * 发现服务，主要用来获取设备支持的服务列表
         * @param gatt GATT
         * @param status 当前状态
         */
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS && mBluetoothGatt != null) {
                arrayNtfCharList = new ArrayList<>();
                // 发现设备，遍历服务，初始化特征
                UUID uuid = null;
                for (BluetoothGattService service : mBluetoothGatt.getServices()) {
                    uuid = service.getUuid();

                    Log.i(TAG, " 服务uuid: "+ uuid);
                    if (uuid.toString().equals(BleDefinedUUIDs.spliceUUID)) {
                        BluetoothGattService gattServer = mBluetoothGatt.getService(uuid);
                        List<BluetoothGattCharacteristic> gattCharacteristics = gattServer.getCharacteristics();
                        for (int j = 0; j < gattCharacteristics.size(); j++) {
                            BluetoothGattCharacteristic chara = gattCharacteristics.get(j);
                            UUID charUid = chara.getUuid();
                            if (charUid.equals(BleDefinedUUIDs.SpliceCharacter.WRITE)) {
                                mCharacteristicWrite = chara;
//                                Log.d("yot132","zz = " + chara.getValue().toString());
                            } else if (charUid.equals(BleDefinedUUIDs.SpliceCharacter.NOTIFY)) {
                                arrayNtfCharList.add(chara);
                            }
                        }
                        writeDescriptorCount = 0;
                        setQppNextNotify(mBluetoothGatt, arrayNtfCharList.get(writeDescriptorCount));
                    }
                }

                Log.i(TAG, "calllll");
                // 连接成功
                if (mBleConnectionCallback != null) {
                    mTimerHandler.removeCallbacks(connectTimeoutRun);
                    mBleConnectionCallback.onConnectSuccess(mBluetoothGatt);

                    /**
                     *
                     *
                     * **/
                    if (uuid.toString().equals(BleDefinedUUIDs.spliceUUID) || uuid.toString().equals("0000a003-0000-1000-8000-00805f9b34fb")) {
                        mTimerHandler.postDelayed(() -> {
                            mCharacteristicWrite.setValue(BleHexConvert.parseHexStringToBytes("BE" + "00010000" + BleCmdUtil.getCRCStr("00010000") + "EB"));
                            gatt.writeCharacteristic(mCharacteristicWrite);
//                        String encodedData = "226d616368696e65547970654d61726b6574223a09224d494e493130304841222c0a09226d616368696e65536f667456657273696f6e223a092274312e303633222c0a0922534e223a09223030303034333235303033222c0a0922626c7565546f6f74684d4143223a092239343a65363a38363a31623a61363a3636222c0a09226163746976617465537461747573223a0922756e616374697661746564220a7d88eaeb";
//                        Log.d("yot132","123 = " + com.example.fiberfoxbluetooth.ble.api.util.ByteUtil.convertToASCII16(encodedData));
//                        try {
//                            byte[] decodedBytes = new byte[encodedData.length() / 2];
//                            for (int i = 0; i < decodedBytes.length; i++) {
//                                int index = i * 2;
//                                int value = Integer.parseInt(encodedData.substring(index, index + 2), 16);
//                                decodedBytes[i] = (byte) value;
//                            }
//
//                            for (byte b : decodedBytes) {
//                                int ascii = b & 0xFF;
//                                System.out.println("ASCII: " + ascii + ", Character: " + (char) ascii);
//                            }
//                        } catch (NumberFormatException e) {
//                            e.printStackTrace();
//                        }

                        }, 100);
                    }
                    /**
                     *
                     *
                     * **/


                }
            }
        }

        /**
         * 若开启监听成功则会回调此方法。写入属性描述值，主要用来根据当前属性描述值写入数据到设备
         * @param gatt GATT
         * @param descriptor 属性描述值
         * @param status 当前状态
         */
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            /*
            新方案每次特征值写入都要到此回调方法执行下一步，否则无法确保特征值写入完成
            新方案特征值不写完会造成接收不到蓝牙返回数据
             */

            Log.i(TAG, "onDescriptorWrite status:" + status + ": " + BleHexConvert.bytesToHexString(descriptor.getValue()));

//            mTimerHandler.postDelayed(() -> {
//                mCharacteristicWrite.setValue(BleHexConvert.parseHexStringToBytes("BE" + "00040006" + BleCmdUtil.getCRCStr( "00040006") + "EB"));
//                gatt.writeCharacteristic(mCharacteristicWrite);
//            }, 1000);
//            mTimerHandler.postDelayed(() -> {
//                mCharacteristicWrite.setValue(BleHexConvert.parseHexStringToBytes("aa11b8000000c9"));
//                gatt.writeCharacteristic(mCharacteristicWrite);
//            }, 1000);
        }

        /**
         * 读取特征值，主要用来读取该特征值包含的可读信息
         * @param gatt GATT
         * @param characteristic 特征值
         * @param status 当前状态
         */
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.i(TAG, "onCharacteristicRead status:" + status);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (mBleConnectionCallback != null) {
                    mBleConnectionCallback.onReceive(characteristic);
                }
            }
        }

        /**
         * 若写入指令成功则回调此方法，说明将数据已经发送给下位机
         * @param gatt GATT
         * @param characteristic 特征值
         * @param status 当前状态
         */
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "指令发送回调 status:" + status + ": " + BleHexConvert.bytesToHexString(characteristic.getValue()));
            // 不需要反馈
            if (mCurrentCmdBean != null) {
                mCurrentCmdBean = null;
            }
            mBleCmdCallback.onSend(BleHexConvert.bytesToHexString(characteristic.getValue()));
//                        mTimerHandler.postDelayed(() -> {
//                mCharacteristicWrite.setValue(BleHexConvert.parseHexStringToBytes("BE" + "00010000" + BleCmdUtil.getCRCStr( "00010000") + "EB"));
//                gatt.writeCharacteristic(mCharacteristicWrite);
//            }, 1000);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                mBleSendCallback.onSuccess();
//            } else {
//                mBleSendCallback.onFail(BaseEvent.CODE_ERROR_SEND, mContext.getString(R.string.ble_command_send_error));
//            }
        }

        /**
         * 特征中value值改变，主要用来接收设备返回的数据信息
         * 若发送的数据符合通信协议，则服务端会向客户端回复相应的数据。发送的数据通过回调onCharacteristicChanged()方法获取，
         * @param gatt GATT
         * @param characteristic 特征值
         */
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("yot132","?ASd?ADS?DSA?DSA?");
            receiveBytes = null;
            byte[] rawValue = characteristic.getValue();
            if (rawValue == null || rawValue.length <= 0) {
                return;
            }
            Log.i(TAG, "prev 返回：" + BleHexConvert.bytesToHexString(rawValue));

            receiveBytes = ArrayUtils.addAll(receiveBytes, rawValue);

            String res = BleHexConvert.bytesToHexString(receiveBytes);
            byte[] realData = new byte[rawValue.length];
            System.arraycopy(receiveBytes, 0, realData, 0, rawValue.length);
            ResultEvent resultEvent = new ResultEvent(realData);
            onReceiveResult(resultEvent);
            boolean isLock = false;
            if (isLock) {
                if (res != null && !res.startsWith("aa19")) {
                    if (res.contains("aa19")) {
                        int index = res.indexOf("aa19");
                        receiveBytes = Arrays.copyOfRange(receiveBytes, index / 2, receiveBytes.length);
                    } else {
                        return;
                    }
                }

                // 接收完成时 发送通知
                while (receiveBytes != null && receiveBytes.length >= 5) {
                    short length = ByteBuffer.wrap(receiveBytes, 3, 2).getShort();
                    // 接收完成时 发送通知
                    if (receiveBytes.length >= 7 + length) {
                        receiveBytes = null;
                        mCharacteristicWrite.setValue(BleHexConvert.parseHexStringToBytes("aa11b8000000c9"));
                        mBluetoothGatt.writeCharacteristic(mCharacteristicWrite);
                    } else {
                        break;
                    }
                }
            } else {
                if (res.contains("be11")) {
                    int index = res.indexOf("BE11");
                    receiveBytes = Arrays.copyOfRange(receiveBytes, index / 2, receiveBytes.length);
                    // 接收完成时 发送通知
                    while (receiveBytes != null && receiveBytes.length >= 5) {
                        short length = ByteBuffer.wrap(receiveBytes, 3, 2).getShort();
                        // 接收完成时 发送通知
                        if (receiveBytes.length >= 8 + length) {
                            String cmd = "";
                            String cmdType = BleHexConvert.bytesToHexString(new byte[]{receiveBytes[1], receiveBytes[2]});
                            cmd = cmd + cmdType + "0004" + "0006";
                            String currentPackage = BleHexConvert.bytesToHexString(new byte[]{receiveBytes[7], receiveBytes[8]});
                            cmd = cmd + currentPackage;

                            receiveBytes = null;
                            Log.d("yot132","img call  =" + currentPackage);
                            mCharacteristicWrite.setValue(BleHexConvert.parseHexStringToBytes("BE" + cmd + BleCmdUtil.getCRCStr(cmd) + "EB"));
                            gatt.writeCharacteristic(mCharacteristicWrite);
                        } else {
                            break;
                        }
                    }
                }
            }
        }

        /**
         * 开启通知
         *      1. 即建立与设备的通信的收发数据通道，BLE开发中只有当客户端成功开启监听后才能与服务端收发数据
         *      2. 若开启监听成功,在onCharacteristicRead()这个方法中收数据
         *      3. 若开启监听成功则会回调BluetoothGattCallback中的onDescriptorWrite()方法
         */
        private void setQppNextNotify(BluetoothGatt bluetoothGatt, BluetoothGattCharacteristic characteristic) {
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
            try {
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    bluetoothGatt.writeDescriptor(descriptor);
                } else {
                    Log.e(TAG, "descriptor is null");
                }
            } catch (Exception e) {
                Log.e(TAG, "descriptor is null");
            }
        }
    };

    /**
     * 蓝牙设备扫描回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            mTimerHandler.post(() -> {
                if (mBleScanCallback != null) {
                    BleScanBean bleScanBean = new BleScanBean(device, rssi);
                    // 过滤掉蓝牙名称和地址为null的
                    if (StringUtil.isBlank(bleScanBean.getName()) || StringUtil.isBlank(bleScanBean.getAddress())) {
                        return;
                    }

                    boolean NoAdd = true;

                    for (BleScanBean oldDevice : mBleScanBeanList) {
                        if (oldDevice.getAddress().equals(bleScanBean.getAddress())) {
                            NoAdd = false;
                            break;
                        }
                    }
                    if (NoAdd) {
                        mBleScanBeanList.add(bleScanBean);
                        Log.d(TAG, "扫描到设备:" + bleScanBean.getAddress() + " 设备名:" + bleScanBean.getName());
                    }

                    mBleScanCallback.onDeviceFound(bleScanBean, mBleScanBeanList);
                }
            });
        }
    };

    /**
     * 停止扫描任务
     */
    private Runnable mScanStopRun = this::stopScanLeDevice;

    /**
     * 开始扫描设备
     *
     * @param bleScanCallback 蓝牙回调
     * @param timeOut         扫描时长
     */
    public void startScanLeDevice(long timeOut, BleScanCallback bleScanCallback) {

        // 断开之前的蓝牙连接
        if (isConnected()) {
            disConnect();
        }
        isScanning = true;
        mBleScanCallback = bleScanCallback;
        //移除之前可能存在的停止线程
        mTimerHandler.removeCallbacks(mScanStopRun);
        //添加新的停止线程，并设置扫描时间
        mTimerHandler.postDelayed(mScanStopRun, timeOut);

        if (mBleScanBeanList == null) {
            mBleScanBeanList = new ArrayList<>();
        } else {
            mBleScanBeanList.clear();
        }

        mBluetoothAdapter.startLeScan(mLeScanCallback);

        if (mBleScanCallback != null) {
            mBleScanCallback.onStart(mBleScanBeanList);
        }
    }

    /**
     * 关闭扫描设备
     */
    public void stopScanLeDevice() {
        Log.i(TAG, "Stop Scan Le Device");

        if (isScanning) {
            isScanning = false;
            mTimerHandler.removeCallbacks(mScanStopRun);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (mBleScanCallback != null) {
                mBleScanCallback.onStop(mBleScanBeanList);
                mBleScanCallback = null;
            }
        }
    }

    /**
     * 当前蓝牙是否连接
     * @return boolean
     */
    public boolean isConnected() {
        return mConnectionState == BluetoothProfile.STATE_CONNECTED;
    }

    /**
     * 蓝牙地址与之前不同，先断开连接，再设置蓝牙地址
     *
     * @param deviceAddress 新蓝牙地址
     */
    public void setDeviceAddress(String deviceAddress) {
        if (StringUtil.isNotBlank(mDeviceAddress) && !deviceAddress.equalsIgnoreCase(mDeviceAddress)) {
            disConnect();
        }
        mDeviceAddress = deviceAddress.toUpperCase();
    }

    public void connectWithAddress(long scanTime, String address, BleScanCallback callback) {

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        startScanLeDevice(scanTime, new BleScanCallback() {

            @Override
            public void onStart(List<BleScanBean> bleScanBeanList) {
                // Do Nothing
            }

            @Override
            public void onDeviceFound(final BleScanBean bleScanBean, List<BleScanBean> bleScanBeanList) {
                Log.d(TAG, "扫描到设备:" + bleScanBean.getAddress() + " 设备名:" + bleScanBean.getName());

                if (StringUtil.isBlank(address)) {
                    stopScanLeDevice();
                    mBluetoothDevice = bleScanBean.getBluetoothDevice();
                    mTimerHandler.postDelayed(() -> callback.onDeviceFound(bleScanBean, null), 500);
                    return;
                }

                if (address.equalsIgnoreCase(bleScanBean.getAddress())) {
                    stopScanLeDevice();
                    mBluetoothDevice = bleScanBean.getBluetoothDevice();
                    mTimerHandler.postDelayed(() -> callback.onDeviceFound(bleScanBean, null), 500);
                }
            }

            @Override
            public void onStop(final List<BleScanBean> bleScanBeanList) {
                if (bleScanBeanList.isEmpty()) {
                    callback.onStop(bleScanBeanList);
                    return;
                }

                if (StringUtil.isBlank(address)) {
                    return;
                }

                for (BleScanBean bean : bleScanBeanList) {
                    if (address.equalsIgnoreCase(bean.getAddress())) {
                        return;
                    }
                }

                callback.onStop(bleScanBeanList);
            }
        });
    }

    /**
     * 扫描设备，不连接
     */
    public void scanDevice(long scanTime, final BleScanCallback callback) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return;
        }

        startScanLeDevice(scanTime, new BleScanCallback() {

            @Override
            public void onStart(List<BleScanBean> bleScanBeanList) {
                // Do Nothing
            }

            @Override
            public void onDeviceFound(final BleScanBean bleScanBean, List<BleScanBean> bleScanBeanList) {
                Log.d(TAG, "扫描到设备:" + bleScanBean.getAddress() + " 设备名:" + bleScanBean.getName());
                callback.onDeviceFound(bleScanBean, null);
            }

            @Override
            public void onStop(final List<BleScanBean> bleScanBeanList) {
                callback.onStop(bleScanBeanList);
            }
        });
    }
}
