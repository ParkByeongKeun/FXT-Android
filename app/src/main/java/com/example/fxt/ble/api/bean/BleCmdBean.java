package com.example.fxt.ble.api.bean;


import com.example.fxt.ble.api.util.BleCmdUtil;
import com.example.fxt.ble.api.util.BleHexConvert;


public class BleCmdBean extends BleBaseBean {

    /**
     * @param commandStr 完整的16进制字符串格式命令
     */
    public BleCmdBean(String commandStr) {
        this(BleHexConvert.parseHexStringToBytes(commandStr));
    }

    /**
     * 完整的16进制格式命令
     */
    public BleCmdBean(byte[] command) {
        this.command = command;
        groupCommand();
    }

    /**
     * 重新组合
     */
    private void groupCommand() {
        this.commandStr = BleHexConvert.bytesToHexString(command);
        // 拆分命令，分包发送
        this.commands = BleCmdUtil.splite(commandStr);
    }

    public void setHeader(byte[] header) {
        this.header = header;
        groupCommand();
    }

    public void setType(byte type) {
        this.type = type;
        groupCommand();
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
        groupCommand();
    }
}
