package com.fiberfox.fxt.ble.api.bean;


import com.fiberfox.fxt.ble.api.util.BleHexConvert;
import com.fiberfox.fxt.ble.api.util.ByteUtil;


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
        this.header = new byte[]{command[0]};
        byte[] t = new byte[]{command[1],command[2]};
        String encodedData = BleHexConvert.bytesToHexString(t);
        this.id = new byte[8];
        this.id =  Integer.toHexString(1).getBytes();
        this.idStr = String.valueOf(id);

        if(encodedData.contains("0001")) {
            this.type = 4;
        }else if (encodedData.contains("1101")){
            this.type = 2;
        }else {
            this.type = 1;
        }

        this.totalPackage = new byte[]{command[5], command[6]};
        this.currentPackage = new byte[]{command[7], command[8]};
        this.length = new byte[]{command[3], command[4]};


        this.lengthInt = ByteUtil.byteArrayToHex(length);



        if(encodedData.equals("1102")) {
            byte[] newByte = lastElementRemove(command);
            this.payload = newByte;
        }else {
            this.payload = new byte[lengthInt];
            System.arraycopy(command, 5, payload, 0, lengthInt);
        }
        return true;
    }

    public static byte[] lastElementRemove(byte[] srcArray) {
        byte[] newArray = new byte[srcArray.length - 3]; //2byte = CRC16 //1byte = tail
        for(int index = 0; index < srcArray.length - 3; index++) {
            newArray[index] = srcArray[index];
        }
        byte[] tempArray = new byte[newArray.length - 11]; //1byte = header, 2byte cmd, 2byte = length, 2byte = total num, 2byte = current num, 2byte = img index
        for(int i = 11 ; i < newArray.length ; i ++) {
            tempArray[i-11] = newArray[i];
        }
        return tempArray;
    }

    /**
     * 是否数据获取完整
     */
    public boolean isGetFull() {
        return command.length >= (lengthInt + 22);
    }
}
