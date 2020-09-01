package com.mrikso.patchengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LinedReader extends BufferedReader {

    private int mCurrentLine = 0;

    public LinedReader(Reader input) {
        super(input);
    }

    public String readLine() throws IOException {
        mCurrentLine++;
        return super.readLine();
    }

    public int getCurrentLine() {
        return mCurrentLine;
    }
}
