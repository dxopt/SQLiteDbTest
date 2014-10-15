package me.ycdev.demo.dbtest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import me.ycdev.demo.dbtest.db.TestDbOpenHelper;
import me.ycdev.demo.dbtest.tester.BaseTester;
import me.ycdev.demo.dbtest.tester.MultiThreadTester;
import me.ycdev.demo.dbtest.tester.SingleThreadTester;
import me.ycdev.demo.dbtest.tester.TestOption;
import me.ycdev.demo.dbtest.tester.TestService;
import me.ycdev.demo.dbtest.utils.AppLogger;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";

    private RadioGroup mModeChoice;
    private Button mSingleThreadBtn;
    private Button mMultipleThreadBtn;
    private Button mMultipleProcessBtn;
    private BaseTester mCurTester;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppLogger.i(TAG, "#onCreate");
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mModeChoice = (RadioGroup) findViewById(R.id.mode_choice);
        mSingleThreadBtn =(Button) findViewById(R.id.single_thread);
        mSingleThreadBtn.setOnClickListener(this);
        mMultipleThreadBtn = (Button) findViewById(R.id.multiple_thread);
        mMultipleThreadBtn.setOnClickListener(this);
        mMultipleProcessBtn = (Button) findViewById(R.id.multiple_process);
        mMultipleProcessBtn.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_clear_db) {
            TestDbOpenHelper.clearDbRecords(this);
            Toast.makeText(this, R.string.toast_db_cleared, Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void refreshTestButtons(boolean testRunning, Button curTestBtn, int textResId) {
        mModeChoice.setEnabled(!testRunning);
        mSingleThreadBtn.setEnabled(!testRunning);
        mMultipleThreadBtn.setEnabled(!testRunning);
        mMultipleProcessBtn.setEnabled(!testRunning);
        if (testRunning) {
            curTestBtn.setText(R.string.btn_stop_test);
        } else {
            curTestBtn.setText(textResId);
        }
        curTestBtn.setEnabled(true);
    }

    @Override
    public void onClick(View v) {
        if (v == mSingleThreadBtn) {
            doSingThreadTest();
        } else if (v == mMultipleThreadBtn) {
            doMultiThreadTest();
        } else if (v == mMultipleProcessBtn) {
            doMultiProcessTest();
        }
    }

    private TestOption getCurTestOption() {
        TestOption option = new TestOption();
        int curChoiceId = mModeChoice.getCheckedRadioButtonId();
        if (curChoiceId == R.id.mode_recommend) {
            option.mode = TestOption.MODE_RECOMMEND;
        } else if (curChoiceId == R.id.mode_single_openhelper) {
            option.mode = TestOption.MODE_SINGLE_OPEN_HELPER;
        } else if (curChoiceId == R.id.mode_multiple_openhelper) {
            option.mode = TestOption.MODE_MULTIPLE_OPEN_HELPER;
        }
        option.threadCount = 16;
        return option;
    }

    private void doSingThreadTest() {
        /**
         * Test result summary:
         *   + Works fine always
         *   + Should close DB after operation done always
         * Test passed cases:
         *   + Nexus One & 2.3.3
         *   + Nexus 4 & 4.4.2
         *   + Galaxy Nexus & 4.3
         */
        // the test result should be OK
        if (mCurTester == null) {
            mCurTester = new SingleThreadTester(this, getCurTestOption());
            mCurTester.startTest();
        } else {
            mCurTester.stopTest();
            mCurTester = null;
        }
        refreshTestButtons(mCurTester != null, mSingleThreadBtn, R.string.btn_single_thread);
    }

    private void doMultiThreadTest() {
        /**
         * Test result summary:
         *   + Almost cannot work
         *   + Should close DB after operation done always
         * Test failed cases:
         *   + Nexus One & 2.3.3: TestFailure#1
         *   + Galaxy Nexus & 4.3: TestFailure#2, TestFailure#3, TestFailure#4
         * Test passed cases:
         *   + Nexus 4 & 4.4.2
         */
        if (mCurTester == null) {
            mCurTester = new MultiThreadTester(this, getCurTestOption());
            mCurTester.startTest();
        } else {
            mCurTester.stopTest();
            mCurTester = null;
        }
        refreshTestButtons(mCurTester != null, mMultipleThreadBtn, R.string.btn_multiple_thread);
    }

    private void doMultiProcessTest() {
        /**
         * Test result summary:
         *   + Cannot work always
         *   + Should close DB after operation done always
         * Test failed cases:
         *   + Nexus One & 2.3.3: TestFailure#1
         *   + Galaxy Nexus & 4.3: TestFailure#2, TestFailure#3, TestFailure#4
         *   + Nexus 4 & 4.4.2: TestFailure#5
         */
        if (mCurTester == null) {
            TestOption option = getCurTestOption();
            mCurTester = new MultiThreadTester(this, option);
            mCurTester.startTest();
            TestService.startTest(this, option);
        } else {
            mCurTester.stopTest();
            mCurTester = null;
            TestService.stopTest(this);
        }
        refreshTestButtons(mCurTester != null, mMultipleProcessBtn, R.string.btn_multiple_process);
    }
}

/* TestFailure#1
2014-10-15 14:34:33
PID: 1895
TID: 13
OS Version: 2.3.3_10
Vendor: HTC
Model: Nexus One
CPU ABI: armeabi-v7a
CPU API2: armeabi

android.database.sqlite.SQLiteException: error code 5: database is locked
	at android.database.sqlite.SQLiteStatement.native_execute(Native Method)
	at android.database.sqlite.SQLiteStatement.execute(SQLiteStatement.java:61)
	at android.database.sqlite.SQLiteDatabase.updateWithOnConflict(SQLiteDatabase.java:1727)
	at android.database.sqlite.SQLiteDatabase.update(SQLiteDatabase.java:1656)
	at me.ycdev.demo.dbtest.db.TestTable.updateRecord(TestTable.java:96)
	at me.ycdev.demo.dbtest.tester.SingleThreadTester.run(SingleThreadTester.java:64)
	at java.lang.Thread.run(Thread.java:1019)
*/

/* TestFailure#2
2014-10-15 14:42:18
PID: 20182
TID: 4605
OS Version: 4.3_18
Vendor: samsung
Model: Galaxy Nexus
CPU ABI: armeabi-v7a
CPU API2: armeabi

android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5)
	at android.database.sqlite.SQLiteConnection.nativeExecuteForLong(Native Method)
	at android.database.sqlite.SQLiteConnection.executeForLong(SQLiteConnection.java:598)
	at android.database.sqlite.SQLiteSession.executeForLong(SQLiteSession.java:652)
	at android.database.sqlite.SQLiteStatement.simpleQueryForLong(SQLiteStatement.java:107)
	at android.database.DatabaseUtils.longForQuery(DatabaseUtils.java:813)
	at android.database.DatabaseUtils.longForQuery(DatabaseUtils.java:801)
	at android.database.sqlite.SQLiteDatabase.getVersion(SQLiteDatabase.java:862)
	at android.database.sqlite.SQLiteOpenHelper.getDatabaseLocked(SQLiteOpenHelper.java:242)
	at android.database.sqlite.SQLiteOpenHelper.getWritableDatabase(SQLiteOpenHelper.java:164)
	at me.ycdev.demo.dbtest.tester.SingleThreadTester.run(SingleThreadTester.java:47)
	at java.lang.Thread.run(Thread.java:841)
*/

/* TestFailure#3
2014-10-15 14:42:17
PID: 20182
TID: 4609
OS Version: 4.3_18
Vendor: samsung
Model: Galaxy Nexus
CPU ABI: armeabi-v7a
CPU API2: armeabi

android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5): , while compiling: PRAGMA journal_mode
	at android.database.sqlite.SQLiteConnection.nativePrepareStatement(Native Method)
	at android.database.sqlite.SQLiteConnection.acquirePreparedStatement(SQLiteConnection.java:889)
	at android.database.sqlite.SQLiteConnection.executeForString(SQLiteConnection.java:634)
	at android.database.sqlite.SQLiteConnection.setJournalMode(SQLiteConnection.java:320)
	at android.database.sqlite.SQLiteConnection.setWalModeFromConfiguration(SQLiteConnection.java:294)
	at android.database.sqlite.SQLiteConnection.open(SQLiteConnection.java:215)
	at android.database.sqlite.SQLiteConnection.open(SQLiteConnection.java:193)
	at android.database.sqlite.SQLiteConnectionPool.openConnectionLocked(SQLiteConnectionPool.java:463)
	at android.database.sqlite.SQLiteConnectionPool.open(SQLiteConnectionPool.java:185)
	at android.database.sqlite.SQLiteConnectionPool.open(SQLiteConnectionPool.java:177)
	at android.database.sqlite.SQLiteDatabase.openInner(SQLiteDatabase.java:804)
	at android.database.sqlite.SQLiteDatabase.open(SQLiteDatabase.java:789)
	at android.database.sqlite.SQLiteDatabase.openDatabase(SQLiteDatabase.java:694)
	at android.app.ContextImpl.openOrCreateDatabase(ContextImpl.java:863)
	at android.content.ContextWrapper.openOrCreateDatabase(ContextWrapper.java:235)
	at android.database.sqlite.SQLiteOpenHelper.getDatabaseLocked(SQLiteOpenHelper.java:224)
	at android.database.sqlite.SQLiteOpenHelper.getWritableDatabase(SQLiteOpenHelper.java:164)
	at me.ycdev.demo.dbtest.tester.SingleThreadTester.run(SingleThreadTester.java:47)
	at java.lang.Thread.run(Thread.java:841)
*/

/* TestFailure#4
2014-10-15 14:44:43
PID: 20676
TID: 4659
OS Version: 4.3_18
Vendor: samsung
Model: Galaxy Nexus
CPU ABI: armeabi-v7a
CPU API2: armeabi

android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5)
	at android.database.sqlite.SQLiteConnection.nativeExecuteForLastInsertedRowId(Native Method)
	at android.database.sqlite.SQLiteConnection.executeForLastInsertedRowId(SQLiteConnection.java:782)
	at android.database.sqlite.SQLiteSession.executeForLastInsertedRowId(SQLiteSession.java:788)
	at android.database.sqlite.SQLiteStatement.executeInsert(SQLiteStatement.java:86)
	at android.database.sqlite.SQLiteDatabase.insertWithOnConflict(SQLiteDatabase.java:1469)
	at android.database.sqlite.SQLiteDatabase.insertOrThrow(SQLiteDatabase.java:1365)
	at me.ycdev.demo.dbtest.db.TestTable.addNewRecord(TestTable.java:71)
	at me.ycdev.demo.dbtest.tester.SingleThreadTester.run(SingleThreadTester.java:58)
	at java.lang.Thread.run(Thread.java:841)
*/

/* TestFailure#5
2014-10-15 02:48:51
PID: 15872
TID: 2172
OS Version: 4.4.2_19
Vendor: LGE
Model: AOSP on Mako
CPU ABI: armeabi-v7a
CPU API2: armeabi

android.database.sqlite.SQLiteDatabaseLockedException: database is locked (code 5)
	at android.database.sqlite.SQLiteConnection.nativeExecuteForLastInsertedRowId(Native Method)
	at android.database.sqlite.SQLiteConnection.executeForLastInsertedRowId(SQLiteConnection.java:782)
	at android.database.sqlite.SQLiteSession.executeForLastInsertedRowId(SQLiteSession.java:788)
	at android.database.sqlite.SQLiteStatement.executeInsert(SQLiteStatement.java:86)
	at android.database.sqlite.SQLiteDatabase.insertWithOnConflict(SQLiteDatabase.java:1469)
	at android.database.sqlite.SQLiteDatabase.insertOrThrow(SQLiteDatabase.java:1365)
	at me.ycdev.demo.dbtest.db.TestTable.addNewRecord(TestTable.java:71)
	at me.ycdev.demo.dbtest.tester.SingleThreadTester.run(SingleThreadTester.java:58)
	at java.lang.Thread.run(Thread.java:841)
*/

/* TestFailure#6: Single SQLiteOpenHelper instance and close DB instances
2014-10-15 16:50:07
PID: 25006
TID: 5024
OS Version: 4.3_18
Vendor: samsung
Model: Galaxy Nexus
CPU ABI: armeabi-v7a
CPU API2: armeabi

java.lang.IllegalStateException: Cannot perform this operation because the connection pool has been closed.
	at android.database.sqlite.SQLiteConnectionPool.throwIfClosedLocked(SQLiteConnectionPool.java:962)
	at android.database.sqlite.SQLiteConnectionPool.waitForConnection(SQLiteConnectionPool.java:599)
	at android.database.sqlite.SQLiteConnectionPool.acquireConnection(SQLiteConnectionPool.java:348)
	at android.database.sqlite.SQLiteSession.acquireConnection(SQLiteSession.java:894)
	at android.database.sqlite.SQLiteSession.executeForCursorWindow(SQLiteSession.java:834)
	at android.database.sqlite.SQLiteQuery.fillWindow(SQLiteQuery.java:62)
	at android.database.sqlite.SQLiteCursor.fillWindow(SQLiteCursor.java:144)
	at android.database.sqlite.SQLiteCursor.getCount(SQLiteCursor.java:133)
	at android.database.AbstractCursor.moveToPosition(AbstractCursor.java:197)
	at android.database.AbstractCursor.moveToFirst(AbstractCursor.java:237)
	at me.ycdev.demo.dbtest.db.TestTable.query(TestTable.java:48)
	at me.ycdev.demo.dbtest.tester.SingleThreadTester.run(SingleThreadTester.java:72)
	at java.lang.Thread.run(Thread.java:841)
*/