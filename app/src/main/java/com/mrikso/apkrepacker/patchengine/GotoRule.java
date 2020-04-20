package com.mrikso.apkrepacker.patchengine;

import java.util.zip.ZipFile;

public class GotoRule extends Core {
    private String name;

    GotoRule() {

    }

    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/GOTO]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
                if ("GOTO:".equals(trim)) {
                    this.name = lineReader.readLine().trim();
                } else {
                  //  bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(lineReader.a()), trim);
                }
                readLine = lineReader.readLine();
            }
        }

    }

    @Override
    public String currentRule(ZipFile zipFile) {
        return this.name;

    }

    @Override
    public boolean inSmali() {
        return false;
    }
}
