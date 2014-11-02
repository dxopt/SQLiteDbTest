package me.ycdev.demo.dbtest.utils;

import android.content.Context;
import android.os.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import me.ycdev.android.lib.common.utils.DateTimeUtils;
import me.ycdev.android.lib.common.utils.StringUtils;

public class CrashHandler implements UncaughtExceptionHandler {
    private static final String TAG = "CrashHandler";

    private UncaughtExceptionHandler mDefaultHandler;
    private Context mAppContext;

    private CrashHandler(Context context) {
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        mAppContext = context.getApplicationContext();
    }

    public static void setupDefaultHandler(Context context) {
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(context));
    }

    @Override
    public void uncaughtException(Thread thread, Throwable e) {
        if (e == null) {
            return;
        }

        try {
            dumpCrashToSD(e);
        } catch (Throwable e1) {
            AppLogger.w(TAG, "failed to process crash", e1);
        }

        // rethrow the exception
        if (mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, e);
        }
    }

    private void dumpCrashToSD(Throwable throwable) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return; // no SD card available
        }

        long curTime = System.currentTimeMillis();
        File sdRoot = Environment.getExternalStorageDirectory();
        File logDir = new File(sdRoot, Constants.EXTERNAL_STORAGE_PATH_LOG_DIR);
        logDir.mkdirs();
        String fileName = String.format(Constants.CRASH_LOG_FILENAME_TEMPLATE,
                DateTimeUtils.generateFileName(curTime));
        File crashLog = new File(logDir, fileName);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(crashLog)));
            printEnvironmentInfo(pw, curTime);
            pw.println();
            throwable.printStackTrace(pw);
            pw.close();
        } catch (Exception e) {
            AppLogger.w(TAG, "cannot dump crash logs: " + e);
        }
    }

    private void printEnvironmentInfo(PrintWriter pw, long curTime) {
        pw.println(StringUtils.formatDateTime(curTime));
        pw.println("PID: " + android.os.Process.myPid());
        pw.println("TID: " + Thread.currentThread().getId());

        pw.print("OS Version: ");
        pw.print(Build.VERSION.RELEASE);
        pw.print("_");
        pw.println(Build.VERSION.SDK_INT);

        pw.println("Vendor: " + Build.MANUFACTURER);

        pw.println("Model: " + Build.MODEL);

        pw.println("CPU ABI: " + Build.CPU_ABI);
        pw.println("CPU API2: " + Build.CPU_ABI2);

    }

}
