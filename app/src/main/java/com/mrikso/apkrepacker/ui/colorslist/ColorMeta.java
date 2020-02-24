package com.mrikso.apkrepacker.ui.colorslist;

import android.os.Parcel;
import android.os.Parcelable;


public class ColorMeta implements Parcelable {

    public String label;
    public String value;
    public String iconUri;

    public ColorMeta(String label, String value) {
        this.label = label;
        this.value = value;
    }

    private ColorMeta(Parcel in) {
        label = in.readString();
        value = in.readString();
        iconUri =  in.readString();
    }

    public static final Creator<ColorMeta> CREATOR = new Creator<ColorMeta>() {
        @Override
        public ColorMeta createFromParcel(Parcel in) {
            return new ColorMeta(in);
        }

        @Override
        public ColorMeta[] newArray(int size) {
            return new ColorMeta[size];
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
        dest.writeString(iconUri);
    }

    public static class Builder {
        private ColorMeta colorMeta;

        public Builder(String name) {
            colorMeta = new ColorMeta(name, "?");
        }

        public Builder setValue(String value) {
            colorMeta.value = value;
            return this;
        }

        public Builder setIcon(String color) {
            if (color == null) {
                colorMeta.iconUri = null;
                return this;
            }
            colorMeta.iconUri = color;
            return this;
        }

        public ColorMeta build() {
            return colorMeta;
        }
    }

}
