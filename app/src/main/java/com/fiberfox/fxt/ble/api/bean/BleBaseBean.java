package com.fiberfox.fxt.ble.api.bean;


import com.fiberfox.fxt.ble.api.util.BleHexConvert;


public class BleBaseBean {

    /**
     * 完整命令
     */
    protected byte[] command;

    /**
     * 拆分的指令集，一条指令最多100个字节
     */
    protected String[] commands;

    /**
     * 完整命令的字符串格式
     */
    protected String commandStr;

    // 命令头
    protected byte[] header;

    // 数据类型
    protected byte type;

    // id(8字节)
    protected byte[] id;

    protected String idStr;

    // 总包数(2字节)
    protected byte[] totalPackage;

    protected int totalPackageInt;

    // 当前包数(2字节)
    protected byte[] currentPackage;

    protected int currentPackageInt;

    /**
     * 数据包
     */
    protected byte[] payload;

    /**
     * 数据包长度
     */
    protected byte[] length;

    protected int lengthInt;

    /**
     * 校验和
     */
    protected byte[] crc;


    public byte[] getCommand() {
        return command;
    }

    public String[] getCommands() {
        return commands;
    }

    public String getCommandStr() {
        return commandStr;
    }

    public byte[] getHeader() {
        return header;
    }

    public byte getType() {
        return type;
    }

    public byte[] getId() {
        return id;
    }

    public byte[] getTotalPackage() {
        return totalPackage;
    }

    public byte[] getCurrentPackage() {
        return currentPackage;
    }

    public byte[] getPayload() {
        return payload;
    }

    public String getPayloadStr() {
        return BleHexConvert.bytesToHexString(payload);
    }

    public byte[] getLength() {
        return length;
    }

    public int getLengthInt() {
        return lengthInt;
    }

    public byte[] getCrc() {
        return crc;
    }

    public int getTotalPackageInt() {
        return totalPackageInt;
    }

    public int getCurrentPackageInt() {
        return currentPackageInt;
    }

    public String getIdStr() {
        return idStr;
    }
}
