package com.mrikso.apkrepacker.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.ProjectItem;
import com.mrikso.apkrepacker.adapter.RecyclerViewAdapter;
import com.mrikso.apkrepacker.fragment.AppsFragment;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.ui.prererence.InitPreference;
import com.mrikso.apkrepacker.ui.prererence.Preference;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.PermissionsUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    public static MainActivity Instance;
    private static RecyclerView mRecyclerView;
    private static RecyclerViewAdapter adapter;
    private static View empty;
    private static List<ProjectItem> data;
    private static Preference preference;
    private static File[] projects;
    private static File root;
    private static DialogFragment loadingDialog;
    private static boolean mShowLoading = true;
    final Fragment appsFragment = new AppsFragment();
    final FragmentManager fm = getSupportFragmentManager();
    private FloatingActionButton fab;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static MainActivity getInstance() {
        return Instance;
    }

    public static void initData() throws IOException {
        data = new ArrayList<>();
        root = new File(preference.getDecodingPath() + "/projects");
        new File(preference.getDecodingPath() + "/.nomedia").createNewFile();
        if (!root.exists() && !root.mkdirs()) {
            return;
        }
        projects = root.listFiles();
        if (projects != null && projects.length > 0) {
            for (File file : projects) {
                File dataFile = new File(file, "apktool.json");
                String apkFileIcon = FileUtil.readJson(dataFile, "apkFileIcon");
                String apkFileName = FileUtil.readJson(dataFile, "apkFileName");
                ProjectItem myList = new ProjectItem(apkFileIcon, (apkFileName != null) ? apkFileName : file.getName(), FileUtil.readJson(dataFile, "apkFilePackageName"), file.getAbsolutePath(), FileUtil.readJson(dataFile, "apkFilePatch"), FileUtil.readJson(dataFile, "versionName"), FileUtil.readJson(dataFile, "versionCode"));
                data.add(myList);
            }
        } else {
            empty.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    public void refreshAdapter(boolean showLoading) {
        mShowLoading = showLoading;
        try {
            projects = root.listFiles();
            if (projects != null && projects.length > 0) {
                empty.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                new getProjectsTask().execute();
            } else {
                empty.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showProgress() {
        Bundle args = new Bundle();
        args.putString(ProgressDialogFragment.TITLE, getInstance().getResources().getString(R.string.dialog_loading_projects));
        args.putString(ProgressDialogFragment.MESSAGE, getInstance().getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, true);
        loadingDialog = ProgressDialogFragment.newInstance();
        loadingDialog.setArguments(args);
        loadingDialog.show(getSupportFragmentManager(), ProgressDialogFragment.TAG);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        initViews();
        refreshAdapter(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        setContentView(R.layout.activity_main);
        App.setContext(this);
        preference = Preference.getInstance(this);
        empty = findViewById(R.id.empty_view);
        fab = findViewById(R.id.fab_bottom_appbar);
        mRecyclerView = findViewById(R.id.recycler_view_bottom_appbar);
        BottomAppBar bottomAppBar = findViewById(R.id.bottom_App_bar);
//        bottomAppBar.setFabCradleMargin(0f); //initial default value 17f
        bottomAppBar.replaceMenu(R.menu.global_menu_main);
        setSupportActionBar(bottomAppBar);
        new InitPreference().init();
        preinitData();
    }

    private void preinitData() {
        if (!PermissionsUtils.checkAndRequestStoragePermissions(this))
            return;
        new getProjectsTask().execute();
        initViews();
    }

    private void initViews() {
        if (AppUtils.getScreenWidthDp(this) >= 1200) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
        } else if (AppUtils.getScreenWidthDp(this) >= 800) {
            mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        } else {
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        adapter = new RecyclerViewAdapter(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_recycler_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);
        swipeRefreshLayout.setOnRefreshListener(() -> refreshAdapter(false));

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                findViewById(R.id.app_bar).setSelected(mRecyclerView.canScrollVertically(-1));
                if (dy < 0 && !fab.isShown()) {
                    fab.show();
                } else if (dy > 0 && fab.isShown()) {
                    fab.hide();
                }
            }
        });
        setupUiClicks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionsUtils.REQUEST_CODE_STORAGE_PERMISSIONS) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                UIUtils.alert(this, getResources().getString(R.string.common_error), getResources().getString(R.string.signer_permissions_denied));
                finish();
            } else {
                preinitData();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.global_menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_apps:
                FragmentUtils.replace(appsFragment, fm, android.R.id.content, AppsFragment.TAG);
                break;
            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                break;
            case R.id.menu_exit:
                finish();
                break;
            case R.id.menu_about:
                intent = new Intent(this, AboutActivity.class);
                this.startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupUiClicks() {
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
            MainActivity.this.startActivity(intent);
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class getProjectsTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                initData();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mShowLoading) {
                showProgress();
            } else {
                mShowLoading = true;
            }
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            adapter.clear();
            adapter.setItems(data);
            mRecyclerView.setAdapter(adapter);
            loadingDialog.dismissAllowingStateLoss();
            loadingDialog.dismiss();
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}