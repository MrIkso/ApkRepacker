package com.jecelyin.editor.v2.adapter;

import android.os.Parcel;
import android.os.Parcelable;
import com.mrikso.apkrepacker.ide.editor.EditorDelegate;

public class SavedState implements Parcelable {

    public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        @Override
        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

    EditorDelegate.SavedState[] states;

    protected SavedState() {
    }

    protected SavedState(Parcel in) {
//            states = in.readParcelableArray();
        states = in.createTypedArray(EditorDelegate.SavedState.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(states, flags);
    }
}
