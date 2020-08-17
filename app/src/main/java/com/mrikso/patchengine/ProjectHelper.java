package com.mrikso.patchengine;

import android.content.Context;

import java.io.File;

public class ProjectHelper {

    public String mProject;
    public String mDataPath;
    public String mApkPath;
    public String mApkPackage;
    public File mCache;
    public Context mContext;
    public boolean mSmaliCliced;

    public String getProjectPath() {
        return mProject;
    }

    public File getCacheDir() {
        return mCache;
    }

    public Context getContext() {
        return mContext;
    }

    public boolean smaliClicked() {
        return mSmaliCliced;
    }

    public String getAppDataPath() {
        return mDataPath;
    }

    public String getApkPath() {
        return mApkPath;
    }

    public String getApkPackage() {
        return mApkPackage;
    }

}
