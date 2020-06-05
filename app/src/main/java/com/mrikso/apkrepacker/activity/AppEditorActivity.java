package com.mrikso.apkrepacker.activity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.FragmentAdapter;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.CompileFragment;
import com.mrikso.apkrepacker.fragment.FilesFragment;
import com.mrikso.apkrepacker.fragment.FindFragment;
import com.mrikso.apkrepacker.fragment.OnBackPressedListener;
import com.mrikso.apkrepacker.fragment.StringsFragment;
import com.mrikso.apkrepacker.patchengine.ParsePatch;
import com.mrikso.apkrepacker.ui.stringlist.DirectoryScanner;
import com.mrikso.apkrepacker.ui.stringlist.StringFile;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppEditorActivity extends BaseActivity {

    static AppEditorActivity Intance;
    final Fragment stringsFragment = new StringsFragment();
    final Fragment filesFragment = new FilesFragment();
    final FindFragment findFragment = new FindFragment();
    public ViewPager mViewPager;
    private FragmentAdapter mFragmentAdapter;
    private String projectPatch;
    private TabLayout mTabLayout;
    private Bundle bundle;
    private AppCompatImageButton buildApp;
    private AppCompatImageButton patchApp;
    private List<String> titles;
    private List<Fragment> fragments;
    private ArrayList<StringFile> files;

    public static AppEditorActivity getInstance() {
        return Intance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intance = this;
        setContentView(R.layout.activity_editor_apk);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
    }

    private void initView() {
        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.tab_pager);

        projectPatch = getIntent().getStringExtra("projectPatch");
        FileUtil.setProjectPath(projectPatch);
        bundle = new Bundle();
        bundle.putString("prjPatch", projectPatch);
        buildApp = findViewById(R.id.build_app);
        patchApp = findViewById(R.id.patch_app);

        mViewPager.setOffscreenPageLimit(2);
        titles = new ArrayList<>();
        titles.add(getString(R.string.menu_string));
        titles.add(getString(R.string.menu_files));
        titles.add(getString(R.string.menu_find_list));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));
        stringsFragment.setArguments(bundle);
        filesFragment.setArguments(bundle);
        fragments = new ArrayList<>();
        if (!stringFilesExists()) {
            fragments.add(stringsFragment);
        }
        fragments.add(filesFragment);
//        fragments.add(findFragment);
        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        //  mViewPager.addOnPageChangeListener(pageChangeListener);

        //   navigation = findViewById(R.id.bottom_navigation);
        //  navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


        String apkIconDrawableBase64 = getIntent().getStringExtra("apkFileIcon");
        String apkFileName = getIntent().getStringExtra("apkFileName");
        String apkPackageName = getIntent().getStringExtra("apkFilePackageName");

        TextView appName = findViewById(R.id.app_name);
        TextView appPkg = findViewById(R.id.app_pkg);
        ImageView appIcon = findViewById(R.id.app_icon);

        appName.setText(apkFileName);
        appPkg.setText(apkPackageName);
        appPkg.setVisibility(apkPackageName == null ? View.GONE : View.VISIBLE);
        appIcon.setImageDrawable(FileUtil.getProjectIconDrawable(apkIconDrawableBase64));

        buildApp.setOnClickListener(v -> buildApp());
        patchApp.setOnClickListener(v -> patchApp());
    }

    public void setSearchArguments(Bundle bundle) {
        findFragment.setArguments(bundle);
        mFragmentAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mFragmentAdapter);
        if (!fragments.contains(findFragment)) {
            fragments.add(findFragment);
            mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
            mViewPager.setAdapter(mFragmentAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
        }
    }

    private boolean stringFilesExists() {
        if (!new File(projectPatch, "resources.arsc").exists() | new File(projectPatch, "res").exists()) {
            DirectoryScanner scanner = new DirectoryScanner();
            files = scanner.findStringFiles(projectPatch);
        }
        return files.isEmpty();
    }

    private void patchApp() {
        new FilePickerDialog(this)
                .setTitleText(this.getResources().getString(R.string.select_patch))
                .setSelectMode(FilePickerDialog.MODE_SINGLE)
                .setSelectType(FilePickerDialog.TYPE_FILE)
                .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                .setBackCancelable(true)
                .setOutsideCancelable(true)
                .setDialogListener(this.getResources().getString(R.string.choose_button_label), this.getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                    @Override
                    public void onSelectedFilePaths(String[] filePaths) {
                        for (String file : filePaths) {
                            ParsePatch.openPatch(new File(file));
                        }
                    }

                    @Override
                    public void onCanceled() {
                    }
                })
                .show();
    }

    private void buildApp() {
        Fragment compileFragment = CompileFragment.newInstance(projectPatch);
        FragmentUtils.replace(compileFragment, this, android.R.id.content);
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment : fm.getFragments()) {
            if (fragment instanceof OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fragment;
                break;
            }
        }

        if (mTabLayout.getSelectedTabPosition() == 2) {
            removeFindFragment();
        } else if (backPressedListener != null) {
            backPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    private void removeFindFragment() {
        fragments.remove(findFragment);
        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(2);
    }
}
