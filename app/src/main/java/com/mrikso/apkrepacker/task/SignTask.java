package com.mrikso.apkrepacker.task;

import android.content.Context;
import android.os.AsyncTask;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.FileManagerActivity;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.SignUtil;

import java.io.File;

public class SignTask extends AsyncTask<File, CharSequence, Boolean> {
    private final SignUtil signTool;
    public File resultFile;
    private Context mContext;
    private FileManagerActivity dialog;

    public SignTask(Context mContext, SignUtil signTool) {
        dialog = FileManagerActivity.getInstance();
        this.mContext = mContext;
        this.signTool = signTool;
    }

    @Override
    protected Boolean doInBackground(File[] p1) {
        boolean success = true;
        for (File file : p1) {
            if (!process(file))
                success = false;
        }
        return success;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        dialog.hideProgress();
        dialog.signedApk = resultFile;
        if(!result)
        UIUtils.toast(App.getContext(), R.string.toast_error_sign_failed);

    }

    @Override
    protected void onProgressUpdate(CharSequence... values) {
        dialog.updateProgress();
    }

    @Override
    protected void onPreExecute() {
        dialog.showProgress();
    }

    protected boolean process(File f) {
        String outApk;
        String dir = f.getParent();
        try {
            outApk = FileUtil.genNameApk(mContext, f.getAbsolutePath(), f.getName(), "_signed", 0);
            File out = new File(dir,outApk);
            signTool.sign(f, out, 14);
            resultFile = out;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

