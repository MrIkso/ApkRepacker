package com.mrikso.patchengine;

import android.util.Log;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.patchfilter.PathFilter;
import com.mrikso.patchengine.patchfilter.PathFilterComponent;
import com.mrikso.patchengine.patchfilter.PathFilterExactEntry;
import com.mrikso.patchengine.patchfilter.PathFilterWildcard;

import java.util.ArrayList;
import java.util.List;

public class PathFinder {

    private final String TAG = "PathFinder";

    private List<PathFilter> filters = new ArrayList();

    public PathFinder(IPatchContext ctx, String pathStr, int line) {
        DLog.d(TAG, "Starting PatchFinder");

        String expanded = PatchRule.assignValues(ctx, pathStr);
        pathStr = expanded != null ? expanded : pathStr;
        if (pathStr.startsWith("[") && pathStr.endsWith("]")) {
            for (String word : splitWords(pathStr)) {
                PathFilter filter = createFilter(ctx, word, line);
                if (filter != null) {
                    this.filters.add(filter);
                } else {
                    this.filters = null;
                    return;
                }
            }
        } else if (pathStr.contains("*")) {
            this.filters.add(new PathFilterWildcard(ctx, pathStr));
        } else {
            this.filters.add(new PathFilterExactEntry(ctx, pathStr));
        }
    }

    private PathFilter createFilter(IPatchContext ctx, String word, int lineIdx) {
        switch (word) {
            case "APPLICATION":
                return new PathFilterComponent(ctx, PathFilterComponent.ComponentType.APPLICATION);
            case "ACTIVITIES":
                return new PathFilterComponent(ctx, PathFilterComponent.ComponentType.ACTIVITY);
            case "LAUNCHER_ACTIVITIES":
                return new PathFilterComponent(ctx, PathFilterComponent.ComponentType.LAUNCHER_ACTIVITY);
        }
        ctx.error(R.string.patch_error_invalid_target, lineIdx);
        return null;
    }

    private List<String> splitWords(String pathStr) {
        List<String> result = new ArrayList<>();
        int startPos = 1;
        int endPos = pathStr.indexOf(93);
        while (startPos > 0 && endPos > startPos) {
            result.add(pathStr.substring(startPos, endPos));
            startPos = pathStr.indexOf(91, endPos) + 1;
            if (startPos > 0) {
                endPos = pathStr.indexOf(93, startPos);
            }
        }
        return result;
    }

    public boolean isSmaliNeeded() {
        if (this.filters == null) {
            return false;
        }
        for (int i = 0; i < this.filters.size(); i++) {
            if (this.filters.get(i).isSmaliNeeded()) {
                return true;
            }
        }
        return false;
    }

    public String getNextPath() {
        DLog.d(TAG, "Starting getNextPatch");
        List<PathFilter> list = this.filters;
        if (list == null) {
            return null;
        }
        String nextEntry = list.get(0).getNextEntry();
        if (this.filters.size() > 1) {
            while (nextEntry != null) {
                boolean matches = true;
                int i = 1;
                while (true) {
                    if (i >= this.filters.size()) {
                        break;
                    } else if (!this.filters.get(i).isTarget(nextEntry)) {
                        matches = false;
                        break;
                    } else {
                        i++;
                    }
                }
                if (matches) {
                    break;
                }
                nextEntry = this.filters.get(0).getNextEntry();
            }
        }
        return nextEntry;
    }

    public boolean isValid() {
        return this.filters != null;
    }

    public boolean isWildMatch() {
        List<PathFilter> list = this.filters;
        if (list == null) {
            return false;
        }
        for (PathFilter filter : list) {
            if (!filter.isWildMatch()) {
                return false;
            }
        }
        return true;
    }
}
