package me.ycdev.demo.dbtest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import me.ycdev.demo.dbtest.dbmgr.SQLiteDbCreator;

public class TestDbCreator implements SQLiteDbCreator {
    @Override
    public SQLiteDatabase createDb(Context cxt) {
        return new TestDbOpenHelper(cxt).getWritableDatabase();
    }
}
