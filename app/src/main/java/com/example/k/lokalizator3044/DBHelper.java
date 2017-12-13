package com.example.k.lokalizator3044;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBHelper extends SQLiteOpenHelper {

    public final static int DB_VERSION = 1;
    public final static String DB_NAME = "itagsdb.db";
    public final static String TABLE_NAME = "itagsdb";
    public final static String ID = "_id";
    public final static String MAC_ADDRESS = "macAddress";
    public final static String NAME = "name";
    public final static String WORKING_MODE = "workingMode";
    public final static String RINGTONE = "ringtone";
    public final static String DISTANCE = "distance";
    public final static String CLICK = "click";
    public final static String DOUBLE_CLICK = "doubleClick";
    //public final static String IF_ENABLED = "ifEnabled";

    //ifen
    public final static String CREATE_BASE = "CREATE TABLE "+ TABLE_NAME +"("+ID+" integer primary key autoincrement, "+ MAC_ADDRESS +" text not null, "+ NAME +" text not null, "+ WORKING_MODE +" text not null, "+ RINGTONE +" text not null, "+ DISTANCE +" text not null, "+ CLICK +" text not null, "+ DOUBLE_CLICK +" text not null);";
    private static final String DELETE_BASE = "DROP TABLE IF EXISTS "+ TABLE_NAME;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BASE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_BASE);
        onCreate(db);
    }
}
