package com.mrikso.apkrepacker.patchengine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

public class LineReader extends BufferedReader {

    private int line = 0;

    public LineReader(Reader reader) {
        super(reader);
    }

    public final int getLine() {
        return this.line;
    }

    public final String readLine() {
        this.line++;
        try {
            return super.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
