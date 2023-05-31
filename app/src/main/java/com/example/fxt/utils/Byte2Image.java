package com.example.fxt.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class Byte2Image {


    /**
     * 将图片内容解析成字节数组
     * @param inStream
     * @return byte[]
     * @throws Exception
     */
    public static byte[] readStream(InputStream inStream) throws Exception {
        byte[] buffer = new byte[1024];
        int len = -1;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        byte[] data = outStream.toByteArray();
        outStream.close();
        inStream.close();
        return data;

    }

    /**
     *  将字节数组转换为ImageView可调用的Bitmap对象
     * @param originalImageBytes
     * @param opts
     * @return Bitmap
     */
    public static Bitmap getPicFromBytes(byte[] originalImageBytes, BitmapFactory.Options opts) {
        // 将字节数组转换为Bitmap对象
        if (originalImageBytes != null){
            if (opts != null)
                return BitmapFactory.decodeByteArray(originalImageBytes, 0, originalImageBytes.length, opts);
            else
                return BitmapFactory.decodeByteArray(originalImageBytes, 0, originalImageBytes.length);
        }
        return null;
    }





    public static void bytes2ImageFile(byte[] bytes) {
        try {
            //将文件保存在路径“/storage/emulated/0/demo.jpg”
//            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/demo.jpg");

            FileOutputStream fos = new FileOutputStream("D:\\bbb1.bmp");
            fos.write(bytes, 0, bytes.length);
            fos.flush();
            fos.close();

        } catch (Exception e) {
            System.out.print("错误："+e.toString());
            e.printStackTrace();
        }
    }


}
