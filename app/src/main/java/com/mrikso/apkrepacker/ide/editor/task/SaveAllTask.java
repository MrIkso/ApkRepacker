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

package com.mrikso.apkrepacker.ide.editor.task;

import androidx.annotation.Nullable;
import com.jecelyin.editor.v2.adapter.EditorAdapter;
import com.jecelyin.editor.v2.manager.TabManager;
import com.mrikso.apkrepacker.activity.TextEditorActivity;
import com.mrikso.apkrepacker.ide.editor.IEditorDelegate;
import com.mrikso.apkrepacker.ide.file.SaveListener;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;
import com.mrikso.apkrepacker.utils.common.DLog;

/**
 * Created by Duy on 30-Apr-18.
 */

public class SaveAllTask extends CoroutinesAsyncTask<Void, Void, Boolean> {
    private static final String TAG = "SaveAllTask";
    private TextEditorActivity editorActivity;
    @Nullable
    private SaveListener saveListener;
    private Exception exception;

    public SaveAllTask(TextEditorActivity editorActivity, @Nullable SaveListener saveListener) {
        this.editorActivity = editorActivity;
        this.saveListener = saveListener;
    }

    @Override
    public void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    public Boolean doInBackground(Void... voids) {
        long startTime = System.currentTimeMillis();

        TabManager tabManager = editorActivity.getTabManager();
        EditorAdapter editorPagerAdapter = tabManager.getEditorAdapter();
        for (IEditorDelegate editorDelegate : editorPagerAdapter.getAllEditor()) {
            try {
                editorDelegate.saveCurrentFile();
            } catch (Exception e) {
                e.printStackTrace();
                exception = e;
            }
        }
        if (DLog.DEBUG) {
            DLog.d(TAG, "doInBackground: time = " + (System.currentTimeMillis() - startTime));
        }

        return exception == null;
    }

    @Override
    public void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        EditorAdapter editorPagerAdapter = editorActivity.getTabManager().getEditorAdapter();
        for (IEditorDelegate editorDelegate : editorPagerAdapter.getAllEditor()) {
            editorDelegate.onDocumentChanged();
        }
        if (aBoolean) {
            if (saveListener != null) {
                saveListener.onSavedSuccess();
            }
        } else {
            if (saveListener != null) {
                saveListener.onSaveFailed(exception);
            }
        }
    }
}
