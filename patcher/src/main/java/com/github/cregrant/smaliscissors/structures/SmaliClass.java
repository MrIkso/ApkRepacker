package com.github.cregrant.smaliscissors.structures;

import com.github.cregrant.smaliscissors.Prefs;

import java.io.File;

public class SmaliClass {
    private final String detectedTarget;
    private final DecompiledFile file;
    private String path;

    public SmaliClass(DecompiledFile df, String target) {
        file = df;
        detectedTarget = target;
        String temp = df.getPath();
        path = 'L' + temp.substring(temp.indexOf(File.separatorChar)+1);
    }

    public String getPath() {
        return path;
    }

    public String getDetectedTarget() {
        return detectedTarget;
    }

    public DecompiledFile getFile() {
        return file;
    }
}
