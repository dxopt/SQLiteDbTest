package me.ycdev.demo.dbtest;

import android.app.Application;

import me.ycdev.demo.dbtest.utils.AppLogger;
import me.ycdev.demo.dbtest.utils.CrashHandler;

public class DbTestApplication extends Application {
    private static final String TAG = "DbTestApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        AppLogger.i(TAG, "app #onCreate");
        CrashHandler.setupDefaultHandler(this);
    }
}
