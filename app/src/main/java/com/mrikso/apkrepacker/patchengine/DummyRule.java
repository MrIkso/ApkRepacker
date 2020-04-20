package com.mrikso.apkrepacker.patchengine;

import java.util.zip.ZipFile;

public class DummyRule  extends Core{

    DummyRule(){

    }

    @Override
    public void start(LineReader lineReader) {
        line = lineReader.getLine();
        String readLine = lineReader.readLine();
        while (readLine != null) {
            String trim = readLine.trim();
            if ("[/DUMMY]".equals(trim)) {
                return;
            }
            if (super.checkName(trim, lineReader)) {
                readLine = lineReader.readLine();
            } else {
              //  bVar.a((int) R.string.patch_error_cannot_parse, Integer.valueOf(cVar.a()), trim);
                readLine = lineReader.readLine();
            }
        }

    }

    @Override
    public boolean inSmali() {
        return false;
    }

    @Override
    public String currentRule(ZipFile zipFile) {
        return null;
    }
}
