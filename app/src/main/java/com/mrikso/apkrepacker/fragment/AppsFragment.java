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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.MainActivity;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.AppsOptionsItemDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.DecompileOptionsDialogFragment;
import com.mrikso.apkrepacker.ui.appslist.AppsAdapter;
import com.mrikso.apkrepacker.ui.appslist.AppsViewModel;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.PackageMeta;
import com.mrikso.apkrepacker.utils.ScrollingViewOnApplyWindowInsetsListener;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class AppsFragment extends Fragment implements AppsAdapter.OnItemInteractionListener, AppsOptionsItemDialogFragment.ItemClickListener, DecompileOptionsDialogFragment.ItemClickListener {

    public static final String TAG = "AppsFragment";
    public static View loadingView;
    private RecyclerView appsList;
    private AppsViewModel mViewModel;
    private AppsAdapter appsAdapter;
    private EditText mEditTextSearch;
    private Chip mChipFilterSplitsOnly;
    private Chip mChipFilterIncludeSystemApps;
    private Context context;
    private PackageMeta current;
    private DecompileFragment decompileFragment;
    private ApplicationInfo applicationInfo;
    private String mAppName;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_apps, container, false);
        context = view.getContext();
        mViewModel = new ViewModelProvider(this).get(AppsViewModel.class);
        loadingView = view.findViewById(R.id.loading_view);
        appsList = view.findViewById(R.id.app_packages);
        appsList.setLayoutManager(new LinearLayoutManager(context));
        appsList.setHasFixedSize(true);
        appsList.setDrawingCacheEnabled(true);
        appsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
//        appsList.setItemViewCacheSize(3000);
        appsList.buildDrawingCache(true);
        appsList.getRecycledViewPool().setMaxRecycledViews(0, 24);
        appsAdapter = new AppsAdapter(context);
        // mViewModel.getPackages().observe(getViewLifecycleOwner(), appsAdapter::setData);
        appsAdapter.setHasStableIds(true);
        appsAdapter.setInteractionListener(this);
        FastScroller fastScroller = new FastScrollerBuilder(appsList).useMd2Style().build();
        appsList.setOnApplyWindowInsetsListener(new ScrollingViewOnApplyWindowInsetsListener(appsList, fastScroller));
        appsList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                view.findViewById(R.id.app_bar).setSelected(appsList.canScrollVertically(-1));
            }
        });

        initData();
        setupToolbar(view);
        return view;
    }

    private void initData() {
        mViewModel.getPackages().observe(getViewLifecycleOwner(), appsAdapter::setData);
        appsList.setAdapter(appsAdapter);
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
        view.findViewById(R.id.button_clear).setOnClickListener(v -> mEditTextSearch.setText(""));

        mEditTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                view.findViewById(R.id.button_clear).setVisibility(s.toString().isEmpty() ? View.GONE : View.VISIBLE);
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
        MainActivity.getInstance().refreshAdapter(false);
    }

    @Override
    public void onBackupButtonClicked(PackageMeta packageMeta) {
        current = packageMeta;
        AppsOptionsItemDialogFragment fragment = AppsOptionsItemDialogFragment.newInstance();
        fragment.show(getChildFragmentManager(), AppsOptionsItemDialogFragment.TAG);
    }

    @Override
    public void onAppsItemClick(Integer item) {
        PreferenceHelper preferenceHelper = PreferenceHelper.getInstance(context);
        switch (item) {
            case R.id.decompile_app:
                //  File app = FileUtil.createBackupFile(current, preference.getDecodingPath());
                try {
                    applicationInfo = context.getPackageManager().getApplicationInfo(current.packageName, 0);
                    mAppName = current.packageName + mAppName;
                    // FileUtils.copyFile(new File(applicationInfo.publicSourceDir), app);
                    int mode = preferenceHelper.getDecodingMode();
                    if (mode == 3) {
                        DecompileOptionsDialogFragment decompileOptionsDialogFragment = DecompileOptionsDialogFragment.newInstance();
                        decompileOptionsDialogFragment.show(getChildFragmentManager(), DecompileOptionsDialogFragment.TAG);
                    } else {
                        decompileFragment = DecompileFragment.newInstance(current.label, applicationInfo.publicSourceDir, true, mode == 0 ? 3 : mode == 1 ? 2 : mode == 2 ? 1 : 0);
                        FragmentUtils.replace(decompileFragment, getActivity().getSupportFragmentManager(), android.R.id.content, "DecompileFragment");
                    }
                } catch (Exception e) {
                    UIUtils.toast(App.getContext(), R.string.toast_error_in_decompile_installed_app);
                    Log.e(TAG, "Error in decompile installed app");
                    e.printStackTrace();
                }
                break;
            case R.id.simple_edit_apk:
                //  File selectedApk = FileUtil.createBackupFile(current, preference.getDecodingPath());
                try {
                    ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(current.packageName, 0);
                    //  FileUtils.copyFile(new File(applicationInfo.publicSourceDir), selectedApk);
                    SimpleEditorFragment simpleEditorFragment = SimpleEditorFragment.newInstance(applicationInfo.publicSourceDir);
                    FragmentUtils.replace(simpleEditorFragment, getActivity().getSupportFragmentManager(), android.R.id.content, SimpleEditorFragment.TAG);
                } catch (Exception e) {
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

    @Override
    public void onModeItemClick(Integer item) {
        switch (item) {
            case R.id.decompile_all:
                decompileFragment = DecompileFragment.newInstance(mAppName, applicationInfo.publicSourceDir, true, 3);
                break;
            case R.id.decompile_all_res:
                decompileFragment = DecompileFragment.newInstance(mAppName, applicationInfo.publicSourceDir, true, 2);
                break;
            case R.id.decompile_all_dex:
                decompileFragment = DecompileFragment.newInstance(mAppName, applicationInfo.publicSourceDir, true, 1);
                break;
        }
        FragmentUtils.replace(decompileFragment, getActivity().getSupportFragmentManager(), android.R.id.content, "DecompileFragment");
    }
}

