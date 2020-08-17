package com.mrikso.apkrepacker.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.PatchAdapter;
import com.mrikso.apkrepacker.adapter.PatchItem;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.viewmodel.PatcherViewModel;

import java.io.File;

public class PatcherFragment extends Fragment implements View.OnClickListener, OnBackPressedListener {

    public static final String TAG = "PatcherFragment";

    private PatcherViewModel mViewModel;
    private PatchAdapter mPathAdapter;
    private ProgressBar mPatchRulesProgress;
    private ProgressBar mPatchListProgress;
    private int allRules = 0;

    public static PatcherFragment newInstance() {
        return new PatcherFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_patcher, container, false);
        mViewModel = new ViewModelProvider(this).get(PatcherViewModel.class);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        MaterialButton selectPatch = view.findViewById(R.id.btn_select_patch);
        ExtendedFloatingActionButton startPatch = view.findViewById(R.id.start_patch);
        selectPatch.setOnClickListener(this);
        startPatch.setOnClickListener(this);

        mPatchListProgress = view.findViewById(R.id.progress_bar_patches);
        mPatchRulesProgress = view.findViewById(R.id.progress_bar_rules);

        mPathAdapter = new PatchAdapter(requireContext());
        RecyclerView mPatchList = view.findViewById(R.id.patch_list);
        mPatchList.setLayoutManager(new LinearLayoutManager(requireContext()));
        mPatchList.setAdapter(mPathAdapter);
        AppCompatTextView log = view.findViewById(R.id.logger);

        mViewModel.getLogLiveData().observe(getViewLifecycleOwner(), log::append);
        mViewModel.getPatchRulesSize().observe(getViewLifecycleOwner(), integer -> mPatchRulesProgress.setMax(integer));
        mViewModel.getPatchCurrentRules().observe(getViewLifecycleOwner(), this::progressRulesObserver);
        toolbar.setNavigationOnClickListener(v -> FragmentUtils.remove(this));
    }

    private void selectPatch(){
        new FilePickerDialog(requireContext())
                .setTitleText(this.getResources().getString(R.string.select_patch))
                .setSelectMode(FilePickerDialog.MODE_MULTI)
                .setSelectType(FilePickerDialog.TYPE_FILE)
                .setExtensions(new String[]{"zip"})
                .setRootDir(FileUtil.getInternalStorage().getAbsolutePath())
                .setBackCancelable(true)
                .setOutsideCancelable(true)
                .setDialogListener(this.getResources().getString(R.string.choose_button_label), this.getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                    @Override
                    public void onSelectedFilePaths(String[] filePaths) {
                        for (String file : filePaths) {
                            File patch = new File(file);
                            mPathAdapter.addItem(new PatchItem(patch.getName(), patch.getAbsolutePath()));
                        }
                    }

                    @Override
                    public void onCanceled() {
                    }
                })
                .show();
    }

    private void progressRulesObserver(int currentRules){
       mPatchRulesProgress.setIndeterminate(false);
       mPatchRulesProgress.setProgress(currentRules);
    }

    private void progressPatchesObserver(){

    }

    @Override
    public boolean onBackPressed() {
        FragmentUtils.remove(this);
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_select_patch:
                selectPatch();
                break;
            case R.id.start_patch:
                if(mPathAdapter.getItemCount() > 0)
                mViewModel.start(mPathAdapter.getPatchData());
                break;
        }
    }
}