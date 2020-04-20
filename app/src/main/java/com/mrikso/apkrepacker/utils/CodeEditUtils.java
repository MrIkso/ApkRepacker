package com.mrikso.apkrepacker.utils;

import java.io.File;

public class CodeEditUtils {
    public static boolean hasExtension(File file, String... exts) {
        for (String ext : exts) {
            if (file.getPath().toLowerCase().endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canEdit(File file) {
        String[] exts = {".java", ".xml", ".txt", ".json", ".smali"};
        return file.canWrite() && hasExtension(file, exts);
    }
}
