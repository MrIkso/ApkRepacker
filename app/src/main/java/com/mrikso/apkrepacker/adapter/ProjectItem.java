package com.mrikso.apkrepacker.adapter;

import android.graphics.drawable.Drawable;

public class ProjectItem {
    private String appIcon;
    private String appName;
    private String appPackage;
    private String apkPatch;
    private String appProjectPatch;
    private String appVersionName;
    private String appVersionCode;

    private boolean isChecked = false;

    //constructor initializing values
    public ProjectItem(String icon, String appName, String appPackage, String appProjectPatch, String appVersionName, String appVersionCode) {
        this.appIcon = icon;
        this.appName = appName;
        this.appPackage = appPackage;
        this.appProjectPatch = appProjectPatch;
        this.appVersionName = appVersionName;
        this.appVersionCode = appVersionCode;
    }

    public ProjectItem(String icon, String appName, String appPackage, String appProjectPatch, String apkPatch, String appVersionName, String appVersionCode) {
        this.appIcon = icon;
        this.appName = appName;
        this.appPackage = appPackage;
        this.appProjectPatch = appProjectPatch;
        this.apkPatch = apkPatch;
        this.appVersionName = appVersionName;
        this.appVersionCode = appVersionCode;
    }

    //getters
    public String getAppIcon() {
        return appIcon;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getApkPatch() {
        return apkPatch;
    }

    public String getAppProjectPatch() {
        return appProjectPatch;
    }

    public String getAppVersionName() {
        return appVersionName;
    }

    public String getAppVersionCode() {
        return appVersionCode;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
