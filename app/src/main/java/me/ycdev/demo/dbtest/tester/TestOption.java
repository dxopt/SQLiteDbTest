package me.ycdev.demo.dbtest.tester;

import android.os.Parcel;
import android.os.Parcelable;

public class TestOption implements Parcelable {
    public boolean needCloseDb = true;
    public int threadCount = 1;

    public TestOption() {
        // empty body
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(needCloseDb ? 1 : 0);
        dest.writeInt(threadCount);
    }

    public TestOption(Parcel in) {
        needCloseDb = in.readInt() == 1;
        threadCount = in.readInt();
    }

    public static final Parcelable.Creator<TestOption> CREATOR
            = new Parcelable.Creator<TestOption>() {
        public TestOption createFromParcel(Parcel in) {
            return new TestOption(in);
        }

        public TestOption[] newArray(int size) {
            return new TestOption[size];
        }
    };
}
