package me.ycdev.android.demo.dbtest.tester;

import android.content.Context;

public class MultiThreadTester extends BaseTester {
    private static final String TAG = "MultiThreadTester";

    private SingleThreadTester[] mTesters;

    public MultiThreadTester(Context cxt, TestOption option) {
        super(cxt, TAG, option);
    }

    @Override
    protected void doStartTest() {
        mTesters = new SingleThreadTester[mTestOption.threadCount];
        for (int i = 0; i < mTesters.length; i++) {
            mTesters[i] = new SingleThreadTester(mAppContext, mTestOption);
            mTesters[i].startTest();
        }
    }

    @Override
    protected void doStopTest() {
        for (int i = 0; i < mTesters.length; i++) {
            mTesters[i].stopTest();
        }
    }

}
