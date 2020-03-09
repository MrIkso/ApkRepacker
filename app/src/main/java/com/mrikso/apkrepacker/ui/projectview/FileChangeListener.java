package com.mrikso.apkrepacker.ui.projectview;

import java.io.File;

public interface FileChangeListener {

    void onFileDeleted(File deleted);

    void onFileCreated(File newFile);

    void doOpenFile(File toEdit);
}
