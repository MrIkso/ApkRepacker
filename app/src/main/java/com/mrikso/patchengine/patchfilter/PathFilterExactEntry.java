package com.mrikso.patchengine.patchfilter;

import com.mrikso.patchengine.interfaces.IPatchContext;

public class PathFilterExactEntry extends PathFilter {

    private int cursor = 0;
    private String entryName;

    public PathFilterExactEntry(IPatchContext ctx, String pathStr) {
        this.entryName = pathStr;
    }

    @Override
    public String getNextEntry() {
        int i = this.cursor;
        if (i != 0) {
            return null;
        }
        this.cursor = i + 1;
        return this.entryName;
    }

    @Override
    public boolean isTarget(String entry) {
        return this.entryName.equals(entry);
    }

    @Override
    public boolean isSmaliNeeded() {
        int pos;
        String str = this.entryName;
        if (!(str == null || (pos = str.indexOf(47)) == -1)) {
            String firstDir = this.entryName.substring(0, pos);
            return "smali".equals(firstDir) || firstDir.startsWith("smali_");
        }
        return false;
    }

    @Override
    public boolean isWildMatch() {
        return false;
    }
}
