package com.mrikso.apkrepacker.adapter;

public class PatchItem {

    public String mPath;
    public String mPatchName;

    public PatchItem(String patchName, String path){
        this.mPatchName = patchName;
        this.mPath = path;
    }
}
