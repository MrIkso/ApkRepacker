package com.mrikso.patchengine.rules;

import android.annotation.SuppressLint;
import android.util.Log;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.mrikso.patchengine.LinedReader;
import com.mrikso.patchengine.PatchRule;
import com.mrikso.patchengine.PathFinder;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.Section;
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
    private List<String> matches = new ArrayList<>();
    private PathFinder pathFinder;
    private List<String> replaces = new ArrayList<>();
    private String replacingStr = null;

    public PatchRuleMatchReplace() {
        keywords.add(TARGET);
        keywords.add(MATCH);
        keywords.add(REGEX);
        keywords.add(REPLACE);
        keywords.add(DOTALL);
        keywords.add(strEnd);
    }

    @Override
    public void parseFrom(LinedReader br, IPatchContext logger) throws IOException {
        startLine = br.getCurrentLine();
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
                        pathFinder = new PathFinder(logger, br.readLine().trim(), br.getCurrentLine());
                        break;
                    case REGEX:
                        bRegex = Boolean.parseBoolean(br.readLine().trim());
                        break;
                    case DOTALL:
                        bDotall = Boolean.parseBoolean(br.readLine().trim());
                        break;
                    default:
                        if (MATCH.equals(line)) {
                            line = readMultiLines(br, matches, true, keywords);
                            // Log.d("MatchReplace", String.format("Match: %s", line));
                            continue;
                        }
                        if (REPLACE.equals(line)) {
                            line = readMultiLines(br, replaces, false, keywords);
                            // Log.d("MatchReplace", String.format("Replace: %s", line));
                            continue;
                        }
                        logger.error(R.string.patch_error_cannot_parse, br.getCurrentLine(), line);
                        break;
                }
            }
            line = br.readLine();
        }
        //  PathFinder pathFinder2 = pathFinder;
        if (pathFinder != null) {
            isWildMatch = pathFinder.isWildMatch();
        }
    }

    @SuppressLint("WrongConstant")
    @Override
    public String executeRule(ProjectHelper projectHelper, ZipFile patchZip, IPatchContext logger) {
        Log.d(TAG, "Execute MatchReplace rule");
        Pattern pattern;
        preProcessing(logger, matches);
        preProcessing(logger, replaces);
        if (bRegex) {
            String regStr = matches.get(0);
            DLog.d(TAG, regStr);
            try {
                if (bDotall) {
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

        //происходит перебор файлов для патчинга
        // String nextPath = pathFinder.getNextPath();
        for (String nextPath = pathFinder.getNextPath(); nextPath != null; nextPath = pathFinder.getNextPath()) {
            Log.d(TAG, "nextPath: " + nextPath);
            //     Log.d(TAG, pattern.pattern());
            executeOnEntry(projectHelper,
                    logger,
                    nextPath,
                    pattern);
            // nextPath = pathFinder.getNextPath();
        }
        //Log.d(TAG, "next patch null");
        return null;
    }

    /**
     * матчим файл, если совпадения находятся патчим его
     *
     * @param projectHelper
     * @param patchCtx      контекст патча
     * @param targetFile    файл для парсинга
     * @param pattern       паттерн
     */
    private void executeOnEntry(ProjectHelper projectHelper, IPatchContext patchCtx, String targetFile, Pattern pattern) {
        boolean modified = false;
        String filepath = projectHelper.getProjectPath() + "/" + targetFile;
        Log.d(TAG, "Start match of target file: " + filepath);
        if (pattern != null) {
            try {
                String content = readFileContent(filepath);
                List<Section> sections = new ArrayList<>();
                Matcher m = pattern.matcher(content);
                while (m.find()) {
                    List<String> groupStrs = null;
                    int groupCount = m.groupCount();
                    if (groupCount > 0) {
                        groupStrs = new ArrayList<>(groupCount);
                        for (int i = 0; i < groupCount; i++) {
                            groupStrs.add(m.group(i + 1));
                        }
                    }
                    String found = content.substring(m.start(),m.end() );
                    DLog.d(TAG, "Found ["+ found + "]");
                    sections.add(new Section(m.start(), m.end(), groupStrs));
                }
                if (!sections.isEmpty()) {
                    try {
                        writeReplaces(filepath, content, sections);
                        modified = true;
                        String message = projectHelper.mContext.getString(R.string.patch_info_num_replaced);
                        patchCtx.info(targetFile + ": " + String.format(message, sections.size()), false);
                    } catch (IOException e) {
                        patchCtx.error(R.string.patch_error_write_to, targetFile);
                    }
                } else if (!isWildMatch) {
                    patchCtx.error(R.string.patch_error_no_match, targetFile);
                }
            } catch (IOException e) {
                patchCtx.error(R.string.patch_error_read_from, targetFile);
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Pattern is null");
            try {
                List<String> lines = readFileLines(filepath);
                List<Integer> matchedIndexes = new ArrayList<>();
                //  int i2 = 0;
                for (int i = 0; i < (lines.size() - matches.size()) + 1; i++) {
                    if (checkMatch(lines, i)) {
                        matchedIndexes.add(i);
                        i += matches.size() - 1;
                    }
                    //  i2++;
                }
                if (matchedIndexes.isEmpty()) {
                    patchCtx.error(R.string.patch_error_no_match, targetFile);
                } else {
                    try {
                        writeReplaces(filepath, lines, matchedIndexes);
                        modified = true;
                        patchCtx.info(R.string.patch_info_num_replaced, false, matchedIndexes.size());
                    } catch (IOException e) {
                        patchCtx.error(R.string.patch_error_write_to, targetFile);
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                patchCtx.error(R.string.patch_error_read_from, targetFile);
                e.printStackTrace();
            }
        }
    }

    //проверяем ли матчинг валиден
    private boolean checkMatch(List<String> lines, int idx) {
        int i = 0;
        while (i < matches.size() && lines.get(idx + i).trim().equals(matches.get(i))) {
            i++;
        }
        return i == matches.size();
    }

    //region Writing patched file
    private void writeReplaces(String filepath, String content, List<Section> sections) throws IOException {
        String curReplace;
        String replaceStr = getReplaceString();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(filepath);
            int startPos = 0;
            for (Section sec : sections) {
                // for (int i = 0; i < sections.size(); i++) {
                //Section sec = sections.get(i);
                fos.write(content.substring(startPos, sec.start).getBytes());
                startPos = sec.end;
                if (sec.getGroupStrs() == null || sec.getGroupStrs().isEmpty()) {
                    curReplace = replaceStr;
                } else {
                    curReplace = getRealReplace(replaceStr, sec);
                }
                fos.write(curReplace.getBytes());
            }
            fos.write(content.substring(startPos).getBytes());
            closeQuietly(fos);
            Log.d(TAG, "Writing patched files");
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
                startIdx = curIdx + matches.size();
            }
            writeLines(out, lines, startIdx, lines.size());
            Log.d(TAG, "Wring patched files");
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
    //endregion

    private String getRealReplace(String replaceStr, Section sec) {
        String result = replaceStr;
        List<String> groups = sec.getGroupStrs();
        for (int i = 0; i < groups.size(); i++) {
            result = result.replace("${GROUP" + (i + 1) + "}", groups.get(i));
        }
        return result;
    }

    private String getReplaceString() {
        if (replacingStr == null) {
            if (replaces.isEmpty()) {
                replacingStr = "";
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(replaces.get(0));
                for (int i = 1; i < replaces.size(); i++) {
                    sb.append("\n");
                    sb.append(replaces.get(i));
                }
                replacingStr = sb.toString();
            }
        }
        return replacingStr;
    }

    @Override
    public boolean isValid(IPatchContext logger) {
        if (pathFinder == null || !pathFinder.isValid()) {
            return false;
        }
        if (!matches.isEmpty()) {
            return true;
        }
        logger.error(R.string.patch_error_no_match_content);
        return false;
    }

    @Override
    public boolean isSmaliNeeded() {
        return pathFinder.isSmaliNeeded();
    }

}
