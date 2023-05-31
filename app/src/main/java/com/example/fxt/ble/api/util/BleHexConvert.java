package com.example.fxt.ble.api.util;

import android.util.Log;

import java.math.BigInteger;


public class BleHexConvert {

    private BleHexConvert(){}

    /**
     * 将16进制转换成byte[]
     */
    public static byte[] parseHexStringToBytes(final String hex) {
        String finalHex = hex.toLowerCase();
        if (hex.toLowerCase().indexOf("0x") >= 0) {
            finalHex = hex.substring(2);
        }
        String tmp = finalHex.replaceAll("[^[0-9][a-f][A-F]]", "");
        byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally

        String part = "";

        for (int i = 0; i < bytes.length; ++i) {
            part = "0x" + tmp.substring(i * 2, i * 2 + 2);
            bytes[i] = Long.decode(part).byteValue();
        }
        return bytes;
    }

    /**
     * lyj  十六进制串转化为byte数组
     */
    public static byte[] hex2byte(String hex) {
        if (hex.length() % 2 != 0) {
            hex = "0" + hex;
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    /**
     * 将byte 转换成 16进制
     */
    public static String byteToHexString(byte src) {
        String hexStr = "";
        int v = src & 0xFF;
        String hv = Integer.toHexString(v).toUpperCase();
        if (hv.length() < 2) {
            hexStr += "0";
        }
        hexStr += hv;
        return hexStr;
    }

    /**
     * 将byte[] 转换成 16进制
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    /**
     * 字符串转16进制(utf-8)编码
     *
     * @param s 原始字符串
     */
    public static String strToHexString(String s) {
        if (s == null || s.equals("")) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try {
            char c;
            for (int i = 0; i < s.length(); i++) {
                c = s.charAt(i);
                if (c >= 0 && c <= 255) {
                    sb.append(Integer.toHexString(c).toUpperCase());
                } else {
                    byte[] b;
                    b = Character.toString(c).getBytes("utf-8");
                    for (int j = 0; j < b.length; j++) {
                        int k = b[j];
                        k = k < 0 ? k + 256 : k;
                        //返回整数参数的字符串表示形式 作为十六进制（base16）中的无符号整数
                        //该值以十六进制（base16）转换为ASCII数字的字符串
                        sb.append(Integer.toHexString(k).toUpperCase());
                    }
                }
            }
        } catch (Exception e) {
            Log.d("BleHexConvert", e.getMessage());
        }
        return sb.toString();
    }

    /**
     * 字符串异或处理
     */
    public static String stringXor(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return "";
        }
        if (str1 == null) {
            return str2;
        }
        if (str2 == null) {
            return str1;
        }
        BigInteger big1 = new BigInteger(str1, 16);
        BigInteger big2 = new BigInteger(str2, 16);
        String resutlt = big1.xor(big2).toString(16);
        int leftCount = str2.length() - resutlt.length();
        for (int i = 0; i < leftCount; i++) {
            resutlt = "0" + resutlt;
        }
        return resutlt;
    }

    public static String getEncryption(String para, String key) {
        byte[] keyArray = parseHexStringToBytes(key);
        byte[] byteArray = parseHexStringToBytes(para);
        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = (byte) (byteArray[i] ^ keyArray[i]);
        }
        return bytesToHexString(byteArray);
    }
}
