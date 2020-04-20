package com.mrikso.apkrepacker.utils;

import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtils {
    public static ZipEntry getEntry(ZipFile zf, String patch){
        return zf.getEntry(patch);
    }
}
