package com.example.fxt.database;//package com.inno.innobluetooth.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 */
public class MySQLiteOpenHelperSplice extends SQLiteOpenHelper {
    private static final String DB_NAME = "fiberfox_ble.db";
    private static final int VERSION = 11;

    public MySQLiteOpenHelperSplice(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 熔接记录结果数据表
        String sql_splice_data = "create table tb_splice_data (" +
                "_id INTEGER primary key autoincrement, " +
                "id REAL," +
                "sn TEXT,app_ver TEXT,fpga_ver TEXT,manufacturer TEXT," +
                "model TEXT,brand TEXT,splice_name TEXT,data_time TEXT," +
                "updateTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime'))," +
                "createTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')))";

        // fiber表
        String sql_splice_fiber_data = "create table tb_splice_fiber_data (" +
                "_id INTEGER primary key autoincrement, " +
                "splice_data_id INTEGER," +
                "loss REAL,left_angle REAL,right_angle REAL,core_angle REAL,core_offset REAL," +
                "splice_result INTEGER,error_value INTEGER,fuse_image_name text," +
                "updateTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime'))," +
                "createTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')))";


        // 数据库创建
        db.execSQL(sql_splice_data);
        db.execSQL(sql_splice_fiber_data);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}
}
