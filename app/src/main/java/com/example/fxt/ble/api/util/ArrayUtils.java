package com.example.fxt.ble.api.util;

import java.util.Arrays;


public class ArrayUtils {

    private ArrayUtils() {}

    public static byte[] addAll(byte[] first, byte[] second) {
        if (first==null){
            first = new byte[0];
        }
        if (second==null||second.length==0){
            return first;
        }
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] addAllByArray(byte[]...items) {
        if (items==null||items.length==0){
            return new byte[0];
        }
        byte[] result=items[0];
        if (result==null){
            return new byte[0];
        }
        for (int i=1;i<items.length;i++){
            if (items[i]==null){
                return new byte[0];
            }
            byte[] oldResult=result;
            result = Arrays.copyOf(result, result.length + items[i].length);
            System.arraycopy(items[i], 0, result, oldResult.length, items[i].length);
        }
        return result;
    }
}
