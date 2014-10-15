package me.ycdev.demo.dbtest.tester;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.*;
import android.os.Process;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import me.ycdev.demo.dbtest.db.TestDbOpenHelper;
import me.ycdev.demo.dbtest.db.TestTable;
import me.ycdev.demo.dbtest.utils.AppLogger;

public class SingleThreadTester extends BaseTester implements Runnable {
    private static final String TAG = "SingleThreadTester";

    private static AtomicInteger sIndexMgr = new AtomicInteger(0);

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

    @Override
    public void run() {
        int pid = Process.myPid();
        long tid = Thread.currentThread().getId();
        AppLogger.i(TAG, "tester is running, " + "pid: " + pid + ", tid: " + tid + ", #" + mIndex);
        Random random = new Random();
        int lastRowId = 1;
        while (mIsRunning) {
            SQLiteDatabase db = new TestDbOpenHelper(mAppContext).getWritableDatabase();
            if (db.isReadOnly()) {
                AppLogger.w(TAG, "db was opened readonly");
            }
            long startTime = SystemClock.uptimeMillis();
            TestTable table = new TestTable(db);
            int type = random.nextInt(3) + 1;
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
            if (mTestOption.needCloseDb) {
                db.close();
            }

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
