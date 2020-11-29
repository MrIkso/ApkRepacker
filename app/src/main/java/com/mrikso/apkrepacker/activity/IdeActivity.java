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

package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.EditorPreferences;
import com.jecelyin.editor.v2.common.Command;
import com.jecelyin.editor.v2.dialog.CharsetsDialog;
import com.jecelyin.editor.v2.dialog.GotoLineDialog;
import com.jecelyin.editor.v2.dialog.LangListDialog;
import com.jecelyin.editor.v2.io.FileEncodingDetector;
import com.jecelyin.editor.v2.manager.MenuManager;
import com.jecelyin.editor.v2.manager.RecentFilesManager;
import com.jecelyin.editor.v2.manager.TabManager;
import com.jecelyin.editor.v2.widget.SymbolBarLayout;
import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.jecelyin.editor.v2.widget.menu.MenuFactory;
import com.jecelyin.editor.v2.widget.menu.MenuItemInfo;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.database.ITabDatabase;
import com.mrikso.apkrepacker.database.JsonDatabase;
import com.mrikso.apkrepacker.ide.editor.EditorDelegate;
import com.mrikso.apkrepacker.ide.editor.IEditorDelegate;
import com.mrikso.apkrepacker.ide.editor.KeyBoardEventListener;

import com.mrikso.apkrepacker.ide.editor.lexer.LexerUtil;
import com.mrikso.apkrepacker.ide.file.FileManager;
import com.mrikso.apkrepacker.task.Smali2JavaTask;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Objects;


public abstract class IdeActivity extends BaseActivity implements MenuItem.OnMenuItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final int RC_OPEN_FILE = 1;
    private final static int RC_SAVE_AS = 3;
    private static final int RC_SETTINGS = 5;
    private static final int RC_PERMISSION_WRITE_STORAGE = 5001;
    private static final int RC_CHANGE_THEME = 350;

    //public SlidingUpPanelLayout mSlidingUpPanelLayout;
    @Nullable
    public RecyclerView mMenuRecyclerView;

    public DrawerLayout mDrawerLayout;
    protected TabManager mTabManager;
    protected Toolbar mToolbar;
    @Nullable
    protected SymbolBarLayout mSymbolBarLayout;
    @Nullable
    protected SmartTabLayout mTabLayout;
    @Nullable
    protected TextView mTxtDocumentInfo;
    protected EditorPreferences mEditorPreferences;
    private long mExitTime;
    private KeyBoardEventListener mKeyBoardListener;
    private MenuManager menuManager;
    private String mPath;
    private int mOffset;
    private Context mContext;
    private ITabDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getRootLayoutId());
        mContext = this;
        initToolbar();
        MenuManager.init(this);

        mEditorPreferences = EditorPreferences.getInstance(this);
        mEditorPreferences.registerOnSharedPreferenceChangeListener(this);

        mDatabase = JsonDatabase.getInstance(this);
        mMenuRecyclerView = findViewById(R.id.menuRecyclerView);
        mTabLayout = findViewById(R.id.tab_layout);
        mTxtDocumentInfo = findViewById(R.id.txt_document_info);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setKeepScreenOn(mEditorPreferences.isKeepScreenOn());
        mDrawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                EditorDelegate currentEditorDelegate = getCurrentEditorDelegate();
                if (currentEditorDelegate != null) {
                    currentEditorDelegate.getEditText().clearFocus();
                }
                mDrawerLayout.requestFocus();
                hideSoftInput();
            }
        });

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, mToolbar, R.string.open_drawer, R.string.close_drawer);
        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        mSymbolBarLayout = findViewById(R.id.symbolBarLayout);
        if (mSymbolBarLayout != null) {
            mSymbolBarLayout.setOnSymbolCharClickListener(
                    (v, text) -> insertText(text));
        }

        //ViewModelProvider provider = new ViewModelProvider(this, new IdeActivityViewModelFactory(getApplication()));
       // viewModel = provider.get(IdeActivityViewModel.class);
       // subscribeObserver();

        bindPreferences();
        setScreenOrientation();

        initMenuView();
        initEditorView();
        //intiDiagnosticView();
        initLeftNavigationView(findViewById(R.id.left_navigation_view));
        //final, create editor
        processIntent();

        //attach listener hide/show keyboard
        mKeyBoardListener = new KeyBoardEventListener(this);
        mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(mKeyBoardListener);

    }
/*
    private void subscribeObserver() {
        viewModel.getAllKeywords(false).observe(this, findKeywordsItems -> {
            if (findKeywordsItems != null) {
                if (mFindList.size() > 0) mFindList.clear();
                mFindList.addAll(findKeywordsItems);
                for (FindKeywordsItem item : mFindList) {
                    dataList.add(item.getKeyword());
                }
                searchAdapter.notifyDataSetChanged();
            }
        });
        viewModel.getAllKeywords(true).observe(this, findKeywordsItems -> {
            if (findKeywordsItems != null) {
                if (mReplaceList.size() > 0) mReplaceList.clear();
                mReplaceList.addAll(findKeywordsItems);
                for (FindKeywordsItem item : mFindList) {
                    dataListReplace.add(item.getKeyword());
                }
                replaceAdapter.notifyDataSetChanged();
            }
        });
    }


 */
    private void initEditorView() {
        mTabManager = new TabManager(IdeActivity.this, findViewById(R.id.editor_view_pager));
        mTabManager.createEditor();
    }

    private void initToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(R.string.code_editor);
    }

    /**
     * Replace view for left navigation view, such as Java NIDE use this for Folder view
     */
    protected void initLeftNavigationView(@NonNull NavigationView nav) {

    }

    /**
     * @return main layout id
     */
    @LayoutRes
    protected abstract int getRootLayoutId();

    private void bindPreferences() {
        mSymbolBarLayout.setVisibility(mEditorPreferences.isReadOnly() ? View.GONE : View.VISIBLE);
        mSymbolBarLayout.setVisibility(mEditorPreferences.isHidePanel() ? View.GONE : View.VISIBLE);
        mEditorPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // super.onSharedPreferenceChanged(sharedPreferences, key);
        switch (key) {
            case EditorPreferences.KEY_ENABLE_HIGHLIGHT:
                Command command = new Command(Command.CommandEnum.HIGHLIGHT);
                command.object = mEditorPreferences.isHighlight() ? null : "None";
                doCommandForAllEditor(command);
                break;

            case EditorPreferences.KEY_KEEP_SCREEN_ON:
                if (mToolbar != null) {
                    mToolbar.setKeepScreenOn(sharedPreferences.getBoolean(key, false));
                }
                break;
            case EditorPreferences.KEY_SCREEN_ORIENTATION:
                setScreenOrientation();
                break;
            case EditorPreferences.KEY_READ_ONLY:
                if (mSymbolBarLayout != null) {
                    mSymbolBarLayout.setVisibility(mEditorPreferences.isReadOnly() ? View.GONE : View.VISIBLE);
                }
                break;
            case EditorPreferences.KEY_HIDE_SYMBOL_PANEL:
                mSymbolBarLayout.setVisibility(mEditorPreferences.isHidePanel() ? View.GONE : View.VISIBLE);
                break;
        }
    }

    private void setScreenOrientation() {
        int orientation = mEditorPreferences.getScreenOrientation();
        if (EditorPreferences.SCREEN_ORIENTATION_AUTO == orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if (EditorPreferences.SCREEN_ORIENTATION_LANDSCAPE == orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if (EditorPreferences.SCREEN_ORIENTATION_PORTRAIT == orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    private void initMenuView() {
        mMenuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        if (menuManager == null)
            menuManager = new MenuManager(this);
    }

    private void processIntent() {
        Intent intent = getIntent();
        mPath = intent.getStringExtra("filePath");
        mOffset = intent.getIntExtra("offset", 1);
        if (mOffset != 0) {
            //  DLog.d("Offset" + mOffset);
            openFile(mPath, FileEncodingDetector.DEFAULT_ENCODING, mOffset);
        } else {
            openFile(mPath);
        }
    }

    /**
     * @param status {@link MenuDef#STATUS_NORMAL}, {@link MenuDef#STATUS_DISABLED}
     */
    public void setMenuStatus(@IdRes int menuResId, int status) {
        if (mToolbar == null) {
            return;
        }
        MenuItem menuItem = mToolbar.getMenu().findItem(menuResId);
        if (menuItem == null) {
            return;
        }
        Drawable icon = menuItem.getIcon();
        if (status == MenuDef.STATUS_DISABLED) {
            menuItem.setEnabled(false);
            menuItem.setIcon(MenuManager.makeToolbarDisabledIcon(icon));
        } else {
            menuItem.setEnabled(true);
            if (menuItem.getGroupId() == MenuDef.GROUP_TOOLBAR) {
                menuItem.setIcon(MenuManager.makeToolbarNormalIcon(icon));
            } else {
                menuItem.setIcon(MenuManager.makeMenuNormalIcon(icon));
            }
        }
    }

    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu container) {
        MenuFactory menuFactory = MenuFactory.getInstance(this);
        List<MenuItemInfo> topMenu = menuFactory.getToolbarIcon();
        for (MenuItemInfo item : topMenu) {
            MenuItem menuItem = container.add(MenuDef.GROUP_TOOLBAR, item.getItemId(), item.getOrder(), item.getTitleResId());
            menuItem.setIcon(MenuManager.makeToolbarNormalIcon(this, item.getIconResId()));
            menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }
/*
        MenuGroup[] values = new MenuGroup[]{MenuGroup.FILE, MenuGroup.EDIT};
        for (MenuGroup group : values) {
            if (group == MenuGroup.TOP) {
                continue;
            }
            SubMenu subMenu = container.addSubMenu(MenuDef.GROUP_TOOLBAR, group.getMenuId(), 0, group.getTitleId());
            subMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            List<MenuItemInfo> items = menuFactory.getMenuItemsWithoutToolbarMenu(group);
            for (MenuItemInfo item : items) {
                MenuItem menuItem = subMenu.add(MenuDef.GROUP_TOOLBAR, item.getItemId(), item.getOrder(), item.getTitleResId());
                menuItem.setIcon(MenuManager.makeMenuNormalIcon(this, item.getIconResId()));
                menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            }
        }


 */
        MenuItem menuItem = container.add(MenuDef.GROUP_TOOLBAR, R.id.m_menu, Menu.NONE, getString(R.string.more_menu));
        menuItem.setIcon(MenuManager.makeToolbarNormalIcon(this, R.drawable.ic_more_horiz_white));
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(container);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return onMenuItemClick(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        closeDrawers();
        int id = item.getItemId();
        onMenuClick(id);
        return true;
    }

    private void onMenuClick(int id) {
        Command.CommandEnum commandEnum;

        if (id == R.id.action_goto_line) {
            EditorDelegate editorDelegate = getCurrentEditorDelegate();
            if (editorDelegate != null) {
                new GotoLineDialog(this, editorDelegate).show();
            }
        } else if (id == R.id.action_file_history) {
            RecentFilesManager rfm = new RecentFilesManager(this);
            rfm.setOnFileItemClickListener((file, encoding) -> openFile(file, encoding, 0));
            rfm.show(this);

        } else if (id == R.id.action_highlight) {
            new LangListDialog(this).show();

        } else if (id == R.id.m_menu) {
            hideSoftInput();
            mDrawerLayout.postDelayed(() -> mDrawerLayout.openDrawer(GravityCompat.END), 200);

        }
        /*else if (id == R.id.action_save_all) {
            UIUtils.toast(this, R.string.save_all);
            //saveAll(0);

        }*/
        else if (id == R.id.action_smali_java) {
            if (AppUtils.apiIsAtLeast(Build.VERSION_CODES.O)) {
                String smali = getCurrentEditorDelegate().getPath();
                if (smali != null) {
                    if (smali.toLowerCase().endsWith(".smali")) {
                        UIUtils.toast(this, "Decompiling");
                        Runnable runnable = () -> new Smali2JavaTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                                new File(smali));
                        runnable.run();
                    } else {
                        UIUtils.toast(this, "Works only smali files!");
                    }
                }
            } else {
                UIUtils.alert(mContext, getString(R.string.attention_title), getString(R.string.jadx_doesnt_support_on_this_prone));
            }
        } else if (id == R.id.m_fullscreen) {
            boolean fullscreenMode = mEditorPreferences.isFullScreenMode();
            mEditorPreferences.setFullScreenMode(!fullscreenMode);
            UIUtils.toast(this, fullscreenMode
                    ? R.string.disabled_fullscreen_mode_message
                    : R.string.enable_fullscreen_mode_message);

        } else if (id == R.id.m_readonly) {
            boolean readOnly = !mEditorPreferences.isReadOnly();
            mEditorPreferences.setReadOnly(readOnly);
            doCommandForAllEditor(new Command(Command.CommandEnum.READONLY_MODE));
        } else if (id == R.id.m_hide_symbol_panel) {
            boolean hidePanel = !mEditorPreferences.isHidePanel();
            mEditorPreferences.setHidePanel(hidePanel);
        } else if (id == R.id.action_encoding) {
            new CharsetsDialog(this).show();

        } else if (id == R.id.action_editor_theme) {
            startActivityForResult(new Intent(this, ThemeEditorActivity.class), RC_CHANGE_THEME);
        } else if (id == R.id.action_editor_setting) {
            EditorSettingsActivity.open(this, RC_SETTINGS);

        } else {
            commandEnum = MenuFactory.getInstance(this).idToCommandEnum(id);
            if (commandEnum != Command.CommandEnum.NONE) {
                doCommand(new Command(commandEnum));
            }
        }
    }

    public String getCurrentLang() {
        return Objects.requireNonNull(getCurrentEditorDelegate().getEditText()).getLang();
    }

    protected void hideSoftInput() {
        doCommand(new Command(Command.CommandEnum.HIDE_SOFT_INPUT));
    }

    public void doCommandForAllEditor(Command command) {
        for (IEditorDelegate editorDelegate : mTabManager.getEditorPagerAdapter().getAllEditor()) {
            editorDelegate.doCommand(command);
        }
    }

    public void doCommand(Command command) {
        EditorDelegate editorDelegate = getCurrentEditorDelegate();
        if (editorDelegate != null) {
            editorDelegate.doCommand(command);
            if (command.what == Command.CommandEnum.HIGHLIGHT) {
                if (mToolbar != null) {
                    mToolbar.setTitle(editorDelegate.getToolbarText());
                }
            }
        }
    }

    public EditorDelegate getCurrentEditorDelegate() {
        if (mTabManager == null || mTabManager.getEditorPagerAdapter() == null) {
            return null;
        }
        return mTabManager.getEditorPagerAdapter().getCurrentEditorDelegate();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_PERMISSION_WRITE_STORAGE &&
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        }
    }

    @Override
    @CallSuper
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case RC_CHANGE_THEME:
                doCommandForAllEditor(new Command(Command.CommandEnum.REFRESH_THEME));
                break;
        }
    }

    private void openText(CharSequence content) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        FileManager fileManager = new FileManager(this);
        File newFile = fileManager.createNewFile(
                "untitled_" + System.currentTimeMillis() + ".txt");
        try {
            FileOutputStream output = new FileOutputStream(newFile);
            IOUtils.write(content.toString(), output);
            output.close();
            mTabManager.newTab(newFile);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public void openJavaText(CharSequence content, String name) {
        if (TextUtils.isEmpty(content)) {
            return;
        }
        try {
            // FileManager fileManager = new FileManager(this);
            File newFile = File.createTempFile(name, ".java");
            FileOutputStream output = new FileOutputStream(newFile);
            IOUtils.write(content.toString(), output, "utf-8");
            output.close();
            mTabManager.newTab(newFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected void openFile(String file) {
        openFile(file, null, 1);
    }

    protected void openFile(final String filePath, final String encoding, final int offset) {
        //ensure file exist, can read/write
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        final File file = new File(filePath);
        if (!file.isFile()) {
            UIUtils.toast(this, R.string.file_not_exists);
            return;
        }
        if (!file.canRead()) {
            UIUtils.alert(this, this.getString(R.string.cannt_read_file, file.getPath()));
            return;
        }

        if (!LexerUtil.isText(file.getName())) {
            UIUtils.showConfirmDialog(this, getString(R.string.not_a_text_file, file.getName()),
                    new UIUtils.OnClickCallback() {
                        @Override
                        public void onOkClick() {
                            createNewEditor(file, offset, encoding);
                        }
                    });
        } else {
            createNewEditor(file, offset, encoding);
        }

    }

    private void createNewEditor(File file, int offset, String encoding) {
        if (!mTabManager.newTab(file, offset, encoding)) {
            return;
        }
        mDatabase.addRecentFile(file.getPath(), encoding);
    }

    public void insertText(CharSequence text) {
        if (text == null) {
            return;
        }
        Command c = new Command(Command.CommandEnum.INSERT_TEXT);
        c.object = text;
        doCommand(c);
    }

    public TabManager getTabManager() {
        return mTabManager;
    }

    @Override
    @CallSuper
    public void onBackPressed() {
        if (closeDrawers()) {
            return;
        }
        if (mTabManager.onDestroy()) {
            if ((System.currentTimeMillis() - mExitTime) > 2000 && mEditorPreferences.isConfirmExit()) {
                UIUtils.toast(this, R.string.press_again_will_exit);
                mExitTime = System.currentTimeMillis();
            } else {
                super.onBackPressed();
            }
        }
    }

    @SuppressLint("WrongConstant")
    protected boolean closeDrawers() {
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(Gravity.START)) {
                mDrawerLayout.closeDrawer(Gravity.START);
                return true;
            }
            if (mDrawerLayout.isDrawerOpen(Gravity.END)) {
                mDrawerLayout.closeDrawer(Gravity.END);
                return true;
            }
        }
        return false;
    }

    public void onShowKeyboard() {
        if (mTabLayout != null) {
            mTabLayout.setVisibility(View.GONE);
        }
        if (mTxtDocumentInfo != null) {
            mTxtDocumentInfo.setVisibility(View.VISIBLE);
        }
    }

    public void onHideKeyboard() {
        if (mTabLayout != null) {
            mTabLayout.setVisibility(View.VISIBLE);
        }
        if (mTxtDocumentInfo != null) {
            mTxtDocumentInfo.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        mDrawerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(mKeyBoardListener);
        super.onDestroy();
    }

    public RecyclerView getMenuRecyclerView() {
        return mMenuRecyclerView;
    }

}
