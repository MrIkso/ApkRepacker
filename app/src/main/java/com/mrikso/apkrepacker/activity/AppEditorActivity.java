package com.mrikso.apkrepacker.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.FragmentAdapter;
import com.mrikso.apkrepacker.fragment.CompileFragment;
import com.mrikso.apkrepacker.fragment.FilesFragment;
import com.mrikso.apkrepacker.fragment.FindFragment;
import com.mrikso.apkrepacker.fragment.OnBackPressedListener;
import com.mrikso.apkrepacker.fragment.PatcherFragment;
import com.mrikso.apkrepacker.fragment.StringsFragment;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.ui.stringlist.DirectoryScanner;
import com.mrikso.apkrepacker.ui.stringlist.StringFile;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.ProjectUtils;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppEditorActivity extends BaseActivity {

    static AppEditorActivity Intance;
    final Fragment stringsFragment = new StringsFragment();
    final Fragment filesFragment = new FilesFragment();
    final FindFragment findFragment = new FindFragment();
    public ViewPager2 mViewPager;
    private FragmentAdapter mFragmentAdapter;
    private String projectPatch;
    private TabLayout mTabLayout;
    private Bundle bundle;
    private AppCompatImageButton buildApp;
    private AppCompatImageButton patchApp;
    private List<String> titles;
    private List<Fragment> fragments;
    private ArrayList<StringFile> files;
    private Context mContext;

    public static AppEditorActivity getInstance() {
        return Intance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intance = this;
       // onNewIntent(getIntent());
        setContentView(R.layout.activity_editor_apk);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
    }

    private void initView() {
        mContext = this;
        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.tab_pager);

        projectPatch = getIntent().getStringExtra("projectPatch");
        ProjectUtils.setProjectPath(projectPatch);
        bundle = new Bundle();
        bundle.putString("prjPatch", projectPatch);
        buildApp = findViewById(R.id.build_app);
        patchApp = findViewById(R.id.patch_app);

        mViewPager.setOffscreenPageLimit(2);
        titles = new ArrayList<>();
        fragments = new ArrayList<>();
        /* проверяем ли есть строки в проекте и тогда добавляем фрагмент */
        if (!stringFilesExists()) {
            titles.add(getString(R.string.menu_string));
            titles.add(getString(R.string.menu_files));
            titles.add(getString(R.string.menu_find));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));
            stringsFragment.setArguments(bundle);
            fragments.add(stringsFragment);
        } else {
            titles.add(getString(R.string.menu_files));
            titles.add(getString(R.string.menu_find));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
        }

        filesFragment.setArguments(bundle);
        fragments.add(filesFragment);
        fragments.add(findFragment);
        mFragmentAdapter = new FragmentAdapter(this, fragments, titles);
        mViewPager.setAdapter(mFragmentAdapter);
        new TabLayoutMediator(mTabLayout, mViewPager, ((tab, position) -> tab.setText(mFragmentAdapter.getPageTitle(position)))).attach();
        // mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if(tab.getPosition() >= 1){
                    StringUtils.hideKeyboard(AppEditorActivity.this);
                }
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
              //  mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
        });

        String apkIconDrawableBase64 = getIntent().getStringExtra("apkFileIcon");
        String apkFileName = getIntent().getStringExtra("apkFileName");
        String apkPackageName = getIntent().getStringExtra("apkFilePackageName");

        TextView appName = findViewById(R.id.app_name);
        TextView appPkg = findViewById(R.id.app_pkg);
        ImageView appIcon = findViewById(R.id.app_icon);

        appName.setText(apkFileName);
        appPkg.setText(apkPackageName);
        appPkg.setVisibility(apkPackageName == null ? View.GONE : View.VISIBLE);
        appIcon.setImageDrawable(ProjectUtils.getProjectIconDrawable(apkIconDrawableBase64, this));

        buildApp.setOnClickListener(v -> buildApp());
        patchApp.setOnClickListener(v -> patchApp());
    }

    /*public void setSearchArguments(Bundle bundle) {
        findFragment.setArguments(bundle);
        //mFragmentAdapter.notifyDataSetChanged();
        //mViewPager.setAdapter(mFragmentAdapter);
        if (!fragments.contains(findFragment)) {
            fragments.add(findFragment);
           *//* mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
            mViewPager.setAdapter(mFragmentAdapter);
            mTabLayout.setupWithViewPager(mViewPager);*//*
        }
        // fragments.add(findFragment);
        mViewPager.getAdapter().notifyDataSetChanged();
    }*/

    private boolean stringFilesExists() {
        if (!new File(projectPatch, "resources.arsc").exists() | new File(projectPatch, "res").exists()) {
            DirectoryScanner scanner = new DirectoryScanner();
            files = scanner.findStringFiles(projectPatch);
        }
        return files.isEmpty();
    }

    private void patchApp() {
        StringUtils.hideKeyboard(this);
        PatcherFragment patcherFragment = PatcherFragment.newInstance();
        FragmentUtils.add(patcherFragment, getSupportFragmentManager(), android.R.id.content, PatcherFragment.TAG);
    }

    private void buildApp() {
        StringUtils.hideKeyboard(this);
        if (PreferenceHelper.getInstance(mContext).isConfirmBuild()) {
            UIUtils.showConfirmDialog(mContext, getString(R.string.confirm_build_title), new UIUtils.OnClickCallback() {
                @Override
                public void onOkClick() {
                    showCompileFragment();
                }
            });
        } else {
            showCompileFragment();
        }

    }

    private void showCompileFragment() {
        Fragment compileFragment = CompileFragment.newInstance(projectPatch);
        FragmentUtils.replace(compileFragment, this, android.R.id.content);
    }

    /*@Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey("showCompileFragment")) {
                Toast.makeText(getBaseContext(), "lol", Toast.LENGTH_SHORT).show();
                //showCompileFragment();
            }
        }
    }*/

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment patcher = fm.findFragmentByTag(PatcherFragment.TAG);
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment : fm.getFragments()) {
            if (fragment instanceof OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fragment;
                break;
            }
        }

       /* if (mTabLayout.getSelectedTabPosition() == 2) {
            removeFindFragment();
        }*/
        if (patcher != null) {
            FragmentUtils.remove(patcher);
        }
        //support back pressed on files tab
        else if (mViewPager.getCurrentItem() == 0) {
            if (backPressedListener != null) {
                backPressedListener.onBackPressed();
            }
        }
        //support back pressed on files tab
        else if (mViewPager.getCurrentItem() == 1) {
            if (backPressedListener != null) {
                backPressedListener.onBackPressed();
            }
        }
        //support back pressed on find tab
        else if (mViewPager.getCurrentItem() == 2) {
           // if (backPressedListener != null) {
                findFragment.onBackPressed();
           // }
        } else {
            super.onBackPressed();
        }
    }

    /*private void removeFindFragment() {
        fragments.remove(findFragment);
        mFragmentAdapter.notifyDataSetChanged();
        //mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
         mViewPager.setAdapter(mFragmentAdapter);
       mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.setCurrentItem(2);
    }*/
}
