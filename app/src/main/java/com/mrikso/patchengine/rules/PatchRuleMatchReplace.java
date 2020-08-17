package com.mrikso.patchengine.rules;

import android.annotation.SuppressLint;
import android.util.Log;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.PathFinder;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class PatchRuleMatchReplace extends PatchRule {
    private static final String TAG = "PatchRuleMatchReplace";

    private static final String DOTALL = "DOTALL:";
    private static final String MATCH = "MATCH:";
    private static final String REGEX = "REGEX:";
    private static final String REPLACE = "REPLACE:";
    private static final String TARGET = "TARGET:";
    private static final String strEnd = "[/MATCH_REPLACE]";
    private boolean bDotall = false;
    private boolean bRegex = false;
    private boolean isWildMatch;
    private List<String> keywords = new ArrayList<>();
    private List<String> matches = new ArrayList();
    private PathFinder pathFinder;
    private List<String> replaces = new ArrayList();
    private String replacingStr = null;

    public PatchRuleMatchReplace() {
        this.keywords.add(TARGET);
        this.keywords.add(MATCH);
        this.keywords.add(REGEX);
        this.keywords.add(REPLACE);
        this.keywords.add(DOTALL);
        this.keywords.add(strEnd);
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        this.startLine = br.getCurrentLine();
        String line = br.readLine();
        while (line != null) {
            line = line.trim();
            if (strEnd.equals(line)) {
                break;
            }
            if (!super.parseAsKeyword(line, br)) {
                Log.d(TAG, "Parse MatchReplace rule");
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
                           // Log.d("MatchReplace", String.format("Match: %s", line));
                            continue;
                        }
                        if (REPLACE.equals(line)) {
                            line = readMultiLines(br, this.replaces, false, this.keywords);
                           // Log.d("MatchReplace", String.format("Replace: %s", line));
                            continue;
                        }
                        logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line);
                        break;
                }
            }
            line = br.readLine();
        }
        PathFinder pathFinder2 = this.pathFinder;
        if (pathFinder2 != null) {
            this.isWildMatch = pathFinder2.isWildMatch();
        }
    }

    @Override
    public String executeRule(ProjectHelper activity, ZipFile patchZip, IPatchContext logger) {
        Log.d(TAG, "Execute MatchReplace rule");
        Pattern pattern;
        preProcessing(logger, this.matches);
        preProcessing(logger, this.replaces);
        if (this.bRegex) {
            String regStr = this.matches.get(0);
            try {
                if (this.bDotall) {
                    pattern = Pattern.compile(regStr.trim(), Pattern.DOTALL);
                } else {
                    pattern = Pattern.compile(regStr.trim());
                }
            } catch (PatternSyntaxException e) {
                logger.error(R.string.patch_error_regex_syntax, e.getMessage());
                return null;
            }
        } else {
            pattern = null;
        }
        String nextPath = this.pathFinder.getNextPath();
        while (nextPath != null) {
            Log.d(TAG, "nextPath: "+ nextPath);
       //     Log.d(TAG, pattern.pattern());
            executeOnEntry(activity, logger, nextPath, pattern);
            nextPath = this.pathFinder.getNextPath();
        }
        Log.d(TAG, "next patch null");
        return null;
    }

    private void executeOnEntry(ProjectHelper activity, IPatchContext patchCtx, String targetFile, Pattern pattern) {
        Log.d("MatchReplace", "Start match of target file");
        boolean modified = false;
        String filepath = activity.getProjectPath() + "/" + targetFile;
        if (pattern != null) {
            try {
                String content = readFileContent(filepath);
                List<Section> sections = new ArrayList<>();
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
                if (!sections.isEmpty()) {
                    try {
                        writeReplaces(filepath, content, sections);
                        modified = true;
                        String message = activity.mContext.getString(R.string.patch_info_num_replaced);
                        patchCtx.info(targetFile + ": " + String.format(message, sections.size()), false);
                    } catch (IOException e) {
                        patchCtx.error(R.string.patch_error_write_to, targetFile);
                    }
                } else if (!this.isWildMatch) {
                    patchCtx.error(R.string.patch_error_no_match, targetFile);
                }
            } catch (IOException e2) {
                patchCtx.error(R.string.patch_error_read_from, targetFile);
                e2.printStackTrace();
            }
        }
        else {
            try {
                List<String> lines = super.readFileLines(filepath);
                List<Integer> matchedIndexes = new ArrayList<>();
                int i2 = 0;
                while (i2 < (lines.size() - this.matches.size()) + 1) {
                    if (checkMatch(lines, i2)) {
                        matchedIndexes.add(i2);
                        i2 += this.matches.size() - 1;
                    }
                    i2++;
                }
                if (matchedIndexes.isEmpty()) {
                    patchCtx.error(R.string.patch_error_no_match, targetFile);
                } else {
                    try {
                        writeReplaces(filepath, lines, matchedIndexes);
                        modified = true;
                        patchCtx.info(R.string.patch_info_num_replaced, false, matchedIndexes.size());
                    } catch (IOException e3) {
                        patchCtx.error(R.string.patch_error_write_to, targetFile);
                    }
                }
            } catch (IOException e4) {
                patchCtx.error(R.string.patch_error_read_from, targetFile);
            }
        }
       /* if (!modified) {
            //ApkInfoActivity apkInfoActivity2 = activity;
        } else if ("AndroidManifest.xml".equals(targetFile)) {
            // activity.setManifestModified(z);
        } else {
            //  ApkInfoActivity apkInfoActivity3 = activity;
            // activity.getResListAdapter().fileModified(str, filepath);
        }*/
    }

    private boolean checkMatch(List<String> lines, int idx) {
        int i = 0;
        while (i < this.matches.size() && lines.get(idx + i).trim().equals(this.matches.get(i))) {
            i++;
        }
        return i == this.matches.size();
    }

    private void writeReplaces(String filepath, String content, List<Section> sections) throws IOException {
        String curReplace;
        String replaceStr = getReplaceString();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filepath);
            int startPos = 0;
            for (int i = 0; i < sections.size(); i++) {
                Section sec = sections.get(i);
                fos.write(content.substring(startPos, sec.start).getBytes());
                startPos = sec.end;
                if (sec.groupStrs == null || sec.groupStrs.isEmpty()) {
                    curReplace = replaceStr;
                } else {
                    curReplace = getRealReplace(replaceStr, sec);
                }
                fos.write(curReplace.getBytes());
            }
            fos.write(content.substring(startPos).getBytes());
            closeQuietly(fos);
            Log.d("MatchReplace", "Wring patched files");
        } finally {
            closeQuietly(fos);
        }
    }

    private void writeReplaces(String filepath, List<String> lines, List<Integer> matchedIndexes) throws IOException {
        String replaceStr = getReplaceString();
        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(filepath));
            int startIdx = 0;
            for (int i = 0; i < matchedIndexes.size(); i++) {
                int curIdx = matchedIndexes.get(i);
                writeLines(out, lines, startIdx, curIdx);
                out.write(replaceStr.getBytes());
                out.write("\n".getBytes());
                startIdx = curIdx + this.matches.size();
            }
            writeLines(out, lines, startIdx, lines.size());
            Log.d("MatchReplace", "Wring patched files");
        } finally {
            closeQuietly(out);
        }
    }

    private void writeLines(BufferedOutputStream out, List<String> lines, int startIdx, int endIdx) throws IOException {
        for (int i = startIdx; i < endIdx; i++) {
            out.write(lines.get(i).getBytes());
            out.write("\n".getBytes());
        }
    }

    private String getRealReplace(String replaceStr, Section sec) {
        String result = replaceStr;
        List<String> groups = sec.groupStrs;
        for (int i = 0; i < groups.size(); i++) {
            result = result.replace("${GROUP" + (i + 1) + "}", groups.get(i));
        }
        return result;
    }

    private String getReplaceString() {
        if (this.replacingStr == null) {
            if (this.replaces.isEmpty()) {
                this.replacingStr = "";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(this.replaces.get(0));
                for (int i = 1; i < this.replaces.size(); i++) {
                    sb.append("\n");
                    sb.append(this.replaces.get(i));
                }
                this.replacingStr = sb.toString();
            }
        }
        return this.replacingStr;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        PathFinder pathFinder2 = this.pathFinder;
        if (pathFinder2 == null || !pathFinder2.isValid()) {
            return false;
        }
        if (!this.matches.isEmpty()) {
            return true;
        }
        logger.error(R.string.patch_error_no_match_content);
        return false;
    }

    @Override
    public boolean isSmaliNeeded() {
        return this.pathFinder.isSmaliNeeded();
    }

    private static class Section {
        public int end;
        public int start;
        List<String> groupStrs;

        public Section(int _start, int _end, List<String> _groupStrs) {
            this.start = _start;
            this.end = _end;
            this.groupStrs = _groupStrs;
        }
    }
}
