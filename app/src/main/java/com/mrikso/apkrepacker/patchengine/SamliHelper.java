package com.mrikso.apkrepacker.patchengine;

import java.io.File;
import java.util.List;

public class SamliHelper extends w{

    private int f1027a;
    private String f1028b;
    private String c;
    private List d;
    private int e = 0;

    public SamliHelper(PrintInterface bVar, int i) {
        this.f1027a = i;
        this.f1028b = bVar.b();
        switch (y.f1029a[i - 1]) {
            case 1:
                this.c = bVar.c();
                return;
            case 2:
                this.d = bVar.d();
                return;
            case 3:
                this.d = bVar.e();
                return;
            default:
                return;
        }
    }

    private String a(String str, String str2, boolean z) {
        String str3 = str + "/" + str2.replaceAll("\\.", "/") + ".smali";
        String str4 = this.f1028b + "/" + str3;
        if (!z || new File(str4).exists()) {
            return str3;
        }
        return null;
    }

    private String b(String str) {
        String a2 = a("smali", str, true);
        int i = 2;
        while (a2 == null && i < 8) {
            String a3 = a("smali_classes" + i, str, true);
            i++;
            a2 = a3;
        }
        return a2 == null ? a("smali", str, false) : a2;
    }

    public final String a() {
        switch (y.f1029a[this.f1027a - 1]) {
            case 1:
                if (this.e == 0) {
                    this.e++;
                    return b(this.c);
                }
                break;
            case 2:
            case 3:
                if (this.e < this.d.size()) {
                    List list = this.d;
                    int i = this.e;
                    this.e = i + 1;
                    return b((String) list.get(i));
                }
                break;
        }
        return null;
    }

    public final boolean a(String str) {
        int indexOf = str.indexOf(47);
        if (indexOf != -1 && str.endsWith(".smali")) {
            String replaceAll = str.substring(indexOf + 1, str.length() - 6).replaceAll("/", ".");
            switch (y.f1029a[this.f1027a - 1]) {
                case 1:
                    return replaceAll.equals(this.c);
                case 2:
                case 3:
                    return this.d.contains(replaceAll);
            }
        }
        return false;
    }

    public final boolean b() {
        return true;
    }

    public final boolean c() {
        return false;
    }
}

