package com.mrikso.apkrepacker.autotranslator.dictionary;

import androidx.annotation.NonNull;

public class DictionaryItem {

    public String mSource;
    public String mTranslated;

    public DictionaryItem(String source, String translated) {
        mSource = source;
        mTranslated = translated;
    }

    public String getTranslated() {
        return mTranslated;
    }

    public void setTranslated(String translated) {
        this.mTranslated = translated;
    }

    public String getSource() {
        return mSource;
    }

    public void setSource(String source) {
        this.mSource = source;
    }

    @NonNull
    @Override
    public String toString() {
        return "DictionaryItem{" +
                "source='" + mSource + '\'' +
                ", translated='" + mTranslated + '\'' +
                '}';
    }
}
