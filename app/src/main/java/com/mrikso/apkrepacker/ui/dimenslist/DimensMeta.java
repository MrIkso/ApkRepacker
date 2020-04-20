package com.mrikso.apkrepacker.ui.dimenslist;

import android.os.Parcel;
import android.os.Parcelable;


public class DimensMeta implements Parcelable {

    public String label;
    public String value;
//    public String iconUri;

    public DimensMeta(String label, String value) {
        this.label = label;
        this.value = value;
    }

    private DimensMeta(Parcel in) {
        label = in.readString();
        value = in.readString();
//        iconUri =  in.readString();
    }

    public static final Creator<DimensMeta> CREATOR = new Creator<DimensMeta>() {
        @Override
        public DimensMeta createFromParcel(Parcel in) {
            return new DimensMeta(in);
        }

        @Override
        public DimensMeta[] newArray(int size) {
            return new DimensMeta[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(label);
        dest.writeString(value);
//        dest.writeString(iconUri);
    }

    public static class Builder {
        private DimensMeta dimensMeta;

        public Builder(String name) {
            dimensMeta = new DimensMeta(name, "?");
        }

        public Builder setValue(String value) {
            dimensMeta.value = value;
            return this;
        }

/*        public Builder setIcon(String color) {
            if (color == null) {
                dimensMeta.iconUri = null;
                return this;
            }
            dimensMeta.iconUri = color;
            return this;
        }*/

        public DimensMeta build() {
            return dimensMeta;
        }
    }

}
