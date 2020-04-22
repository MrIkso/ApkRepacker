package com.mrikso.apkrepacker.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.duy.common.DLog;
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
import com.mrikso.apkrepacker.utils.PermissionsUtils;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    public static MainActivity Intance;
    public static String apkProjectPatch;
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
    private BottomAppBar bottomAppBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int color = 0;
    private Bundle bundle;
    public GetProjectsTask projectsTask;

    public static MainActivity getInstance() {
        return Intance;
    }

    public static void initData() throws IOException {
        data = new ArrayList<>();
        root = new File(preference.getDecodingPath() + "/projects");
        if (!root.exists() && !root.mkdirs()) {
            return;
        }
        File nomedia = new File(preference.getDecodingPath()+ "/.nomedia");
        if(nomedia.exists()){
            nomedia.createNewFile();
        }
        projects = root.listFiles();
        if (projects != null && projects.length > 0) {
            for (File file : projects) {
                apkProjectPatch = readJson(new File(file, "apktool.json"));
                if (apkProjectPatch != null) {
                    ProjectItem myList = new ProjectItem(AppUtils.getApkName(getInstance(), apkProjectPatch), AppUtils.getApkPackage(getInstance(), apkProjectPatch), file.getAbsolutePath(), apkProjectPatch, AppUtils.getApkIcon(getInstance(), apkProjectPatch));
                    data.add(myList);
                } else {
                    ProjectItem myList = new ProjectItem(file.getName(), null, file.getAbsolutePath(), ContextCompat.getDrawable(getInstance(), R.drawable.default_app_icon));
                    data.add(myList);
                }
            }
        }
//        else {
//            empty.setVisibility(View.VISIBLE);
//            mRecyclerView.setVisibility(View.GONE);
//        }
    }

    private static String readJson(File file) {
        String content = null;
        try {
            content = IOUtils.toString(new FileInputStream(file));
            JSONObject json = new JSONObject(content);
            return loadApkPatch(json);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String loadApkPatch(JSONObject jsonObject) throws JSONException {
        return getString(jsonObject, "apkFilePatch");
    }

    public static String getString(JSONObject json, String key) throws JSONException {
        String s;
        if (json.isNull(key))
            s = null;
        else
            s = json.getString(key);
        return s;
    }

    public void refreshAdapter(boolean showLoading) {
        mShowLoading = showLoading;
        try {
            projects = root.listFiles();//фикс некоректного обновления списка
            if (projects != null && projects.length > 0) {
                empty.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                projectsTask.execute();
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
        refreshAdapter(false);
        //Toast.makeText(Intance, "onConfigurationChanged", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bundle = savedInstanceState;
        Intance = this;
        setContentView(R.layout.activity_main);
        App.setContext(this);
        preference = Preference.getInstance(this);
        empty = findViewById(R.id.empty_view);
//        loadingView = findViewById(R.id.loading_view);
        fab = findViewById(R.id.fab_bottom_appbar);
        mRecyclerView = findViewById(R.id.recycler_view_bottom_appbar);
        bottomAppBar = findViewById(R.id.bottom_App_bar);
//        bottomAppBar.setFabCradleMargin(0f); //initial default value 17f
        bottomAppBar.replaceMenu(R.menu.global_menu_main);
        setSupportActionBar(bottomAppBar);
        /*        InitPreference initPreference = */
        new InitPreference().init();
//        initPreference.init();
        preinitData();
    }

    private void preinitData() {
        if (!PermissionsUtils.checkAndRequestStoragePermissions(this))
            return;
//        initData();
        projectsTask = new GetProjectsTask();
        projectsTask.execute();
        initViews();
    }

    private void initViews() {
        if (AppUtils.getScreenWidthDp(this) >= 1200) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else if (AppUtils.getScreenWidthDp(this) >= 800) {
            final GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
            mRecyclerView.setLayoutManager(gridLayoutManager);
        } else {
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(linearLayoutManager);
        }
        adapter = new RecyclerViewAdapter(this);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_recycler_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            if (color > 4) {
                color = 0;
            }
            refreshAdapter(false);

            swipeRefreshLayout.setRefreshing(false);
        }, 10));

        mRecyclerView.setAdapter(adapter);
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
                fm.beginTransaction().setCustomAnimations(R.anim.anim_in_fragment, R.anim.anim_out_fragment).replace(android.R.id.content, appsFragment).addToBackStack(null).commit();
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
            MainActivity.this.startActivity(intent);//, ActivityOptions.makeSceneTransitionAnimation
        });
    }

    private class GetProjectsTask extends AsyncTask<Void, Integer, Void> {

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
            if (mShowLoading)
                 showProgress();
//            Toast.makeText(getInstance(), "Loading...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
//            Toast.makeText(getInstance(), "Done!!!", Toast.LENGTH_SHORT).show();
            adapter.clear();
            adapter.setItems(data);
            mRecyclerView.setAdapter(adapter);
            loadingDialog.dismissAllowingStateLoss();
        }
    }
}