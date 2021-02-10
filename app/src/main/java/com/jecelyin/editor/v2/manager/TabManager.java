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

package com.jecelyin.editor.v2.manager;

import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.viewpager.widget.ViewPager;


import com.jecelyin.editor.v2.adapter.EditorAdapter;
import com.jecelyin.editor.v2.adapter.TabAdapter;
import com.jecelyin.editor.v2.common.TabCloseListener;
import com.jecelyin.editor.v2.dialog.SaveConfirmDialog;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.TextEditorActivity;
import com.mrikso.apkrepacker.database.JsonDatabase;
import com.mrikso.apkrepacker.database.entity.RecentFileItem;
import com.mrikso.apkrepacker.ide.editor.EditorDelegate;
import com.mrikso.apkrepacker.ide.editor.IEditorDelegate;
import com.mrikso.apkrepacker.ide.editor.task.SaveAllTask;
import com.mrikso.apkrepacker.ide.file.SaveListener;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.mrikso.apkrepacker.view.EditorView;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class TabManager implements ViewPager.OnPageChangeListener {
    private final TextEditorActivity mainActivity;
    private final TabAdapter tabAdapter;
    private EditorAdapter editorAdapter;

    public TabManager(TextEditorActivity activity) {
        this.mainActivity = activity;

        this.tabAdapter = new TabAdapter();
        tabAdapter.setOnClickListener(this::onTabMenuViewsClick);
        //  mainActivity.getTabRecyclerView().addItemDecoration(new HorizontalDividerItemDecoration.Builder(activity.getContext()).build());
        mainActivity.getTabRecyclerView().setAdapter(tabAdapter);

        initEditor();

        mainActivity.mToolbar.setNavigationOnClickListener(v -> mainActivity.mDrawerLayout.openDrawer(GravityCompat.START));
        mainActivity.mTabPager.setOnPageChangeListener(this);
    }

    private void onTabMenuViewsClick(View v) {
        switch (v.getId()) {
            case R.id.btn_close:
                closeTab((int) v.getTag());
                break;
            default:
                int position = (int) v.getTag();
                mainActivity.closeMenu();
                setCurrentTab(position);
                break;
        }
    }

    private void initEditor() {
        editorAdapter = new EditorAdapter(mainActivity);
        mainActivity.mTabPager.setAdapter(editorAdapter); //优先，避免TabAdapter获取不到正确的CurrentItem

      /*  if (Pref.getInstance(mainActivity).isOpenLastFiles()) {
            ArrayList<DBHelper.RecentFileItem> recentFiles = DBHelper.getInstance(mainActivity).getRecentFiles(true);

            File f;
            for (DBHelper.RecentFileItem item : recentFiles) {
                f = new File(item.path);
                if (!f.isFile())
                    continue;
                editorAdapter.newEditor(false, f, item.offset, item.encoding);
                setCurrentTab(editorAdapter.getCount() - 1); //fixme: auto load file, otherwise click other tab will crash by search result
            }
            editorAdapter.notifyDataSetChanged();
            updateTabList();

            int lastTab = Pref.getInstance(mainActivity).getLastTab();
            setCurrentTab(lastTab);
        }*/

        editorAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                updateTabList();

             /*   if (!exitApp && editorAdapter.getCount() == 0) {
                   // newTab();
                }*/
            }
        });

        //if (editorAdapter.getCount() == 0)
        // editorAdapter.newEditor("test" /*mainActivity.getString(R.string.new_filename, editorAdapter.countNoFileEditor() + 1)*/, null);
    }

    public void newTab() {
        editorAdapter.newEditor("test"/*mainActivity.getString(R.string.new_filename, editorAdapter.getCount() + 1)*/, null);
        setCurrentTab(editorAdapter.getCount() - 1);
    }

    public boolean newTab(CharSequence content) {
        editorAdapter.newEditor("test"/*mainActivity.getString(R.string.new_filename, editorAdapter.getCount() + 1)*/, content);
        setCurrentTab(editorAdapter.getCount() - 1);
        return true;
    }

    public boolean newTab(File path) {
        return newTab(path, 0, "utf-8");
    }

    public boolean newTab(File path, String encoding) {
        return newTab(path, 0, encoding);
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

    public int getTabCount() {
        if (tabAdapter == null)
            return 0;
        return tabAdapter.getItemCount();
    }

    public int getCurrentTab() {
        return mainActivity.mTabPager.getCurrentItem();
    }

    public void setCurrentTab(final int index) {
        mainActivity.mTabPager.setCurrentItem(index);
        tabAdapter.setCurrentTab(index);
        updateToolbar();
    }

    public void closeTab(int position) {
        editorAdapter.removeEditor(position, (path, encoding, offset) -> {
            JsonDatabase.getInstance(mainActivity).updateRecentFile(path, false);
            int currentTab = getCurrentTab();
            if (getTabCount() != 0) {
                setCurrentTab(currentTab); //设置title等等
            }
            //tabAdapter.setCurrentTab(currentTab);
        }, false);
    }

    public EditorAdapter getEditorAdapter() {
        return editorAdapter;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        tabAdapter.setCurrentTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private void updateTabList() {
        tabAdapter.setTabInfoList(editorAdapter.getTabInfoList());
        tabAdapter.notifyDataSetChanged();
    }

    public void updateEditorView(int index, EditorView editorView) {
        editorAdapter.setEditorView(index, editorView);
    }

    public void onDocumentChanged() {
        DLog.d("TabManager", "DocumentChanged");
        updateTabList();
        updateToolbar();
    }

    private void updateToolbar() {
        EditorDelegate delegate = editorAdapter.getItem(getCurrentTab());
        if (delegate == null)
            return;
        //mainActivity.mToolbar.setTitle(delegate.getToolbarText());
    }

    public boolean onDestroy() {

        ArrayList<File> needSaveFiles = new ArrayList<>();
        ArrayList<IEditorDelegate> allEditor = editorAdapter.getAllEditor();
        for (IEditorDelegate editorDelegate : allEditor) {
            String path = editorDelegate.getPath();
            String encoding = editorDelegate.getEncoding();
            int offset = editorDelegate.getCursorOffset();
            if (editorDelegate.isChanged()) {
                needSaveFiles.add(editorDelegate.getDocument().getFile());
            }
            RecentFileItem recentFileItem = new RecentFileItem();
            recentFileItem.setPath(path);
            recentFileItem.setEncoding(encoding);
            recentFileItem.setOffset(offset);
            recentFileItem.setLastOpen(true);
            recentFileItem.setTime(System.currentTimeMillis());
            JsonDatabase.getInstance(mainActivity).updateRecentFile(path, encoding, offset);
        }

        if (needSaveFiles.isEmpty()) {
            return true;
        } else {

            StringBuilder fileName = new StringBuilder("(");
            for (int i = 0; i < needSaveFiles.size(); i++) {
                File needSaveFile = needSaveFiles.get(i);
                fileName.append(needSaveFile.getName());
                if (i != needSaveFiles.size() - 1) {
                    fileName.append(", ");
                }
            }
            fileName.append(")");

            SaveConfirmDialog saveConfirmDialog = new SaveConfirmDialog(mainActivity, fileName.toString(),
                    (dialog, which) -> {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            SaveAllTask saveAllTask = new SaveAllTask(mainActivity, new SaveListener() {
                                @Override
                                public void onSavedSuccess() {
                                    mainActivity.finish();
                                }

                                @Override
                                public void onSaveFailed(Exception e) {

                                }
                            });
                            dialog.dismiss();
                            saveAllTask.execute();
                        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
                            dialog.cancel();
                            mainActivity.finish();
                        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                            dialog.cancel();
                        }

                    });
            saveConfirmDialog.show();
            return false;
        }
    }

    public void closeAllUnchanged() {
        editorAdapter.removeAll(new TabCloseListener() {
            @Override
            public void onClose(String path, String encoding, int offset) {
                editorAdapter.removeAll(this, true);
            }
        }, true);
    }
}
