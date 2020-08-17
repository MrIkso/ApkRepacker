package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.IOException;
import java.util.zip.ZipFile;

public class PatchRuleDummy extends PatchRule {

    private static final String strEnd = "[/DUMMY]";

    public PatchRuleDummy() {
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            String line2 = line.trim();
            if (!strEnd.equals(line2)) {
                if (!super.parseAsKeyword(line2, br)) {
                    logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line2);
                }
                line = br.readLine();
            } else {
                return;
            }
        }
    }

    @Override
    public String executeRule(ProjectHelper activity, ZipFile patchZip, IPatchContext logger) {
        return null;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        return true;
    }

    @Override
    public boolean isSmaliNeeded() {
        return false;
    }
}
