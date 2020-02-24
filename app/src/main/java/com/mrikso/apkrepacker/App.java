package com.mrikso.apkrepacker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.mrikso.apkrepacker.utils.ExceptionHandler;

public class App extends Application {

    private static Context mContext;
    private static App instance;
    private SharedPreferences preferences;

    public static Context getContext() {
        return mContext;
    }

    public static void setContext(Context mContext) {
        App.mContext = mContext;
    }

    public App() {
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
            ExceptionHandler.get(this).start();
    }

    public static App get() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public SharedPreferences getPreferences() {
        if (preferences == null){
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return preferences;
    }
}

