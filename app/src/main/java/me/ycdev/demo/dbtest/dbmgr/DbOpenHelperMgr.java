package me.ycdev.demo.dbtest.dbmgr;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Constructor;
import java.util.HashMap;

public class DbOpenHelperMgr {
    private static class HelperInfo {
        SQLiteOpenHelper helper;
        int mReferenceCount;
    }

    private Context mAppContext;
    private HashMap<Class<? extends SQLiteOpenHelper>, HelperInfo> mOpenHelpers
            = new HashMap<Class<? extends SQLiteOpenHelper>, HelperInfo>();

    private static volatile DbOpenHelperMgr sInstance;

    private DbOpenHelperMgr(Context cxt) {
        mAppContext = cxt.getApplicationContext();
    }

    public static DbOpenHelperMgr getInstance(Context cxt) {
        if (sInstance == null) {
            synchronized (DbOpenHelperMgr.class) {
                if (sInstance == null) {
                    sInstance = new DbOpenHelperMgr(cxt);
                }
            }
        }
        return sInstance;
    }

    public SQLiteDatabase acquireDatabase(Class<? extends SQLiteOpenHelper> helperClass) {
        SQLiteOpenHelper helper = null;
        synchronized (DbOpenHelperMgr.class) {
            HelperInfo info = mOpenHelpers.get(helperClass);
            if (info == null) {
                try {
                    Constructor<? extends SQLiteOpenHelper> constructor = helperClass.getConstructor(Context.class);
                    helper = constructor.newInstance(mAppContext);
                } catch (Exception e) {
                    new RuntimeException("failed to create SQLiteOpenHelper instance", e);
                }
                info = new HelperInfo();
                info.helper = helper;
                info.mReferenceCount = 0;
                mOpenHelpers.put(helperClass, info);
            }
            helper = info.helper;
            info.mReferenceCount++;
        }
        return helper.getWritableDatabase();
    }

    public void releaseDatabase(Class<? extends SQLiteOpenHelper> helperClass) {
        synchronized (DbOpenHelperMgr.class) {
            HelperInfo info = mOpenHelpers.get(helperClass);
            if (info != null) {
                info.mReferenceCount--;
                if (info.mReferenceCount == 0) {
                    info.helper.close();
                    info.helper = null;
                    mOpenHelpers.remove(helperClass);
                }
            }
        }
    }
}
