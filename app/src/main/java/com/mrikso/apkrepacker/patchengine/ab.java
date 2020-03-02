package com.mrikso.apkrepacker.patchengine;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ab extends w {

    private String f998a;
    private String f999b;
    private String c;
    private boolean d = false;
    private List e = new LinkedList();
    private List f = new ArrayList();
    private int g = 0;

    public ab(PrintInterface bVar, String str) {
        this.f998a = str;
        this.f999b = "^" + str.replace("*", ".*") + ".";
        this.c = bVar.b();
    }

    public final String a() {
        if (!this.d) {
            File[] listFiles = new File(this.c).listFiles();
            if (listFiles != null) {
                for (File file : listFiles) {
                    if (file.isDirectory()) {
                        this.e.add(file.getName());
                    } else {
                        String name = file.getName();
                        if (a(name)) {
                            this.f.add(name);
                        }
                    }
                }
            }
            this.d = true;
        }
        if (this.g < this.f.size()) {
            String str = (String) this.f.get(this.g);
            this.g++;
            return str;
        }
        if (!this.e.isEmpty()) {
            this.g = 0;
            this.f.clear();
            while (!this.e.isEmpty()) {
                String str2 = (String) this.e.remove(0);
                File[] listFiles2 = new File(this.c + "/" + str2).listFiles();
                if (listFiles2 != null) {
                    for (File file2 : listFiles2) {
                        String str3 = str2 + "/" + file2.getName();
                        if (file2.isDirectory()) {
                            this.e.add(str3);
                        } else if (a(str3)) {
                            this.f.add(str3);
                        }
                    }
                }
                if (!this.f.isEmpty()) {
                    break;
                }
            }
            if (!this.f.isEmpty()) {
                this.g = 1;
                return (String) this.f.get(0);
            }
        }
        return null;
    }

    public final boolean a(String str) {
        return str.matches(this.f999b);
    }

    public final boolean b() {
        return this.f998a.startsWith("smali") || this.f998a.endsWith(".smali");
    }

    public final boolean c() {
        return true;
    }
}

