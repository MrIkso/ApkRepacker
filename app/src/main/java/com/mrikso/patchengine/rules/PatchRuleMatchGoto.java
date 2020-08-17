package com.mrikso.patchengine.rules;

import android.annotation.SuppressLint;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.PathFinder;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.Section;
import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

public class PatchRuleMatchGoto extends PatchRule {

    private static final String DOTALL = "DOTALL:";
    private static final String GOTO = "GOTO:";
    private static final String MATCH = "MATCH:";
    private static final String REGEX = "REGEX:";
    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/MATCH_GOTO]";
    private boolean bDotall = false;
    private boolean isUseRegex = false;
    private String gotoRule;
    private List<String> keywords = new ArrayList<>();
    private List<String> matches = new ArrayList();
    private PathFinder pathFinder;

    public PatchRuleMatchGoto() {

        this.keywords.add(TARGET);
        this.keywords.add(MATCH);
        this.keywords.add(REGEX);
        this.keywords.add(GOTO);
        this.keywords.add(DOTALL);
        this.keywords.add(strEnd);
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (!strEnd.equals(line)) {
                if (!super.parseAsKeyword(line, br)) {
                    switch (line) {
                        case TARGET:
                            pathFinder = new PathFinder(logger, br.readLine().trim(), br.getCurrentLine());
                            break;
                        case REGEX:
                            isUseRegex = Boolean.parseBoolean(br.readLine().trim());
                            break;
                        case DOTALL:
                            bDotall = Boolean.parseBoolean(br.readLine().trim());
                            break;
                        default:
                            if (MATCH.equals(line)) {
                                line = readMultiLines(br, this.matches, true, this.keywords);
                                continue;
                            }
                            if (GOTO.equals(line)) {
                                gotoRule = br.readLine().trim();
                                line = br.readLine();
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
    public String executeRule(ProjectHelper projectHelper, ZipFile patchZip, IPatchContext logger) {
        preProcessing(logger, this.matches);
        String nextPath = this.pathFinder.getNextPath();
        while (nextPath != null) {
            if (entryMatches(projectHelper, logger, nextPath)) {
                return this.gotoRule;
            }
            nextPath = this.pathFinder.getNextPath();
        }
        return null;
    }

    @SuppressLint("WrongConstant")
    private boolean entryMatches(ProjectHelper activity, IPatchContext patchCtx, String targetFile) {
        Pattern pattern;
        String filepath = activity.getProjectPath() + "/" + targetFile;
        if (isUseRegex) {
            try {
                String content = readFileContent(filepath);
                List<Section> sections = new ArrayList<>();
                String regStr = this.matches.get(0);
                if (this.bDotall) {
                    pattern = Pattern.compile(regStr.trim(), Pattern.DOTALL);
                } else {
                    pattern = Pattern.compile(regStr.trim());
                }
                Matcher m = pattern.matcher(content);
                for (int position = 0; m.find(position); position = m.end()) {
                    List<String> groupStrs = null;
                    int groupCount = m.groupCount();
                    if (groupCount > 0) {
                        groupStrs = new ArrayList<>(groupCount);
                        for (int i = 0; i < groupCount; i++) {
                            groupStrs.add(m.group(i + 1));
                        }
                    }
                    sections.add(new Section(m.start(), m.end(), groupStrs));
                }
                return !sections.isEmpty();
            } catch (IOException e) {
                patchCtx.error(R.string.patch_error_read_from, targetFile);
                return false;
            }
        } else {
            try {
                List<String> lines = super.readFileLines(filepath);
                boolean matches2 = false;
                int i2 = 0;
                while (i2 < (lines.size() - this.matches.size()) + 1 && !(matches2 = checkMatch(lines, i2))) {
                    i2++;
                }
                return matches2;
            } catch (IOException e2) {
                patchCtx.error(R.string.patch_error_read_from, targetFile);
                return false;
            }
        }
    }

    private boolean checkMatch(List<String> lines, int idx) {
        int i = 0;
        while (i < this.matches.size() && lines.get(idx + i).trim().equals(this.matches.get(i))) {
            i++;
        }
        return i == this.matches.size();
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        PathFinder pathFinder2 = this.pathFinder;
        if (pathFinder2 == null || !pathFinder2.isValid()) {
            return false;
        }
        if (this.matches.isEmpty()) {
            logger.error(R.string.patch_error_no_match_content);
            return false;
        } else if (this.gotoRule == null) {
            logger.error(R.string.patch_error_no_goto_target);
            return false;
        } else {
            List<String> allRuleName = logger.getPatchNames();
            if (allRuleName != null && allRuleName.contains(this.gotoRule)) {
                return true;
            }
            logger.error(R.string.patch_error_goto_target_notfound, this.gotoRule);
            return false;
        }
    }

    @Override
    public boolean isSmaliNeeded() {
        return this.pathFinder.isSmaliNeeded();
    }

}
