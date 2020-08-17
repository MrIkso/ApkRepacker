package com.mrikso.patchengine.patchfilter;

import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class PathFilterWildcard extends PathFilter {

    private IPatchContext ctx;
    private String decodedRootPath;
    private int fileCursor = 0;
    private List<String> fileList = new ArrayList();
    private List<String> folderList = new LinkedList();
    private boolean initialized = false;
    private String regexPath;
    private String wildPathStr;

    public PathFilterWildcard(IPatchContext context, String pathStr) {
        this.ctx = context;
        this.wildPathStr = pathStr;
        this.regexPath = "^" + pathStr.replace("*", ".*") + "$";
        this.decodedRootPath = context.getDecodeRootPath();
    }

    private void init() {
        File[] subFiles = new File(this.decodedRootPath).listFiles();
        if (subFiles != null) {
            for (File f : subFiles) {
                if (f.isDirectory()) {
                    this.folderList.add(f.getName());
                } else {
                    String relativePath = f.getName();
                    if (isTarget(relativePath)) {
                        this.fileList.add(relativePath);
                    }
                }
            }
        }
        this.initialized = true;
    }

    @Override
    public String getNextEntry() {
        if (!this.initialized) {
            init();
        }
        if (this.fileCursor < this.fileList.size()) {
            String path = this.fileList.get(this.fileCursor);
            this.fileCursor++;
            return path;
        } else if (this.folderList.isEmpty()) {
            return null;
        } else {
            this.fileCursor = 0;
            this.fileList.clear();
            while (!this.folderList.isEmpty()) {
                String path2 = this.folderList.remove(0);
                File[] files = new File(this.decodedRootPath + "/" + path2).listFiles();
                if (files != null) {
                    for (File f : files) {
                        String relativePath = path2 + "/" + f.getName();
                        if (f.isDirectory()) {
                            this.folderList.add(relativePath);
                        } else if (isTarget(relativePath)) {
                            this.fileList.add(relativePath);
                        }
                    }
                }
                if (!this.fileList.isEmpty()) {
                    break;
                }
            }
            if (this.fileList.isEmpty()) {
                return null;
            }
            this.fileCursor = 1;
            return this.fileList.get(0);
        }
    }

    @Override
    public boolean isTarget(String entryPath) {
        return entryPath.matches(this.regexPath);
    }

    @Override
    public boolean isSmaliNeeded() {
        return this.wildPathStr.startsWith("smali") || this.wildPathStr.endsWith(".smali");
    }

    @Override
    public boolean isWildMatch() {
        return true;
    }
}
