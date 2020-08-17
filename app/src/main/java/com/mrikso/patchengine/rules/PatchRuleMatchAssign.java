package com.mrikso.patchengine.rules;

import android.annotation.SuppressLint;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.PathFinder;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class PatchRuleMatchAssign extends PatchRule {

    private static final String ASSIGN = "ASSIGN:";
    private static final String DOTALL = "DOTALL:";
    private static final String MATCH = "MATCH:";
    private static final String REGEX = "REGEX:";
    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/MATCH_ASSIGN]";
    private List<String> assigns = new ArrayList();
    private boolean bDotall = false;
    private boolean bRegex = false;
    private List<String> keywords = new ArrayList<>();
    private List<String> matches = new ArrayList();
    private PathFinder pathFinder;

    public PatchRuleMatchAssign() {
        this.keywords.add(TARGET);
        this.keywords.add(MATCH);
        this.keywords.add(REGEX);
        this.keywords.add(ASSIGN);
        this.keywords.add(DOTALL);
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
                            this.pathFinder = new PathFinder(logger, br.readLine().trim(), br.getCurrentLine());
                            break;
                        case REGEX:
                            this.bRegex = Boolean.parseBoolean(br.readLine().trim());
                            break;
                        case DOTALL:
                            this.bDotall = Boolean.parseBoolean(br.readLine().trim());
                            break;
                        default:
                            if (MATCH.equals(line)) {
                                line = readMultiLines(br, this.matches, true, this.keywords);
                                continue;
                            }
                            if (ASSIGN.equals(line)) {
                                line = readMultiLines(br, this.assigns, false, this.keywords);
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
        preProcessing(logger, this.matches);
        String nextPath = this.pathFinder.getNextPath();
        while (nextPath != null && !executeOnEntry(activity, patchZip, logger, nextPath)) {
            nextPath = this.pathFinder.getNextPath();
        }
        return null;
    }

    private boolean executeOnEntry(ProjectHelper activity, ZipFile patchZip, IPatchContext patchCtx, String targetFile) {
        Pattern pattern;
        String filepath;
        String content;
        String filepath2 = activity.getProjectPath() + "/" + targetFile;
        int i = 0;
        try {
            String content2 = readFileContent(filepath2);
            String regStr = matches.get(0);
            if (bDotall) {
                pattern = Pattern.compile(regStr.trim(), Pattern.DOTALL);
            } else {
                pattern = Pattern.compile(regStr.trim());
            }
            Matcher m = pattern.matcher(content2);
            if (m.find(0)) {
                List<String> groupStrs = new ArrayList<>();
                int groupCount = m.groupCount();
                if (groupCount > 0) {
                    for (int i2 = 0; i2 < groupCount; i2++) {
                        groupStrs.add(m.group(i2 + 1));
                    }
                }
                for (String strAssign : assigns) {
                    String strAssign2 = strAssign.trim();
                    int position = strAssign2.indexOf(61);
                    if (position != -1) {
                        String name = strAssign2.substring(i, position);
                        String assignedVal = getRealValue(strAssign2.substring(position + 1), groupStrs);
                        patchCtx.setVariableValue(name, assignedVal);
                        content = content2;
                        filepath = filepath2;
                        patchCtx.info("%s=\"%s\"", false, name, assignedVal);
                    } else {
                        content = content2;
                        filepath = filepath2;
                    }
                    content2 = content;
                    filepath2 = filepath;
                    i = 0;
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            patchCtx.error(R.string.patch_error_read_from, filepath2);
            return false;
        }
    }

    private String getRealValue(String valueBefore, List<String> groupStrs) {
        String result = valueBefore;
        for (int i = 0; i < groupStrs.size(); i++) {
            result = result.replace("${GROUP" + (i + 1) + "}", groupStrs.get(i));
        }
        return result;
    }

    public boolean isValid(IPatchContext logger) {
        PathFinder pathFinder2 = this.pathFinder;
        if (pathFinder2 == null || !pathFinder2.isValid()) {
            return false;
        }
        if (this.matches.isEmpty()) {
            logger.error(R.string.patch_error_no_match_content);
            return false;
        } else if (this.bRegex) {
            return true;
        } else {
            logger.error(R.string.patch_error_regex_not_true);
            return false;
        }
    }

    public boolean isSmaliNeeded() {
        return this.pathFinder.isSmaliNeeded();
    }
}
