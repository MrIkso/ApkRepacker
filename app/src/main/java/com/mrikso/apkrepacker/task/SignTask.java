package com.mrikso.apkrepacker.task;

import android.content.Context;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.MyFilesFragment;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.SignUtil;

import java.io.File;

public class SignTask extends CoroutinesAsyncTask<File, CharSequence, Boolean> {
    private final SignUtil signTool;
    public File resultFile;
    private Context mContext;
    private MyFilesFragment dialog;

    public SignTask(Context context, MyFilesFragment filesFragment , SignUtil signTool) {
        dialog = filesFragment;
        this.mContext = context;
        this.signTool = signTool;
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
        super.onPostExecute(result);
        dialog.hideProgress();
        dialog.signedApk = resultFile;
        if(!result)
        UIUtils.toast(mContext, R.string.toast_error_sign_failed);

    }

    @Override
    public void onProgressUpdate(CharSequence... values) {
        dialog.updateProgress();
    }

    @Override
    public void onPreExecute() {
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

