package com.github.cregrant.smaliscissors.engine;

import java.io.File;

public class DecompiledFile {
    private String path;
    private String body;
    private boolean isModified = false;
    private final boolean isXML;
    private boolean isBigSize = false;

    DecompiledFile(boolean isXmlFile, String filePath) {
        isXML = isXmlFile;
        path = filePath;
    }

    public boolean isXML() {
        return this.isXML;
    }

    public String getPath() {
        return this.path;
    }

    public void setModified(boolean state) {
        this.isModified = state;
    }

    public boolean isModified() {
        return !this.isModified;
    }

    public void setPath(String newPath) {
        this.path = newPath;
    }

    public String getBody() {
        if ((!isXML && Prefs.keepSmaliFilesInRAM) || (isXML && Prefs.keepXmlFilesInRAM))
            return this.body;
        else
            return IO.read(Prefs.projectPath + File.separator + path);
    }

    public void setBody(String newBody) {
        if ((!isXML && Prefs.keepSmaliFilesInRAM) || (isXML && Prefs.keepXmlFilesInRAM))
            this.body = newBody;
        else
            IO.write(Prefs.projectPath + File.separator + path, newBody);
    }

    public void setBigSize(boolean state) {
        this.isBigSize = state;
    }

    public boolean isBigSize() {
        return !this.isBigSize;
    }

    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof DecompiledFile) {
            return this.path.equals(((DecompiledFile)anObject).getPath()) && this.isModified == ((DecompiledFile)anObject).isModified && this.body.equals(((DecompiledFile)anObject).getBody());
        }
        return false;
    }
}