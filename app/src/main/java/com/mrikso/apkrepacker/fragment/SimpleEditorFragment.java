package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.task.SimpleEditTask;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.QickEditParams;
import com.mrikso.apkrepacker.utils.SignUtil;
import com.mrikso.apkrepacker.utils.ThemeWrapper;
import com.mrikso.apkrepacker.utils.qickedit.AppInfo;

import java.io.File;

public class SimpleEditorFragment extends Fragment implements TextWatcher, ProgressDialogFragment.ProgressDialogFragmentListener {

    private File selected, outputFile;
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
    private AppInfo appInfo;
    private Bitmap[] bmp = new Bitmap[1];
    private DialogFragment dialog;

    public SimpleEditorFragment() {
        // Required empty public constructor
    }

    public static SimpleEditorFragment newInstance(String selected) {
        SimpleEditorFragment fragment = new SimpleEditorFragment();
        Bundle args = new Bundle();
        args.putString("selected", selected);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selected = new File(getArguments().getString("selected"));
        setRetainInstance(true);
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
            appInfo = new AppInfo(view.getContext(), selected);
            Object[] info = AppUtils.getApkInfo(view.getContext(), selected.getAbsolutePath());
            mAppIcon.setImageDrawable((Drawable) info[0]);
            mAppName.setText(appInfo.label());
            mAppPackage.setText(appInfo.pname());
            mAppVersion.setText(appInfo.version());
            mAppVersionBuild.setText(info[4].toString());
            mMinimumSdk.setText(info[5].toString());
            mTargetSdk.setText(info[6].toString());
            installLocation = (int) info[7];
            bmp[0] = null;
            buildValues();
            toolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
            mAppIcon.setOnClickListener(v -> selectIcon());
            mAppPackage.addTextChangedListener(this);
            mSave.setOnClickListener(v -> buildApp());
        } catch (Exception ex) {
            ex.printStackTrace();
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
        /*-1 default(none)
        0 - auto
        1 - internal
        2 - external
         */
        if (installLocation >= -1 && installLocation < 3) {
            mInstallLoc.setSelection(installLocation + 1);
        }
    }

    private void selectIcon() {
        new FilePickerDialog(mContext)
                .setTitleText(this.getResources().getString(R.string.select_icon))
                .setSelectMode(FilePickerDialog.MODE_SINGLE)
                .setSelectType(FilePickerDialog.TYPE_FILE)
                .setExtensions(new String[]{"gif", "png", "jpg", "jpeg", "bmp", "webp"})
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
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.ARGB_8888;
        bmp[0] = BitmapFactory.decodeFile(file, option);
        BitmapDrawable draw = new BitmapDrawable(mContext.getResources(), bmp[0]);
        mAppIcon.setImageDrawable(draw);
        // tv.setText(picker.getPath());
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
            // mOptionClone.setVisibility(View.VISIBLE);
        } else {
            mOptionClone.setVisibility(View.GONE);
        }
    }

    private void buildApp() {
        final String appIcPath = (appInfo.iconValue() == null ? "" : appInfo.iconValue().split(":")[1]);
        QickEditParams.setNewname(mAppName.getText().toString());
        QickEditParams.setNewPackage(mAppPackage.getText().toString());
        QickEditParams.setVersionCode(mAppVersionBuild.getText().toString());
        QickEditParams.setVersionName(mAppVersion.getText().toString());
        QickEditParams.setInstallLocation(mInstallLoc.getSelectedItemPosition());
        QickEditParams.setMinimumSdk(Integer.parseInt(mMinimumSdk.getText().toString()));
        QickEditParams.setTargetSdk(Integer.parseInt(mTargetSdk.getText().toString()));
        QickEditParams.setInRes(mResourcesCb.isChecked());
        QickEditParams.setInDex(mDexCb.isChecked());
        QickEditParams.setIconName(appIcPath);
        QickEditParams.setBitmap(bmp[0]);
        //  UIUtils.toast(mContext, "In developing!");
        Runnable build = () -> SignUtil.loadKey(mContext, signTool -> new SimpleEditTask(mContext, this, signTool).execute(selected));
        build.run();
    }

    public void showProgress() {
        Bundle args = new Bundle();
        args.putString(ProgressDialogFragment.TITLE, getResources().getString(R.string.build_run_title));
        args.putString(ProgressDialogFragment.MESSAGE, getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        //  args.putInt(ProgressDialogFragment.MAX, 100);
        dialog = ProgressDialogFragment.newInstance();
        dialog.setArguments(args);
        dialog.show(getChildFragmentManager(), ProgressDialogFragment.TAG);
    }

    public void updateProgress(Integer... values) {
        ProgressDialogFragment progress = getProgressDialogFragment();
        if (progress == null) {
            return;
        }
        progress.updateProgress(values[0]);
    }

    public void hideProgress(boolean result) {
        dialog.dismiss();
        if (result) {
            UIUtils.toast(mContext, getString(R.string.toast_apk_succes_edited));
            UIUtils.showConfirmDialog(ThemeWrapper.getDialogThemeContext(getContext()), getString(R.string.done), getString(R.string.install_app_message), new UIUtils.OnClickCallback() {
                @Override
                public void onOkClick() {
                    FileUtil.installApk(getContext(), outputFile);
                }
            });
        } else {
            UIUtils.toast(mContext, getString(R.string.toast_apk_falied_edited));
        }
    }

    private ProgressDialogFragment getProgressDialogFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        return (ProgressDialogFragment) fragment;
    }

    @Override
    public void onProgressCancelled() {}

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
