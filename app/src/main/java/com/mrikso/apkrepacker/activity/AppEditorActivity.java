package com.mrikso.apkrepacker.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.FragmentAdapter;
import com.mrikso.apkrepacker.fragment.CompileFragment;
import com.mrikso.apkrepacker.fragment.FilesFragment;
import com.mrikso.apkrepacker.fragment.FindFragment;
import com.mrikso.apkrepacker.fragment.OnBackPressedListener;
import com.mrikso.apkrepacker.fragment.StringsFragment;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.util.ArrayList;
import java.util.List;

public class AppEditorActivity extends BaseActivity {

    final Fragment stringsFragment = new StringsFragment();
    final Fragment filesFragment = new FilesFragment();
    final FindFragment findFragment = new FindFragment();
 //   final Fragment compileFragment = new CompileFragment(projectPatch);
    final FragmentManager fm = getSupportFragmentManager();
    //private Fragment active = stringsFragment;
  //  private BottomNavigationView navigation;
    private String projectPatch, apkPath;
    private TabLayout mTabLayout;
    public ViewPager mViewPager;
    private Bundle bundle;
    private RelativeLayout buildApp;
    static AppEditorActivity Intance;
    FragmentAdapter mFragmentAdapter;

/*
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = item -> {
        switch (item.getItemId()) {
            case R.id.action_translation_apk:
                    stringsFragment.setArguments(bundle);
                loadFragment(stringsFragment, false);
                   // fm.beginTransaction().remove(stringsFragment);
                  //  fm.beginTransaction().replace(R.id.container, stringsFragment).commit();
                return true;
            case R.id.action_files:
                filesFragment.setArguments(bundle);
                loadFragment(filesFragment, true);
               // fm.beginTransaction().addToBackStack(null).replace(R.id.container, filesFragment).commit();
                return true;
            case R.id.action_find_list:
                    findFragment.setArguments(bundle);
                loadFragment(findFragment, false);
                   // fm.beginTransaction().remove(findFragment);
                   // fm.beginTransaction().replace(R.id.container, findFragment).commit();
                return true;
        }
        return false;
    };


 */

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
        apkPath = getIntent().getStringExtra("apkPatch");
        buildApp = findViewById(R.id.build_app);

        mViewPager.setOffscreenPageLimit(2);
        List<String> titles = new ArrayList<>();
        titles.add(getString(R.string.menu_string));
        titles.add(getString(R.string.menu_files));
        titles.add(getString(R.string.menu_find_list));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));
        stringsFragment.setArguments(bundle);
        filesFragment.setArguments(bundle);
        List<Fragment> fragments = new ArrayList<>();
        fragments.add(stringsFragment);
        fragments.add(filesFragment);
        fragments.add(findFragment);
        mFragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments, titles);
        mViewPager.setAdapter(mFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
      //  mViewPager.addOnPageChangeListener(pageChangeListener);

     //   navigation = findViewById(R.id.bottom_navigation);
      //  navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        Object[] info = AppUtils.getApkInfo(this, apkPath);
        TextView appName = findViewById(R.id.app_name);
        TextView appPkg = findViewById(R.id.app_pkg);
        ImageView appIcon = findViewById(R.id.app_icon);
        if (info != null){
            appName.setText(info[1].toString());
            appPkg.setText(info[2].toString());
            appIcon.setImageDrawable((Drawable)info[0]);
        }else {
            appName.setText("UNKNOWN");
            appPkg.setText("UNKNOWN");
            appIcon.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_launcher));
        }
            //fm.beginTransaction().remove(stringsFragment);

            //fm.beginTransaction().replace(R.id.container, stringsFragment).commit();
           // loadFragment(stringsFragment, false);
            buildApp.setOnClickListener(v -> buildApp());
    }
/*
    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        if(addToBackStack){
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }*/
public void setSearhArguments(Bundle bundle){
    findFragment.setArguments(bundle);
    mFragmentAdapter.notifyDataSetChanged();
    mViewPager.setAdapter(mFragmentAdapter);
}
    private void buildApp(){
        Fragment compileFragment = new CompileFragment(projectPatch);
      //  compileFragment.setArguments(bundle);
        fm.beginTransaction().addToBackStack(null).replace(android.R.id.content, compileFragment).commit();
      //  Runnable build = () -> SignUtil.loadKey(ctx, signTool -> new BuildTask(ctx, signTool).execute(new File(projectPatch)));
       // build.run();
        //new BuildTask(ctx).execute(new File(projectPatch));
    }

   @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        OnBackPressedListener backPressedListener = null;
        for (Fragment fragment: fm.getFragments()) {
            if (fragment instanceof  OnBackPressedListener) {
                backPressedListener = (OnBackPressedListener) fragment;
                break;
            }
        }

        if (backPressedListener != null) {
            backPressedListener.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    /*@Override
    public void onBackPressed(){
        super.onBackPressed();
    }*/
}
