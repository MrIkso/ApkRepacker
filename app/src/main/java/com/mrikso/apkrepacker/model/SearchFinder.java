package com.mrikso.apkrepacker.model;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SearchFinder{

    private File mCurrentPath;
    private List<File> mFileList = new ArrayList<>();
    private ArrayList<String> mExtensions = new ArrayList<>();

    public File getCurrentPath() {
        return mCurrentPath;
    }

    public void setCurrentPath(File currentPath) {
        mCurrentPath = currentPath;
    }

    public List<File> getFileList() {
        return mFileList;
    }

    public void query(String query) {
        Log.i("SearchFinder", "start");
        List<File> result = new ArrayList<>();
        search(query.toLowerCase(), mCurrentPath, result);
        Collections.sort(result);
        mFileList = result;
    }

    public void setExtensions(ArrayList<String> extensions) {
        mExtensions = extensions;
    }

    private void search(String query, File dir, List<File> result) {
        String[] suffixs = mExtensions.toArray(new String[mExtensions.size()]);
        Collection<File> children = FileUtils.listFiles(dir,suffixs , true);
        for (File file : children) {
            if (file.getName().toLowerCase().contains(query)) {
                result.add(file);
            }
        }
    }
}
