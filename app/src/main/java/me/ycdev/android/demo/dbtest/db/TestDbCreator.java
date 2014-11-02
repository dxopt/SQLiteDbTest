package me.ycdev.android.demo.dbtest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import me.ycdev.android.lib.common.dbmgr.SQLiteDbCreator;

public class TestDbCreator implements SQLiteDbCreator {
    @Override
    public SQLiteDatabase createDb(Context cxt) {
        return new TestDbOpenHelper(cxt).getWritableDatabase();
    }
}
