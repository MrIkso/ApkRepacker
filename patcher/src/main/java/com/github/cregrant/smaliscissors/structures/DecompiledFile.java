package com.github.cregrant.smaliscissors.structures;

import com.github.cregrant.smaliscissors.IO;
import com.github.cregrant.smaliscissors.Prefs;

import java.io.File;

public class DecompiledFile {
    private String path;
    private String body;
    private boolean isModified = false;
    private final boolean isXML;
    private boolean isBigSize = false;

    public DecompiledFile(boolean isXmlFile, String filePath) {
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
        return this.isModified;
    }

    public String getBody() {
        if (isXML ? Prefs.keepXmlFilesInRAM : Prefs.keepSmaliFilesInRAM)
            return this.body;
        else
            return IO.read(Prefs.projectPath + File.separator + path);
    }

    public void setBody(String newBody) {
        if (isXML ? Prefs.keepXmlFilesInRAM : Prefs.keepSmaliFilesInRAM)
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

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof DecompiledFile) {
            return this.path.equals(((DecompiledFile) obj).path) && this.isModified == ((DecompiledFile) obj).isModified && this.body.equals(((DecompiledFile) obj).getBody());
        }
        return false;
    }
}