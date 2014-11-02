package me.ycdev.android.demo.dbtest.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TestDbOpenHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "test.db";
    private static final int DB_VERSION = 1;

    public TestDbOpenHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TestTable.createTableIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing to do now
    }

    public static void clearDbRecords(Context cxt) {
        SQLiteDatabase db = new TestDbOpenHelper(cxt).getWritableDatabase();
        TestTable table = new TestTable(db);
        table.clearRecords();
        db.close();
    }

}