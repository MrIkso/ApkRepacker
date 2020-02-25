package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.AppsOptionsItemDialogFragment;
import com.mrikso.apkrepacker.task.DecodeTask;
import com.mrikso.apkrepacker.ui.appslist.AppsAdapter;
import com.mrikso.apkrepacker.ui.appslist.AppsViewModel;
import com.mrikso.apkrepacker.ui.prererence.Preference;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.PackageMeta;

import org.apache.commons.io.FileUtils;

import java.io.File;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AppsFragment extends Fragment implements AppsAdapter.OnItemInteractionListener, AppsOptionsItemDialogFragment.ItemClickListener {

    private static final String TAG = "AppsFragment";
    private RecyclerView appsList;
    private AppsViewModel mViewModel;
    private AppsAdapter appsAdapter;
    private EditText mEditTextSearch;
    private Chip mChipFilterSplitsOnly;
    private Chip mChipFilterIncludeSystemApps;
    private Context context;
    private PackageMeta current;

    public AppsFragment() {
        // Required empty public constructor
    }

    public static AppsFragment newInstance(String param1, String param2) {
        AppsFragment fragment = new AppsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        context = view.getContext();
        mViewModel = ViewModelProviders.of(this).get(AppsViewModel.class);
        //  loading = view.findViewById(R.id.app_packages_loading);
        appsList = view.findViewById(R.id.app_packages);
        appsList.setLayoutManager(new LinearLayoutManager(App.getContext()));
        appsList.getRecycledViewPool().setMaxRecycledViews(0, 24);
        appsAdapter = new AppsAdapter(App.getContext());
        // mViewModel.getPackages().observe(getViewLifecycleOwner(), appsAdapter::setData);

        appsAdapter.setInteractionListener(this);
        new FastScrollerBuilder(appsList).build();

        initData();
        setupToolbar(view);
        return view;
    }

    private void initData() {
        mViewModel.getPackages().observe(getViewLifecycleOwner(), appsAdapter::setData);
        appsList.setAdapter(appsAdapter);
        // Log.i("df", String.valueOf(appsAdapter.isChanged()));
    /*if (appsAdapter.isChanged()) {
        appsList.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
    }*/
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
    }

    private void setupToolbar(View view) {
        //Search
        mEditTextSearch = view.findViewById(R.id.et_search);
        mChipFilterSplitsOnly = view.findViewById(R.id.chip_filter_splits);
        mChipFilterIncludeSystemApps = view.findViewById(R.id.chip_filter_system);

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filterPackages();
            }
        });

        CompoundButton.OnCheckedChangeListener onCheckedChangeListener = (group, checkedId) -> filterPackages();
        mChipFilterSplitsOnly.setOnCheckedChangeListener(onCheckedChangeListener);
        mChipFilterIncludeSystemApps.setOnCheckedChangeListener(onCheckedChangeListener);
        filterPackages();
    }

    private void filterPackages() {
        mViewModel.filter(mEditTextSearch.getText().toString(), mChipFilterSplitsOnly.isChecked(), mChipFilterIncludeSystemApps.isChecked());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onBackupButtonClicked(PackageMeta packageMeta) {
        current = packageMeta;
        AppsOptionsItemDialogFragment fragment = AppsOptionsItemDialogFragment.newInstance();
        fragment.show(getChildFragmentManager(), AppsOptionsItemDialogFragment.TAG);
    }

    @Override
    public void onAppsItemClick(Integer item) {
        Preference preference = Preference.getInstance(context);
        switch (item) {
            case R.id.decompile_app:
                File app = FileUtil.createBackupFile(current, preference.getDecodingPath());
                try {
                    ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(current.packageName, 0);
                    FileUtils.copyFile(new File(applicationInfo.publicSourceDir), app);
                    DecompileFragment decompileFragment =  DecompileFragment.newInstance(current.label, app.getAbsolutePath(), true);
                    ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(android.R.id.content, decompileFragment).commit();
                } catch (Exception e) {
                    UIUtils.toast(App.getContext(), R.string.toast_error_in_decompile_installed_app);
                    Log.e(TAG, "Error in decompile installed app");
                    e.printStackTrace();
                }
                break;
            case R.id.simple_edit_apk:
                File selectedApk = FileUtil.createBackupFile(current, preference.getDecodingPath());
                try {
                    ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(current.packageName, 0);
                    FileUtils.copyFile(new File(applicationInfo.publicSourceDir), selectedApk);
                    SimpleEditorFragment simpleEditorFragment = new SimpleEditorFragment(selectedApk);
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction().addToBackStack(null).replace(android.R.id.content, simpleEditorFragment).commit();
                }catch (Exception e){
                    UIUtils.toast(App.getContext(), R.string.toast_error_in_simple_edit_installed_app);
                    Log.e(TAG, "Error in simple edit installed app");
                    e.printStackTrace();
                }
                break;
            case R.id.uninstall_app:
                AppUtils.uninstallApp(context, current.packageName);
                break;
            case R.id.goto_settings_app:
                AppUtils.gotoApplicationSettings(context, current.packageName);
                break;
        }
    }
}

