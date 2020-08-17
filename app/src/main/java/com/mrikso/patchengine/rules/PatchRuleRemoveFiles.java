package com.mrikso.patchengine.rules;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class PatchRuleRemoveFiles extends PatchRule {

    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/REMOVE_FILES]";
    private List<String> targetList = new ArrayList();

    public PatchRuleRemoveFiles() {
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        String next;
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            String line2 = line.trim();
            if (!strEnd.equals(line2)) {
                if (super.parseAsKeyword(line2, br)) {
                    line = br.readLine();
                } else if (TARGET.equals(line2)) {
                    while (true) {
                        String readLine = br.readLine();
                        next = readLine;
                        if (readLine == null) {
                            break;
                        }
                        next = next.trim();
                        if (next.startsWith("[")) {
                            break;
                        } else if (!"".equals(next)) {
                            this.targetList.add(next);
                        }
                    }
                    line = next;
                } else {
                    logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line2);
                    line = br.readLine();
                }
            } else {
                return;
            }
        }
    }

    @Override
    public String executeRule(ProjectHelper activity, ZipFile patchZip, IPatchContext logger) {
        String rootPath = activity.getProjectPath();
        //ResListAdapter resAdapter = activity.getResListAdapter();
        for (int i = 0; i < this.targetList.size(); i++) {
            String filePath = rootPath + "/" + this.targetList.get(i);
            int pos = filePath.lastIndexOf(47);
            // String dirPath = filePath.substring(0, pos);
            //  String fileName = filePath.substring(pos + 1);
            File delete = new File(filePath);
            if (delete.exists()) {
                if (delete.isFile()) {
                    try {
                        FileUtils.deleteQuietly(new File(filePath));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        FileUtils.deleteDirectory(new File(filePath));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (!this.targetList.isEmpty()) {
            return true;
        }
        logger.error(R.string.patch_error_no_target_file);
        return false;
    }

    @Override
    public boolean isSmaliNeeded() {
        for (String file : this.targetList) {
            if (super.isInSmaliFolder(file)) {
                return true;
            }
        }
        return false;
    }
}
