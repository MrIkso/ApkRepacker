package com.mrikso.apkrepacker.patchengine;

import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ParsePatch {
    private static ZipFile patch;
    private static Reader reader;

    public static void openPatch(File file) {
        try {
            patch = new ZipFile(file);
            ZipEntry entry = patch.getEntry("patch.txt");
            if (entry == null) {
                patch.close();
                patch = null;
                Log.i("ParsePatch", "patch.txt not found");
            }
            InputStream inputStream = patch.getInputStream(entry);
            reader = reader.read(inputStream);
            inputStream.close();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
