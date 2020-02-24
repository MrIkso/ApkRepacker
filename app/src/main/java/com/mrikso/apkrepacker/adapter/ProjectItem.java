package com.mrikso.apkrepacker.adapter;

import android.graphics.drawable.Drawable;

public class ProjectItem {
    private String appName;
    private String appPackage;
    private Drawable appIcon;
    private String apkPatch;
    private String appProjectPatch;

    //constructor initializing values
    public ProjectItem(String appName, String appPackage,String appProjectPatch, Drawable icon) {
        this.appName = appName;
        this.appPackage = appPackage;
        this.appIcon = icon;
        this.appProjectPatch = appProjectPatch;
    }

    public ProjectItem(String appName, String appPackage,String appProjectPatch,String apkPatch, Drawable icon) {
        this.appName = appName;
        this.appPackage = appPackage;
        this.appIcon = icon;
        this.appProjectPatch = appProjectPatch;
        this.apkPatch = apkPatch;
    }

    //getters
    public String getAppName() {
        return appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getApkPatch() {
        return apkPatch;
    }

    public String getAppProjectPatch(){
        return appProjectPatch;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }
}
