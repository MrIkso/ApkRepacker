package com.mrikso.apkrepacker.viewmodel.projects;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


@Keep
public class ProjectItemJson {

    @SerializedName("apkFileName")
    @Expose
    public String apkFileName;

    @SerializedName("apkFilePackageName")
    @Expose
    public String apkFilePackageName;

    @SerializedName("apkFileIcon")
    @Expose
    public String apkFileIcon;

    @SerializedName("apkFilePatch")
    @Expose
    public String apkFilePatch;

    @SerializedName("VersionInfo")
    @Expose
    public VersionInfo versionInfo;

}
