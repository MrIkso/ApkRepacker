package com.github.cregrant.smaliscissors.engine;

import java.io.File;

class DecompiledFile {
    private String path;
    private String body;
    private boolean isModified = false;
    private final boolean isXML;
    private boolean isBigSize = false;

    DecompiledFile(boolean isXmlFile, String filePath) {
        isXML = isXmlFile;
        path = filePath;
    }

    boolean isXML() {
        return this.isXML;
    }

    String getPath() {
        return this.path;
    }

    void setModified(boolean state) {
        this.isModified = state;
    }

    boolean isModified() {
        return this.isModified;
    }

    String getBody() {
        if (isXML ? Prefs.keepXmlFilesInRAM : Prefs.keepSmaliFilesInRAM)
            return this.body;
        else
            return IO.read(Prefs.projectPath + File.separator + path);
    }

    void setBody(String newBody) {
        if (isXML ? Prefs.keepXmlFilesInRAM : Prefs.keepSmaliFilesInRAM)
            this.body = newBody;
        else
            IO.write(Prefs.projectPath + File.separator + path, newBody);
    }

    void setBigSize(boolean state) {
        this.isBigSize = state;
    }

    boolean isBigSize() {
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