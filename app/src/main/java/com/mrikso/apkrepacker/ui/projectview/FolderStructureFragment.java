package com.mrikso.apkrepacker.ui.projectview;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.IOUtils;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.dialogs.CreateNewClass;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.ProjectFileOptionDialog;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class FolderStructureFragment extends Fragment implements ProjectFileContract.View, ProjectFileContract.FileActionListener,
        ProjectFileOptionDialog.ItemClickListener {
    public static final String TAG = "FolderStructureFragment";
    private static final int REQUEST_PICK_FILE = 498;

    private File mLastSelectedDir = null;
    @Nullable
    private FileChangeListener mParentListener;

    private ViewGroup mContainerView;
    private AppCompatTextView mTxtProjectName;
    private AndroidTreeView mTreeView;

    private SharedPreferences mPreferences;
    @Nullable
    private File mProject;
    private android.widget.ProgressBar progressBar;
    private Context mContext;

    private TreeNode.TreeNodeClickListener mNodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            FolderHolder.TreeItem i = (FolderHolder.TreeItem) value;
            File file = i.getFile();
            if (mParentListener != null && file.isFile()) {
                mParentListener.doOpenFile(file);
            }
        }
    };
    private TreeNode.TreeNodeLongClickListener mNodeLongClickListener = (node, value) -> {
        FolderHolder.TreeItem i = (FolderHolder.TreeItem) value;
        File file = i.getFile();
        if (file.isFile()) {
            showFileInfo(file);
        } else if (file.isDirectory()) {
            showDialogNew(file);
        }
        return true;
    };

    /**
     * Create folder view, project can be null, we will init after
     */
    public static FolderStructureFragment newInstance(@Nullable File project) {
        FolderStructureFragment fragment = new FolderStructureFragment();
        fragment.setProject(project);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder_structure, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mContext = view.getContext();
        mContainerView = view.findViewById(R.id.container);
        progressBar = view.findViewById(R.id.progress_bar);
        mTxtProjectName = view.findViewById(R.id.txt_project_name);
        //Runnable frame = () -> new LoadTree().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mProject);
        view.findViewById(R.id.img_refresh).setOnClickListener(view13 -> {
            progressBar.setVisibility(View.VISIBLE);
            mContainerView.setVisibility(View.GONE);
            //     frame.run();
        });

        //Runnable frame = () -> new LoadTree().execute(mProject);
        //  frame.run();
        display(mProject, false);
        view.findViewById(R.id.img_expand_all).setOnClickListener(view1 -> {
            if (mTreeView != null) expand(mTreeView.getRoot());
        });
        view.findViewById(R.id.img_collapse).setOnClickListener(view12 -> {
            if (mTreeView != null) mTreeView.collapseAll();
        });
        if (savedInstanceState != null) {
            if (mTreeView != null) {
                String state = savedInstanceState.getString("tState");
                if (!TextUtils.isEmpty(state)) {
                    mTreeView.restoreState(state);
                }
            }
        } else if (mTreeView != null) {
            String state = mPreferences.getString("tree_state", "");
            if (!state.isEmpty()) mTreeView.restoreState(state);
        }

    }

    @Nullable
    private TreeNode createFileStructure(@Nullable File projectFile) {
        if (projectFile == null)
            return null;
        File rootDir = projectFile;
        TreeNode root = new TreeNode(new FolderHolder.TreeItem(rootDir, rootDir, this));
        try {
            root.addChildren(getNode(rootDir, rootDir));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    private ArrayList<TreeNode> getNode(File projectFile, File parent) {
        ArrayList<TreeNode> nodes = new ArrayList<>();
        try {
            if (parent.isDirectory()) {
                File[] child = parent.listFiles();

                if (child != null) {
                    ArrayList<File> dirs = new ArrayList<>();
                    ArrayList<File> files = new ArrayList<>();
                    for (File file : child) {
                        if (file.isFile() && !file.isHidden()) files.add(file);
                        if (file.isDirectory() && !file.isHidden()) dirs.add(file);
                    }
                    Collections.sort(dirs, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                    Collections.sort(files, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                    for (File file : dirs) {
                        TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, file, this));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(projectFile, file));
                        }
                        nodes.add(node);
                    }
                    for (File file : files) {
                        TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, file, this));
                        if (file.isDirectory()) {
                            node.addChildren(getNode(projectFile, file));
                        }
                        nodes.add(node);
                    }
                }
            } else {
                TreeNode node = new TreeNode(new FolderHolder.TreeItem(projectFile, parent, this));
                nodes.add(node);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nodes;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mTreeView != null) {
            outState.putString("tState", mTreeView.getSaveState());
        }
    }

    @Override
    public void display(File projectFile, boolean expand) {
        this.mProject = projectFile;
        if (mProject != null && mTxtProjectName != null) {
            mTxtProjectName.setText(mProject.getName());
        }

        TreeNode root = refresh();
        if (expand && mTreeView != null) {
            expand(root);
        }
    }

    private void expand(TreeNode root) {
        if (mTreeView == null || mProject == null) {
            return;
        }
        expandRecursive(root, node -> {
            FolderHolder.TreeItem value = (FolderHolder.TreeItem) node.getValue();
            if (value == null) {
                return true;
            }
            File file = value.getFile();
            return !(file.getName().equals("lib"));
        });
    }

    private void expandRecursive(TreeNode node, Predicate<TreeNode> test) {
        if (test.accept(node)) {
            mTreeView.expandNode(node, false);
            if (node.getChildren().size() > 0) {
                for (TreeNode treeNode : node.getChildren()) {
                    expandRecursive(treeNode, test);
                }
            }
        }
    }

    @Override
    public TreeNode refresh() {
        if (mProject == null) {
            return null;
        }
        TreeNode root = TreeNode.root();
        Runnable frame = () -> new LoadTree().execute( root);
        frame.run();


        return root;
    }

    @Override
    public void setPresenter(ProjectFileContract.Presenter presenter) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.mParentListener = (FileChangeListener) getActivity();
        } catch (ClassCastException ignored) {
        }
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public void onDestroyView() {
        if (mTreeView != null) {
            String saveState = mTreeView.getSaveState();
            mPreferences.edit().putString("tree_state", saveState).apply();
        }
        super.onDestroyView();
    }

    /**
     * show dialog with file info
     * filePath, path, size, extension ...
     *
     * @param file - file to show info
     */
    private void showFileInfo(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(file.getName());
        String message =
                "Path: " + file.getPath() + "\n" +
                        "Size: " + file.length() + " byte";
        builder.setMessage(message);
        builder.create().show();
    }

    public void setProject(File mProject) {
        this.mProject = mProject;
    }

    @Override
    public void onFileClick(@NonNull File file, @Nullable ProjectFileContract.Callback callBack) {

    }

    @Override
    public void onNewFileCreated(@NonNull File file) {
        if (mParentListener != null) {
            mParentListener.doOpenFile(file);
        }
    }

    private void showDialogNew(@NonNull File parent) {
        mLastSelectedDir = parent;
        ProjectFileOptionDialog fragment = ProjectFileOptionDialog.newInstance();
        fragment.show(getChildFragmentManager(), ProjectFileOptionDialog.TAG);
    }

    @Override
    public void clickRemoveFile(final File file, final ProjectFileContract.Callback callback) {
        try {
            FileUtil.deleteFile(file);
            callback.onSuccess(file);
            if (file.isDirectory()) {
                UIUtils.toast(App.getContext(), String.format(getString(R.string.toast_deleted_dictionary),
                        file.getName()));
            } else {
                UIUtils.toast(App.getContext(), String.format(getString(R.string.toast_deleted_item),
                        file.getName()));
            }
            if (mParentListener != null) {
                mParentListener.onFileDeleted(file);
            }
        } catch (Exception e) {
            callback.onFailed(e);
            UIUtils.toast(App.getContext(), R.string.toast_error_on_delete_file);
            DLog.e(e);
        }
    }

    @Override
    public void onClickNewButton(File file, ProjectFileContract.Callback callback) {
        showDialogNew(file);
    }

    @Override
    public void onFileItemClick(Integer item) {
        if (mProject != null)
            switch (item) {
                case R.id.create_class_file:
                    CreateNewClass createNewClass = new CreateNewClass(mContext, mLastSelectedDir,
                            new CreateNewClass.OnFileCreatedListener() {
                                @Override
                                public void onCreateSuccess(File file) {
                                    mParentListener.onFileCreated(file);
                                }

                                @Override
                                public void onCreateFailed(File file, Exception e) {
                                    //callback.onFailed(e);
                                }
                            });
                    createNewClass.show();
                    break;
                case R.id.create_xml_file:
                    UIUtils.showInputDialog(mContext, R.string.action_create_xml, 0, null, EditorInfo.TYPE_CLASS_TEXT,
                            new UIUtils.OnShowInputCallback() {
                                @Override
                                public void onConfirm(CharSequence input) {
                                    try {
                                        createNewFile(input.toString() + ".xml", mLastSelectedDir);
                                    } catch (Exception e) {
                                        DLog.e(e);
                                    }
                                }
                            });
                    break;
                case R.id.add_new_folder:
                    UIUtils.showInputDialog(mContext, R.string.action_create_new_folder, 0, null, EditorInfo.TYPE_CLASS_TEXT,
                            new UIUtils.OnShowInputCallback() {
                                @Override
                                public void onConfirm(CharSequence input) {
                                    try {
                                        FileUtil.createDirectory(mLastSelectedDir, input.toString());
                                    } catch (Exception e) {
                                        UIUtils.toast(App.getContext(), R.string.toast_error_on_add_folder);
                                        DLog.e(e);
                                    }
                                }
                            });
                    break;
                case R.id.select_file:
                    selectNewFile();
                    break;
            }
    }

    private void selectNewFile() {
        new FilePickerDialog(mContext)
                .setTitleText(this.getResources().getString(R.string.select_directory))
                .setSelectMode(FilePickerDialog.MODE_MULTI)
                .setSelectType(FilePickerDialog.TYPE_ALL)
                .setRootDir(Environment.getExternalStorageDirectory().getAbsolutePath())
                .setBackCancelable(true)
                .setOutsideCancelable(true)
                .setDialogListener(this.getResources().getString(R.string.choose_button_label), this.getResources().getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                    @Override
                    public void onSelectedFilePaths(String[] filePaths) {
                        for (String file : filePaths) {
                            try {
                                FileUtil.copyFile(new File(file), mLastSelectedDir);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCanceled() {
                    }
                })
                .show();
    }

    private void createNewFile(String fileName, File currentFolder) {

        try {
            if (!fileName.endsWith(".xml")) {
                fileName += ".xml";
            }
            File xmlFile = new File(currentFolder, fileName);
            xmlFile.getParentFile().mkdirs();

            String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
            if (currentFolder.getName().matches("^color(-v[0-9]+)?")) {
                content += "<selector>\n</selector>";
            } else if (currentFolder.getName().matches("^menu(-v[0-9]+)?")) {
                content += "<menu " +
                        "xmlns:android=\"http://schemas.android.com/apk/res/android\">\n" +
                        "\n" +
                        "</menu>";
            } else if (currentFolder.getName().matches("^values(-v[0-9]+)?")) {
                content += "<resources>\n</resources>";
            } else if (currentFolder.getName().matches("^layout(-v[0-9]+)?")) {
                content += "<LinearLayout\n" +
                        "    xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
                        "    android:layout_width=\"match_parent\"\n" +
                        "    android:layout_height=\"match_parent\">" +
                        "\n" +
                        "</LinearLayout>";
            }

            IOUtils.writeFile(xmlFile, content);
            mParentListener.onFileCreated(xmlFile);
        } catch (Exception e) {
            UIUtils.toast(mContext, "Can not create new file");
        }
    }

    public interface Predicate<T> {
        boolean accept(T t);
    }

    private class LoadTree extends CoroutinesAsyncTask<TreeNode, CharSequence, Boolean> {

        @Override
        public Boolean doInBackground(TreeNode... root) {
            TreeNode fileStructure = createFileStructure(mProject);
            if (fileStructure != null) {
                root[0].addChildren(fileStructure);
            }

            String saveState = null;
            if (mTreeView != null) {
                saveState = mTreeView.getSaveState();
            }

            mTreeView = new AndroidTreeView(mContext, root[0]);
            mTreeView.setDefaultAnimation(false);
            mTreeView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
            mTreeView.setDefaultViewHolder(FolderHolder.class);
            mTreeView.setDefaultNodeClickListener(mNodeClickListener);
            mTreeView.setDefaultNodeLongClickListener(mNodeLongClickListener);
            if (saveState != null) {
                mTreeView.restoreState(saveState);
            }
            return true;
        }

        @Override
        public void onPostExecute(Boolean result) {
            progressBar.setVisibility(View.GONE);
            mContainerView.setVisibility(View.VISIBLE);
            mContainerView.removeAllViews();
            View view = mTreeView.getView();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mContainerView.addView(view, params);
        }
    }

}
