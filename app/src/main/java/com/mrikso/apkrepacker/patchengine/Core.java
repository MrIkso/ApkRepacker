package com.mrikso.apkrepacker.patchengine;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

public abstract class Core {
    protected int line;
    private String mLine;

    protected static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            }
        }
    }

    public final boolean checkName(String str, LineReader cVar ) {
        if (!"NAME:".equals(str)) {
            return false;
        }
        this.mLine = cVar.readLine();
        if (this.mLine != null) {
            this.mLine = this.mLine.trim();
        }
        return true;
    }

    static String a(BufferedReader bufferedReader, List list, boolean z, List list2) {
        String readLine = null;
        try {
            readLine = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (readLine != null) {
            if (z) {
                readLine = readLine.trim();
            }
            if (list2.contains(readLine)) {
                break;
            }
            list.add(readLine);
            try {
                readLine = bufferedReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return readLine;
    }

    public abstract void start(LineReader lineReader);

    public abstract String currentRule(ZipFile zipFile);

    static boolean checkIsSmali(String str) {
        int lastIndexOf;
        if (str == null || (lastIndexOf = str.lastIndexOf(47)) == -1) {
            return false;
        }
        String substring = str.substring(0, lastIndexOf);
        return "smali".equals(substring) || substring.startsWith("smali_");
    }

}
