package com.example.fxt.ble.device.base;


import static com.example.fxt.ble.api.util.ByteUtil.getShortByLittleMode;

import android.util.Log;

import com.example.fxt.ble.api.BleAPI;
import com.example.fxt.ble.api.bean.BleCmdBean;
import com.example.fxt.ble.api.bean.BleResultBean;
import com.example.fxt.ble.api.callback.BleCmdCallback;
import com.example.fxt.ble.api.event.ResultEvent;
import com.example.fxt.ble.api.event.WriteEvent;
import com.example.fxt.ble.api.event.base.BaseEvent;
import com.example.fxt.ble.api.util.BleHexConvert;
import com.example.fxt.ble.device.splicer.BleSplicerCallback;
import com.example.fxt.ble.device.splicer.bean.FeedbackBean;
import com.example.fxt.utils.StringUtil;
import com.google.gson.Gson;


public abstract class BleBaseRequest<T extends BleResultBean> implements BleCmdCallback {

    private static final String TAG = "LYJ BleBaseRequest";

    private BleSplicerCallback mBleSplicerCallback;

    /**
     * 设备mac地址
     */
    private String mAddress;

    /**
     * 指令
     */
    private String mCommandStr;

    /**
     * 最大尝试重发次数
     */
    private static final int MAX_RETRY_TIMES = 1;

    /**
     * 已尝试重发次数
     */
    private int hasRetryTimes = 0;

    private byte[] allData = null;

    private int currentIndex;

    private Gson gson;

    public BleBaseRequest() {
    }

    public BleBaseRequest(String address, BleSplicerCallback bleSplicerCallback) {
        mBleSplicerCallback = bleSplicerCallback;
        mAddress = address;
    }

    public String getCommandStr() {
        return mCommandStr;
    }

    public void setCommandStr(String commandStr) {
        mCommandStr = commandStr;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public BleSplicerCallback getBleSplicerCallback() {
        return mBleSplicerCallback;
    }

    public void setBleSplicerCallback(BleSplicerCallback bleSplicerCallback) {
        mBleSplicerCallback = bleSplicerCallback;
    }

    /**
     * 添加数据接收监听
     */
    public void addReceiveCallback(){
        BleAPI.getBleWrapper().setCmdCallback(this);
    }

    /**
     * 发送指令
     */
    public void send() {
        if (mBleSplicerCallback == null) {
            Log.d(TAG, "mBleLockCallback发送时不可为null");
            return;
        }

        if (StringUtil.isBlank(mAddress)) {
            mBleSplicerCallback.onFailed(BaseEvent.CODE_ERROR_ADDRESS, "device address cannot be empty");
            return;
        }

        if (mCommandStr == null || "".equals(mCommandStr.trim())) {
            mBleSplicerCallback.onFailed(BaseEvent.CODE_ERROR_CMD_FORMAT, "instruction cannot be empty");
            return;
        }

        Log.e(TAG, "蓝牙指令:" + mCommandStr + " 蓝牙地址:" + mAddress);
        sendCmd(mCommandStr, mAddress, this);
    }

    /**
     * 发送指令
     * @param cmd 16进制指令 无需关心长度
     */
    private void sendCmd(String cmd, String address, BleCmdCallback bleCmdCallback) {
        try {
            BleAPI.getBleWrapper().sendCmd(new BleCmdBean(cmd), address, bleCmdCallback);
        } catch (Exception e) {
            Log.e(TAG, "设置new BleCmdBean()错误：" + e.toString());
            BleAPI.getBleWrapper().sendCmd(null, address, bleCmdCallback);
        }
    }

    /**
     * 命令写入蓝牙事件
     * 注：蓝牙写入回调与蓝牙反馈的回调没有先后顺序，这里需要注意一些先后逻辑
     */
    @Override
    public void onCmdWrite(WriteEvent event) {
        // 蓝牙回调接口为null，不处理
        if (mBleSplicerCallback == null) {
            return;
        }

        if (event.getBleCmdBean() == null) {
            mBleSplicerCallback.onFailed(event.getCode(), event.getMsg());
            Log.d(TAG, "onFailed(onCmdWrite): 指令格式错误");
            return;
        }

        if (event.getCode() == BaseEvent.CODE_SUCCESS) {
            mBleSplicerCallback.onSuccess(new BleResultBean());
            return;
        }

        // 设备无应答,尝试重新发送
        if (hasRetryTimes < MAX_RETRY_TIMES && BleAPI.getBleWrapper().isConnected()) {
            Log.d(TAG, "尝试重发蓝牙指令" + mCommandStr);
            hasRetryTimes++;
            sendCmd(mCommandStr, mAddress, this);
        } else {
            hasRetryTimes = 0;
//            BleAPI.removeCmdCallback();
            Log.e(TAG, "onFailed(onCmdWrite):" + event.getMsg() + ":" + event.getCode());
            mBleSplicerCallback.onFailed(event.getCode(), event.getMsg());
//            mBleSplicerCallback = null;
        }
    }

    /**
     * 命令接收事件
     */
    @Override
    public void onReceiveResult(ResultEvent event) {
        if (mBleSplicerCallback == null) {
            return;
        }

        BleAPI.setAcceptBleCommand(event.getResultBean().getCommandStr());

        if (event.getCode() == BaseEvent.CODE_SUCCESS) {
            Log.i(TAG, "onReceiveResult onSuccess:" + event.getResultBean().getCommandStr());

            T resultBean = getResultBean(event.getResultBean());
            byte[] newData = resultBean.getPayload();
            int total = getShortByLittleMode(resultBean.getTotalPackage(), 0);
            int current = getShortByLittleMode(resultBean.getCurrentPackage(), 0);
            Log.i(TAG, "total:" + total);
            Log.i(TAG, "current:" + current);
            if (current == 1){
                allData = null;
                currentIndex = 0;
            }

            if (currentIndex == current){
                if (gson == null){
                    gson = new Gson();
                }
                FeedbackBean feedbackBean = new FeedbackBean();
                String json = gson.toJson(feedbackBean);
                Log.i(TAG, "反馈 json: "+ json);
                setCommandStr(BleHexConvert.strToHexString("ACK"+json));
                sendCmd(mCommandStr, mAddress, this);
                return;
            }

            if (allData == null) {
                allData = newData;
            }else {
                byte[] temp = allData;
                allData = new byte[temp.length + newData.length];
                System.arraycopy(temp, 0, allData, 0, temp.length);
                System.arraycopy(newData, 0, allData, temp.length, newData.length);
            }

            currentIndex = current;

            // 包没有收完
            if (current <= total){
                if (gson == null){
                    gson = new Gson();
                }
                FeedbackBean feedbackBean = new FeedbackBean();
                String json = gson.toJson(feedbackBean);
                Log.i(TAG, "反馈 json: "+ json);
                setCommandStr(BleHexConvert.strToHexString("ACK"+json));
                sendCmd(mCommandStr, mAddress, this);
            }

            Log.i(TAG, "allData length:" + allData.length);

            if (current == total){
                String res = BleHexConvert.bytesToHexString(allData);
                Log.e(TAG, "所有数据 allData(hex string)：" + res);

                mBleSplicerCallback.onReceiveSuccess(getResultBean(resultBean, allData));
                allData = null;
                currentIndex = 0;
            }

        } else {
            Log.d(TAG, "onFailed(onReceiveResult):" + event.getMsg() + ":" + event.getCode());

            if (BleAPI.getBleWrapper().isConnected()) {
                if (gson == null){
                    gson = new Gson();
                }
                FeedbackBean feedbackBean = new FeedbackBean(1);
                String json = gson.toJson(feedbackBean);
                Log.i(TAG, "发送失败的回调 json: "+ json);
                setCommandStr(BleHexConvert.strToHexString("ACK"+json));
                sendCmd(mCommandStr, mAddress, this);

                mBleSplicerCallback.onFailed(event.getCode(), event.getMsg());
                allData = null;
//                mBleSplicerCallback = null;
            }
        }
    }

    private BleResultBean getResultBean(BleResultBean bleResultBean, byte[] hexStr) {
        return new BleResultBean(bleResultBean, hexStr);
    }

    public T getResultBean(BleResultBean resultBean) {
        return (T) resultBean;
    }

    @Override
    public boolean isNeedReceiveMore(ResultEvent event) {
        return false;
    }
}