package com.mrikso.patchengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LinedReader extends BufferedReader {

    private int curLine = 0;

    public LinedReader(Reader input) {
        super(input);
    }

    public String readLine() throws IOException {
        this.curLine++;
        return super.readLine();
    }

    public int getCurrentLine() {
        return this.curLine;
    }
}
