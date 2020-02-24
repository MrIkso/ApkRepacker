/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.editor.v2.ui.manager;

import com.jecelyin.editor.v2.adapter.EditorAdapter;
import com.jecelyin.editor.v2.common.TabCloseListener;
import com.jecelyin.editor.v2.ui.activities.MainActivity;
import com.jecelyin.editor.v2.ui.editor.EditorDelegate;
import com.mrikso.apkrepacker.R;

import java.io.File;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class TabManager  {
    private final MainActivity mainActivity;
    private EditorAdapter editorAdapter;
    private boolean exitApp;

    public TabManager(MainActivity activity) {
        this.mainActivity = activity;
        initEditor();
    }

    private void initEditor() {
        editorAdapter = new EditorAdapter(mainActivity);
        mainActivity.mTabPager.setAdapter(editorAdapter);
    }


    public boolean newTab(CharSequence content) {
        editorAdapter.newEditor(mainActivity.getString(R.string.new_filename, editorAdapter.getCount() + 1), content);
        setCurrentTab(editorAdapter.getCount() - 1);
        return true;
    }


    public boolean newTab(File path, int offset, String encoding) {
        int count = editorAdapter.getCount();
        for (int i = 0; i < count; i++) {
            EditorDelegate fragment = editorAdapter.getItem(i);
            if (fragment.getPath() == null)
                continue;
            if (fragment.getPath().equals(path.getPath())) {
                setCurrentTab(i);
                return false;
            }
        }
        editorAdapter.newEditor(path, offset, encoding);
        setCurrentTab(count);
        return true;
    }


    public int getCurrentTab() {
        return mainActivity.mTabPager.getCurrentItem();
    }

    public void setCurrentTab(final int index) {
        mainActivity.mTabPager.setCurrentItem(index);

        updateToolbar();
    }

    public EditorAdapter getEditorAdapter() {
        return editorAdapter;
    }

    public void onDocumentChanged(int index) {
        updateToolbar();
    }

    private void updateToolbar() {
        EditorDelegate delegate = editorAdapter.getItem(getCurrentTab());
        if (delegate == null)
            return;
        mainActivity.mToolbar.setTitle(delegate.getToolbarText());
    }

    public boolean closeAllTabAndExitApp() {
        EditorDelegate.setDisableAutoSave(true);
        exitApp = true;
        return editorAdapter.removeAll((path, encoding, offset) -> mainActivity.finish());
    }
}
