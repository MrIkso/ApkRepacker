package com.mrikso.apkrepacker.ui.filemanager.batch;

import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;

public interface Variable {

    int apply(StringBuilder sb, int i, FileHolder fileHolder);

    String describe();

    String pattern();
}
