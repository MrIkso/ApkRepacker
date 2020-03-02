package com.mrikso.apkrepacker.patchengine;

import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Reader {

    public List taskList = new ArrayList();

    public static Reader read(InputStream inputStream) {
        Reader reader = new Reader();
        LineReader lineReader = new LineReader(new InputStreamReader(inputStream));
        for (String readLine = lineReader.readLine(); readLine != null; readLine = lineReader.readLine()) {
            String line = readLine.trim();
            if (line.startsWith("[")) {
                switch (line) {
                    case "[MIN_ENGINE_VER]":
                       int v = Integer.valueOf(lineReader.readLine()).intValue();
                        Log.i("Reader", String.format("Version: %d", v ));
                        break;
                    case "[AUTHOR]":
                       String auth =  lineReader.readLine();
                        Log.i("Reader", String.format("AUTHOR: %s", auth ));
                        break;
                    case "[PACKAGE]":
                        lineReader.readLine();
                        break;
                    default:
                      Core core = null;
                        switch (line) {
                            case "[ADD_FILES]":
                                core = new AddFilesRule();
                                break;
                            case "[REMOVE_FILES]":
                                core = new RemoveFilesRule();
                                break;
                            case "[MERGE]":
                                core = new MergeRule();
                                break;
                            case "[MATCH_REPLACE]":
                                core = new MatchReplaceRule();
                                break;
                            case "[MATCH_GOTO]":
                                core = new MatchGotoRule();
                                break;
                            case "[MATCH_ASSIGN]":
                                core = new MatchAssignRule();
                                break;
                            case "[FUNCTION_REPLACE]":
                                core = new FunctionReplaceRule();
                                break;
                            case "[SIGNATURE_REVISE]":
                                //core = new AddFilesRule();
                                break;
                            case "[GOTO]":
                                core = new GotoRule();
                                break;
                            case "[DUMMY]":
                                core = new DummyRule();
                                break;
                            case "[EXECUTE_DEX]":
                                core = new ExecuteDex();
                                break;
                            default:
                                Log.i("Reader","Unknown Rule");
                                break;
                        }
                        if (core != null) {
                            core.start(lineReader);
                            reader.taskList.add(core);
                        }
                       break;
                }
            }
            else if (!line.startsWith("#") && !"".equals(line)) {
                Log.i("Reader",String.format("Unknown Rule %d,  %s", Integer.valueOf(lineReader.getLine()), line));
              //  print.print((int) R.string.patch_error_unknown_rule, Integer.valueOf(cVar.a()), trim);
            }
        }
        return reader;
    }
}

