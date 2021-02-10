package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import android.widget.Toast;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.EditorPreferences;
import com.jecelyin.editor.v2.common.ClusterCommand;
import com.jecelyin.editor.v2.common.Command;
import com.jecelyin.editor.v2.dialog.CharsetsDialog;
import com.jecelyin.editor.v2.dialog.GotoLineDialog;
import com.jecelyin.editor.v2.dialog.LangListDialog;
import com.jecelyin.editor.v2.io.FileEncodingDetector;
import com.jecelyin.editor.v2.manager.EditorPager;
import com.jecelyin.editor.v2.manager.MenuManager;
import com.jecelyin.editor.v2.manager.TabManager;
import com.jecelyin.editor.v2.widget.SymbolBarLayout;

import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.database.JsonDatabase;
import com.mrikso.apkrepacker.database.entity.Project;
import com.mrikso.apkrepacker.ide.editor.EditorDelegate;
import com.mrikso.apkrepacker.ide.file.FileManager;
import com.mrikso.apkrepacker.ide.file.SaveListener;
import com.mrikso.apkrepacker.task.Smali2JavaTask;
import com.mrikso.apkrepacker.ui.projectview.ProjectTreeStructureFragment;
import com.mrikso.apkrepacker.ui.projectview.treeview.interfaces.FileChangeListener;
import com.mrikso.apkrepacker.utils.AppExecutor;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.apkrepacker.utils.common.DLog;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;

public class TextEditorActivity extends BaseActivity implements SharedPreferences.OnSharedPreferenceChangeListener, FileChangeListener {

    private static final int RC_OPEN_FILE = 1;
    private final static int RC_SAVE = 3;
    private static final int RC_PERMISSION_STORAGE = 2;
    private static final int RC_SETTINGS = 5;

    public Toolbar mToolbar;
    public LinearLayout mLoadingLayout;
    public EditorPager mTabPager;
    public NavigationView mProjectNavigationView;
    public DrawerLayout mDrawerLayout;
    public RecyclerView mTabRecyclerView;

    public SymbolBarLayout mSymbolBarLayout;

    private TabManager tabManager;

    private EditorPreferences mEditorPreferences;
    private ClusterCommand clusterCommand;

    private long mExitTime;

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            DLog.d(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_editor);
        mEditorPreferences = EditorPreferences.getInstance(this);
        ;

        mToolbar = findViewById(R.id.toolbar);
        mLoadingLayout = findViewById(R.id.loading_layout);
        mTabPager = findViewById(R.id.tab_pager);
        mProjectNavigationView = findViewById(R.id.right_navigation_view);
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

        mTabRecyclerView = findViewById(R.id.open_tabs_list);
        mSymbolBarLayout = findViewById(R.id.symbolBarLayout);
        mSymbolBarLayout.setOnSymbolCharClickListener((v, text) -> insertText(text));

        bindPreferences();
        setScreenOrientation();

        start();
    }

    private void bindPreferences() {
        // mDrawerLayout.setKeepScreenOn(pref.isKeepScreenOn());
        //  mDrawerLayout.setDrawerLockMode(pref.isEnabledDrawers() ? DrawerLayout.LOCK_MODE_UNDEFINED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mSymbolBarLayout.setVisibility(mEditorPreferences.isReadOnly() ? View.GONE : View.VISIBLE);
        mSymbolBarLayout.setVisibility(mEditorPreferences.isHidePanel() ? View.GONE : View.VISIBLE);
        //bind other preference
//        pref.getSharedPreferences().registerOnSharedPreferenceChangeListener(this); //不能这样使用，无法监听
//        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        mEditorPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (mToolbar == null)
            return;
        switch (key) {
            case EditorPreferences.KEY_ENABLE_HIGHLIGHT:
                Command command = new Command(Command.CommandEnum.HIGHLIGHT);
                command.object = mEditorPreferences.isHighlight() ? null : "None";
                doClusterCommand(command);
                break;
            case EditorPreferences.KEY_SCREEN_ORIENTATION:
                setScreenOrientation();
                break;
            case EditorPreferences.KEY_READ_ONLY:
                mSymbolBarLayout.setVisibility(mEditorPreferences.isReadOnly() ? View.GONE : View.VISIBLE);
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

    private void start() {
        ((ViewGroup) mLoadingLayout.getParent()).removeView(mLoadingLayout);

//                inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mTabPager.setVisibility(View.VISIBLE);

        initUI();
    }

    private void initUI() {
        mTabRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        initToolbar();

        AppExecutor.getInstance().getDiskIO().execute(() -> {
            initLeftNavigationView(mProjectNavigationView);
        });

        processIntent();
    }

    private void initToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        AppCompatTextView tabHeaderTitle = findViewById(R.id.title_name);
        tabHeaderTitle.setText(R.string.code_editor);

        AppCompatTextView tabSubTitle = findViewById(R.id.project_name);
        tabSubTitle.setText(ProjectUtils.getProjectName());
        AppCompatImageButton imageButton = findViewById(R.id.popup_menu);
        imageButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.inflate(R.menu.tabs_menu);
            popupMenu.setOnMenuItemClickListener(this::onOptionsItemSelected);
            MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), v);
            menuHelper.setForceShowIcon(true);
            menuHelper.show();
        });
        tabManager = new TabManager(this);
    }

    protected void initLeftNavigationView(@NonNull NavigationView nav) {
        String tag = ProjectTreeStructureFragment.TAG;
        ViewGroup viewGroup = nav.findViewById(R.id.right_navigation_view);
        viewGroup.removeAllViews();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.right_navigation_view, new ProjectTreeStructureFragment(), tag).commit();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        processIntent();
    }

    private void processIntent() {
        Intent intent = getIntent();
        String mPath = intent.getStringExtra("filePath");
        int mOffset = intent.getIntExtra("offset", 1);
        if (mOffset != 0) {
            //  DLog.d("Offset" + mOffset);
            openFile(mPath, FileEncodingDetector.DEFAULT_ENCODING, mOffset);
        } else {
            openFile(mPath);
        }
    }

    public void setMenuStatus(@IdRes int menuResId, int status) {
       /*MenuItem menuItem = mToolbar.getMenu().findItem(menuResId);
        if (menuItem == null) {
            throw new RuntimeException("Can't find a menu item");
        }*/
        /*Drawable icon = menuItem.getIcon();
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
        }*/
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.code_editor_menu, menu);
        if (menu instanceof MenuBuilder) {  //To display icon on overflow menu
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onMenuClick(item.getItemId());
        return super.onOptionsItemSelected(item);
    }

    private void onMenuClick(int id) {
        Command.CommandEnum commandEnum;

        closeMenu();

        switch (id) {
            case R.id.action_explore:
                break;
            case R.id.action_save:
                doClusterCommand(new Command(Command.CommandEnum.SAVE));
                break;
            case R.id.action_undo:
                doClusterCommand(new Command(Command.CommandEnum.UNDO));
                break;
            case R.id.action_redo:
                doClusterCommand(new Command(Command.CommandEnum.REDO));
                break;
            case R.id.action_find_replace:
                doClusterCommand(new Command(Command.CommandEnum.FIND));
                break;
            case R.id.action_goto_top:
                doClusterCommand(new Command(Command.CommandEnum.GOTO_TOP));
                break;
            case R.id.action_goto_end:
                doClusterCommand(new Command(Command.CommandEnum.GOTO_END));
                break;
            case R.id.action_goto_line:
                EditorDelegate editorDelegate = getCurrentEditorDelegate();
                if (editorDelegate != null) {
                    new GotoLineDialog(this, editorDelegate).show();
                }
                break;
            case R.id.action_smali_java:
                if (AppUtils.apiIsAtLeast(Build.VERSION_CODES.O)) {
                    String smali = getCurrentEditorDelegate().getPath();
                    if (smali != null) {
                        if (smali.toLowerCase().endsWith(".smali")) {
                            UIUtils.toast(this, "Decompiling");
                            Runnable runnable = () -> new Smali2JavaTask(this).execute(
                                    new File(smali));
                            runnable.run();
                        } else {
                            UIUtils.toast(this, "Works only smali files!");
                        }
                    }
                } else {
                    UIUtils.alert(this, getString(R.string.attention_title), getString(R.string.jadx_doesnt_support_on_this_prone));
                }
                break;
            case R.id.action_highlight:
                new LangListDialog(this).show();
                break;
            case R.id.action_readonly:
                boolean readOnly = !mEditorPreferences.isReadOnly();
                mEditorPreferences.setReadOnly(readOnly);
                doClusterCommand(new Command(Command.CommandEnum.READONLY_MODE));
                break;
            case R.id.action_hide_symbol_panel:
                boolean hidePanel = !mEditorPreferences.isHidePanel();
                mEditorPreferences.setHidePanel(hidePanel);
                break;
            case R.id.action_encoding:
                new CharsetsDialog(this).show();
                break;
            case R.id.action_info:
                doClusterCommand(new Command(Command.CommandEnum.DOC_INFO));
                break;
            case R.id.action_editor_setting:
                EditorSettingsActivity.open(this, RC_SETTINGS);
                break;
            case R.id.action_exit:
                if (tabManager != null)
                    tabManager.onDestroy();
                break;
            case R.id.action_close_unchanged:
                if (tabManager != null)
                    tabManager.closeAllUnchanged();
                break;
            case R.id.action_save_all:
                commandEnum = Command.CommandEnum.SAVE;
                Command command = new Command(commandEnum);
                command.args.putBoolean(EditorDelegate.KEY_CLUSTER, true);
                command.object = new SaveListener() {

                    @Override
                    public void onSavedSuccess() {
                        doNextCommand();
                    }

                    @Override
                    public void onSaveFailed(Exception e) {
                        UIUtils.alert(getBaseContext(), e.getMessage());
                    }
                };
                doClusterCommand(command);
                break;
        }
    }

    public void closeMenu() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        }
    }

    private void hideSoftInput() {
        doCommand(new Command(Command.CommandEnum.HIDE_SOFT_INPUT));
    }

    private void showSoftInput() {
        doCommand(new Command(Command.CommandEnum.SHOW_SOFT_INPUT));
    }

    public void doClusterCommand(Command command) {
        clusterCommand = tabManager.getEditorAdapter().makeClusterCommand();
        clusterCommand.setCommand(command);
        clusterCommand.doNextCommand();
    }

    public void doNextCommand() {
        if (clusterCommand == null)
            return;
        clusterCommand.doNextCommand();
    }

    public void doCommand(Command command) {
        clusterCommand = null;
        EditorDelegate editorDelegate = getCurrentEditorDelegate();

        if (editorDelegate != null) {
            editorDelegate.doCommand(command);

            if (command.what == Command.CommandEnum.HIGHLIGHT) {
                //  mToolbar.setTitle(editorDelegate.getToolbarText());
            }
            //mToolbar.setSubtitle(editorDelegate.getToolbarSubText());
        }
    }

    public EditorDelegate getCurrentEditorDelegate() {
        if (tabManager == null || tabManager.getEditorAdapter() == null)
            return null;
        return tabManager.getEditorAdapter().getCurrentEditorDelegate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;
        /*switch (requestCode) {
            case RC_OPEN_FILE:
                if (data == null)
                    break;
                openFile(FileExplorerActivity.getFile(data), FileExplorerActivity.getFileEncoding(data), data.getIntExtra("offset", 0));
                break;
            case RC_SAVE:
                String file = FileExplorerActivity.getFile(data);
                String encoding = FileExplorerActivity.getFileEncoding(data);
                tabManager.getEditorAdapter().getCurrentEditorDelegate().saveTo(new File(file), encoding);
                break;
            case RC_SETTINGS:
                break;
        }*/
    }

    private void openText(CharSequence content) {
        if (TextUtils.isEmpty(content))
            return;
        tabManager.newTab(content);
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
            tabManager.newTab(newFile);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void openFile(String file) {
        openFile(file, null, 0);
    }

    public void openFile(String file, String encoding, int offset) {
        if (TextUtils.isEmpty(file))
            return;
        File f = new File(file);
        if (!f.isFile()) {
            UIUtils.toast(this, R.string.file_not_exists);
            return;
        }
        if (!tabManager.newTab(f, offset, encoding))
            return;
        JsonDatabase.getInstance(this).addRecentFile(file, encoding);
    }

    public void insertText(CharSequence text) {
        if (text == null)
            return;
        Command c = new Command(Command.CommandEnum.INSERT_TEXT);
        c.object = text;
        doCommand(c);
    }

    public TabManager getTabManager() {
        return tabManager;
    }

   /* @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            if (mDrawerLayout != null) {
                if (mDrawerLayout.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawerLayout.closeDrawer(Gravity.RIGHT);
                    return true;
                }
            }

            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                UIUtils.toast(this, R.string.press_again_will_exit);
                mExitTime = System.currentTimeMillis();
                return true;
            } else {
                return tabManager == null || tabManager.closeAllTabAndExitApp();
            }
        }
        return super.onKeyDown(keyCode, event);
    }*/

    @Override
    public void onBackPressed() {
        if (closeDrawers()) {
            return;
        }
        if (tabManager.onDestroy()) {
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

    public String getCurrentLang() {
        EditorDelegate editorDelegate = getCurrentEditorDelegate();
        if (editorDelegate == null)
            return null;

        return editorDelegate.getEditText().getLang();
    }

    public RecyclerView getTabRecyclerView() {
        return mTabRecyclerView;
    }

    @Override
    public void onFileDeleted(File deleted) {

    }

    @Override
    public void onFileCreated(File newFile) {

    }

    @Override
    public void doOpenFile(String toEdit) {
        openFile(toEdit);
    }
}

