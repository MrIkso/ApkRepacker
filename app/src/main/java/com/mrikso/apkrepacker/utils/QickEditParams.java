package com.mrikso.apkrepacker.utils;

import android.graphics.Bitmap;

public class QickEditParams {
    private static String oldpackage, newpackage;
    private static String oldname, newname;
    private static int installLocation;
    private static String buildCode, buildNumber;
    private static int minimumSdk, targetSdk;
    private static boolean inRes, inDex;
    private static Bitmap bitmap;
    private static String iconName;

    public static void setOldName(String old) {
        oldname = old;
    }

    public static void setOldPackage(String old) {
        oldpackage = old;
    }

    public static void setInstallLocation(int location) {
        installLocation = location;
    }

    ////getters

    public static String getOldPackage() {
        return oldpackage;
    }

    public static int getInstallLoacation() {
        return installLocation;
    }

    public static String getVersionCode() {
        return buildCode;
    }

    public static void setVersionCode(String code) {
        buildCode = code;
    }

    public static String getVersionName() {
        return buildNumber;
    }

    public static void setVersionName(String number) {
        buildNumber = number;
    }

    public static String getNewPackage() {
        return newpackage;
    }

    public static void setNewPackage(String newPackage) {
        newpackage = newPackage;
    }

    public static String getNewname() {
        return newname;
    }

    public static void setNewname(String newname1) {
        newname = newname1;
    }

    public static int getMinimumSdk() {
        return minimumSdk;
    }

    public static void setMinimumSdk(int sdk) {
        minimumSdk = sdk;
    }

    public static int getTargetSdk() {
        return targetSdk;
    }

    public static void setTargetSdk(int sdk) {
        targetSdk = sdk;
    }

    public static boolean isInRes() {
        return inRes;
    }

    public static void setInRes(boolean res) {
        inRes = res;
    }

    public static boolean isInDex() {
        return inDex;
    }

    public static void setInDex(boolean dex) {
        inDex = dex;
    }

    public static void setBitmap(Bitmap bit)
    {
        bitmap = bit;
    }
    public static void setIconName(String str)
    {
        if (!str.equals(""))
            iconName = str;
    }

    public static Bitmap getBitmap()
    {
       return bitmap;
    }
    public static String getIconName()
    {
        if (!iconName.equals(""))
          return iconName;
        return null;
    }
}
