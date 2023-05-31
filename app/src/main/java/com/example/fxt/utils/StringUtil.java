package com.example.fxt.utils;

public class StringUtil {

    private StringUtil(){}

    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(cs.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }


    /**
     * 判断两个字符串是否相等，都为null时返回false
     */
    public static boolean equals(String str1, String str2) {
        if (str1 != null) {
            return str1.equals(str2);
        } else {
            return false;
        }
    }

    /**
     * 字符串是否为空，空的定义如下 1、为null <br>
     * 2、为""<br>
     *
     * @param str 被检测的字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}
