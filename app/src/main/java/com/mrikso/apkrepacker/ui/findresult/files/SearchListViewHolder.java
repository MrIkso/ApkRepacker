package com.mrikso.apkrepacker.ui.findresult.files;

import android.view.ViewGroup;

import com.mrikso.apkrepacker.ui.filemanager.holder.FileListViewHolder;
import com.mrikso.apkrepacker.utils.ProjectUtils;

import java.io.File;

public class SearchListViewHolder extends FileListViewHolder {

    SearchListViewHolder(ViewGroup parent) {
        super(parent);
    }

    @Override
    public void bind(File filePath, int position, OnItemClickListener listener, boolean selected, boolean projectMode) {
        super.bind(filePath, position, listener, selected, projectMode);
        mFileSize.setText(filePath.getAbsolutePath().substring((ProjectUtils.getProjectPath() + "/").length()));
    }
}
