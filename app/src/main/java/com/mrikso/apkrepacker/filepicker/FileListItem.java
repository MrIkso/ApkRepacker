
package com.mrikso.apkrepacker.filepicker;

import java.util.Locale;

/**
 * The model/container class holding file list data.
 */
public class FileListItem implements Comparable<FileListItem> {
    private String name, path;
    private long time;
    private boolean isDirectory, isMarked;

    public FileListItem(String name, String path, long time, boolean isDirectory) {
        this.name = name;
        this.path = path;
        this.time = time;
        this.isDirectory = isDirectory;
    }

    public FileListItem(String name, String path, long time, boolean isDirectory, boolean isMarked) {
        this.name = name;
        this.path = path;
        this.time = time;
        this.isDirectory = isDirectory;
        this.isMarked = isMarked;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        this.isDirectory = directory;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isMarked() {
        return isMarked;
    }

    public void setMarked(boolean marked) {
        this.isMarked = marked;
    }

    @Override
    public int compareTo(FileListItem fileListItem) {
        if (fileListItem.isDirectory() && isDirectory()) {
            // If the comparison is between two directories, return the isDirectory with alphabetic order first.
            return name.toLowerCase().compareTo(fileListItem.getName().toLowerCase(Locale.getDefault()));
        } else if (!fileListItem.isDirectory() && !isDirectory()) {
            // If the comparison is not between two directories, return the file with alphabetic order first.
            return name.toLowerCase().compareTo(fileListItem.getName().toLowerCase(Locale.getDefault()));
        } else if (fileListItem.isDirectory() && !isDirectory()) {
            // If the comparison is between a isDirectory and a file, return the isDirectory.
            return 1;
        } else {
            // Same as above but order of occurrence is different.
            return -1;
        }
    }

    @Override
    public String toString() {
        return "FileListItem{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", time=" + time +
                ", isDirectory=" + isDirectory +
                ", isMarked=" + isMarked +
                '}';
    }
}