package com.mrikso.patchengine.resource;

import android.util.Log;

import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IBeforeAddFile;
import com.mrikso.patchengine.rules.PatchRuleMerge;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class ResourceMerger implements IBeforeAddFile {
    private String rootPath;
    private PatchRuleMerge patchRuleMerge;

    public ResourceMerger(PatchRuleMerge patchRuleMerge, String rootPath) {
        this.patchRuleMerge = patchRuleMerge;
        this.rootPath = rootPath;
    }

    public boolean consumeAddedFile(ProjectHelper activity, ZipFile zfile, ZipEntry entry) throws Exception {
        int pos;
        String name = entry.getName();
        String targetPath = this.rootPath + "/" + name;
        if ("res/values/public.xml".equals(name)) {
            return true;
        }
        if (patchRuleMerge.replacedIds == null || !name.endsWith(".smali") || ((!name.startsWith("smali/") && !name.startsWith("smali_")) || (pos = name.indexOf(47)) == -1)) {
            File f = new File(targetPath);
            if (!f.exists()) {
                if (name.startsWith("res/")) {
                    f.getParentFile().mkdirs();
                }
                return false;
            }
            if (name.endsWith(".xml")) {
                String[] paths = name.split("/");
                if (paths.length == 3 && paths[0].equals("res") && (paths[1].equals("values") || paths[1].startsWith("values-"))) {
                    mergeResourceFiles(targetPath, zfile, entry);
                    return true;
                }
            }
            return false;
        }
        refactorAndSaveSmaliFiles(targetPath, zfile, entry);
        //activity.getResListAdapter().fileModified(name.substring(0, pos + 1) + "a.smali", targetPath);
        return true;
    }

    private void refactorAndSaveSmaliFiles(String targetPath, ZipFile zfile, ZipEntry entry) throws IOException {
        List<String> lines = readZipEntry(zfile, entry);
        BufferedWriter bw = null;
        try {
            File parentFolder = new File(targetPath).getParentFile();
            if (!parentFolder.exists()) {
                parentFolder.mkdirs();
            }
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(targetPath)));
            for (int i = 0; i < lines.size(); i++) {
                bw.write(refactorId(lines.get(i)));
                bw.write(10);
            }
        } finally {
            closeQuietly(bw);
        }
    }

    private String refactorId(String line) {
        boolean idModified = false;
        int pos = line.indexOf("0x7f");
        while (pos != -1 && pos + 10 <= line.length()) {
            String originStr = line.substring(pos, pos + 10);
            int newId = patchRuleMerge.replacedIds.get(ResourceItem.string2Id(originStr));
            if (newId != 0) {
                line = line.replace(originStr, ResourceItem.id2String(newId));
                idModified = true;
            } else {
                Log.e("DEBUG", "Cannot find id " + originStr);
            }
            pos = line.indexOf("0x7f", pos + 10);
        }
        if (!idModified || !line.trim().startsWith("const/high16 v")) {
            return line;
        }
        return line.replace("const/high16 v", "const v");
    }

    private List<String> readZipEntry(ZipFile zfile, ZipEntry entry) throws IOException {
        List<String> lines = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(zfile.getInputStream(entry)));
            while (true) {
                String readLine = br.readLine();
                if (readLine == null) {
                    return lines;
                }
                lines.add(readLine);
            }
        } finally {
            closeQuietly(br);
        }
    }

    private void mergeResourceFiles(String path, ZipFile zfile, ZipEntry entry) throws Exception {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(zfile.getInputStream(entry)));
            List<String> items = new ArrayList<>();
            while (true) {
                String readLine = br.readLine();
                if (readLine != null) {
                    String line2 = readLine.trim();
                    if (!line2.startsWith("<?xml") && !line2.startsWith("<resources>")) {
                        if (!line2.startsWith("</resources>")) {
                            items.add(line2);
                        }
                    }
                } else {
                    patchRuleMerge.appendResourceLines(path, items);
                    return;
                }
            }
        } finally {
            closeQuietly(br);
        }
    }
}