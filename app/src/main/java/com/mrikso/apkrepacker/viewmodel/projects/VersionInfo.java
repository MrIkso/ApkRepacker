package com.mrikso.apkrepacker.viewmodel.projects;

import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Keep
public class VersionInfo {
    @SerializedName("versionName")
    @Expose
    private String versionName;

    @SerializedName("versionCode")
    @Expose
    private String versionCode;

    public String getVersionName() {
        return versionName;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
