package com.example.fxt.ble.api.util;


import com.example.fxt.utils.StringUtil;


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
}
