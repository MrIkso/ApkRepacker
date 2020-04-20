package com.mrikso.apkrepacker.ui.keydialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageButton;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.ui.prererence.Preference;

public class KeystorePreference extends DialogPreference implements AdapterView.OnItemSelectedListener {
    private Spinner format;
    private EditText key_path;
    private TextView cert;
    private EditText alias;
    private LinearLayout password;
    private EditText storePass;
    private EditText keyPass;
    private AppCompatImageButton selectKey, selectCert;
    private Preference mPref;

    public KeystorePreference(Context ctx, AttributeSet a) {
        super(ctx, a);
        mPref = Preference.getInstance(ctx);
        setDialogLayoutResource(R.layout.keystore);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        format = view.findViewById(R.id.format);
        key_path = view.findViewById(R.id.key_path);
        cert = view.findViewById(R.id.cert);
        alias = view.findViewById(R.id.alias);
        password = view.findViewById(R.id.password);
        storePass = view.findViewById(R.id.storePass);
        keyPass = view.findViewById(R.id.keyPass);
        format.setOnItemSelectedListener(this);
        selectKey = view.findViewById(R.id.button_select_key);
        selectCert = view.findViewById(R.id.button_select_cert);
        selectCert.setOnClickListener(v -> {
                    Log.i("sdv", "starterd");
                    new FilePickerDialog(getContext())
                            .setTitleText(view.getContext().getResources().getString(R.string.select_key))
                            .setSelectMode(FilePickerDialog.MODE_SINGLE)
                            .setSelectType(FilePickerDialog.TYPE_FILE)
                            //.setExtensions(new String[]{"jks", "bks"})
                            .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                            .setBackCancelable(true)
                            .setOutsideCancelable(true)
                            .setDialogListener(view.getContext().getResources().getString(R.string.choose_button_label),
                                    view.getContext().getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
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
                            .setTitleText(view.getContext().getResources().getString(R.string.select_key))
                            .setSelectMode(FilePickerDialog.MODE_SINGLE)
                            .setSelectType(FilePickerDialog.TYPE_FILE)
                            //.setExtensions(new String[]{"jks", "bks"})
                            .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                            .setBackCancelable(true)
                            .setOutsideCancelable(true)
                            .setDialogListener(view.getContext().getResources().getString(R.string.choose_button_label),
                                    view.getContext().getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
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
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // SharedPreferences.Editor editor = getSharedPreferences().edit();
            mPref.setKeyType(format.getSelectedItemPosition());
            mPref.setPrivateKeyPath(key_path.getText().toString());
            mPref.setCertPath(alias.getText().toString());
            //  editor.putInt("key_type", format.getSelectedItemPosition());
            //  editor.putString("key_path", key_path.getText().toString());
            // editor.putString("cert_or_alias", alias.getText().toString());
            /// String store_pass = preference.getStoreKey();
            // String key_pass = preference.getPrivateKey();
            mPref.setStoreKey(storePass.getText().toString());
            mPref.setPrivateKey(keyPass.getText().toString());
            //editor.putString("store_pass", storePass.getText().toString());
            //editor.putString("key_pass", keyPass.getText().toString());
            //editor.apply();
        } else if (which == DialogInterface.BUTTON_NEUTRAL) {
            mPref.setStoreKey("");
            mPref.setPrivateKey("");
            // SharedPreferences.Editor editor = getSharedPreferences().edit();
            // editor.putString("store_pass", "");
            // editor.putString("key_pass", "");
            // editor.apply();
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        //  SharedPreferences sp = getSharedPreferences();

        int type = mPref.getKeyType();
        //sp.getInt("key_type", 0);
        format.setSelection(type);
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

    @Override
    public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4) {
        if (p3 == 3) {
            selectCert.setVisibility(View.VISIBLE);
            password.setVisibility(View.GONE);
            cert.setText(R.string.cert_path);
        } else {
            selectCert.setVisibility(View.GONE);
            password.setVisibility(View.VISIBLE);
            cert.setText(R.string.key_alias);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> p1) {
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        TextView title = view.findViewById(android.R.id.title);

        TextView summary = view.findViewById(android.R.id.summary);

    }
}
