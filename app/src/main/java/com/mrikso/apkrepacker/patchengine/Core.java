package com.mrikso.apkrepacker.patchengine;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
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

    public final String a(String str) {
        File file = new File(str);
        StringBuilder sb = new StringBuilder(((int) file.length()) + 32);
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String readLine = bufferedReader.readLine();
            if (readLine != null) {
                sb.append(readLine);
            }
            while (true) {
                String readLine2 = bufferedReader.readLine();
                if (readLine2 == null) {
                    return sb.toString();
                }
                sb.append(IOUtils.LINE_SEPARATOR_UNIX);
                sb.append(readLine2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            a(bufferedReader);
        }
        return sb.toString();
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

    public static String a(PrintInterface bVar, String str) {
        int i;
        int i2 = 0;
        ArrayList arrayList = new ArrayList();
        int indexOf = str.indexOf("${");
        while (indexOf != -1) {
            int i3 = indexOf + 2;
            int indexOf2 = str.indexOf("}", i3);
            if (indexOf2 == -1) {
                break;
            }
            String a2 = bVar.a(str.substring(i3, indexOf2));
            if (a2 != null) {
                arrayList.add(new h(i3 - 2, indexOf2 + 1, a2));
            }
            indexOf = str.indexOf("${", indexOf2);
        }
        if (arrayList.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Iterator it = arrayList.iterator();
        while (true) {
            i = i2;
            if (!it.hasNext()) {
                break;
            }
            h hVar = (h) it.next();
            int i4 = hVar.f1011a;
            if (i4 > i) {
                sb.append(str.substring(i, i4));
            }
            sb.append(hVar.c);
            i2 = hVar.f1012b;
        }
        if (i < str.length()) {
            sb.append(str.substring(i));
        }
        return sb.toString();
    }

    protected static void a(PrintInterface bVar, List list) {
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 < list.size()) {
                String a2 = a(bVar, (String) list.get(i2));
                if (a2 != null) {
                    list.set(i2, a2);
                }
                i = i2 + 1;
            } else {
                return;
            }
        }
    }

    public final String getLine() {
        return this.mLine;
    }

    public abstract void start(LineReader lineReader);

    public abstract String currentRule(ZipFile zipFile);

    public abstract boolean inSmali();

    static boolean checkIsSmali(String str) {
        int lastIndexOf;
        if (str == null || (lastIndexOf = str.lastIndexOf(47)) == -1) {
            return false;
        }
        String substring = str.substring(0, lastIndexOf);
        return "smali".equals(substring) || substring.startsWith("smali_");
    }

    public final List b(String str) {
        ArrayList arrayList = new ArrayList();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(str)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    return arrayList;
                }
                if (!"".equals(readLine.trim())) {
                    arrayList.add(readLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                a(bufferedReader);
            }
        }
    }

}
