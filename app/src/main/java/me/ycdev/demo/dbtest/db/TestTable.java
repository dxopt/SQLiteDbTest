package me.ycdev.demo.dbtest.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

import java.util.Random;

import me.ycdev.androidlib.utils.DateTimeUtils;

public class TestTable {
    public static class DbRecord {
        public String createTime;
        public String updateTime;
        public int value;
        public String data;
    }

    private static final String TABLE_NAME = "test";
    private static final String FIELD_CREATE_TIME = "create_time";
    private static final String FIELD_UPDATE_TIME = "update_time";
    private static final String FIELD_VALUE = "value";
    private static final String FIELD_DATA = "data";

    private SQLiteDatabase mDb;

    public static void createTableIfNeeded(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY,"
                + FIELD_CREATE_TIME + " TEXT,"
                + FIELD_UPDATE_TIME + " TEXT,"
                + FIELD_VALUE + " INTEGER,"
                + FIELD_DATA + " CHAR);");
    }

    public TestTable(SQLiteDatabase db) {
        mDb = db;
    }

    public DbRecord query(long id) {
        String selection = BaseColumns._ID + "=" + id;
        String[] columns = new String[] {
                FIELD_CREATE_TIME, FIELD_UPDATE_TIME, FIELD_VALUE, FIELD_DATA
        };
        DbRecord record = null;
        Cursor cursor = mDb.query(TABLE_NAME, columns, selection, null, null, null, null);
        if (cursor.moveToFirst()) {
            record = new DbRecord();
            record.createTime = cursor.getString(0);
            record.updateTime = cursor.getString(1);
            record.value = cursor.getInt(2);
            record.data = cursor.getString(3);
        }
        cursor.close();
        return record;
    }

    /**
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long addNewRecord() {
        String curTime = DateTimeUtils.formatTimeForDisplay(System.currentTimeMillis());
        ContentValues values = new ContentValues();
        values.put(FIELD_CREATE_TIME, curTime);
        values.put(FIELD_UPDATE_TIME, curTime);
        values.put(FIELD_VALUE, 1);
        values.put(FIELD_DATA, generateData());
        // the following operaion may fail with the error
        // "android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5)"
        return mDb.insertOrThrow(TABLE_NAME, null, values);
    }

    private String generateData() {
        Random random = new Random();
        char[] buf = new char[256];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = (char)('a' + random.nextInt(26));
        }
        return new String(buf);
    }

    public void updateRecord(long id) {
        DbRecord record = query(id);
        if (record == null) {
            return;
        }

        String where = BaseColumns._ID + "=" + id;
        String curTime = DateTimeUtils.formatTimeForDisplay(System.currentTimeMillis());
        ContentValues values = new ContentValues();
        values.put(FIELD_UPDATE_TIME, curTime);
        values.put(FIELD_VALUE, record.value + 1);
        // the following operation may throw the exception
        // "android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5)"
        mDb.update(TABLE_NAME, values, where, null);
    }

    public void clearRecords() {
        mDb.delete(TABLE_NAME, null, null);
    }
}
