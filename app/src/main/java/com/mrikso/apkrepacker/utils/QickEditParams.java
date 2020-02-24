package com.mrikso.apkrepacker.utils;

public class QickEditParams {
    static String oldpackage, newpackage;
    static String oldname, newname;
    static String installLoacation;
    static String buildCode, buildNumber;
    static int minimumSdk, targetSdk;
    static boolean inRes, inDex;

    public static void setOldPackage(String old) {
        oldpackage = old;
    }

    public static void setOldName(String old) {
        oldname = old;
    }

    public static void setInstallLocation(String location) {
        installLoacation = location;
    }

    ////getters
    public static String getInstallLoacation() {
        return installLoacation;
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
}
