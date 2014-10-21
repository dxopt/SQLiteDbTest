package me.ycdev.demo.dbtest.db2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import me.ycdev.demo.dbtest.dbmgr.SQLiteDbCreator;

public class TestDbCreator2 implements SQLiteDbCreator {
    @Override
    public SQLiteDatabase createDb(Context cxt) {
        return new TestDbOpenHelper2(cxt).getWritableDatabase();
    }
}
