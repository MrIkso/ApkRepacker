package com.mrikso.apkrepacker.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor {

    private static AppExecutor instance;
    private Executor diskIO = Executors.newSingleThreadExecutor();

    private AppExecutor() {
    }

    public static AppExecutor getInstance() {
        if (instance == null) {
            instance = new AppExecutor();
        }
        return instance;
    }

    public Executor getDiskIO() {
        return diskIO;
    }
}
