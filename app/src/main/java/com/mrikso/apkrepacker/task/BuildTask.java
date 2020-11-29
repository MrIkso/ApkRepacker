package com.mrikso.apkrepacker.task;

import android.content.Context;

import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;
import com.mrikso.apkrepacker.ui.apkbuilder.Androlib;
import com.mrikso.apkrepacker.ui.apkbuilder.IBuilderCallback;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.SignUtil;
import com.mrikso.apkrepacker.utils.common.DLog;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Objects;
import java.util.logging.Level;

import brut.androlib.ApkOptions;
import brut.androlib.meta.MetaInfo;
import brut.util.Logger;

public class BuildTask extends CoroutinesAsyncTask<File, CharSequence, Boolean> {
    private final SignUtil signTool;
    private Context mContext;
    private PreferenceHelper preferenceHelper;
    private Logger logger;
    private IBuilderCallback mBuilderCallback;

    public BuildTask(Context context, SignUtil signTool, Logger logger, IBuilderCallback builderCallback) {
        this.mContext = context;
        this.signTool = signTool;
        this.logger = logger;
        this.mBuilderCallback = builderCallback;
    }

    @Override
    public Boolean doInBackground(File[] p1) {
        boolean success = true;
        for (File file : p1) {
            if (!process(file))
                success = false;
        }
        return success;
    }

    protected boolean process(File f) {
        preferenceHelper = PreferenceHelper.getInstance(mContext);
        ApkOptions options = ApkOptions.INSTANCE;
        String outApk;
        Androlib androlib = new Androlib(options, logger);
        androlib.setContext(mContext);
        androlib.setCallback(mBuilderCallback);
        try {
            File tmp = File.createTempFile("APKTOOL", null);
            try {
                //start building
                long start = System.currentTimeMillis();
                MetaInfo meta = androlib.build(f, tmp);
                if (preferenceHelper.isSignResultApk()) {
                    outApk = FileUtil.genNameApk(mContext, f.getAbsolutePath(), meta.apkFileName, "_signed", 0);
                } else {
                    outApk = FileUtil.genNameApk(mContext, f.getAbsolutePath(), meta.apkFileName, "_unsigned", 0);
                }
                File buildApkPath = new File(preferenceHelper.getDecodingPath() + "/output");
                if (!buildApkPath.exists() && !buildApkPath.mkdirs()) {
                    return false;
                }
                File out = new File(buildApkPath, outApk);
                int min;
                if (meta.sdkInfo != null)
                    min = Integer.parseInt(Objects.requireNonNull(meta.sdkInfo.get("minSdkVersion")));
                else
                    min = 14;
                if (preferenceHelper.isSignResultApk()) {
                    /*TaskStepInfo taskStepInfo = new TaskStepInfo();
                    taskStepInfo.stepTotal++;
                    mBuilderCallback.setTaskStepInfo(new TaskStepInfo().stepIndex++);*/
                    signTool.sign(tmp, out, min, logger);
                    setResult(out);
                } else {
                    try {
                        if (out.exists()) {
                            FileUtil.deleteFile(out);
                        }
                        FileUtils.copyFile(tmp, out);
                        setResult(out);
                    } catch (Exception e) {
                        e.printStackTrace();
                        cancel(true);
                    }
                }
                long buildTime = System.currentTimeMillis() - start;
                mBuilderCallback.taskTime(buildTime);
                DLog.i("Build app time="+ buildTime);
            } finally {
                FileUtils.deleteDirectory(new File(f.getAbsolutePath() + "/build"));
                tmp.delete();
            }
            return true;
        } catch (Exception e) {
           // mBuilderCallback.taskFailed(e.getMessage());
            logger.log(Level.WARNING, e.getMessage(), e);
            //e.printStackTrace();
            cancel(true);
            return false;
        }
    }

    public void setResult(File f) {
        mBuilderCallback.taskSucceed(f);
    }

    public void setResult(String f) {
        try {
            File resultFile = new File(f);
            mBuilderCallback.taskSucceed(resultFile);
        } catch (Exception e) {
           // mBuilderCallback.taskFailed("Result file failed" +e.getMessage());
            logger.log(Level.WARNING, "Result file failed", e);
            cancel(true);
        }
    }
}
