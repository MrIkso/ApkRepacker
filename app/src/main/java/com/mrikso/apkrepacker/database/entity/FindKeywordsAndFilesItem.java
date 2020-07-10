package com.mrikso.apkrepacker.database.entity;

import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;


public class FindKeywordsAndFilesItem {

    @NonNull
    @SerializedName("find_keyword")
    private List<String> mKeyword = new ArrayList<>();

    @NonNull
    @SerializedName("find_files_keyword")
    private List<String> mFiles = new ArrayList<>();

    public FindKeywordsAndFilesItem(){
        super();
    }

    public void setKeyword(String keyword) {
        mKeyword.add(keyword);
    }

    public void setFilesKeyword(String keyword) {
        mFiles.add(keyword);
    }

    public void setKeyword(List<String> keyword) {
        mKeyword.addAll(keyword);
    }

    public void setFilesKeyword(List<String> keyword) {
        mFiles.addAll(keyword);
    }

    public List<String> getKeyword() {
        return mKeyword;
    }

    public List<String> getFilesKeyword() {
        return mFiles;
    }
}
