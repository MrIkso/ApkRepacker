package com.mrikso.apkrepacker.patchengine;

import android.util.Log;

import java.io.File;
import java.io.InputStream;
import java.util.List;
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
            ParsePatch parsePatch = new ParsePatch();
            parsePatch.start(reader.taskList, patch);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void start(List rules, ZipFile zipFile) {
        new PatchRuleExecutor(this, rules, zipFile).start();
    }

    public static int nextRule(List list, String rule) {
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= list.size()) {
                return -1;
            }
            if (rule.equals(((Core) list.get(i2)).getLine())) {
                return i2;
            }
            i = i2 + 1;
        }
    }

}
