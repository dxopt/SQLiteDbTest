package me.ycdev.demo.dbtest.db2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TestDbOpenHelper2 extends SQLiteOpenHelper {
    private static final String DB_NAME = "test2.db";
    private static final int DB_VERSION = 1;

    public TestDbOpenHelper2(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        TestTable2.createTableIfNeeded(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // nothing to do now
    }

    public static void clearDbRecords(Context cxt) {
        SQLiteDatabase db = new TestDbOpenHelper2(cxt).getWritableDatabase();
        TestTable2 table = new TestTable2(db);
        table.clearRecords();
        db.close();
    }

}