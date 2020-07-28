package com.mrikso.apkrepacker.task;

import android.content.Context;
import android.os.AsyncTask;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.MyFilesFragment;

import java.io.File;
import java.util.logging.Level;

import brut.androlib.ApkOptions;
import brut.androlib.res.AndrolibResources;
import brut.util.Logger;

public class ImportFrameworkTask extends AsyncTask<File, CharSequence, Boolean> implements Logger {
    private Context mContext;
    private MyFilesFragment dialog;

    public ImportFrameworkTask(Context mContext, MyFilesFragment filesFragment){
        dialog = filesFragment;
        this.mContext = mContext;
    }

    @Override
    protected Boolean doInBackground(File... files) {
        boolean success = true;
        for (File file : files) {
            if (!process(file))
                success = false;
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        dialog.hideProgress();
        if(!result)
            UIUtils.toast(App.getContext(), R.string.toast_error_import_framework_failed);

    }

    @Override
    protected void onProgressUpdate(CharSequence... values) {
        dialog.updateProgress();
    }

    @Override
    protected void onPreExecute() {
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
