package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipFile;

public class PatchRuleGoto extends PatchRule {

    private static final String GOTO = "GOTO:";
    private static final String strEnd = "[/GOTO]";
    private String targetRule;

    public PatchRuleGoto() {
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            String line2 = line.trim();
            if (!strEnd.equals(line2)) {
                if (!super.parseAsKeyword(line2, br)) {
                    if (GOTO.equals(line2)) {
                        this.targetRule = br.readLine().trim();
                    } else {
                        logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line2);
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
        return this.targetRule;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (this.targetRule == null) {
            logger.error(R.string.patch_error_no_goto_target);
            return false;
        }
        List<String> allRuleName = logger.getPatchNames();
        if (allRuleName != null && allRuleName.contains(this.targetRule)) {
            return true;
        }
        logger.error(R.string.patch_error_goto_target_notfound, this.targetRule);
        return false;
    }

    @Override
    public boolean isSmaliNeeded() {
        return false;
    }
}
