package com.mrikso.patchengine.patchfilter;

import com.mrikso.patchengine.interfaces.IPatchContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PathFilterComponent extends PathFilter {

    private String applicationName;
    private ComponentType compType;
    private List<String> componentList = new ArrayList<>();
    private int cursor = 0;
    private String decodeRootPath;

    public PathFilterComponent(IPatchContext ctx, ComponentType componentType) {
        this.compType = componentType;
        this.decodeRootPath = ctx.getDecodeRootPath();
        int i = ComponetClass.component[componentType.ordinal()];
        switch (i) {
            case 1:
                this.applicationName = ctx.getApplicationManifest();
                break;
            case 2:
                this.componentList = ctx.getActivities();
                break;
            case 3:
                this.componentList = ctx.getLauncherActivities();
                break;
        }
    }

    @Override
    public String getNextEntry() {
        int i = ComponetClass.component[this.compType.ordinal()];
        if (i == 1) {
            int i2 = this.cursor;
            if (i2 != 0) {
                return null;
            }
            this.cursor = i2 + 1;
            return getSmaliPath(this.applicationName);
        } else if ((i != 2 && i != 3) || this.cursor >= this.componentList.size()) {
            return null;
        } else {
            List<String> list = this.componentList;
            int i3 = this.cursor;
            this.cursor = i3 + 1;
            return getSmaliPath(list.get(i3));
        }
    }

    private String getSmaliPath(String clsName) {
        String path = getRelativePath("smali", clsName, true);
        int index = 2;
        while (path == null && index < 8) {
            path = getRelativePath("smali_classes" + index, clsName, true);
            index++;
        }
        if (path == null) {
            return getRelativePath("smali", clsName, false);
        }
        return path;
    }

    private String getRelativePath(String smaliFolderName, String clsName, boolean notExistRetNull) {
        String relativePath = smaliFolderName + "/" + clsName.replaceAll("\\.", "/") + ".smali";
        String absolutionPath = this.decodeRootPath + "/" + relativePath;
        if (!notExistRetNull) {
            return relativePath;
        }
        if (new File(absolutionPath).exists()) {
            return relativePath;
        }
        return null;
    }

    @Override
    public boolean isTarget(String entryPath) {
        int pos = entryPath.indexOf(47);
        if (pos == -1 || !entryPath.endsWith(".smali")) {
            return false;
        }
        String clsName = entryPath.substring(pos + 1, entryPath.length() - 6).replaceAll("/", ".");
        int i = ComponetClass.component[this.compType.ordinal()];
        switch (i) {
            case 1:
                return clsName.equals(this.applicationName);
            case 2:
            case 3:
                return this.componentList.contains(clsName);
        }
        return false;
    }

    @Override
    public boolean isSmaliNeeded() {
        return true;
    }

    @Override
    public boolean isWildMatch() {
        return false;
    }

    public enum ComponentType {
        APPLICATION,
        ACTIVITY,
        LAUNCHER_ACTIVITY
    }

    public static class ComponetClass {
        static final int[] component;

        static {
            int[] iArr = new int[ComponentType.values().length];
            component = iArr;
            try {
                iArr[ComponentType.APPLICATION.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
                e.printStackTrace();
            }
            try {
                component[ComponentType.ACTIVITY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
                e2.printStackTrace();
            }
            try {
                component[ComponentType.LAUNCHER_ACTIVITY.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
                e3.printStackTrace();
            }
        }
    }
}
