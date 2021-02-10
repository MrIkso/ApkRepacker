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

import com.mrikso.apkrepacker.ui.projectview.ProjectTreeStructureFragment;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ProjectUtils;

import java.io.File;

public class CodeEditorActivity  extends BaseActivity  {
   // protected ProjectFileContract.Presenter mFilePresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

 //   @Override
    protected int getRootLayoutId() {
        return R.layout.activity_code_editor;
    }

   /* @Override
    public void onFileDeleted(File deleted) {
        Pair<Integer, IEditorDelegate> position = mTabManager.getEditorDelegate(deleted);
        if (position != null) {
            mTabManager.closeTab(position.first);
        }
    }

    @Override
    public void onFileCreated(File newFile) {
        mFilePresenter.refresh(new File(ProjectUtils.getProjectPath()));
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
    }*/
    /**
     * @return current file selected
     */
   /* @Nullable
    protected File getCurrentFile() {
        EditorDelegate editorFragment = getCurrentEditorDelegate();
        if (editorFragment != null) {
            return editorFragment.getDocument().getFile();
        }
        return null;
    }
*/
    //@Override
    protected void initLeftNavigationView(@NonNull NavigationView nav) {
     //   super.initLeftNavigationView(nav);
        String tag = ProjectTreeStructureFragment.TAG;
      //  FolderStructureFragment folderStructureFragment = FolderStructureFragment.newInstance(new File(ProjectUtils.getProjectPath()));
        ViewGroup viewGroup = nav.findViewById(R.id.right_navigation_view);
        viewGroup.removeAllViews();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.right_navigation_view, new ProjectTreeStructureFragment(), tag).commit();
       // mFilePresenter = new ProjectFilePresenter(folderStructureFragment);
    }
}
