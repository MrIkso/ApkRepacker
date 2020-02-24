package com.mrikso.apkrepacker.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtils {

    public static Boolean getBoolean(Context context, String key, Boolean defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static Integer getInteger(Context context, String key, Integer defaultValue) {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(key, defaultValue);
    }

    public static void putInt(Context context, String key, int value) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPref.edit().putInt(key, value).apply();
    }
}