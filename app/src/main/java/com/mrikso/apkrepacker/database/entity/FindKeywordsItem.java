package com.mrikso.apkrepacker.database.entity;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

@Keep
public class FindKeywordsItem {

    @NonNull
    @SerializedName("find_keyword")
    private List<String> mKeyword = new ArrayList<>();

    @NonNull
    @SerializedName("replace_keyword")
    private List<String> mReplaceKeyword = new ArrayList<>();

    public FindKeywordsItem(){
        super();
    }

    public void setKeyword(String keyword) {
            mKeyword.add(keyword);
    }

    public void setReplaceKeyword(String keyword) {
            mReplaceKeyword.add(keyword);
    }

    public void setKeyword(List<String> keyword) {
        mKeyword.addAll(keyword);
    }

    public void setReplaceKeyword(List<String> keyword) {
        mReplaceKeyword.addAll(keyword);
    }

    public List<String> getKeyword() {
        return mKeyword;
    }

    public List<String> getReplaceKeyword() {
        return mReplaceKeyword;
    }
}
