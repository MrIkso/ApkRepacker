package com.mrikso.apkrepacker.task;

import android.content.Context;
import android.os.AsyncTask;

import com.jecelyin.common.utils.DLog;
import com.mrikso.apkrepacker.fragment.CompileFragment;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.SignUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;

import brut.androlib.Androlib;
import brut.androlib.ApkOptions;
import brut.androlib.meta.MetaInfo;
import brut.util.Logger;

public class BuildTask extends AsyncTask<File, CharSequence, Boolean> implements Logger
{
    private final SignUtil signTool;
    private Context mContext;
    public File resultFile;
    private PreferenceHelper preferenceHelper;
    private CompileFragment compileFragment;

    public BuildTask(Context mContext, SignUtil signTool, CompileFragment compileFragment)
    {
        this.mContext = mContext;
        this.signTool = signTool;
        this.compileFragment = compileFragment;
    }

    @Override
    protected Boolean doInBackground(File[] p1)
    {
        boolean success = true;
        for (File file : p1)
        {
            if (!process(file))
                success = false;
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean result)
    {
        final ArrayList<String> arrText = compileFragment.getTextArray();
        compileFragment.append(arrText);
        compileFragment.builded(resultFile);
    }

    @Override
    protected void onProgressUpdate(CharSequence... values)
    {
        compileFragment.append(values[0]);
    }

    @Override
    public void info(int id, Object... args)
    {
        publishProgress(String.format("I: %s", getText(id, args)));
    }

    @Override
    public void warning(int id, Object... args)
    {
        publishProgress(String.format("W: %s", getText(id, args)));
       // DLog.w(getText(id, args));
    }

    @Override
    public void fine(int id, Object... args)
    {
        // publishProgress(String.format("F:%s\n",getText(id, args)));
    }

    @Override
    public void text(int id, Object... args)
    {
        publishProgress(getText(id, args));
    }

    @Override
    public void error(int id, Object... args)
    {
        publishProgress(String.format("E: %s", getText(id, args)));
        DLog.e(getText(id, args));
    }

    @Override
    public void log(Level level, String format, Throwable ex)
    {
        char ch = level.getName().charAt(0);
        String fmt = "%c: %s";
        publishProgress(String.format(fmt, ch, format));
        log(fmt, ch, ex);
    }

    private void log(String fmt, char ch, Throwable ex)
    {
        if (ex == null) return;
        publishProgress(String.format(fmt, ch, ex.getMessage()));
        for (StackTraceElement ste:ex.getStackTrace())
            publishProgress(String.format(fmt, ch, ste));
        log(fmt, ch, ex.getCause());
    }

    private String getText(int id, Object[] args)
    {
        return mContext.getResources().getString(id, args);
    }

    protected boolean process(File f) {
        preferenceHelper = PreferenceHelper.getInstance(mContext);
        ApkOptions options = ApkOptions.INSTANCE;
        String outApk;
        Androlib androlib = new Androlib(options, this);
        try {
            File tmp = File.createTempFile("APKTOOL", null);
            try {
                MetaInfo meta = androlib.build(f, tmp);
                if (preferenceHelper.isSignResultApk()) {
                    outApk = FileUtil.genNameApk(mContext, f.getAbsolutePath(), meta.apkFileName, "_signed", 0);
                }
                else {
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
                    signTool.sign(tmp, out, min, this);
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
                    }
                }
            } finally {
                FileUtils.deleteDirectory(new File(f.getAbsolutePath() + "/build"));
                tmp.delete();
            }
            return true;
        } catch (Exception e) {
            log(Level.WARNING, e.getMessage(), e);
            e.printStackTrace();
            return false;
        }
    }

    public void setResult(File f)
    {
        resultFile = f;
    }

    public void setResult(String f)
    {
        try
        {
            resultFile = new File(f);
        }
        catch (Exception e)
        {
            log(Level.SEVERE, "Result file failed", e);
        }
    }
}
