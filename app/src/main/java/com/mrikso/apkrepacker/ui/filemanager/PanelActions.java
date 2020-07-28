package com.mrikso.apkrepacker.ui.filemanager;

import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.base.BaseFilesFragment;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.CreateDirectoryOperation;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.CreateFileOperation;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.DeleteOperation;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.FileOperationRunnerInjector;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.RenameOperation;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.CreateDirectoryArguments;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.CreateFileArguments;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.DeleteArguments;
import com.mrikso.apkrepacker.ui.filemanager.storage.operation.argument.RenameArguments;
import com.mrikso.apkrepacker.ui.filemanager.utils.Utils;
import com.mrikso.apkrepacker.utils.AppExecutor;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class PanelActions {

    private Context mContext;
    private BaseFilesFragment mFragment;

    public PanelActions(Context context, @NonNull BaseFilesFragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    public static PanelActions getInstance(Context context, @NonNull BaseFilesFragment fragment) {
        return new PanelActions(context, fragment);
    }

    public void actionRenameFile(File file) {
        UIUtils.showInputDialog(mContext, R.string.action_rename, 0, file.getName(), EditorInfo.TYPE_CLASS_TEXT, new UIUtils.OnShowInputCallback() {
            @Override
            public void onConfirm(CharSequence input) {
                try {
                    tryRename(file, input.toString());
                } catch (Exception e) {
                    UIUtils.toast(mContext, R.string.toast_error_on_rename);
                    DLog.e(e);
                }
            }
        });
    }


    public void actionCreateFile(File parent) {
        UIUtils.showInputDialog(mContext, R.string.action_create_new_file, 0, null, EditorInfo.TYPE_CLASS_TEXT,
                new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        try {

                            File tbc = new File(parent, input.toString());
                            if (!tbc.exists()) {
                                FileOperationRunnerInjector.operationRunner(mContext).run(new CreateFileOperation(mContext),
                                        CreateFileArguments.createFileArguments(tbc));
                            }

                        } catch (Exception e) {
                            UIUtils.toast(mContext, R.string.error);
                            DLog.e(e);
                        }
                    }
                });
    }

    public void actionCreateNewDirectory(File parent) {
        UIUtils.showInputDialog(mContext, R.string.action_create_new_folder, 0, null, EditorInfo.TYPE_CLASS_TEXT,
                new UIUtils.OnShowInputCallback() {
                    @Override
                    public void onConfirm(CharSequence input) {
                        try {
                            //File parentPath = new File(file.getAbsolutePath());
                            File tbc = new File(parent, input.toString());
                            if (!tbc.exists()) {
                                FileOperationRunnerInjector.operationRunner(mContext).run(new CreateDirectoryOperation(mContext),
                                        CreateDirectoryArguments.createDirectoryArguments(tbc, mFragment));
                            }

                        } catch (Exception e) {
                            UIUtils.toast(mContext, R.string.error);
                            DLog.e(e);
                        }
                    }
                });
    }

    private void tryRename(File file, String newName) {
        File destFile = new File(file.getParent(), newName);
        if (destFile.exists()) {
            Toast.makeText(mContext, R.string.file_exists, Toast.LENGTH_SHORT).show();
        } else {
            renameTo(file, destFile.getName());
        }
    }

    private void renameTo(File file, String newName) {

        if (newName.length() > 0) {

            try {
                FileOperationRunnerInjector.operationRunner(mContext).run(
                        new RenameOperation(mContext), RenameArguments.renameArguments(file, newName, mFragment));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void actionDelete(FileHolder... params){
        AppExecutor.getInstance().getDiskIO().execute(() ->{
            try {
                FileOperationRunnerInjector.operationRunner(mContext).run(new DeleteOperation(mContext),
                        DeleteArguments.deleteArgs(Objects.requireNonNull(params[0].getFile().getParentFile()),mFragment, params));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void actionShare(FileHolder file){
        Utils.sendFile(file, mContext);
    }

}
