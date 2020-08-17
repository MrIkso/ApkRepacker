package com.mrikso.apkrepacker.adapter;

public class PatchItem {

    public String mPath;
    public String mPatchName;

    public PatchItem(String mPatchName, String mPath){
        this.mPatchName = mPatchName;
        this.mPath = mPath;
    }
}
