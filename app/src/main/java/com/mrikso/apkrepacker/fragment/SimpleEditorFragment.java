package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.task.SimpleEditTask;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.QickEditParams;
import com.mrikso.apkrepacker.utils.SignUtil;

import java.io.File;

public class SimpleEditorFragment extends Fragment implements TextWatcher {

    private File selected;
    private TextInputEditText mAppName, mAppPackage, mAppVersion, mAppVersionBuild, mMinimumSdk, mTargetSdk;
    private AppCompatImageView mAppIcon;
    private ExtendedFloatingActionButton mSave;
    private AppCompatCheckBox mDexCb, mResourcesCb;
    private AppCompatSpinner mInstallLoc;
    private Toolbar toolbar;
    private Context mContext;
    private LinearLayout mOptionClone;
    private String oldPackage;
    private int installLocation;

    public SimpleEditorFragment(File selected) {
        // Required empty public constructor
        this.selected = selected;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_simple_editor, container, false);
        mContext = view.getContext();
        toolbar = view.findViewById(R.id.toolbar);
        mAppName = view.findViewById(R.id.app_name);
        mAppPackage = view.findViewById(R.id.app_package);
        mAppVersion = view.findViewById(R.id.app_version_name);
        mAppVersionBuild = view.findViewById(R.id.app_version_code);
        mMinimumSdk = view.findViewById(R.id.app_minimum_sdk);
        mTargetSdk = view.findViewById(R.id.app_target_sdk);
        mAppIcon = view.findViewById(R.id.app_icon_edit);
        mSave = view.findViewById(R.id.save_ex_fab);
        mInstallLoc = view.findViewById(R.id.install_location_spinner);
        mDexCb = view.findViewById(R.id.in_dex_cb);
        mResourcesCb = view.findViewById(R.id.in_resources_cb);
        mOptionClone = view.findViewById(R.id.option_clone);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        try {
            Object[] info = AppUtils.getApkInfo(view.getContext(), selected.getAbsolutePath());
            mAppIcon.setImageDrawable(AppUtils.getApkIcon(view.getContext(), selected.getAbsolutePath()));
            mAppName.setText(info[1].toString());
            mAppPackage.setText(info[2].toString());
            mAppVersion.setText(info[3].toString());
            mAppVersionBuild.setText(info[4].toString());
            mMinimumSdk.setText(info[5].toString());
            mTargetSdk.setText(info[6].toString());
            installLocation = (int) info[7];
            buildValues();
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
            mAppIcon.setOnClickListener(v -> selectIcon());
            mAppPackage.addTextChangedListener(this);
            mSave.setOnClickListener(v -> buildApp());
        } catch (Exception ex) {
            UIUtils.toast(App.getContext(), R.string.toast_error_cant_parse_apk);
        }
    }

    private void buildValues() {
        QickEditParams.setOldName(mAppName.getText().toString());
        oldPackage = mAppPackage.getText().toString();
        QickEditParams.setOldPackage(oldPackage);
        String[] location = getResources().getStringArray(R.array.install_location);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_item, location);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInstallLoc.setAdapter(arrayAdapter);
        if (installLocation >= -1 && installLocation < 3) {
            mInstallLoc.setSelection(installLocation + 1);
        }
    }

    private void selectIcon() {
        new FilePickerDialog(mContext)
                .setTitleText(this.getResources().getString(R.string.select_icon))
                .setSelectMode(FilePickerDialog.MODE_SINGLE)
                .setSelectType(FilePickerDialog.TYPE_FILE)
                .setExtensions(new String[]{"png"})
                .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                .setBackCancelable(true)
                .setOutsideCancelable(true)
                .setDialogListener(this.getResources().getString(R.string.choose_button_label), this.getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                    @Override
                    public void onSelectedFilePaths(String[] filePaths) {
                        createNewIcon(filePaths[0]);
                    }

                    @Override
                    public void onCanceled() {
                    }
                })
                .show();
    }

    private void createNewIcon(String file) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String string = s.toString();
        if (oldPackage == null || !string.contentEquals(oldPackage)) {
            mOptionClone.setVisibility(View.VISIBLE);
        } else {
            mOptionClone.setVisibility(View.GONE);
        }
    }

    private void buildApp(){
        QickEditParams.setNewname(mAppName.getText().toString());
        QickEditParams.setNewPackage(mAppPackage.getText().toString());
        QickEditParams.setVersionCode(mAppVersionBuild.getText().toString());
        QickEditParams.setVersionName(mAppVersion.getText().toString());
        QickEditParams.setInstallLocation(setInstallLocation(mInstallLoc.getSelectedItemPosition()));
        QickEditParams.setMinimumSdk(Integer.parseInt(mMinimumSdk.getText().toString()));
        QickEditParams.setTargetSdk(Integer.parseInt(mTargetSdk.getText().toString()));
        QickEditParams.setInRes(mResourcesCb.isChecked());
        QickEditParams.setInDex(mDexCb.isChecked());
        UIUtils.toast(mContext, "In developing!");
       // Runnable build = () -> SignUtil.loadKey(mContext, signTool -> new SimpleEditTask(mContext, signTool).execute(selected));
        //build.run();
    }

    private String setInstallLocation(int installLocation){
        switch (installLocation){
            case 0:
                return null;
            case 1:
                return "android:installLocation=\"auto\"";
            case 2:
                return "android:installLocation=\"internalOnly\"";
            case 3:
                return "android:installLocation=\"preferExternal\"";
                default:
                    return null;
        }
    }
}
