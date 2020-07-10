package com.mrikso.apkrepacker.ui.keydialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.preference.PreferenceDialogFragmentCompat;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.ui.prererence.PreferenceHelper;

public class KeystorePreferenceFragmentDialog extends PreferenceDialogFragmentCompat {
    private EditText key_path;
    private TextInputLayout cert;
    private TextInputEditText fx_type;
    private EditText alias;
    private LinearLayout password;
    private EditText storePass;
    private EditText keyPass;
    private AppCompatImageButton selectKey, selectCert;
    private PreferenceHelper mPref;
    private KeystorePreference preference;

    public static KeystorePreferenceFragmentDialog newInstance(String key){
        KeystorePreferenceFragmentDialog keystorePreference = new KeystorePreferenceFragmentDialog();
        Bundle bundle = new Bundle(1);
        bundle.putString(PreferenceDialogFragmentCompat.ARG_KEY, key);
        keystorePreference.setArguments(bundle);
        return keystorePreference;
    }
    @Override
    protected View onCreateDialogView(Context context) {
        return getLayoutInflater().inflate(R.layout.keystore, null);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mPref = PreferenceHelper.getInstance(view.getContext());
        preference = (KeystorePreference) getPreference();
      //  getPreference().setDialogLayoutResource(R.layout.keystore);
        TextView title = view.findViewById(android.R.id.title);
        TextView summary = view.findViewById(android.R.id.summary);
        key_path = view.findViewById(R.id.key_path);
        fx_type = view.findViewById(R.id.et_type);
        cert = view.findViewById(R.id.cert);
        alias = view.findViewById(R.id.alias);
        password = view.findViewById(R.id.password);
        storePass = view.findViewById(R.id.storePass);
        keyPass = view.findViewById(R.id.keyPass);
        selectKey = view.findViewById(R.id.button_select_key);
        selectCert = view.findViewById(R.id.button_select_cert);
        selectCert.setOnClickListener(v -> {
                    new FilePickerDialog(getContext())
                            .setTitleText(getString(R.string.select_key))
                            .setSelectMode(FilePickerDialog.MODE_SINGLE)
                            .setSelectType(FilePickerDialog.TYPE_FILE)
                            //.setExtensions(new String[]{"jks", "bks"})
                            .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                            .setBackCancelable(true)
                            .setOutsideCancelable(true)
                            .setDialogListener(getString(R.string.choose_button_label),
                                    getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                                        @Override
                                        public void onSelectedFilePaths(String[] filePaths) {
                                            for (String dir : filePaths) {
                                                try {
                                                    alias.setText(dir);
                                                    //FileUtil.copyFile(new File(dir), new File(currentDirectory.getAbsolutePath()));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            // setPath(new File(currentDirectory.getAbsolutePath()));
                                        }

                                        @Override
                                        public void onCanceled() {
                                        }
                                    })
                            .show();
                }
        );
        selectKey.setOnClickListener(v -> {
                    //  Log.i("sdv", "starterd");
                    new FilePickerDialog(getContext())
                            .setTitleText(getString(R.string.select_key))
                            .setSelectMode(FilePickerDialog.MODE_SINGLE)
                            .setSelectType(FilePickerDialog.TYPE_FILE)
                            //.setExtensions(new String[]{"jks", "bks"})
                            .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                            .setBackCancelable(true)
                            .setOutsideCancelable(true)
                            .setDialogListener(getString(R.string.choose_button_label),
                                    getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                                        @Override
                                        public void onSelectedFilePaths(String[] filePaths) {
                                            for (String dir : filePaths) {
                                                try {
                                                    key_path.setText(dir);
                                                    //FileUtil.copyFile(new File(dir), new File(currentDirectory.getAbsolutePath()));
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            // setPath(new File(currentDirectory.getAbsolutePath()));
                                        }

                                        @Override
                                        public void onCanceled() {
                                        }
                                    })
                            .show();
                }
        );

        if (fx_type.getBackground() instanceof MaterialShapeDrawable) {
            fx_type.setBackground(addRippleEffect((MaterialShapeDrawable) fx_type.getBackground()));
        }
        fx_type.setOnClickListener(v -> {
            showListDialog(view.getContext(), view.getContext().getResources().getStringArray(R.array.key_format), mPref.getKeyType());
        });
    }


    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        //  SharedPreferences sp = getSharedPreferences();

        int type = mPref.getKeyType();
        //sp.getInt("key_type", 0);
//        format.setSelection(type);

        String[] array = builder.getContext().getResources().getStringArray(R.array.key_format);
        fx_type.setText(array[type]);

        if (type == 3) {
            selectCert.setVisibility(View.VISIBLE);
            password.setVisibility(View.GONE);
            cert.setHint(getString(R.string.cert_path));
        } else {
            selectCert.setVisibility(View.GONE);
            password.setVisibility(View.VISIBLE);
            cert.setHint(getString(R.string.key_alias));
        }

        String keyPath = mPref.getPrivateKeyPath();
        //sp.getString("key_path", "");
        key_path.setText(keyPath);
        String cert_or_alias = mPref.getCertPath();
        // sp.getString("cert_or_alias", "");
        alias.setText(cert_or_alias);
        String store_pass = mPref.getStoreKey();
        storePass.setText(store_pass);
        String key_pass = mPref.getPrivateKey();
        //sp.getString("key_pass", "");
        keyPass.setText(key_pass);
    }

    private void showListDialog(Context context, String[] list, int checkedItem) {
        UIUtils.showListDialog(context, 0, 0, list, checkedItem, new UIUtils.OnListCallback() {

            @Override
            public void onSelect(MaterialDialog dialog, int which) {
                String[] array = dialog.getContext().getResources().getStringArray(R.array.key_format);
                fx_type.setText(array[which]);
                mPref.setKeyType(which);
                if (which == 3) {
                    selectCert.setVisibility(View.VISIBLE);
                    password.setVisibility(View.GONE);
                    cert.setHint(getString(R.string.cert_path));
                } else {
                    selectCert.setVisibility(View.GONE);
                    password.setVisibility(View.VISIBLE);
                    cert.setHint(getString(R.string.key_alias));
                }
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

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if(positiveResult){
            mPref.setPrivateKeyPath(key_path.getText().toString());
            mPref.setCertPath(alias.getText().toString());
            mPref.setStoreKey(storePass.getText().toString());
            mPref.setPrivateKey(keyPass.getText().toString());
        }
        else {
            mPref.setStoreKey("");
            mPref.setPrivateKey("");
        }
    }
}