package com.mrikso.patchengine;

import android.util.Log;

import com.mrikso.apkrepacker.R;
import com.mrikso.patchengine.interfaces.IBeforeAddFile;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.utils.IOUtil;


import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;

public abstract class PatchRule {

    private static final String TAG = "PatchRule";

    private static final String NAME = "NAME:";
    protected String ruleName;
    protected int startLine;

    public static String assignValues(IPatchContext ctx, String rawStr) {
        Log.d(TAG, "start assign values");
        ArrayList<ReplaceRec> replaces = new ArrayList<>();
        int position = rawStr.indexOf("${");
        while (position != -1) {
            position += 2;
            int endPos = rawStr.indexOf("}", position);
            if (endPos != -1) {
                String realVal = ctx.getVariableValue(rawStr.substring(position, endPos));
                if (realVal != null) {
                    Log.d(TAG, "adding replaces " + realVal);
                    replaces.add(new ReplaceRec(position - 2, endPos + 1, realVal));
                }
                position = rawStr.indexOf("${", endPos);
            }
        }
        if (!replaces.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            int startPos = 0;
            for (ReplaceRec rec : replaces) {
                int curPos = rec.getStartPos();
                if (curPos > startPos) {
                    sb.append(rawStr.substring(startPos, curPos));
                }
                sb.append(rec.getReplacing());
                startPos = rec.getEndPos();
            }
            if (startPos < rawStr.length()) {
                Log.i(TAG, rawStr.substring(startPos));
                sb.append(rawStr.substring(startPos));
            }
            Log.d(TAG, "end assign values");
            return sb.toString();
        }
        return null;
    }

    public abstract String executeRule(ProjectHelper projectHelper, ZipFile zipFile, IPatchContext iPatchContext);

    public abstract boolean isSmaliNeeded();

    public abstract boolean isValid(IPatchContext iPatchContext);

    public abstract void parseFrom(LinedReader linedReader, IPatchContext iPatchContext) throws IOException;

    public String getRuleName() {
        return this.ruleName;
    }

    public String readFileContent(String filepath) throws IOException {
        return IOUtils.toString(new FileInputStream(new File(filepath)), StandardCharsets.UTF_8);
        /*File f = new File(filepath);
        StringBuilder sb = new StringBuilder((int) f.length() + 32);
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        try {
            String line = br.readLine();
            if (line != null) {
                sb.append(line);
            }
            while (true) {
                String readLine = br.readLine();
                if (readLine == null) {
                    return sb.toString();
                }
                sb.append("\n");
                sb.append(readLine);
            }
        } finally {
            closeQuietly(br);
        }*/
    }

    public List<String> readFileLines(String filepath) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filepath)));
        while (true) {
            try {
                String readLine = br.readLine();
                if (readLine == null) {
                    return lines;
                }
                if (!"".equals(readLine.trim())) {
                    lines.add(readLine);
                }
            } finally {
                closeQuietly(br);
            }
        }
    }

    protected String readMultiLines(BufferedReader br, List<String> lines, boolean bTrim, List<String> endKeywords) throws IOException {
        String string;
        String line = br.readLine();
        while (true) {
            string = line;
            if (line != null) {
                string = line;
                if (bTrim) {
                    string = line.trim();
                }
                if (endKeywords.contains(string)) {
                    break;
                }
                lines.add(string);
                line = br.readLine();
                continue;
            }
            break;
        }
        return string;
    }

    protected void preProcessing(IPatchContext ctx, List<String> values) {
        for (int i = 0; i < values.size(); i++) {
            String assignedVal = assignValues(ctx, values.get(i));
            if (assignedVal != null) {
                Log.d("PatchRule", assignedVal);
                values.set(i, assignedVal);
            }
            Log.d("PatchRule", "assignedVal null");
        }
    }

    private String getParentFolder(String path) {
        int pos = path.lastIndexOf("/");
        if (pos > 0) {
            return path.substring(0, pos);
        }
        return null;
    }

    private void addFileEntry(ProjectHelper projectHelper, ZipFile zfile, ZipEntry entry, String targetDir, IPatchContext logger) {
        String path = targetDir + "/" + entry.getName();
        String parent = getParentFolder(path);
        assert parent != null;
        while (!new File(parent).exists()) {
            parent = getParentFolder(parent);
        }
        String[] paths = path.substring(parent.length() + 1).split("/");
        if (paths.length > 1) {
            int i = 0;
            while (i < paths.length - 1) {
                try {
                    IOUtil.makeDir(parent, paths[i]);
                    parent = parent + "/" + paths[i];
                    i++;
                } catch (Exception e) {
                    logger.error(R.string.failed_create_dir, e.getMessage());
                    return;
                }
            }
        }
        InputStream input = null;
        try {
            input = zfile.getInputStream(entry);
            FileOutputStream out = new FileOutputStream(path);
            IOUtils.copy(input, out);
        } catch (Exception e2) {
            logger.error(R.string.general_error, e2.getMessage());
        } finally {
            closeQuietly(input);
        }
    }

    public void addFilesInZip(ProjectHelper projectHelper, String zipFile, IBeforeAddFile hook, IPatchContext logger) throws Exception {
        boolean consumed;
        String targetDir = projectHelper.getProjectPath();
        ZipFile zfile2 = new ZipFile(zipFile);
        Enumeration<? extends ZipEntry> entries = zfile2.entries();
        while (entries.hasMoreElements()) {
            ZipEntry ze = entries.nextElement();
            if (!ze.isDirectory()) {
                if (hook != null) {
                    consumed = hook.consumeAddedFile(projectHelper, zfile2, ze);
                } else {
                    consumed = false;
                }
                if (!consumed) {
                    addFileEntry(projectHelper, zfile2, ze, targetDir, logger);
                }
            }
        }
        zfile2.close();
    }

    public boolean parseAsKeyword(String line, LinedReader br) throws IOException {
        if (!NAME.equals(line)) {
            return false;
        }
        String readLine = br.readLine();
        this.ruleName = readLine;
        if (readLine == null) {
            return true;
        }
        this.ruleName = readLine.trim();
        return true;
    }

    public boolean isInSmaliFolder(String targetFile) {
        int pos;
        if (!(targetFile == null || (pos = targetFile.lastIndexOf(47)) == -1)) {
            String firstDir = targetFile.substring(0, pos);
            return "smali".equals(firstDir) || firstDir.startsWith("smali_");
        }
        return false;
    }

}
