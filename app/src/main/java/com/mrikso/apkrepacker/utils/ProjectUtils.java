package com.mrikso.apkrepacker.utils;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.mrikso.apkrepacker.R;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import brut.androlib.meta.VersionInfo;

public class ProjectUtils {
    private static String projectPath;
    private static String currentPath;

    public static String getProjectPath() {
        return projectPath;
    }

    public static void setProjectPath(String path) {
        projectPath = path;
    }

    public static void setCurrentPath(String path) {
        currentPath = path;
    }

    public static String readJson(File file, String stringName) {
        String content;
        try {
            content = IOUtils.toString(new FileInputStream(file), StandardCharsets.UTF_8);
            JSONObject json = new JSONObject(content);
            VersionInfo versionInfo = VersionInfo.load(json);
            if (stringName.equals("versionName")) {
                return versionInfo.versionName;
            } else if (stringName.equals("versionCode")) {
                return versionInfo.versionCode;
            } else {
                return json.isNull(stringName) ? null : json.getString(stringName);
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Drawable getProjectIconDrawable(String appIconBase64, Context context) {
        Drawable icon = (FileUtil.decodeBase64(appIconBase64) != null) ? new BitmapDrawable(context.getResources(), FileUtil.decodeBase64(appIconBase64)) : null;
        return appIconBase64 != null ? icon : ContextCompat.getDrawable(context, R.drawable.default_app_icon);
    }

    public static String getCurrentPath() {
        return currentPath;
    }

    public static String getProjectName() {
        return new File(getProjectPath()).getName();
    }
}
