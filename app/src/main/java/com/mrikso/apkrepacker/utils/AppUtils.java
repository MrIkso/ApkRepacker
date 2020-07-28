package com.mrikso.apkrepacker.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.mrikso.apkrepacker.R;

import java.io.File;

public class AppUtils {

    public static boolean checkAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean apiIsAtLeast(int sdkInt) {
        return Build.VERSION.SDK_INT >= sdkInt;
    }

    public static void uninstallApp(Context context, String packageName) {
        context.startActivity(new Intent(Intent.ACTION_DELETE, Uri.parse("package:" + packageName)));
    }

    @SuppressLint("WrongConstant")
    public static void installApk(Context c, File apk) {
        Uri data;
        if (Build.VERSION.SDK_INT >= 24) {
            Uri.Builder builder = new Uri.Builder();
            builder.authority(c.getPackageName() + ".fileprovider");
            builder.scheme("content");
            byte[] buf = apk.getAbsolutePath().getBytes();
            builder.path(Base64.encodeToString(buf, Base64.NO_WRAP));
            data = builder.build();
        } else
            data = Uri.fromFile(apk);
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setFlags(Intent.EXTRA_DOCK_STATE_LE_DESK);
        intent.setData(data);
        c.startActivity(intent);
    }

    public static void gotoApplicationSettings(Context context, String packageName) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", packageName, null));
        context.startActivity(intent);
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            String version = info.versionName;
            String code = String.valueOf(AppUtils.apiIsAtLeast(Build.VERSION_CODES.P) ? info.getLongVersionCode() : info.versionCode);
            return context.getResources().getString(R.string.about_version, version, code);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.about_version, null, null);
        }
    }

    public static String getArchName() {
        for (String androidArch : Build.SUPPORTED_ABIS) {
            switch (androidArch) {
                case "arm64-v8a":
                    return androidArch;

                case "armeabi-v7a":
                    return androidArch;

                    /*
                case "x86_64":
                    return androidArch;

                case "x86":
                    return androidArch;

                     */
            }
        }
        return "armeabi-v7a";
    }

    public static Drawable getApkIcon(Context c, String n) {
        Object[] apkInfo = getApkInfo(c, n);
        if (apkInfo != null)
            return (Drawable) apkInfo[0];
        else
            return null;
    }

    public static String getApkPackage(Context context, String apk) {
        Object[] apkInfo = getApkInfo(context, apk);
        if (apkInfo != null)
            return (String) apkInfo[2];
        else
            return null;
    }

    public static String getApkName(Context context, String apk) {
        Object[] apkInfo = getApkInfo(context, apk);
        if (apkInfo != null)
            return (String) apkInfo[1];
        else
            return null;
    }

    public static Object[] getApkInfo(Context c, String apk) {
        Object[] res = new Object[8];
        try {
            Manifest mf = new Manifest(apk);
            PackageManager pm = c.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apk, PackageManager.GET_ACTIVITIES);
            if (packageInfo != null) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                appInfo.sourceDir = apk;
                appInfo.publicSourceDir = apk;
                res[0] = appInfo.loadIcon(pm);
                res[1] = appInfo.loadLabel(pm);
                res[2] = packageInfo.packageName;
                res[3] = packageInfo.versionName;
                res[4] = packageInfo.versionCode;
                if (apiIsAtLeast(Build.VERSION_CODES.N)) {
                    res[5] = appInfo.minSdkVersion;
                } else {
                    res[5] = mf.getMinSdkVersion();//мне впадло писать метод
                }
                res[6] = appInfo.targetSdkVersion;
                res[7] = packageInfo.installLocation;
                return res;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
