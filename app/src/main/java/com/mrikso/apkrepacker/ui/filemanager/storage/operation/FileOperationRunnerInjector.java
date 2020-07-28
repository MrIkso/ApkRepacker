package com.mrikso.apkrepacker.ui.filemanager.storage.operation;

import android.content.Context;

import com.mrikso.apkrepacker.ui.filemanager.storage.access.ExternalStorageAccessManager;


public abstract class FileOperationRunnerInjector {
    private FileOperationRunnerInjector() {
    }

    /**
     * Builds a default instance of {@link FileOperationRunner}.
     */
    public static FileOperationRunner operationRunner(Context c) {
        return new FileOperationRunner(new ExternalStorageAccessManager(c), c);
    }
}
