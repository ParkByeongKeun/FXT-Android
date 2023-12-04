package com.fiberfox.fxt.ble.api.util;


import com.fiberfox.fxt.utils.StringUtil;


public class BleCmdUtil {

    private static final int MAX_LENGTH = 100;

    private BleCmdUtil(){}

    /**
     * 命令拆分
     */
    public static String[] splite(String hexCmd) {
        String[] cmds = null;
        if (StringUtil.isNotBlank(hexCmd)) {
            String cmd = hexCmd.toLowerCase().startsWith("0x") ? hexCmd.substring(2) : hexCmd;
            if (cmd.length() <= MAX_LENGTH) {
                cmds = new String[]{cmd};
            } else {
                int length = cmd.length() / MAX_LENGTH;
                int left = cmd.length() % MAX_LENGTH;
                cmds = new String[length + (left > 0 ? 1 : 0)];
                for (int i = 0; i < length; i++) {
                    cmds[i] = cmd.substring(i * MAX_LENGTH, (i + 1) * MAX_LENGTH);
                }
                if (cmds.length == length + 1) {
                    cmds[length] = cmd.substring(length * MAX_LENGTH);
                }
            }
        }
        return cmds;
    }


    /**
     * 给命令增加CRC 16进制
     */
    public static String getCRCCmd(String hex) {
        String hexTemp = hex;
        if (hex.toLowerCase().startsWith("0x")) {
            hexTemp = hex.substring(4);
        } else if (hex.toLowerCase().startsWith("aa")) {
            hexTemp = hex.substring(2);
        }
        return hex + getCRCStr(hexTemp);
    }

    /**
     * 获取CRC 16进制
     */
    public static String getCRCStr(String data) {
        byte[] bytes = BleHexConvert.parseHexStringToBytes(data);
        return getCRCStr(bytes);
    }

    /**
     * 获取CRC 16进制
     */
    public static String getCRCStr(byte[] data) {
        int crc = CRC16.calcCrc16(data);
        return String.format("%04X", crc);
    }

    /**
     * 根据指令获取到crc
     */
    public static byte[] getCRCBytes(byte[] src) {
        String crcStr = getCRCStr(src);
        return BleHexConvert.parseHexStringToBytes(crcStr);
    }

}
