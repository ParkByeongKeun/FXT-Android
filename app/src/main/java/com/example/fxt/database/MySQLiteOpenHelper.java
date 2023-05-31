package com.example.fxt.database;//package com.inno.innobluetooth.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "fiberfox_ofi.db";
    private static final int VERSION = 1;

    public MySQLiteOpenHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_splice_data = "create table tb_ofi_data (" +
                "_id INTEGER primary key autoincrement, " +
                "frequency TEXT,direction TEXT,measure TEXT,location TEXT,memo TEXT,note TEXT,data_time TEXT,serial TEXT" +
                "createTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')))";

        String sql_splice_fiber_data = "create table tb_fnms_data (" +
                "_id INTEGER primary key autoincrement, " +
                "user TEXT,salt TEXT,hash TEXT,note TEXT,data_time TEXT," +
                "createTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')))";

        // 数据库创建
        db.execSQL(sql_splice_data);
        db.execSQL(sql_splice_fiber_data);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}
}
