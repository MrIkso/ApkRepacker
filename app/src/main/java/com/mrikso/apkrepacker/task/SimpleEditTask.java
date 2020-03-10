package com.mrikso.apkrepacker.task;

import android.content.Context;
import android.os.AsyncTask;

import com.duy.common.DLog;
import com.mrikso.apkrepacker.fragment.SimpleEditorFragment;
import com.mrikso.apkrepacker.model.QickEdit;
import com.mrikso.apkrepacker.ui.prererence.Preference;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.SignUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class SimpleEditTask extends AsyncTask<File, Integer, Boolean> {

    private static SignUtil signTool;
    private File resultFile;
    private Context mContext;
    private Preference preference;
    private SimpleEditorFragment simpleEditorFragment;

    public SimpleEditTask(Context mContext, SimpleEditorFragment simpleEditorFragment, SignUtil signTool) {
        this.mContext = mContext;
        this.signTool = signTool;
        this.simpleEditorFragment = simpleEditorFragment;
    }

    @Override
    protected void onPreExecute() {
        simpleEditorFragment.showProgress();
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
        simpleEditorFragment.hideProgress(result);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        simpleEditorFragment.updateProgress(values[0]);
    }

    protected boolean process(File input) {
        preference = Preference.getInstance(mContext);
        String outApk;
        try {
            File tmp = File.createTempFile("temp", ".apk");
            try {
                if (preference.isSignResultApk()) {
                    outApk = FileUtil.genNameApk(mContext, input.getAbsolutePath(), input.getName(), "_signed", 0);
                } else {
                    outApk = FileUtil.genNameApk(mContext, input.getAbsolutePath(), input.getName(), "_unsigned", 0);
                }
                File buildApkPath = new File(preference.getDecodingPath() + "/output");
                if (!buildApkPath.exists() && !buildApkPath.mkdirs()) {
                    return false;
                }
                DLog.i("start");
                File out = new File(buildApkPath, outApk);
                QickEdit qickEdit = new QickEdit();
                qickEdit.build(input, tmp);
                DLog.i("edited done");
                if (preference.isSignResultApk()) {
                    DLog.i("start sign apk");
                    signTool.sign(tmp, out, 14);
                    DLog.i("temp file: " + tmp.getAbsolutePath());
                    //FileUtils.copyFile(tmp, out);
                    setResult(out);
                } else {
                    try {
                        if (out.exists()) {
                            FileUtil.deleteFile(out);
                        }
                        DLog.i("done");
                        FileUtils.copyFile(tmp, out);
                        setResult(out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                /*
                if (preference.isSignResultApk()) {
                    outApk = FileUtil.genNameApk(mContext, f.getAbsolutePath(), AppUtils.getApkName(mContext, f.getAbsolutePath()), "_signed", 0);
                } else {
                    outApk = FileUtil.genNameApk(mContext, f.getAbsolutePath(),AppUtils.getApkName(mContext, f.getAbsolutePath()), "_unsigned", 0);
                }
                File buildApkPath = new File(preference.getDecodingPath() + "/output");
                if (!buildApkPath.exists() && !buildApkPath.mkdirs()) {
                    return false;
                }
                File out = new File(buildApkPath, outApk);
                if (preference.isSignResultApk()) {
                    signTool.sign(tmp, out, 14, this);
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

                 */
            } finally {
                tmp.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void setResult(File f) {
        resultFile = f;
        DLog.i(resultFile.getAbsolutePath());
    }

    public void setResult(String f) {
        try {
            resultFile = new File(f);
            DLog.i(resultFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
