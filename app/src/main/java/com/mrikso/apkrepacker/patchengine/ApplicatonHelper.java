package com.mrikso.apkrepacker.patchengine;

import java.util.ArrayList;
import java.util.List;

public class ApplicatonHelper {

    private List params = new ArrayList();

    ApplicatonHelper(PrintInterface bVar, String str, int i) {
        SamliHelper smaliHelper;
        String a2 = Core.a(bVar, str);
        str = a2 != null ? a2 : str;
        if (str.startsWith("[") && str.endsWith("]")) {
            for (String str2 : a(str)) {
                if ("APPLICATION".equals(str2)) {
                    smaliHelper = new SamliHelper(bVar, z.f1030a);
                } else if ("ACTIVITIES".equals(str2)) {
                    smaliHelper = new SamliHelper(bVar, z.f1031b);
                } else if ("LAUNCHER_ACTIVITIES".equals(str2)) {
                    smaliHelper = new SamliHelper(bVar, z.c);
                } else {
                  //  bVar.a((int) R.string.patch_error_invalid_target, Integer.valueOf(i));
                    smaliHelper = null;
                }
                if (smaliHelper != null) {
                    this.params.add(smaliHelper);
                } else {
                    this.params = null;
                    return;
                }
            }
        } else if (str.contains("*")) {
            this.params.add(new ab(bVar, str));
        } else {
            this.params.add(new aa(str));
        }
    }

    private static List<String> a(String str) {
        ArrayList arrayList = new ArrayList();
        int i = 1;
        int indexOf = str.indexOf(93);
        while (i > 0 && indexOf > i) {
            arrayList.add(str.substring(i, indexOf));
            i = str.indexOf(91, indexOf) + 1;
            if (i > 0) {
                indexOf = str.indexOf(93, i);
            }
        }
        return arrayList;
    }

    /* access modifiers changed from: package-private */
    public final boolean a() {
        if (this.params == null) {
            return false;
        }
        for (int i = 0; i < this.params.size(); i++) {
            if (((w) this.params.get(i)).b()) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public final String b() {
        boolean z;
        if (this.params == null) {
            return null;
        }
        String a2 = ((w) this.params.get(0)).a();
        if (this.params.size() <= 1) {
            return a2;
        }
        String str = a2;
        while (str != null) {
            int i = 1;
            while (true) {
                if (i >= this.params.size()) {
                    z = true;
                    break;
                } else if (!((w) this.params.get(i)).a(str)) {
                    z = false;
                    break;
                } else {
                    i++;
                }
            }
            if (z) {
                break;
            }
            str = ((w) this.params.get(0)).a();
        }
        return str;
    }

    public final boolean c() {
        return this.params != null;
    }

    public final boolean d() {
        if (this.params == null) {
            return false;
        }

        return true;
    }

}
