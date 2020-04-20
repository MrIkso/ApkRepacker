package com.mrikso.apkrepacker.utils.qickedit;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.io.File;

public class AppInfo
{
    private Drawable icon;
    private String iconValue;
    private CharSequence label;
    private String pname;
    private String version;
    private int code;
    private int minSdk;
    private int targetSdk;
    private PackageManager pManager;
    private PackageInfo pInfo;
    private boolean valid;
    private boolean isSplitRequired;

    public AppInfo(Context ctx, File apk)
    {
        this.valid = false;
        init(ctx, apk.getAbsolutePath());
    }

    public AppInfo(Context ctx, String apk)
    {
        this.valid = false;
        init(ctx, apk);
    }

    private void init(Context ctx, String path)
    {
        try
        {
            ManifestAnalyser mf = new ManifestAnalyser(path);
            this.minSdk = mf.getMinSdkVersion();
            this.targetSdk = mf.getTargetSdkVersion();
            this.isSplitRequired = mf.isSplitRequired();
            this.pManager = ctx.getPackageManager();
            this.pInfo = pManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
            if (pInfo != null)
            {
                ApplicationInfo appInfo = pInfo.applicationInfo;
                appInfo.sourceDir = path;
                appInfo.publicSourceDir = path;
                Resources resources = pManager.getResourcesForApplication(appInfo);
                this.icon = appInfo.loadIcon(pManager);
                this.iconValue = resources.getResourceName(appInfo.icon);
                this.label = appInfo.loadLabel(pManager);
                this.pname = pInfo.packageName;
                this.version = pInfo.versionName;
                this.code = pInfo.versionCode;
            }
            else
            {
                this.label = mf.getPackageLabel();
                this.pname = mf.getPackageName();
                this.version = mf.getVersionName();
                this.code = mf.getVersionCode();
            }
            this.valid = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            this.valid = false;
        }
    }

    public Drawable icon()
    {
        return icon;
    }

    public String iconValue()
    {
        return iconValue;
    }

    public String label()
    {
        return label.toString();
    }

    public String pname()
    {
        return pname;
    }

    public String version()
    {
        return version;
    }

    public int code()
    {
        return code;
    }

    public int minSdk()
    {
        return minSdk;
    }

    public int targetSdk()
    {
        return targetSdk;
    }

    public PackageManager getPackageManager()
    {
        return pManager;
    }

    public PackageInfo getPackageInfo()
    {
        return pInfo;
    }

    public boolean isSplitRequired()
    {
        return isSplitRequired;
    }

    public boolean isValid()
    {
        return valid;
    }
}
