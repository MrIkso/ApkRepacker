package com.mrikso.patchengine;

import java.util.List;

public class Section {
    public int end;
    public int start;
    public List<String> groupStrs;

    public Section(int _start, int _end, List<String> _groupStrs) {
        this.start = _start;
        this.end = _end;
        this.groupStrs = _groupStrs;
    }
}