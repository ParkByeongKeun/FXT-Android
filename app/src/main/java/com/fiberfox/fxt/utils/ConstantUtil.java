package com.fiberfox.fxt.utils;


import android.os.Environment;

import java.io.File;

public final class ConstantUtil {

    public static final class StrConstant{
       public static String DEVICE_NAME = "device_name";
       public static String DEVICE_ADDRESS = "device_address";
       public static String BEAN = "bean";
        public static String YOLO = "yolo";
    }

    //文件路径相关模块
    private static final String BASE_PATH = Environment.getExternalStorageDirectory() + File.separator;
    public static final String PNG_PATH = BASE_PATH + "png" + File.separator;

}
