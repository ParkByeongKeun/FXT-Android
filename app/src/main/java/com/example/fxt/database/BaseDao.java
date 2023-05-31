package com.example.fxt.database;//package com.inno.innobluetooth.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * 基础dao类
 */
public abstract class BaseDao<T> {
    private Context mContext;               //上下文环境
    private MySQLiteOpenHelper helper;      //数据库帮助类
    protected SQLiteDatabase db;            //数据库对象

    public BaseDao(Context context) {
        this.mContext = context;
        helper = new MySQLiteOpenHelper(mContext);

        db = helper.getReadableDatabase();
    }

    // 关闭数据库的方法
    public void close() {
        db.close();
    }

    // 往数据库插入数据
    public abstract void insert(T t);

    public abstract void update(T t);

    // 根据id删除单条数据
    public abstract void deleteById(int id);

    // 根据id + sn + 时间 设置图片路径
    public abstract void setImagePathByIDAndSn(int id, String sn, String newImagePath);

    // 查询所有结果数据
    public abstract List<T> selectAllSpliceData();

    // 根据sn查询结果数据
    public abstract T selectResultDataByFileName(String sn);

}
