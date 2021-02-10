package com.mrikso.apkrepacker.ui.projectview.treeview.interfaces;

import java.io.File;

public interface FileChangeListener {

    void onFileDeleted(File deleted);

    void onFileCreated(File newFile);

    void doOpenFile(String toEdit);
}
