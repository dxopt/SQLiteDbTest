package me.ycdev.demo.dbtest.dbmgr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public interface SQLiteDbCreator {
    public abstract SQLiteDatabase createDb(Context cxt);
}
