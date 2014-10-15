package me.ycdev.demo.dbtest.tester;

import android.os.Parcel;
import android.os.Parcelable;

public class TestOption implements Parcelable {
    public static final int MODE_RECOMMEND = 1;
    public static final int MODE_SINGLE_OPEN_HELPER = 2;
    public static final int MODE_MULTIPLE_OPEN_HELPER = 3;

    public int mode;
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
        dest.writeInt(mode);
        dest.writeInt(threadCount);
    }

    public TestOption(Parcel in) {
        mode = in.readInt();
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
