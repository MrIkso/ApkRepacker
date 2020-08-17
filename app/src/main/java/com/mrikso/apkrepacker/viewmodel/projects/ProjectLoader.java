package com.mrikso.apkrepacker.viewmodel.projects;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.ui.projectlist.ProjectItem;
import com.mrikso.apkrepacker.utils.common.DLog;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ProjectLoader extends LiveData<List<ProjectItem>> {

    private static ProjectLoader sInstance;
    private final String TAG = "ProjectLoader";
    private Context mContext;
    private Executor mExecutor = Executors.newFixedThreadPool(3);
    private PreferenceHelper mPreferenceHelper;

    public ProjectLoader(Context context) {
        sInstance = this;

        mContext = context.getApplicationContext();
        mPreferenceHelper = PreferenceHelper.getInstance(mContext);

        loadProjects();
    }

    public static ProjectLoader getInstance(Context context) {
        synchronized (ProjectLoader.class) {
            return sInstance != null ? sInstance : new ProjectLoader(context);
        }
    }

    public void loadProjects() {
        //   mExecutor.execute(() -> {
        new Thread(() -> {
            List<ProjectItem> projectItems = new ArrayList<>();
            long start = System.currentTimeMillis();

            try {
                File root = new File(mPreferenceHelper.getDecodingPath() + "/projects");
                new File(mPreferenceHelper.getDecodingPath() + "/.nomedia").createNewFile();
                if (!root.exists() && !root.mkdirs()) {
                    return;
                }
                File[] projects = root.listFiles();
                if (projects != null && projects.length > 0) {
                    for (File file : projects) {
                        File dataFile = new File(file, "apktool.json");
                        if (dataFile.exists()) {
                            FileInputStream input = new FileInputStream(dataFile);
                            String json = IOUtils.toString(input, StandardCharsets.UTF_8);
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            ProjectItemJson project = gson.fromJson(json, ProjectItemJson.class);
                            input.close();
                            String apkFileIcon = project.apkFileIcon;
                            String apkFileName = project.apkFileName;
                            ProjectItem myList = new ProjectItem(apkFileIcon, (apkFileName != null) ? apkFileName : file.getName(),
                                    project.apkFilePackageName,
                                    file.getAbsolutePath(), project.apkFilePatch, (project.versionInfo != null) ? project.versionInfo.getVersionName() : null,
                                    (project.versionInfo != null) ? project.versionInfo.getVersionCode() : null);
                            projectItems.add(myList);
                        } else {
                            DLog.i(TAG, String.format(Locale.ENGLISH, "Is not project in %s", file.getAbsolutePath()));
                        }
                    }
                }
            } catch (IOException io) {
                DLog.e(TAG, io);
            }
            DLog.d(TAG, String.format(Locale.ENGLISH, "Loaded projects in %d ms", (System.currentTimeMillis() - start)));
            postValue(projectItems);
        }).run();
        // });
    }

}
