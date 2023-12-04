package com.fiberfox.fxt.database;//package com.inno.innobluetooth.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库帮助类
 */
public class MySQLiteOpenHelperEnclosure extends SQLiteOpenHelper {
    private static final String DB_NAME = "fiberfox_enclosure.db";
    private static final int VERSION = 3;

    public MySQLiteOpenHelperEnclosure(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql_splice_data = "create table tb_enclosure_data (" +
                "_id INTEGER primary key autoincrement, " +
                "coordinate TEXT,imagePath TEXT,note TEXT,data_time TEXT, user TEXT," +
                "createTime TimeStamp NOT NULL DEFAULT (datetime('now','localtime')))";

        // 数据库创建
        db.execSQL(sql_splice_data);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {}
}
