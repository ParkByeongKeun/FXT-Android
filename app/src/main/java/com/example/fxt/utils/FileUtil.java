package com.example.fxt.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class FileUtil {

    private FileUtil(){}

    /**
     * 将图片保存到外部工共目录 系统相册中
     * @param b 图片
     * @param context ctx
     */
    public static void saveBitmapToDcim(Context context, Bitmap b, String fileName){
//        String fileName = String.valueOf(System.currentTimeMillis()) + ".jpg";
        try{
            OutputStream fos;
            ContentResolver resolver = context.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));

            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Objects.requireNonNull(fos).close();
        }catch (IOException e) {
            // Log Message
            e.printStackTrace();
        }
    }

    /**
     * 保存到应用沙箱中
     *
     * @param b 图片
     * @param strFileName 图片名
     */
    public static String saveBitmapToSandBox(Context context, Bitmap b, String strFileName) {
        // 创建文件路径
        String filePath = context.getFilesDir() + File.separator + strFileName;
        File file = new File(filePath);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return filePath;
    }

    /**
     * 加载本地图片
     *
     * @param url 本地图片路径
     * @return bitmap
     */
    public static Bitmap getLocalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            // 把流转化为Bitmap图片
            return BitmapFactory.decodeStream(fis);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
