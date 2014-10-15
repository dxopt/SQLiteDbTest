package me.ycdev.demo.dbtest.tester;

import android.content.Context;

import me.ycdev.demo.dbtest.utils.AppLogger;

public abstract class BaseTester {
    private final String TAG;

    protected Context mAppContext;
    protected boolean mIsRunning;
    protected TestOption mTestOption;

    public BaseTester(Context cxt, String logTag, TestOption option) {
        mAppContext = cxt.getApplicationContext();
        TAG = logTag;
        mTestOption = option;
    }

    public synchronized void startTest() {
        if (mIsRunning) {
            AppLogger.w(TAG, "tester is running");
            return;
        }

        mIsRunning = true;
        doStartTest();
    }

    protected abstract void doStartTest();

    public synchronized void stopTest() {
        mIsRunning = false;
        doStopTest();
    }

    protected abstract void doStopTest();
}
