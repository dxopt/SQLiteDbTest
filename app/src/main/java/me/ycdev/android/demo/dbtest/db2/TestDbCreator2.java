package me.ycdev.android.demo.dbtest.db2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import me.ycdev.android.lib.common.dbmgr.SQLiteDbCreator;

public class TestDbCreator2 implements SQLiteDbCreator {
    @Override
    public SQLiteDatabase createDb(Context cxt) {
        return new TestDbOpenHelper2(cxt).getWritableDatabase();
    }
}
