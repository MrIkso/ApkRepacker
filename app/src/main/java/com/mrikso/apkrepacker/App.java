package com.mrikso.apkrepacker;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.mrikso.apkrepacker.ui.filemanager.utils.CopyHelper;
import com.mrikso.apkrepacker.utils.ExceptionHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import static com.mrikso.apkrepacker.ui.filemanager.misc.ThumbnailHelper.imageDecoder;

public class App extends Application {

    private static Context mContext;
    private static App instance;
    private CopyHelper mCopyHelper;
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
        mCopyHelper = new CopyHelper();
        ExceptionHandler.get(this).start();
        initImageLoader();
    }

    public static App get() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    private void initImageLoader() {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .diskCacheSize(10240000) // 10MB
                .imageDecoder(imageDecoder(getApplicationContext()))
                .build();
        ImageLoader.getInstance().init(config);
    }

    public CopyHelper getCopyHelper() {
        return mCopyHelper;
    }

    public SharedPreferences getPreferences() {
        if (preferences == null) {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
        }
        return preferences;
    }
}

