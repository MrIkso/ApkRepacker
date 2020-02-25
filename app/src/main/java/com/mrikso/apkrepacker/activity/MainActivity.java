package com.mrikso.apkrepacker.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.ProjectItem;
import com.mrikso.apkrepacker.adapter.RecyclerViewAdapter;
import com.mrikso.apkrepacker.filepicker.Utility;
import com.mrikso.apkrepacker.fragment.AppsFragment;
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
    public String apkProjectPatch;
    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private RecyclerViewAdapter adapter;
    private View empty;
    private List<ProjectItem> data;
    private BottomAppBar bottomAppBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int color = 0;
    private Preference preference;
    private File[] projects;
    private File root;

    final Fragment appsFragment = new AppsFragment();
    final FragmentManager fm = getSupportFragmentManager();

    public static MainActivity getInstance() {
        return Intance;
    }

    public static String getString(JSONObject json, String key) throws JSONException {
        String s;
        if (json.isNull(key))
            s = null;
        else
            s = json.getString(key);
        return s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intance = this;
        setContentView(R.layout.activity_main);
        App.setContext(this);
        preference = Preference.getInstance(this);
        empty = findViewById(R.id.empty_view);
        fab = findViewById(R.id.fab_bottom_appbar);
        mRecyclerView = findViewById(R.id.recycler_view_bottom_appbar);
        bottomAppBar = findViewById(R.id.bottom_App_bar);
        bottomAppBar.setFabCradleMargin(0f); //initial default value 17f
        bottomAppBar.replaceMenu(R.menu.global_menu_main);
        setSupportActionBar(bottomAppBar);
        InitPreference initPreference = new InitPreference();
        initPreference.init();
        preinitData();

    }

    private void preinitData(){
        if (!PermissionsUtils.checkAndRequestStoragePermissions(this))
            return;
        initData();
        initViews();
    }

    private void initData() {
        data = new ArrayList<>();
        root = new File(preference.getDecodingPath() + "/projects");
        if (!root.exists() && !root.mkdirs()) {
            return;
        }
        projects = root.listFiles();
        if (projects != null && projects.length >0) {
            for (File file : projects) {
                apkProjectPatch = readJson(new File(file, "apktool.json"));
                if (apkProjectPatch != null) {
                    ProjectItem myList = new ProjectItem(
                            file.getName(), AppUtils.getApkPackage(MainActivity.this, apkProjectPatch), file.getAbsolutePath(),
                            apkProjectPatch,AppUtils.getApkIcon(MainActivity.this, apkProjectPatch));
                    data.add(myList);
                } else {
                    ProjectItem myList = new ProjectItem(
                            file.getName(), "null", file.getAbsolutePath(),
                            ContextCompat.getDrawable(MainActivity.this, R.mipmap.ic_launcher));
                    data.add(myList);
                }
            }
        } else {
            empty.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    private String readJson(File file) {
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

    private String loadApkPatch(JSONObject jsonObject) throws JSONException {
        return getString(jsonObject, "apkFilePatch");
    }

    private void initViews() {

       // empty = findViewById(R.id.empty_view);
       // mRecyclerView = findViewById(R.id.recycler_view_bottom_appbar);

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
        adapter.setItems(data);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout_recycler_view);
        swipeRefreshLayout.setColorSchemeResources(R.color.google_blue, R.color.google_green, R.color.google_red, R.color.google_yellow);
        swipeRefreshLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            if (color > 4) {
                color = 0;
            }
            refleshAdpter();

            swipeRefreshLayout.setRefreshing(false);
        }, 100));

        mRecyclerView.setAdapter(adapter);
        setupUiClicks();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionsUtils.REQUEST_CODE_STORAGE_PERMISSIONS) {
            if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                UIUtils.alert(this, getResources().getString(R.string.common_error),
                        getResources().getString(R.string.signer_permissions_denied));
             finish();}
            else
               preinitData();
        }
    }

    private void setupUiClicks() {
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
            MainActivity.this.startActivity(intent);//, ActivityOptions.makeSceneTransitionAnimation
        });
    }

    public void refleshAdpter() {
      //  if(data!= null)
        //data.clear();
        initData();
        adapter.clear();
        adapter.setItems(data);
        mRecyclerView.setAdapter(adapter);
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
                fm.beginTransaction().replace(android.R.id.content, appsFragment).addToBackStack(null)
                        .commit();
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

    @Override
    protected void onResume() {
        super.onResume();
        try {
            projects = root.listFiles();//фикс некоректного обновления списка
            if (projects != null && projects.length > 0) {
                empty.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                refleshAdpter();
            } else {
                empty.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
