package com.mrikso.apkrepacker.fragment.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputEditText;
import com.jecelyin.common.utils.IOUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.File;

public class CreateNewClass extends BaseDialog {
    private OnFileCreatedListener listener;
    @Nullable
    private String mCurrPackage;
    @Nullable
    private File mCurrFolder;
    private TextInputEditText mPackage;

    public CreateNewClass(Context context, File mCurrFolder, CreateNewClass.OnFileCreatedListener listener) {
        super(context);
        this.mCurrFolder = mCurrFolder;
        this.listener = listener;
    }

    @Override
    public AlertDialog.Builder show() {
        AlertDialog.Builder builder = getBuilder();
        builder.setTitle(R.string.action_create_class);
        View viewinf = getLayoutInflater().inflate(R.layout.dialog_create_class, null);
        mPackage = viewinf.findViewById(R.id.class_package);
        TextInputEditText className = viewinf.findViewById(R.id.class_name);
        initPackage();
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            createNewClass(className.getText().toString(), mCurrFolder);
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        //AlertDialog alertDialog = builder.create();
        builder.setView(viewinf);
        builder.show();
        return builder;
    }

    private void initPackage() {
        if (mCurrPackage == null || mCurrPackage.isEmpty()) {
            if (mCurrFolder != null) {
                mCurrPackage = FileUtil.findPackage(new File(FileUtil.getProjectPath()), mCurrFolder);
            }
        }
        mPackage.setText(mCurrPackage);
    }

    private void createNewClass(String fileName, File currentFolder) {
        File xmlFile = new File(currentFolder, fileName + ".smali");
        xmlFile.getParentFile().mkdirs();
        String content = ".class public L" + mPackage.getText().toString().replace(".", File.separator) + File.separator
                + fileName + ";\n.super Ljava/lang/Object;\n";

        IOUtils.writeFile(xmlFile, content);
        listener.onCreateSuccess(xmlFile);
    }

    public interface OnFileCreatedListener {
        /**
         * the files before, not exist
         */
        void onCreateSuccess(File deleted);

        void onCreateFailed(File deleted, Exception e);
    }
}
