package com.mrikso.apkrepacker.patchengine;

import android.annotation.SuppressLint;

import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class MatchGotoRule extends Core {
    /* renamed from: b  reason: collision with root package name */
    private ApplicatonHelper f1018b;
    private List c = new ArrayList();
    private String d;
    private boolean e = false;
    private boolean f = false;
    private List rules = new ArrayList();
PrintInterface printInterface;

    MatchGotoRule() {
        this.rules.add("[/MATCH_GOTO]");
        this.rules.add("TARGET:");
        this.rules.add("MATCH:");
        this.rules.add("REGEX:");
        this.rules.add("GOTO:");
        this.rules.add("DOTALL:");
    }

    private boolean ssa( PrintInterface bVar, String str) {
        int i = 0;
        String str2 = FileUtil.getProjectPath() + "/" + str;
        if (this.e) {
            String a2 = a(str2);
            ArrayList arrayList = new ArrayList();
            String str3 = (String) this.c.get(0);
            @SuppressLint("WrongConstant")
            Matcher matcher = (this.f ? Pattern.compile(str3.trim(), 32) : Pattern.compile(str3.trim())).matcher(a2);
            for (int i2 = 0; matcher.find(i2); i2 = matcher.end()) {
                ArrayList arrayList2 = null;
                int groupCount = matcher.groupCount();
                if (groupCount > 0) {
                    arrayList2 = new ArrayList(groupCount);
                    for (int i3 = 0; i3 < groupCount; i3++) {
                        arrayList2.add(matcher.group(i3 + 1));
                    }
                }
                arrayList.add(new p(matcher.start(), matcher.end(), arrayList2));
            }
            return !arrayList.isEmpty();
        } else {
            List b2 = super.b(str2);
            boolean z = false;
            while (i < (b2.size() - this.c.size()) + 1 && !(z = a(b2, i))) {
                i++;
            }
            return z;
        }
    }

    private boolean a(List list, int i) {
        int i2 = 0;
        while (i2 < this.c.size() && ((String) list.get(i + i2)).trim().equals(this.c.get(i2))) {
            i2++;
        }
        return i2 == this.c.size();
    }


    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/MATCH_GOTO]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                if ("TARGET:".equals(trim)) {
                    this.f1018b = new ApplicatonHelper(printInterface, lineReader.readLine().trim(), lineReader.getLine());
                } else if ("REGEX:".equals(trim)) {
                    this.e = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                } else if ("DOTALL:".equals(trim)) {
                    this.f = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                } else if ("MATCH:".equals(trim)) {
                    readLine = a(lineReader, this.c, true, this.rules);
                } else if ("GOTO:".equals(trim)) {
                    this.d = lineReader.readLine().trim();
                    readLine = lineReader.readLine();
                } else {
                    //bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(lineReader.a()), trim);
                }
                readLine = lineReader.readLine();
            }
        }

    }

    @Override
    public String currentRule(ZipFile zipFile) {
        return null;
    }

    @Override
    public boolean inSmali() {
        return false;
    }

    final class p {
        public p(int i, int i2, List list) {
        }
    }
}
