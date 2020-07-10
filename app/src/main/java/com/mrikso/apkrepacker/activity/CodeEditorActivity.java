package com.mrikso.apkrepacker.activity;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentTransaction;

import com.mrikso.apkrepacker.ide.editor.EditorDelegate;
import com.mrikso.apkrepacker.ide.editor.IEditorDelegate;
import com.google.android.material.navigation.NavigationView;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.projectview.FileChangeListener;
import com.mrikso.apkrepacker.ui.projectview.FolderStructureFragment;
import com.mrikso.apkrepacker.ui.projectview.ProjectFileContract;
import com.mrikso.apkrepacker.ui.projectview.ProjectFilePresenter;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.File;

public class CodeEditorActivity  extends IdeActivity implements FileChangeListener {
    protected ProjectFileContract.Presenter mFilePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getRootLayoutId() {
        return R.layout.activity_code_editor;
    }

    @Override
    public void onFileDeleted(File deleted) {
        Pair<Integer, IEditorDelegate> position = mTabManager.getEditorDelegate(deleted);
        if (position != null) {
            mTabManager.closeTab(position.first);
        }
    }

    @Override
    public void onFileCreated(File newFile) {
        mFilePresenter.refresh(new File(FileUtil.getProjectPath()));
        openFile(newFile.getPath());
    }

    @Override
    public void doOpenFile(File toEdit) {
       // if (CodeEditUtils.canEdit(toEdit)) {
            //save current file
            openFile(toEdit.getPath());
            //close drawer
            closeDrawers();
      //  } else {

       // }
    }
    /**
     * @return current file selected
     */
    @Nullable
    protected File getCurrentFile() {
        EditorDelegate editorFragment = getCurrentEditorDelegate();
        if (editorFragment != null) {
            return editorFragment.getDocument().getFile();
        }
        return null;
    }

    @Override
    protected void initLeftNavigationView(@NonNull NavigationView nav) {
        super.initLeftNavigationView(nav);
        String tag = FolderStructureFragment.TAG;
        FolderStructureFragment folderStructureFragment = FolderStructureFragment.newInstance(new File(FileUtil.getProjectPath()));
        ViewGroup viewGroup = nav.findViewById(R.id.left_navigation_content);
        viewGroup.removeAllViews();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.left_navigation_content, folderStructureFragment, tag).commit();
        mFilePresenter = new ProjectFilePresenter(folderStructureFragment);
    }
}
