package com.mrikso.apkrepacker.task;

import android.content.Context;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.MyFilesFragment;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;

import java.io.File;
import java.util.logging.Level;

import brut.androlib.ApkOptions;
import brut.androlib.res.AndrolibResources;
import brut.util.Logger;

public class ImportFrameworkTask extends CoroutinesAsyncTask<File, CharSequence, Boolean> implements Logger {
    private Context mContext;
    private MyFilesFragment dialog;

    public ImportFrameworkTask(Context mContext, MyFilesFragment filesFragment){
        dialog = filesFragment;
        this.mContext = mContext;
    }

    @Override
    public Boolean doInBackground(File... files) {
        boolean success = true;
        for (File file : files) {
            if (!process(file))
                success = false;
        }
        return success;
    }

    @Override
    public void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        dialog.hideProgress();
        if(!result)
            UIUtils.toast(mContext, R.string.toast_error_import_framework_failed);

    }

    @Override
    public void onProgressUpdate(CharSequence... values) {
        dialog.updateProgress();
    }

    @Override
    public void onPreExecute() {
        dialog.showProgress();
    }
    protected boolean process(File file) {
        try
        {
            AndrolibResources res = new AndrolibResources(this);
            res.apkOptions = ApkOptions.INSTANCE;
            res.installFramework(file);
            return true;
        }
        catch (Exception e)
        {
            publishProgress("E: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void text(int id, Object... args) {

    }

    @Override
    public void error(int text, Object... args) {

    }

    @Override
    public void log(Level warring, String format, Throwable ex) {

    }

    @Override
    public void fine(int id, Object... args) {

    }

    @Override
    public void warning(int id, Object... args) {

    }

    @Override
    public void info(int id, Object... args) {

    }
}
