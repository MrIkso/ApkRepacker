package com.mrikso.apkrepacker.patchengine;

import java.util.zip.ZipFile;

public class DymmyRule  extends Core{
    @Override
    public void start(LineReader lineReader) {

    }

    @Override
    public String currentRule(ZipFile zipFile) {
        return null;
    }
}
