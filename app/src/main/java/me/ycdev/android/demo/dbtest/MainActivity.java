package me.ycdev.android.demo.dbtest;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import me.ycdev.android.demo.dbtest.db.TestDbOpenHelper;
import me.ycdev.android.demo.dbtest.tester.BaseTester;
import me.ycdev.android.demo.dbtest.tester.MultiThreadTester;
import me.ycdev.android.demo.dbtest.tester.SingleThreadTester;
import me.ycdev.android.demo.dbtest.tester.TestOption;
import me.ycdev.android.demo.dbtest.tester.TestService;
import me.ycdev.android.demo.dbtest.utils.AppLogger;

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
