package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.task.SimpleEditTask;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.QickEditParams;
import com.mrikso.apkrepacker.utils.SignUtil;
import com.mrikso.apkrepacker.utils.qickedit.AppInfo;

import java.io.File;

public class SimpleEditorFragment extends Fragment implements TextWatcher, ProgressDialogFragment.ProgressDialogFragmentListener {

    public static String TAG = "SimpleEditorFragment";
    private File selected, outputFile;
    private TextInputEditText mAppName, mAppPackage, mAppVersion, mAppVersionBuild, mMinimumSdk, mTargetSdk, mInstallLoc;
    private AppCompatImageView mAppIcon;
    private ExtendedFloatingActionButton mSave;
    private AppCompatCheckBox mDexCb, mResourcesCb;
    private Toolbar toolbar;
    private Context mContext;
    private LinearLayout mOptionClone;
    private String oldPackage;
    private String[] location;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        mInstallLoc = view.findViewById(R.id.et_install_location);
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
            toolbar.setNavigationOnClickListener(v -> FragmentUtils.remove(this));
            mAppIcon.setOnClickListener(v -> selectIcon());
            mAppPackage.addTextChangedListener(this);
            mSave.setOnClickListener(v -> buildApp());
            if (mInstallLoc.getBackground() instanceof MaterialShapeDrawable) {
                mInstallLoc.setBackground(addRippleEffect((MaterialShapeDrawable) mInstallLoc.getBackground()));
            }
            mInstallLoc.setOnClickListener(v -> showListDialog(getContext(), location));
        } catch (Exception ex) {
            ex.printStackTrace();
            UIUtils.toast(App.getContext(), R.string.toast_error_cant_parse_apk);
        }
    }

    private void buildValues() {
        QickEditParams.setOldName(mAppName.getText().toString());
        oldPackage = mAppPackage.getText().toString();
        QickEditParams.setOldPackage(oldPackage);
        location = getResources().getStringArray(R.array.install_location);

        /*-1 default(none)
        0 - auto
        1 - internal
        2 - external
         */
        if (installLocation >= -1 && installLocation < 3) {
            mInstallLoc.setText(location[installLocation + 1]);
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
//        QickEditParams.setInstallLocation(mInstallLoc.getSelectedItemPosition());
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

    private void showListDialog(Context context, String[] list) {
        UIUtils.showListDialog(context, 0, 0, list, 0, new UIUtils.OnListCallback() {

            @Override
            public void onSelect(MaterialDialog dialog, int which) {
                QickEditParams.setInstallLocation(which);
                mInstallLoc.setText(location[which]);
            }
        }, null);
    }

    private Drawable addRippleEffect(MaterialShapeDrawable boxBackground) {
        int[] attrs = new int[]{R.attr.colorControlHighlight};
        TypedArray ta = getContext().obtainStyledAttributes(attrs);
        ColorStateList rippleColor = ta.getColorStateList(0);
        ta.recycle();
        Drawable mask = new MaterialShapeDrawable(boxBackground.getShapeAppearanceModel());
        mask.setTint(Color.WHITE);
        return new RippleDrawable(rippleColor, boxBackground, mask);
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
            UIUtils.showConfirmDialog(getContext(), getString(R.string.dialog_install_app), new UIUtils.OnClickCallback() {
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
    public void onProgressCancelled() {
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
}
