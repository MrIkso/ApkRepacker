package com.mrikso.patchengine;

import org.jetbrains.annotations.NotNull;

public class ReplaceRec {
    private int endPos;
    private String replacing;
    private int startPos;

    public ReplaceRec(int startPos, int endPos, String replacing) {
        this.startPos = startPos;
        this.endPos = endPos;
        this.replacing = replacing;
    }

    public int getEndPos() {
        return endPos;
    }

    public int getStartPos() {
        return startPos;
    }

    public String getReplacing() {
        return replacing;
    }

    @NotNull
    @Override
    public String toString() {
        return "ReplaceRec{" +
                "endPos=" + endPos +
                ", replacing='" + replacing + '\'' +
                ", startPos=" + startPos +
                '}';
    }
}
