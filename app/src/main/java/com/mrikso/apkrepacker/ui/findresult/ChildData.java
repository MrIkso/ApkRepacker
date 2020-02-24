package com.mrikso.apkrepacker.ui.findresult;

import android.core.text.SpannableStringBuilder;
import android.os.Parcel;
import android.os.Parcelable;

public class ChildData implements Parcelable{

    private String name;
    private SpannableStringBuilder spannableStringBuilder;
private String path;

    public ChildData(Parcel parcel) { name = parcel.readString();}

    public ChildData(String name) {
        this.name = name;
    }
    public ChildData(SpannableStringBuilder name, String path) {
        this.spannableStringBuilder = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public SpannableStringBuilder getSpannableName() {
        return spannableStringBuilder;
    }

    public String getPath(){
        return path;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final Creator<ChildData> CREATOR = new Creator<ChildData>() {
        @Override
        public ChildData createFromParcel(Parcel in) {
            return new ChildData(in);
        }

        @Override
        public ChildData[] newArray(int size) {
            return new ChildData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
         parcel.writeString(name);
    }
}
