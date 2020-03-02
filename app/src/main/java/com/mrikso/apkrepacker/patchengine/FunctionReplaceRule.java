package com.mrikso.apkrepacker.patchengine;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class FunctionReplaceRule extends Core {

    private List lines = new ArrayList();
    private List rules = new ArrayList();

    FunctionReplaceRule() {
        this.rules.add("[/FUNCTION_REPLACE]");
        this.rules.add("TARGET:");
        this.rules.add("FUNCTION:");
        this.rules.add("REPLACE:");
    }

    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/FUNCTION_REPLACE]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                if ("TARGET:".equals(trim)) {
                    lineReader.readLine().trim();
                } else if ("FUNCTION:".equals(trim)) {
                    lineReader.readLine().trim();
                } else if ("REPLACE:".equals(trim)) {
                    readLine = a(lineReader, this.lines, false, this.rules);
                } else {
                   // bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(lineReader.a()), trim);
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
}
