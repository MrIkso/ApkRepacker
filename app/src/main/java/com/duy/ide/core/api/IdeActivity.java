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

package com.duy.ide.core.api;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.duy.ide.database.ITabDatabase;
import com.duy.ide.database.SQLHelper;
import com.duy.ide.editor.EditorDelegate;
import com.duy.ide.editor.IEditorDelegate;
import com.duy.ide.editor.IEditorStateListener;
import com.duy.ide.editor.editor.R;
import com.duy.ide.file.FileManager;
import com.duy.ide.file.dialogs.DialogNewFile;
import com.duy.ide.settings.EditorSettingsActivity;
import com.google.android.material.navigation.NavigationView;
import com.jecelyin.common.task.TaskListener;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.SysUtils;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.Preferences;
import com.jecelyin.editor.v2.common.Command;
import com.jecelyin.editor.v2.dialog.CharsetsDialog;
import com.jecelyin.editor.v2.dialog.GotoLineDialog;
import com.jecelyin.editor.v2.dialog.LangListDialog;
import com.jecelyin.editor.v2.io.FileEncodingDetector;
import com.jecelyin.editor.v2.manager.MenuManager;
import com.jecelyin.editor.v2.manager.RecentFilesManager;
import com.jecelyin.editor.v2.manager.TabManager;
import com.jecelyin.editor.v2.utils.ExtGrep;
import com.jecelyin.editor.v2.utils.GrepBuilder;
import com.jecelyin.editor.v2.utils.MatcherResult;
import com.jecelyin.editor.v2.widget.SymbolBarLayout;
import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.jecelyin.editor.v2.widget.menu.MenuFactory;
import com.jecelyin.editor.v2.widget.menu.MenuItemInfo;
import com.mrikso.apkrepacker.activity.BaseActivity;
import com.mrikso.apkrepacker.activity.ThemeEditorActivity;
import com.mrikso.apkrepacker.task.Smali2JavaTask;
import com.mrikso.apkrepacker.ui.autocompleteeidttext.CustomAdapter;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.apache.commons.io.IOUtils;
import org.gjt.sp.jedit.Catalog;
import org.gjt.sp.jedit.Mode;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import static android.view.View.VISIBLE;


public abstract class IdeActivity extends BaseActivity implements MenuItem.OnMenuItemClickListener,
        IEditorStateListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final int RC_OPEN_FILE = 1;
    private final static int RC_SAVE_AS = 3;
    private static final int RC_SETTINGS = 5;
    private static final int RC_PERMISSION_WRITE_STORAGE = 5001;
    private static final int RC_CHANGE_THEME = 350;

    private static final int ID_FIND_PREV = 1;
    private static final int ID_FIND_NEXT = 2;
    private static final int ID_REPLACE = 3;
    private static final int ID_REPLACE_ALL = 4;
    private static final int ID_FIND_TEXT = 5;
    private static LinearLayout searchPanel;
    //Handle create on MainThread, use for update UI
    private final Handler mHandler = new Handler();
    //public SlidingUpPanelLayout mSlidingUpPanelLayout;
    @Nullable
    public RecyclerView mMenuRecyclerView;
    public String findText, replaceText;
    protected TabManager mTabManager;
    protected Toolbar mToolbar;
    protected DrawerLayout mDrawerLayout;
    @Nullable
    protected SymbolBarLayout mSymbolBarLayout;
    @Nullable
    protected SmartTabLayout mTabLayout;
    @Nullable
    protected TextView mTxtDocumentInfo;
    protected Preferences mPreferences;
    private long mExitTime;
    private KeyBoardEventListener mKeyBoardListener;
    private MenuManager menuManager;
    private String mPath;
    private int mOffset;
    private MatcherResult lastResults;
    private ExtGrep grep;
    private boolean mCaseSensitive;
    private boolean mWholeWordsOnly;
    private boolean mRegex, mReplaceMode;
    private CustomAdapter searchAdapter;
    private CustomAdapter repaceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getRootLayoutId());
        initToolbar();
        MenuManager.init(this);

        mPreferences = Preferences.getInstance(this);
        mPreferences.registerOnSharedPreferenceChangeListener(this);

        mMenuRecyclerView = findViewById(R.id.menuRecyclerView);
        mTabLayout = findViewById(R.id.tab_layout);
        mTxtDocumentInfo = findViewById(R.id.txt_document_info);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setKeepScreenOn(mPreferences.isKeepScreenOn());
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

        bindPreferences();
        setScreenOrientation();

        TextView versionView = findViewById(R.id.versionTextView);
        versionView.setText(SysUtils.getVersionName(this));

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

    private void initEditorView() {
        mTabManager = new TabManager(this, findViewById(R.id.editor_view_pager));
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

    /**
     * You should override this method to init editor fragment
     */
    @Override
    @CallSuper
    public void onEditorViewCreated(@NonNull IEditorDelegate editorDelegate) {
        // editorDelegate.setCodeFormatProvider(getCodeFormatProvider());
    }

    @Override
    public void onEditorViewDestroyed(@NonNull IEditorDelegate editorDelegate) {

    }

    private void bindPreferences() {
        mSymbolBarLayout.setVisibility(mPreferences.isReadOnly() ? View.GONE : View.VISIBLE);
        mSymbolBarLayout.setVisibility(mPreferences.isHidePanel() ? View.GONE : View.VISIBLE);
        mPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // super.onSharedPreferenceChanged(sharedPreferences, key);
        switch (key) {
            case Preferences.KEY_ENABLE_HIGHLIGHT:
                Command command = new Command(Command.CommandEnum.HIGHLIGHT);
                command.object = mPreferences.isHighlight() ? null : Catalog.DEFAULT_MODE_NAME;
                doCommandForAllEditor(command);
                break;

            case Preferences.KEY_KEEP_SCREEN_ON:
                if (mToolbar != null) {
                    mToolbar.setKeepScreenOn(sharedPreferences.getBoolean(key, false));
                }
                break;
            case Preferences.KEY_SCREEN_ORIENTATION:
                setScreenOrientation();
                break;
            case Preferences.KEY_READ_ONLY:
                if (mSymbolBarLayout != null) {
                    mSymbolBarLayout.setVisibility(mPreferences.isReadOnly() ? View.GONE : VISIBLE);
                }
                break;
            case Preferences.KEY_HIDE_SYMBOL_PANEL:
                mSymbolBarLayout.setVisibility(mPreferences.isHidePanel() ? View.GONE : View.VISIBLE);
                break;
        }
    }

    private void setScreenOrientation() {
        int orientation = mPreferences.getScreenOrientation();
        if (Preferences.SCREEN_ORIENTATION_AUTO == orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if (Preferences.SCREEN_ORIENTATION_LANDSCAPE == orientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        } else if (Preferences.SCREEN_ORIENTATION_PORTRAIT == orientation) {
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
        mOffset = intent.getIntExtra("offset", 0);
        if (mOffset != 0) {
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

        if (id == R.id.action_new_file) {
            createNewFile();

        } else if (id == R.id.action_goto_line) {
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

        } else if (id == R.id.action_save_all) {
            UIUtils.toast(this, R.string.save_all);
            //saveAll(0);

        } else if (id == R.id.action_smali_java) {
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
        } else if (id == R.id.m_fullscreen) {
            boolean fullscreenMode = mPreferences.isFullScreenMode();
            mPreferences.setFullScreenMode(!fullscreenMode);
            UIUtils.toast(this, fullscreenMode
                    ? R.string.disabled_fullscreen_mode_message
                    : R.string.enable_fullscreen_mode_message);

        } else if (id == R.id.m_readonly) {
            boolean readOnly = !mPreferences.isReadOnly();
            mPreferences.setReadOnly(readOnly);
            doCommandForAllEditor(new Command(Command.CommandEnum.READONLY_MODE));
        } else if (id == R.id.m_hide_symbol_panel) {
            boolean hidePanel = !mPreferences.isHidePanel();
            mPreferences.setHidePanel(hidePanel);
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
    /**
     * Called when user click open file expoler menu
     */
    /*
    public void openFileExplorer() {
        EditorDelegate editorDelegate = getCurrentEditorDelegate();
        String sourceDir;
        String homeDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (BuildConfig.DEBUG) {
            homeDir = getFilesDir().getAbsolutePath();
        }
        if (editorDelegate != null) {
            sourceDir = new File(editorDelegate.getPath()).getParent();
        } else {
            sourceDir = homeDir;
        }
        FileExplorerActivity.startPickFileActivity(this, sourceDir, homeDir, RC_OPEN_FILE);
    }


     */

    /**
     * Called when user click create new file button, should override if you need more feature
     */
    public void createNewFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        RC_PERMISSION_WRITE_STORAGE);
                return;
            }
        }

        String[] fileExtensions = getSupportedFileExtensions();
        EditorDelegate currentEditorDelegate = getCurrentEditorDelegate();
        String path;
        if (currentEditorDelegate != null) {
            path = currentEditorDelegate.getPath();
            if (new File(path).isFile()) {
                path = new File(path).getParent();
            }
        } else {
            path = Environment.getExternalStorageDirectory().getPath();
        }
        DialogNewFile dialog = DialogNewFile.newInstance(fileExtensions, path, file -> mTabManager.newTab(file));
        dialog.show(getSupportFragmentManager(), DialogNewFile.class.getSimpleName());
    }

    protected String[] getSupportedFileExtensions() {
        return new String[]{".txt"};
    }
/*
    public void saveAll(final int requestCode) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle(R.string.saving);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        SaveAllTask saveAllTask = new SaveAllTask(this, new SaveListener() {
            @Override
            public void onSavedSuccess() {
                dialog.dismiss();
                onSaveComplete(requestCode);
            }

            @Override
            public void onSaveFailed(Exception e) {
                dialog.dismiss();
                UIUtils.alert(IdeActivity.this, e.getMessage());
            }
        });
        saveAllTask.execute();
    }


 */

    /**
     * Called when save all files completed
     */
    protected void onSaveComplete(int requestCode) {

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

    protected EditorDelegate getCurrentEditorDelegate() {
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
            createNewFile();
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
            IOUtils.write(content.toString(), output);
            output.close();
            mTabManager.newTab(newFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    protected void openFile(String file) {
        openFile(file, null, 0);
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

        boolean textFile = false;
        for (Map.Entry<String, Mode> mode : Catalog.modes.entrySet()) {
            if (mode.getValue().accept(file.getPath(), file.getName(), "")) {
                textFile = true;
                break;
            }
        }
        if (!textFile) {
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
        SQLHelper.getInstance(IdeActivity.this).addRecentFile(file.getPath(), encoding);
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
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
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

    public String getCurrentLang() {
        EditorDelegate editorDelegate = getCurrentEditorDelegate();
        if (editorDelegate == null)
            return null;

        return editorDelegate.getLang();
    }

    protected void onShowKeyboard() {
        if (mTabLayout != null) {
            mTabLayout.setVisibility(View.GONE);
        }
        if (mTxtDocumentInfo != null) {
            mTxtDocumentInfo.setVisibility(VISIBLE);
        }
    }

    protected void onHideKeyboard() {
        if (mTabLayout != null) {
            mTabLayout.setVisibility(VISIBLE);
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

    public void initSearchPanel(final EditorDelegate editorDelegate) {
        final TableRow replaceRow = findViewById(R.id.replace_row);
        searchPanel = findViewById(R.id.search_panel);
        searchPanel.setVisibility(View.VISIBLE);
        mRegex = mPreferences.isRegexMode();
        mCaseSensitive = mPreferences.isMatchCaseMode();
        mWholeWordsOnly = mPreferences.isWholeWordsOnlyMode();
        replaceRow.setVisibility(mReplaceMode ? View.VISIBLE : View.GONE);
        TextView nextResult = findViewById(R.id.search_next_result);
        TextView prevResult = findViewById(R.id.search_prev_result);
        TextView replaceOption = findViewById(R.id.search_replace_option);
        final TextView allReplace = findViewById(R.id.all_replace_option);
        allReplace.setEnabled(false);
        allReplace.setTextColor(getResources().getColor(R.color.color_gray_text_disabled));
        TextView moreOption = findViewById(R.id.search_more_option);
        searchAdapter = new CustomAdapter(this, getSearchData());
        repaceAdapter = new CustomAdapter(this, getReplaceData());
        final AppCompatAutoCompleteTextView searchET = findViewById(R.id.search_text);
        final AppCompatAutoCompleteTextView replaceET = findViewById(R.id.replace_text);
        searchET.setAdapter(searchAdapter);
        replaceET.setAdapter(repaceAdapter);
        searchET.setText(findText);
        replaceET.setText(replaceText != null ? replaceText : "");
        moreOption.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(IdeActivity.this, view);
            popupMenu.inflate(R.menu.search_panel);
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.regex_check_menu) {
                    if (item.isChecked()) {
                        mRegex = false;
                    } else {
                        mRegex = true;
                        UIUtils.toast(IdeActivity.this, R.string.use_regex_to_find_tip);
                    }
                } else if (id == R.id.whole_words_only_menu) {
                    if (item.isChecked()) {
                        mWholeWordsOnly = false;
                    } else {
                        mWholeWordsOnly = true;
                    }
                } else if (id == R.id.whole_words_only_menu) {

                } else if (id == R.id.match_case_check) {
                    if (item.isChecked()) {
                        mCaseSensitive = false;
                    } else {
                        mCaseSensitive = true;
                    }
                } else if (id == R.id.close_search_panel) {
                    searchPanel.setVisibility(View.GONE);
                }
                return true;
            });
            popupMenu.getMenu().findItem(R.id.regex_check_menu).setChecked(mRegex);
            popupMenu.getMenu().findItem(R.id.whole_words_only_menu).setChecked(mWholeWordsOnly);
            popupMenu.getMenu().findItem(R.id.match_case_check).setChecked(mCaseSensitive);
            mPreferences.setRegexMode(mRegex);
            mPreferences.setWholeWordsOnlyMode(mWholeWordsOnly);
            mPreferences.setMatchCaseMode(mCaseSensitive);
            popupMenu.show();
        });
        nextResult.setOnClickListener(view -> {
            if (onFindButtonClick(searchET, replaceET, editorDelegate)) ;
            {
                doFind(ID_FIND_NEXT, grep, editorDelegate);
            }

        });
        prevResult.setOnClickListener(view -> {
            if (lastResults != null) {
                doFind(ID_FIND_PREV, grep, editorDelegate);
            }
        });
        replaceOption.setOnClickListener(view -> {
            mReplaceMode = true;
            replaceRow.setVisibility(View.VISIBLE);
            allReplace.setEnabled(true);
            allReplace.setTextColor(getThemeAccentColor(IdeActivity.this));
            if (replaceText != null) {
                if (lastResults != null) {
                    editorDelegate.getEditableText().replace(lastResults.start(), lastResults.end(), ExtGrep.parseReplacement(lastResults, replaceText));
                    lastResults = null;
                }
            }
        });
        allReplace.setOnClickListener(view -> grep.replaceAll(editorDelegate.getEditText(), replaceText));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    findText = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        replaceET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    replaceText = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private boolean onFindButtonClick(AppCompatAutoCompleteTextView find, AppCompatAutoCompleteTextView replce, EditorDelegate delegate) {
        //注意不要trim
        findText = find.getText().toString();
        if (TextUtils.isEmpty(findText)) {
            UIUtils.toast(this, R.string.cannot_be_empty);
            return false;
        }
        GrepBuilder builder = GrepBuilder.start();
        if (!mCaseSensitive) {
            builder.ignoreCase();
        }
        if (mWholeWordsOnly) {
            builder.wordRegex();
        }
        builder.setRegex(findText, mRegex);
        grep = builder.build();
        ITabDatabase database = SQLHelper.getInstance(IdeActivity.this);
//        database.addFindKeyword(findText, false);
      //  database.addFindKeyword(replaceText, true);
        /*
        grep = builder.build();
        SQLHelper dbHelper = new SQLHelper(this);

        searchAdapter.addValue(findText);
        dbHelper.clearFindKeywords(false);
        for (String item : searchAdapter.getDataList()) {
            dbHelper.addFindKeyword(item, false);
        }
        if (mReplaceMode) {
            repaceAdapter.addValue(replaceText);
            dbHelper.clearFindKeywords(true);
            for (String item : repaceAdapter.getDataList()) {
                dbHelper.addFindKeyword(item, true);
            }
        }

         */

        return findNext(delegate, grep);
    }

    private boolean findNext(final EditorDelegate delegate, final ExtGrep grep) {
        grep.grepText(ExtGrep.GrepDirect.NEXT,
                delegate.getEditableText(),
                delegate.getCursorOffset(),
                new TaskListener<MatcherResult>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onSuccess(MatcherResult match) {
                        if (match == null) {
                            UIUtils.toast(IdeActivity.this, R.string.find_not_found);
                            return;
                        }
                        //delegate.clearSelectable();
                        getCurrentEditorDelegate().getEditText().requestFocus();
                        delegate.addHighlight(match.start(), match.end());

                        //     getMainActivity().initSearchPanel(holder.mReplaceCheckBox.isChecked(),searchText,replaceText, grep, match, fragment);
                        //getMainActivity().startSupportActionMode(new FindTextActionModeCallback(replaceText, fragment, grep, match));
                    }

                    @Override
                    public void onError(Exception e) {
                        DLog.e(e);
                        UIUtils.toast(IdeActivity.this, e.getMessage());
                    }
                }
        );
        return true;
    }

    private void doFind(int id, ExtGrep grep, final EditorDelegate editorDelegate) {
        id = id == ID_FIND_PREV ? ID_FIND_PREV : ID_FIND_NEXT;
        if (grep != null) {
            grep.grepText(id == ID_FIND_PREV ? ExtGrep.GrepDirect.PREV : ExtGrep.GrepDirect.NEXT,
                    editorDelegate.getEditableText(),
                    editorDelegate.getCursorOffset(),
                    new TaskListener<MatcherResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onSuccess(MatcherResult match) {
                            if (match == null) {
                                UIUtils.toast(IdeActivity.this, R.string.find_not_found);
                                return;
                            }
                            editorDelegate.addHighlight(match.start(), match.end());
                            lastResults = match;
                        }

                        @Override
                        public void onError(Exception e) {
                            DLog.e(e);
                            UIUtils.toast(IdeActivity.this, e.getMessage());
                        }
                    });
        }

    }

    private List<String> getSearchData() {
        List<String> items = SQLHelper.getInstance(this).getFindKeywords(false);
        return items;
    }

    private List<String> getReplaceData() {
        List<String> items = SQLHelper.getInstance(this).getFindKeywords(true);
        return items;
    }
    public static int getThemeAccentColor (final Context context) {
        final TypedValue value = new TypedValue ();
        context.getTheme ().resolveAttribute (R.attr.toolbarTextColor, value, true);
        return value.data;
    }
    private class KeyBoardEventListener implements ViewTreeObserver.OnGlobalLayoutListener {
        IdeActivity activity;

        KeyBoardEventListener(IdeActivity activityIde) {
            this.activity = activityIde;
        }

        public void onGlobalLayout() {
            int i = 0;
            int navHeight = this.activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            navHeight = navHeight > 0 ? this.activity.getResources().getDimensionPixelSize(navHeight) : 0;
            int statusBarHeight = this.activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarHeight > 0) {
                i = this.activity.getResources().getDimensionPixelSize(statusBarHeight);
            }
            Rect rect = new Rect();
            activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
            if (activity.mDrawerLayout.getRootView().getHeight() - ((navHeight + i) + rect.height()) <= 0) {
                activity.onHideKeyboard();
            } else {
                activity.onShowKeyboard();
            }
        }
    }
}
