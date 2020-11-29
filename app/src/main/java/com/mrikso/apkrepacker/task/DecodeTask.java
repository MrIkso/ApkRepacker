package com.mrikso.apkrepacker.task;

import android.content.Context;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.DecompileFragment;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.common.DLog;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Level;

import brut.androlib.Androlib;
import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.androlib.ApkOptions;
import brut.directory.ExtFile;
import brut.util.Logger;
import brut.util.OS;

public class DecodeTask extends CoroutinesAsyncTask<File, CharSequence, Boolean> implements Logger {
    private Context mContext;
    private final int action;
    private final String name;
    public File resultFile;
    private DecompileFragment decompileFragment;

    public DecodeTask(Context ctx, int action, String name, DecompileFragment decompileFragment) {
        this.decompileFragment = decompileFragment;
        this.mContext = ctx;
        this.name = name;
        this.action = action;
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

    @Override
    public void onPostExecute(Boolean result) {
        final ArrayList<String> arrText = decompileFragment.getTextArray();
        decompileFragment.append(arrText);
        decompileFragment.decompileResult(resultFile);
    }

    @Override
    public void onProgressUpdate(CharSequence... values) {
        decompileFragment.append(values[0]);
    }

    @Override
    public void info(int id, Object... args) {
        publishProgress(String.format("I: %s", getText(id, args)));
        DLog.d(String.format("I: %s", getText(id, args)));
    }

    @Override
    public void warning(int id, Object... args) {
        publishProgress(String.format("W: %s", getText(id, args)));
        DLog.d(String.format("W: %s", getText(id, args)));
    }

    @Override
    public void fine(int id, Object... args) {
        // publishProgress(String.format("F:%s\n",getText(id, args)));
    }

    @Override
    public void text(int id, Object... args) {
        publishProgress(getText(id, args));
    }

    @Override
    public void error(int id, Object... args) {
        publishProgress(String.format("E: %s", getText(id, args)));
        DLog.d(String.format("E: %s", getText(id, args)));
    }

    @Override
    public void log(Level level, String format, Throwable ex) {
        char ch = level.getName().charAt(0);
        String fmt = "%c: %s";
        publishProgress(String.format(fmt, ch, format));
        log(fmt, ch, ex);
    }

    private void log(String fmt, char ch, Throwable ex) {
        if (ex == null) return;
        publishProgress(String.format(fmt, ch, ex.getMessage()));

        for (StackTraceElement ste : ex.getStackTrace()) {
            publishProgress(String.format(fmt, ch, ste));
            DLog.d(String.format(fmt, ch, ste));
        }
        log(fmt, ch, ex.getCause());
    }

    private String getText(int id, Object[] args) {
        return mContext.getResources().getString(id, args);
    }

    protected boolean process(File f) {
        final ExtFile dir = getOutDir(f, name);
        if (dir == null)
            return false;
        OS.rmdir(dir);
        dir.mkdirs();
        setResult(dir);
        ExtFile apk = new ExtFile(f);
        ApkOptions o = ApkOptions.INSTANCE;
        Androlib lib = new Androlib(o, this);
        ApkDecoder decoder = new ApkDecoder(apk, lib, this);
        int action = this.action;
        decoder.setApkFileName(name);
        decoder.setBaksmaliDebugMode(true);
        decoder.setDecodeAssets(ApkDecoder.DECODE_ASSETS_FULL);
        if ((action & 2) > 0)
            decoder.setDecodeResources(ApkDecoder.DECODE_RESOURCES_FULL);
        else
            decoder.setDecodeResources(ApkDecoder.DECODE_RESOURCES_NONE);
        if ((action & 1) > 0)
            decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_SMALI);
        else
            decoder.setDecodeSources(ApkDecoder.DECODE_SOURCES_NONE);
        decoder.setOutDir(dir);
        decoder.setApi(14);
        decoder.setForceDelete(true);
        return decode(decoder);
        /*
        finally
        {
            File smali = new File(dir, "smali");
            try
            {
             //   if (!smali.exists() && (action & 1) > 0)
                   // deodex(f, dir);
            }
            catch (Exception e)
            {
                log(Level.WARNING, "Deodex failed", e);
            }
        }

         */
    }

    private boolean decode(ApkDecoder decoder) {
        try {
            decoder.decode(mContext);
            return true;
        } catch (AndrolibException e) {
            log(Level.WARNING, "Decompile failed", e);
        }
        return false;
    }

    public ExtFile getOutDir(File f, String name) {
        PreferenceHelper preferenceHelper = PreferenceHelper.getInstance(mContext);
        String dir = f.getParent();
        if (name == null)
            name = f.getName();
        int e = name.lastIndexOf('.');
        if (e >= 0)
            name = name.substring(0, e);
        boolean overwrite = true;// Settings.getb(task.getContext(), "overwrite_apk", true);
        // boolean allToOut = true;//Settings.getb(task.getContext(), "all_to_out_dir", false);
        // boolean inst = false;//FileUtil.isSystemApp(task.getContext(), f);
        //boolean noaccess = false;//(Settings.isKitKat() && !f.getAbsolutePath().startsWith(Environment.getExternalStorageDirectory().getAbsolutePath()));
        //if (allToOut || inst)
        //  {
        dir = preferenceHelper.getDecodingPath() + "/projects";//Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test";//Settings.output_directory;
        if (dir == null) {
            warning(R.string.output_directory_not_set);
            return null;
        }
        File d = new File(dir);
        if (!d.exists() && !d.mkdirs()) {
            warning(R.string.output_directory_not_extsts, dir);
            return null;
        }
        if (!d.isDirectory()) {
            warning(R.string.not_directory, dir);
            return null;
        }
        //}
//        name = name + "_src";
        if (!overwrite)
            name = FileUtil.genName(mContext, dir, name, "", 0);
        return new ExtFile(dir, name);
    }

    public void setResult(File f) {
        resultFile = f;
    }

    public void setResult(String f) {
        try {
            resultFile = new File(f);
        } catch (Exception e) {
            log(Level.SEVERE, "Result file failed", e);
        }
    }
}
