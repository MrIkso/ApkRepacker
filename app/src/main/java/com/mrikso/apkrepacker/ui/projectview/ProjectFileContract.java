package com.mrikso.apkrepacker.ui.projectview;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.unnamed.b.atv.model.TreeNode;

import java.io.File;

public class ProjectFileContract {
    public interface View {
        void display(File projectFile, boolean expand);

        TreeNode refresh();

        void setPresenter(ProjectFileContract.Presenter presenter);
    }

    public interface Presenter {
        void show(File projectFile, boolean expand);

        void refresh(File projectFile);
    }

    public interface FileActionListener {
        /**
         * This method will be call when user click file or folder
         */
        void onFileClick(@NonNull File file, @Nullable Callback callBack);

        void onNewFileCreated(@NonNull File file);

        void clickRemoveFile(File file, Callback callback);

        void onClickNewButton(File file, Callback callback);
    }

    public interface Callback {
        void onSuccess(File file);

        void onFailed(@Nullable Exception e);
    }
}
