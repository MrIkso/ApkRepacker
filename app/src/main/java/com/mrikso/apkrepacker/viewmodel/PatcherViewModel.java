package com.mrikso.apkrepacker.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.github.cregrant.smaliscissors.Main;
import com.github.cregrant.smaliscissors.common.outer.DexExecutor;
import com.github.cregrant.smaliscissors.common.outer.PatcherTask;
import com.mrikso.apkrepacker.adapter.PatchItem;
import com.mrikso.apkrepacker.utils.PatcherAppender;
import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.mrikso.apkrepacker.utils.manifestparser.SdkConstants;
import com.mrikso.apkrepacker.utils.manifestparser.xml.AndroidManifestParser;
import com.mrikso.apkrepacker.utils.manifestparser.xml.ManifestData;
import com.mrikso.patchengine.PatchExecutor;
import com.mrikso.patchengine.ProjectHelper;
import com.mrikso.patchengine.interfaces.IPatchContext;
import com.mrikso.patchengine.interfaces.IRulesInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;

public class PatcherViewModel extends AndroidViewModel implements IRulesInfo, IPatchContext, DexExecutor {

    private Map<String, String> mGlobalVariables = new HashMap<>();
    private PatchExecutor mPatchExecutor;
    private ProjectHelper mProjectHelper;
    private MutableLiveData<String> mLog = new MutableLiveData<>();
    private MutableLiveData<Integer> mPatchCount = new MutableLiveData<>(0);
    private MutableLiveData<Integer> mPatchSize = new MutableLiveData<>(0);
    private MutableLiveData<Integer> mPatchCurrentCountRules = new MutableLiveData<>(0);
    private MutableLiveData<Integer> mPatchAllRulesSize = new MutableLiveData<>(0);
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    public PatcherViewModel(@NonNull Application application) {
        super(application);
        mContext = application.getApplicationContext();
        mProjectHelper = new ProjectHelper();
        mProjectHelper.mContext = mContext;
        mProjectHelper.mProject = ProjectUtils.getProjectPath();
        mProjectHelper.mDataPath = mContext.getFilesDir().getPath();
        mProjectHelper.mCache = mContext.getExternalCacheDir();
    }

    public LiveData<String> getLogLiveData() {
        return mLog;
    }

    public LiveData<Integer> getPathSize() {
        return mPatchSize;
    }

    public LiveData<Integer> getPatchCount() {
        return mPatchCount;
    }

    /* получаем сколько правил в патче */
    public LiveData<Integer> getPatchRulesSize() {
        return mPatchAllRulesSize;
    }

    /* получаем сколько правил в патче было применено */
    public LiveData<Integer> getPatchCurrentRules() {
        return mPatchCurrentCountRules;
    }

    private void runPatch(String path) {
        mPatchExecutor = new PatchExecutor(mProjectHelper, path, this, this);
        mPatchExecutor.applyPatch();
    }

    public void start(List<PatchItem> patchItemList) {
        mPatchSize.postValue(patchItemList.size());
        //int count = 0;
        Handler logHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                mLog.setValue(String.valueOf(msg.obj));
            }
        };
        PatcherAppender.setHandler(logHandler);

        ArrayList<PatcherTask> tasks = new ArrayList<>();
        for (PatchItem item : patchItemList) {
            //count++;
            //runPatch(item.mPath);
            PatcherTask task = new PatcherTask(mProjectHelper.mProject);
            task.addPatchPath(item.mPath);
            tasks.add(task);
        }

        Runnable r = () -> {
            try {
                Main.runPatcher(this, null, tasks);
            } catch (Exception e) {
                mLog.setValue("Failed.");
            } finally {
                PatcherAppender.clear();
            }
        };
        Executors.newSingleThreadExecutor().execute(r);
        mPatchCount.postValue(patchItemList.size());
    }

    @Override
    public void runDex(String dexPath, String entrance, String mainClass, String apkPath, String zipPath, String projectPath, String param, String tempDir) {
        try {
            Class<?> loadedClass = new DexClassLoader(dexPath, tempDir, null, com.github.cregrant.smaliscissors.Main.class.getClassLoader()).loadClass(mainClass);
            Method method = loadedClass.getMethod(entrance, String.class, String.class, String.class, String.class);
            method.invoke(loadedClass.getDeclaredConstructor().newInstance(), apkPath, zipPath, projectPath, param);
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | ClassNotFoundException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public List<String> getActivities() {
        try {
            List<String> act = new ArrayList<>();
            ManifestData manifestData = AndroidManifestParser.parse(new File(getDecodeRootPath() +"/"+ SdkConstants.FN_ANDROID_MANIFEST_XML));
            for (ManifestData.Activity activity : manifestData.getActivities()) {
                act.add(activity.getName());
            }
            return act;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public String getApplicationManifest() {
        try {
            ManifestData manifestData = AndroidManifestParser.parse(new File(getDecodeRootPath() + "/"+ SdkConstants.FN_ANDROID_MANIFEST_XML));
            return manifestData.getPackage();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    @Nullable
    @Override
    public String getDecodeRootPath() {
        return ProjectUtils.getProjectPath();
    }

    @Nullable
    @Override
    public List<String> getLauncherActivities() {
        try {
            List<String> act = new ArrayList<>(1);
            ManifestData manifestData = AndroidManifestParser.parse(new File(getDecodeRootPath()+ "/"+ SdkConstants.FN_ANDROID_MANIFEST_XML));
            DLog.d("LauncherActivity : " + manifestData.getLauncherActivity().getName());
            act.add(manifestData.getLauncherActivity().getName());
            return act;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    @Override
    public List<String> getPatchNames() {
        if (mPatchExecutor != null) {
            return mPatchExecutor.getRuleNames();
        }
        return null;
    }

    @Nullable
    @Override
    public List<String> getSmaliFolders() {
        List<String> folders = new ArrayList<>();
        folders.add("smali");
        try {
            ZipFile zipfile = new ZipFile(mProjectHelper.getApkPath());
            Enumeration<? extends ZipEntry> entries = zipfile.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.endsWith(".dex") && !name.contains("/") && !name.equals("classes.dex")) {
                    folders.add("smali_" + name.substring(0, name.length() - 4));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return folders;
    }

    @Nullable
    @Override
    public String getVariableValue(@Nullable String key) {
        return mGlobalVariables.get(key);
    }

    @Override
    public void error(int resourceId, Object... args) {
        String txt = mContext.getString(resourceId);
        if (args != null) {
            txt = String.format(txt, args);
        }
        appendText(txt + "\n", false, true);
    }

    private void appendText(final String txt, final boolean bold, final boolean red) {
        DLog.d("PatcherViewModel",txt);
        mLog.postValue(txt);
    }

    @Override
    public void info(int resourceId, boolean bold, Object... args) {
        StringBuilder sb = new StringBuilder();
        String txt = mContext.getString(resourceId);
        if (args != null) {
            txt = String.format(txt, args);
        }
        if (bold) {
            sb.append("\n");
        } else {
            sb = new StringBuilder();
        }
        sb.append(txt);
        sb.append("\n");
        appendText(sb.toString(), bold, false);

    }

    @Override
    public void info(String format, boolean bold, Object... args) {
        StringBuilder sb = new StringBuilder();
        String txt = format;
        if (args != null) {
            txt = String.format(txt, args);
        }
        if (bold) {
            sb.append("\n");
        } else {
            sb = new StringBuilder();
        }
        sb.append(txt);
        sb.append("\n");
        appendText(sb.toString(), bold, false);
    }

    @Override
    public void patchFinished() {
        //    appendText("done", true, false);
        Log.d("patch", "done!");
    }

    @Override
    public void setVariableValue(@Nullable String key, @Nullable String value) {
        mGlobalVariables.put(key, value);
    }

  /*  @Nullable
    @Override
    public String getString(int resourceId) {
        return mContext.getString(resourceId);
    }*/

    @Override
    public void allRules(int count) {
        mPatchAllRulesSize.postValue(count);
    }

    @Override
    public void currentRules(int count) {
        mPatchCurrentCountRules.postValue(count);
    }
}