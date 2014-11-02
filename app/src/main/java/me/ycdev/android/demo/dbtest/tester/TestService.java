package me.ycdev.android.demo.dbtest.tester;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class TestService extends Service {
    private static final String EXTRA_OPTION = "extra.option";

    private BaseTester mCurTester;

    public TestService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mCurTester == null) {
            TestOption option = intent.getParcelableExtra(EXTRA_OPTION);
            mCurTester = new MultiThreadTester(this, option);
            mCurTester.startTest();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCurTester != null) {
            mCurTester.stopTest();
        }
    }

    public static void startTest(Context cxt, TestOption option) {
        Intent intent = new Intent(cxt, TestService.class);
        intent.putExtra(EXTRA_OPTION, option);
        cxt.startService(intent);
    }

    public static void stopTest(Context cxt) {
        Intent intent = new Intent(cxt, TestService.class);
        cxt.stopService(intent);
    }
}
