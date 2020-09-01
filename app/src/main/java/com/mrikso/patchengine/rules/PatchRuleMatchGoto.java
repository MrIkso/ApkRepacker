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
        keywords.add(TARGET);
        keywords.add(MATCH);
        keywords.add(REGEX);
        keywords.add(GOTO);
        keywords.add(DOTALL);
        keywords.add(strEnd);
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
                                line = readMultiLines(br, matches, true, keywords);
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
        preProcessing(logger, matches);
        String nextPath = pathFinder.getNextPath();
        while (nextPath != null) {
            if (entryMatches(projectHelper, logger, nextPath)) {
                return gotoRule;
            }
            nextPath = pathFinder.getNextPath();
        }
        return null;
    }

    private boolean entryMatches(ProjectHelper projectHelper, IPatchContext patchCtx, String targetFile) {
        Pattern pattern;
        String filepath = projectHelper.getProjectPath() + "/" + targetFile;
        if (isUseRegex) {
            try {
                String content = readFileContent(filepath);
                List<Section> sections = new ArrayList<>();
                String regStr = matches.get(0);
                if (bDotall) {
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
                while (i2 < (lines.size() - matches.size()) + 1 && !(matches2 = checkMatch(lines, i2))) {
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
        while (i < matches.size() && lines.get(idx + i).trim().equals(matches.get(i))) {
            i++;
        }
        return i == matches.size();
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (pathFinder == null || !pathFinder.isValid()) {
            return false;
        }
        if (matches.isEmpty()) {
            logger.error(R.string.patch_error_no_match_content);
            return false;
        } else if (gotoRule == null) {
            logger.error(R.string.patch_error_no_goto_target);
            return false;
        } else {
            List<String> allRuleName = logger.getPatchNames();
            if (allRuleName != null && allRuleName.contains(gotoRule)) {
                return true;
            }
            logger.error(R.string.patch_error_goto_target_notfound, gotoRule);
            return false;
        }
    }

    @Override
    public boolean isSmaliNeeded() {
        return pathFinder.isSmaliNeeded();
    }

}
