package me.ycdev.android.demo.dbtest.tester;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.*;
import android.os.Process;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import me.ycdev.android.demo.dbtest.db.TestDbCreator;
import me.ycdev.android.demo.dbtest.db.TestDbOpenHelper;
import me.ycdev.android.demo.dbtest.db.TestTable;
import me.ycdev.android.demo.dbtest.db2.TestDbCreator2;
import me.ycdev.android.demo.dbtest.db2.TestDbOpenHelper2;
import me.ycdev.android.demo.dbtest.db2.TestTable2;
import me.ycdev.android.lib.common.dbmgr.SQLiteDbMgr;
import me.ycdev.android.demo.dbtest.utils.AppLogger;

public class SingleThreadTester extends BaseTester implements Runnable {
    private static final String TAG = "SingleThreadTester";

    private static AtomicInteger sIndexMgr = new AtomicInteger(0);
    private static volatile SQLiteOpenHelper sDbOpenHelper;
    private static volatile SQLiteOpenHelper sDbOpenHelper2;

    private int mIndex;
    private Thread mThread;

    public SingleThreadTester(Context cxt, TestOption option) {
        super(cxt, TAG, option);
        mIndex = sIndexMgr.incrementAndGet();
    }

    @Override
    protected void doStartTest() {
        mThread = new Thread(this);
        mThread.start();
    }

    @Override
    protected void doStopTest() {
        mThread.interrupt();
    }

    private SQLiteDatabase getDatabase() {
        if (mTestOption.mode == TestOption.MODE_RECOMMEND) {
            if (mIndex % 2 == 1) {
                return SQLiteDbMgr.acquireDatabase(mAppContext, TestDbCreator.class);
            } else {
                return SQLiteDbMgr.acquireDatabase(mAppContext, TestDbCreator2.class);
            }
        } else if (mTestOption.mode == TestOption.MODE_SINGLE_OPEN_HELPER) {
            if (sDbOpenHelper == null || sDbOpenHelper2 == null) {
                synchronized (SingleThreadTester.class) {
                    if (sDbOpenHelper == null) {
                        sDbOpenHelper = new TestDbOpenHelper(mAppContext);
                    }
                    if (sDbOpenHelper2 == null) {
                        sDbOpenHelper2 = new TestDbOpenHelper2(mAppContext);
                    }
                }
            }
            if (mIndex % 2 == 1) {
                return sDbOpenHelper.getWritableDatabase();
            } else {
                return sDbOpenHelper2.getWritableDatabase();
            }
        } else if (mTestOption.mode == TestOption.MODE_MULTIPLE_OPEN_HELPER) {
            if (mIndex % 2 == 1) {
                return new TestDbOpenHelper(mAppContext).getWritableDatabase();
            } else {
                return new TestDbOpenHelper2(mAppContext).getWritableDatabase();
            }
        }
        return null;
    }

    private void releaseDatabase(SQLiteDatabase db) {
        if (mTestOption.mode == TestOption.MODE_RECOMMEND) {
            if (mIndex % 2 == 1) {
                SQLiteDbMgr.releaseDatabase(mAppContext, TestDbCreator.class);
            } else {
                SQLiteDbMgr.releaseDatabase(mAppContext, TestDbCreator2.class);
            }
        } else if (mTestOption.mode == TestOption.MODE_SINGLE_OPEN_HELPER) {
            // don't close DB and helper
        } else if (mTestOption.mode == TestOption.MODE_MULTIPLE_OPEN_HELPER) {
            db.close();
        }
    }

    @Override
    public void run() {
        int pid = Process.myPid();
        long tid = Thread.currentThread().getId();
        AppLogger.i(TAG, "tester is running, " + "pid: " + pid + ", tid: " + tid + ", #" + mIndex);
        Random random = new Random();
        int lastRowId = 1;
        while (mIsRunning) {
            SQLiteDatabase db = getDatabase();
            long startTime = SystemClock.uptimeMillis();
            int type = random.nextInt(3) + 1;
            if (mIndex % 2 == 1) {
                TestTable table = new TestTable(db);
                if (type == 1) { // query
                    long rowId = random.nextInt(lastRowId) + 1;
                    table.query(rowId);
                } else if (type == 2) { // insert
                    long rowId = table.addNewRecord();
                    if (rowId != -1 && rowId < Integer.MAX_VALUE) {
                        lastRowId = (int) rowId;
                    }
                } else if (type == 3) { // update
                    long rowId = random.nextInt(lastRowId) + 1;
                    table.updateRecord(rowId);
                }
            } else {
                TestTable2 table = new TestTable2(db);
                if (type == 1) { // query
                    long rowId = random.nextInt(lastRowId) + 1;
                    table.query(rowId);
                } else if (type == 2) { // insert
                    long rowId = table.addNewRecord();
                    if (rowId != -1 && rowId < Integer.MAX_VALUE) {
                        lastRowId = (int) rowId;
                    }
                } else if (type == 3) { // update
                    long rowId = random.nextInt(lastRowId) + 1;
                    table.updateRecord(rowId);
                }
            }
            releaseDatabase(db);

            long timeUsed = SystemClock.uptimeMillis() - startTime;
            int sleepTime = random.nextInt(50) + 1;

            AppLogger.d(TAG, "pid: " + pid + ", tid: " + tid + ", #" + mIndex
                    + ", did action: " + type + ", timeUsed: " + timeUsed + ", to sleep: " + sleepTime);
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        AppLogger.i(TAG, "tester is stopped, " + "pid: " + pid + ", tid: " + tid + ", #" + mIndex);
    }
}
