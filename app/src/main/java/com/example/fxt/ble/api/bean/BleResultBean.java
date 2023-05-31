package com.example.fxt.ble.api.bean;


import com.example.fxt.ble.api.util.BleHexConvert;
import com.example.fxt.ble.api.util.ByteUtil;


public class BleResultBean extends BleBaseBean {

    public BleResultBean() {}

    /**
     * @param commandStr 完整的16进制字符串格式命令
     */
    public BleResultBean(String commandStr) {
        this.command = BleHexConvert.parseHexStringToBytes(commandStr);
        this.commandStr = commandStr;
        resolveResult(command);
    }

    public BleResultBean(BleResultBean bleResultBean, byte[] payload) {
        this.idStr = ByteUtil.getAsciiString(bleResultBean.getId(),0,bleResultBean.getId().length-1);
        this.type = bleResultBean.getType();
        this.payload = payload;
    }

    /**
     * 完整的16进制格式命令
     */
    public BleResultBean(byte[] command) {
        this.command = command;
        this.commandStr = BleHexConvert.bytesToHexString(command);
        resolveResult(command);
    }

    /**
     * 解析命令
     * header 命令头；type数据类型；
     */
    private boolean resolveResult(byte[] command) {
        this.header = new byte[]{command[0], command[1], command[2]};
        this.type = command[3];
        this.id = new byte[8];
        System.arraycopy(command, 4, id, 0, 8);
        this.totalPackage = new byte[]{command[12], command[13]};
        this.currentPackage = new byte[]{command[14], command[15]};
        this.length = new byte[4];
        System.arraycopy(command, 16, length, 0, 4);

        this.lengthInt = ByteUtil.getIntByLittleMode(length, 0);
        this.payload = new byte[lengthInt];

        if (isGetFull()) {
            System.arraycopy(command, 20, payload, 0, lengthInt);
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否数据获取完整
     */
    public boolean isGetFull() {
        return command.length >= (lengthInt + 22);
    }
}
