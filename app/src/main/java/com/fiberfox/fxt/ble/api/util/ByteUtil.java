package com.fiberfox.fxt.ble.api.util;

public class ByteUtil {

    private ByteUtil(){}

    /**
     * 通过byte数组取到short(小端模式)
     */
    public static short getShortByLittleMode(byte[] b, int index) {
        return (short) ((b[index + 1] << 8) | b[index] & 0xff);
    }

    public static int getIntByLittleMode(byte[] b, int index) {
        int res = 0;
        for (int i = 1; i >= 0; i--){
            res = res << 8 | b[i + index] & 0xff;
        }
        return res;
    }

    public static int getIntByDec(byte[] b, int index) {
        int value = ((b[1] & 0xFF) << 8) | (b[0] & 0xFF);
        return value;
    }
    public static int byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder();
        for(final byte b: a)
            sb.append(String.format("%02x", b/*&0xff*/));

        return Integer.parseInt(sb.toString(),16);
    }
    /**
     * 转换short为byte(大端模式：高位在前)
     */
    public static void putShortByBigMode(byte[] b, short s, int index) {
        b[index + 0] = (byte) (s >> 8);
        b[index + 1] = (byte) (s >> 0);
    }

    /**
     * 通过byte数组取到short(大端模式：高位在前)
     */
    public static short getShortByBigMode(byte[] b, int index) {
        return (short) ((b[index + 0] << 8) | b[index + 1] & 0xff);
    }

    /**
     * byte[] 转Ascii 字符串
     */
    public static String getAsciiString(byte[] b, int index, int length) {
        StringBuilder stringBuilder = new StringBuilder();
        if (index < 0 || index >= b.length) {
            return null;
        }
        if (b.length < length + index) {
            length = b.length - index;
        }
        char[] tChars = new char[length];
        for (int i = 0; i < length; i++) {
            tChars[i] = (char) b[i + index];
        }
        stringBuilder.append(tChars);
        return stringBuilder.toString();
    }

    public static String convertToASCII(String string) {
        StringBuilder sb = new StringBuilder();
        char[] ch = string.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            sb.append(Integer.valueOf(ch[i]).intValue());
        }
        return sb.toString();
    }

    /**
     * 16进制ascii
     * @param string
     * @return
     */
    public static String convertToASCII16(String string) {
        StringBuilder sb = new StringBuilder();
        char[] ch = string.toCharArray();
        for (int i = 0; i < ch.length; i++) {
            sb.append(BleHexConvert.byteToHexString(Byte.parseByte(Integer.toString(ch[i]))));
        }
        return sb.toString();
    }
}
