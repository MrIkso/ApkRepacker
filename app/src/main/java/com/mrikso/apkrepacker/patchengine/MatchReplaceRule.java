package com.mrikso.apkrepacker.patchengine;

import com.mrikso.apkrepacker.App;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class MatchReplaceRule extends Core {

    private ApplicatonHelper target;
    private List c = new ArrayList();
    private List d = new ArrayList();
    private String e = null;
    private boolean useRegex = false;
    private boolean gotall = false;
    private List rules = new ArrayList();
    private boolean i;
    PrintInterface printInterface;

    MatchReplaceRule() {
        rules.add("[/MATCH_REPLACE]");
        rules.add("TARGET:");
        rules.add("MATCH:");
        rules.add("REGEX:");
        rules.add("REPLACE:");
        rules.add("DOTALL:");
    }
    
    private static String buildRegex(String str, r rVar) {
        List list = rVar.c;
        int i2 = 0;
        while (true) {
            int i3 = i2;
            if (i3 >= list.size()) {
                return str;
            }
            str = str.replace("${GROUP" + (i3 + 1) + "}", (CharSequence) list.get(i3));
            i2 = i3 + 1;
        }
    }

    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/MATCH_REPLACE]".equals(trim)) {
                break;
            } else if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                if ("TARGET:".equals(trim)) {
                    target = new ApplicatonHelper(printInterface, lineReader.readLine().trim(), lineReader.getLine());
                } else if ("REGEX:".equals(trim)) {
                    useRegex = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                } else if ("DOTALL:".equals(trim)) {
                    gotall = Boolean.valueOf(lineReader.readLine().trim()).booleanValue();
                } else if ("MATCH:".equals(trim)) {
                    readLine = a(lineReader, c, true, rules);
                } else if ("REPLACE:".equals(trim)) {
                    readLine = a(lineReader, d, false, rules);
                } else {
                   // bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(lineReader.a()), trim);
                }
                readLine = lineReader.readLine();
            }
        }
        if (target != null) {
            i = target.d();
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
}
