package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class PatchRuleFuncReplace extends PatchRule {

    private static final String FUNCTION = "FUNCTION:";
    private static final String REPLACE = "REPLACE:";
    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/FUNCTION_REPLACE]";
    private List<String> keywords = new ArrayList<>();
    private List<String> replaceContents = new ArrayList();
    private String strFunction;
    private String targetFile;

    public PatchRuleFuncReplace() {
        this.keywords.add(TARGET);
        this.keywords.add(FUNCTION);
        this.keywords.add(REPLACE);
        this.keywords.add(strEnd);
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (!strEnd.equals(line)) {
                if (!super.parseAsKeyword(line, br)) {
                    switch (line) {
                        case TARGET:
                            this.targetFile = br.readLine().trim();
                            break;
                        case FUNCTION:
                            this.strFunction = br.readLine().trim();
                            break;
                        default:
                            if (REPLACE.equals(line)) {
                                line = readMultiLines(br, this.replaceContents, false, this.keywords);
                                continue;
                            }
                            logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line);
                            break;
                    }
                }
                line = br.readLine();
            } else {
                return;
            }
        }
    }

    @Override
    public String executeRule(ProjectHelper activity, ZipFile patchZip, IPatchContext logger) {
        logger.error(R.string.general_error, "Not supported yet.");
        return null;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        return false;
    }

    @Override
    public boolean isSmaliNeeded() {
        return false;
    }
}
