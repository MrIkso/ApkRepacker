package com.mrikso.apkrepacker.patchengine;

public class aa extends w{
    private String f996a;
    private int f997b = 0;

    public aa(String str) {
        this.f996a = str;
    }

    public final String a() {
        if (this.f997b != 0) {
            return null;
        }
        this.f997b++;
        return this.f996a;
    }

    public final boolean a(String str) {
        return this.f996a.equals(str);
    }

    public final boolean b() {
        int indexOf;
        if (this.f996a == null || (indexOf = this.f996a.indexOf(47)) == -1) {
            return false;
        }
        String substring = this.f996a.substring(0, indexOf);
        return "smali".equals(substring) || substring.startsWith("smali_");
    }

    public final boolean c() {
        return false;
    }

}
