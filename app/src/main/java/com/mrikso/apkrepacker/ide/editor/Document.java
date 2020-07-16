/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mrikso.apkrepacker.ide.editor;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ide.editor.task.SaveTask;
import com.mrikso.apkrepacker.ide.file.ReadFileListener;
import com.mrikso.apkrepacker.ide.file.SaveListener;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.StringUtils;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.EditorPreferences;
import com.jecelyin.editor.v2.io.FileReader;
import com.jecelyin.editor.v2.io.LocalFileWriter;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class Document implements ReadFileListener {
    private final EditorDelegate mEditorDelegate;
    private final Context mContext;
    private final EditorPreferences mEditorPreferences;
    private int mLineCount;
    private String mEncoding = "UTF-8";
    @Nullable
    private byte[] mSourceMD5;
    private int mSourceLength;
    private String mModeName;
    @NonNull
    private File mFile;

    Document(@NonNull Context context, @NonNull EditorDelegate editorDelegate, @NonNull File currentFile) {
        mEditorDelegate = editorDelegate;
        mContext = context;
        mFile = currentFile;
        mEditorPreferences = EditorPreferences.getInstance(context);
    }

    /**
     * Returns the md5sum for given string. Or dummy byte array on error
     * Suppress NoSuchAlgorithmException because MD5 algorithm always present in JRE
     *
     * @param charSequence Given string
     * @return md5 sum of given string
     */
    private static byte[] md5(@NonNull CharSequence charSequence) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] ba = new byte[2];
            for (int i = 0, n = charSequence.length(); i < n; i++) {
                char cp = charSequence.charAt(i);
                ba[0] = (byte) (cp & 0xff);
                ba[1] = (byte) (cp >> 8 & 0xff);
                digest.update(ba);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            DLog.e("Can't Calculate MD5 hash!", e);
            return charSequence.toString().getBytes();
        }
    }


    void onSaveInstanceState(@NonNull EditorDelegate.SavedState ss) {
        ss.modeName = mModeName;
        ss.lineNumber = mLineCount;
        ss.textMd5 = mSourceMD5;
        ss.textLength = mSourceLength;
        ss.encoding = mEncoding;
        ss.file = mFile;
    }

    void onRestoreInstanceState(@NonNull EditorDelegate.SavedState ss) {

        if (ss.lineNumber > 0) {
            mLineCount = ss.lineNumber;
        }
        mSourceMD5 = ss.textMd5;
        mSourceLength = ss.textLength;
        mEncoding = ss.encoding;
        mFile = ss.file;
    }

    void loadFile(File file, @Nullable String encodingName) {
        if (!file.isFile() || !file.exists()) {
            UIUtils.alert(mContext, mContext.getString(R.string.cannt_access_file, file.getPath()));
            return;
        }
        if (!file.canRead()) {
            UIUtils.alert(mContext, mContext.getString(R.string.cannt_read_file, file.getPath()));
            return;
        }
        mFile = file;
        FileReader reader = new FileReader(mFile, encodingName);
        new ReadFileTask(reader, this).execute();
    }

    @Override
    public void onStart() {
        mEditorDelegate.onLoadStart();
    }

    @Override
    public String onAsyncReaded(FileReader fileReader, boolean ok) {
        String text = fileReader.getBuffer();

        mLineCount = fileReader.getLineCount();
        mEncoding = fileReader.getEncoding();

        mSourceMD5 = md5(text);
        mSourceLength = text.length();

        return text;
    }

    @Override
    public void onDone(String spannableStringBuilder, boolean ok) {
        if (mEditorDelegate == null || mEditorDelegate.getEditText() == null)
            return;
        if (!ok) {
            mEditorDelegate.onLoadFinish();
            UIUtils.alert(mContext, mContext.getString(R.string.read_file_exception));
            return;
        }

        mEditorDelegate.getEditText().setText(spannableStringBuilder);
        mEditorDelegate.onLoadFinish();
    }


    public String getModeName() {
        return mModeName;
    }

    @NonNull
    public File getFile() {
        return mFile;
    }

    public String getPath() {
        return mFile.getPath();
    }

    public int getLineCount() {
        return mLineCount;
    }

    public String getEncoding() {
        return mEncoding;
    }

    @WorkerThread
    public void writeToFile(File file, String encoding) throws Exception {
        LocalFileWriter writer = new LocalFileWriter(file, encoding);
        writer.writeToFile(mEditorDelegate.getText());

        onSaveSuccess(file, encoding);
    }

    /**
     * Write current content to new file and set new file to edit
     *
     * @param file - file to write
     */
    protected void saveInBackground(final File file, final String encoding, SaveListener listener) {
        SaveTask saveTask = new SaveTask(file, encoding, this, listener);
        saveTask.execute();
    }

    private void onSaveSuccess(File newFile, String encoding) {
        mFile = newFile;
        mEncoding = encoding;
        mSourceMD5 = md5(mEditorDelegate.getText());
        mSourceLength = mEditorDelegate.getText().length();
    }

    public boolean isChanged() {

        if (mSourceMD5 == null) {
            return mEditorDelegate.getText().length() != 0;
        }
        if (mSourceLength != mEditorDelegate.getText().length()) {
           return true;
        }

        byte[] curMD5 = md5(mEditorDelegate.getText());

        return !StringUtils.isEqual(mSourceMD5, curMD5);
    }

    public byte[] getMd5() {
        return mSourceMD5;
    }

    private final static class ReadFileTask extends AsyncTask<File, Void, String> {
        private final ReadFileListener listener;
        private final FileReader fileReader;

        ReadFileTask(FileReader reader, ReadFileListener listener) {
            this.fileReader = reader;
            this.listener = listener;
        }

        @Override
        protected void onPreExecute() {
            listener.onStart();
        }

        @Override
        protected String doInBackground(File... params) {
            if (!fileReader.read()) {
                return null;
            }

            return listener.onAsyncReaded(fileReader, true);
        }

        @Override
        protected void onPostExecute(String spannableStringBuilder) {
            listener.onDone(spannableStringBuilder, spannableStringBuilder != null);
        }
    }
}
