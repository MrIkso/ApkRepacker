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

public class MatchAssignRule extends Core {

    private ApplicatonHelper f1017b;
    PrintInterface printInterface;
    private List c = new ArrayList();
    private List<String> d = new ArrayList();
    private boolean e = false;
    private boolean f = false;
    private List rules = new ArrayList();

    MatchAssignRule() {
        this.rules.add("[/MATCH_ASSIGN]");
        this.rules.add("TARGET:");
        this.rules.add("MATCH:");
        this.rules.add("REGEX:");
        this.rules.add("ASSIGN:");
        this.rules.add("DOTALL:");
    }

    private static String a(String str, List list) {
        int i = 0;
        while (true) {
            int i2 = i;
            if (i2 >= list.size()) {
                return str;
            }
            str = str.replace("${GROUP" + (i2 + 1) + "}", (CharSequence) list.get(i2));
            i = i2 + 1;
        }
    }

    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/MATCH_ASSIGN]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                if ("TARGET:".equals(trim)) {
                    this.f1017b = new ApplicatonHelper(printInterface, lineReader.readLine().trim(), lineReader.getLine());
                } else if ("REGEX:".equals(trim)) {
                    this.e = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                } else if ("DOTALL:".equals(trim)) {
                    this.f = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                } else if ("MATCH:".equals(trim)) {
                    readLine = a(lineReader, this.c, true, this.rules);
                } else if ("ASSIGN:".equals(trim)) {
                    readLine = a(lineReader, this.d, false, this.rules);
                } else {
                    //bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(lineReader.a()), trim);
                }
                readLine = lineReader.readLine();
            }
        }

    }

    @Override
    public String currentRule(ZipFile zipFile) {
        a(printInterface, this.c);
        String b2 = this.f1017b.b();
        while (b2 != null && !ss(printInterface, b2)) {
            b2 = this.f1017b.b();
        }

        return null;
    }

    public boolean ss(PrintInterface bVar, String str) {
        String a2 = a(FileUtil.getProjectPath() + "/" + str);
        String str2 = (String) this.c.get(0);
        @SuppressLint("WrongConstant")
        Matcher matcher = (this.f ? Pattern.compile(str2.trim(), 32) : Pattern.compile(str2.trim())).matcher(a2);
        if (!matcher.find(0)) {
            return false;
        }
        ArrayList arrayList = new ArrayList();
        int groupCount = matcher.groupCount();
        if (groupCount > 0) {
            for (int i = 0; i < groupCount; i++) {
                arrayList.add(matcher.group(i + 1));
            }
        }
        for (String trim : this.d) {
            String trim2 = trim.trim();
            int indexOf = trim2.indexOf(61);
            if (indexOf != -1) {
                String substring = trim2.substring(0, indexOf);
                String a3 = a(trim2.substring(indexOf + 1), arrayList);
                bVar.a(substring, a3);
                bVar.print("%s=\"%s\"", false, substring, a3);
            }
        }
        return true;
    }

    @Override
    public boolean inSmali() {
        return this.f1017b.a();

    }
}
