package com.mrikso.patchengine.patchfilter;

public abstract class PathFilter {

    public abstract String getNextEntry();

    public abstract boolean isSmaliNeeded();

    public abstract boolean isTarget(String str);

    public abstract boolean isWildMatch();
}
