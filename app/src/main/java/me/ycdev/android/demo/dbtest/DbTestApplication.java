package me.ycdev.android.demo.dbtest;

import android.app.Application;

import me.ycdev.android.demo.dbtest.utils.AppLogger;
import me.ycdev.android.demo.dbtest.utils.CrashHandler;

public class DbTestApplication extends Application {
    private static final String TAG = "DbTestApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.i(TAG, "app #onCreate");
        CrashHandler.setupDefaultHandler(this);
    }
}
