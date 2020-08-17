package com.mrikso.apkrepacker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.filepicker.FilePickerDialog;
import com.mrikso.apkrepacker.fragment.base.BaseFilesFragment;
import com.mrikso.apkrepacker.recycler.OnItemSelectedListener;
import com.mrikso.apkrepacker.ui.filemanager.PathButtonAdapter;
import com.mrikso.apkrepacker.ui.filemanager.PanelActions;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.utils.CopyHelper;
import com.mrikso.apkrepacker.ui.filemanager.utils.Utils;
import com.mrikso.apkrepacker.ui.imageviewer.ImageViewerActivity;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.utils.ViewUtils;
import com.mrikso.apkrepacker.viewmodel.FilesFragmentViewModel;
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class FilesFragment extends BaseFilesFragment implements OnBackPressedListener, OnItemSelectedListener,
        View.OnClickListener, PopupMenu.OnMenuItemClickListener {

    private File mCurrentDirectory;
    private File mSelectedFile;
    private String mProjectPath;
    private FloatingActionMenu mFab;
    private RecyclerView mPathBar;
    private LinearLayout mSelectionBar;
    private LinearLayout mPathBarContainer;
    private LinearLayout mBottomMenuContainer;
    private PathButtonAdapter mPathAdapter;
    private AppCompatTextView mSelectedCount, mPaste;
    private AppCompatImageButton mClearSelection;
    private AppCompatImageButton mSelectAll, mCut, mDelete, mCopy, mRename, mMoreMenuBottomBar, mCreateDirectory;

   // private ArrayList<FileHolder> mSelected = new ArrayList<>();
    private PanelActions mActions;
    private CopyHelper mCopyHelper;

    private boolean isCanPaste = false;

    private FilesFragmentViewModel mViewModel;

    public FilesFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            mProjectPath = bundle.getString("prjPatch");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(this).get(FilesFragmentViewModel.class);
        return inflater.inflate(R.layout.fragment_files, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
    }

    @Override
    protected void onDataApplied() {
        super.onDataApplied();


        mCurrentDirectory = new File(getPath());
        mPathAdapter.setPath(mCurrentDirectory, true);
        mPathBar.setAdapter(mPathAdapter);

        getFileAdapter().setOnItemSelectedListener(this);
    }

    private void initViews(View view) {
        mActions = PanelActions.getInstance(requireContext(), this);
        mPathBar = view.findViewById(R.id.pathScrollView);
        mFab = view.findViewById(R.id.fab_menu);
        mBottomMenuContainer = view.findViewById(R.id.bottom_menu);
        AppCompatImageButton homeFolder = view.findViewById(R.id.home_folder_app);
        homeFolder.setOnClickListener(this);
        FloatingActionButton addFile = view.findViewById(R.id.fab_add_file);
        addFile.setOnClickListener(this);
        FloatingActionButton addFolder = view.findViewById(R.id.fab_add_folder);
        addFolder.setOnClickListener(this);
        FloatingActionButton copyNewFile = view.findViewById(R.id.fab_copy_file);
        copyNewFile.setOnClickListener(this);
        FloatingActionButton copyNewFolder = view.findViewById(R.id.fab_copy_folder);
        copyNewFolder.setOnClickListener(this);
        FloatingActionButton search = view.findViewById(R.id.fab_search);
        search.setOnClickListener(this);

        mPathAdapter = new PathButtonAdapter();

        mCopy = view.findViewById(R.id.action_copy);
        mCopy.setOnClickListener(this);
        mCut = view.findViewById(R.id.action_cut);
        mCut.setOnClickListener(this);
        mDelete = view.findViewById(R.id.action_delete);
        mDelete.setOnClickListener(this);
        mRename = view.findViewById(R.id.action_rename);
        mRename.setOnClickListener(this);

        mCreateDirectory = view.findViewById(R.id.action_new_folder);
        mCreateDirectory.setOnClickListener(this);
        mPaste = view.findViewById(R.id.action_paste);
        mPaste.setOnClickListener(this);

        mMoreMenuBottomBar = view.findViewById(R.id.action_overflow);
        mMoreMenuBottomBar.setOnClickListener(this);

        mPathBarContainer = view.findViewById(R.id.container_path);
        mSelectionBar = view.findViewById(R.id.container_selection_bar);
        mSelectAll = view.findViewById(R.id.action_select_all);
        mSelectAll.setOnClickListener(this);
        mClearSelection = view.findViewById(R.id.ib_clear_selection);
        mSelectedCount = view.findViewById(R.id.tv_selection_status);

        mPathBar.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mPathAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mPathBar.scrollToPosition(mPathAdapter.getItemCount() - 1);
            }
        });

        mPathAdapter.setOnItemClickListener((position, v) -> {
            File file = mPathAdapter.getItem(position);
            setPath(file);
            refresh();
            if (Objects.equals(file.getParent(), new File(getPath()).getParent())) {
                savePosition(false);
            }
        });
        mFab.setClosedOnTouchOutside(true);
        getFileAdapter().setProjectMode(true);
        setPath(new File(mProjectPath));
        refresh();
    }

    @Override
    public void onItemSelected() {
        invalidatePathAdapter();
    }

    private void invalidatePathAdapter() {
        if (getFileAdapter() != null) {
            boolean isSelected = getFileAdapter().anySelected();
            ViewUtils.setVisibleOrGone(mFab, !isSelected);
            ViewUtils.setVisibleOrGone(mBottomMenuContainer, isSelected);
            ViewUtils.setVisibleOrGone(mSelectionBar, isSelected);
            ViewUtils.setVisibleOrGone(mPathBarContainer, !isSelected);
            if (isSelected) {
                mCopyHelper = App.get().getCopyHelper();
                mCopyHelper.setFilesFragment(this);
            //    mSelected.addAll(getFileAdapter().getSelectedItems());
                mSelectAll.setOnClickListener(v -> getFileAdapter().selectAll());
                mClearSelection.setOnClickListener(v -> getFileAdapter().clearSelection());
                mSelectedCount.setText(getString(R.string.selected, getFileAdapter().getSelectedItemCount()));
            }
        }
    }

    private void invalidateClipboardPanel() {
        getFileAdapter().clearSelection();
        ViewUtils.setVisibleOrGone(mFab, !isCanPaste);
        ViewUtils.setVisibleOrGone(mBottomMenuContainer, isCanPaste);
        ViewUtils.setVisibleOrGone(mSelectionBar, isCanPaste);
        ViewUtils.setVisibleOrGone(mPathBarContainer, !isCanPaste);
        ViewUtils.setVisibleOrGone(mCreateDirectory, isCanPaste);
        ViewUtils.setVisibleOrGone(mPaste, isCanPaste);

        ViewUtils.setVisibleOrGone(mCopy, !isCanPaste);
        ViewUtils.setVisibleOrGone(mCut, !isCanPaste);
        ViewUtils.setVisibleOrGone(mRename, !isCanPaste);
        ViewUtils.setVisibleOrGone(mDelete, !isCanPaste);
        ViewUtils.setVisibleOrGone(mMoreMenuBottomBar, !isCanPaste);
        ViewUtils.setVisibleOrGone(mSelectAll, !isCanPaste);
        mClearSelection.setOnClickListener(v -> {
            mCopyHelper.clear();
            isCanPaste = false;
            invalidateClipboardPanel();
        });
        int stringResource = (mCopyHelper.getOperationType() == CopyHelper.COPY
                ? R.plurals.menu_copy_items_to : R.plurals.menu_move_items_to);
        mSelectedCount.setText(getResources().getQuantityString(stringResource,
                mCopyHelper.getItemCount(), mCopyHelper.getItemCount()));

    }

    @Override
    public boolean onBackPressed() {
        if (getFileAdapter().anySelected()) {
            getFileAdapter().clearSelection();
            return true;
        }
        savePosition(false);

        Fragment manager1 = getChildFragmentManager().findFragmentByTag(SearchFragment.TAG);
        Fragment manager2 = getChildFragmentManager().findFragmentByTag(ColorEditorFragment.TAG);
//        Fragment manager3 = getChildFragmentManager().findFragmentByTag(DimensEditorFragment.TAG);

        if (manager1 != null) {
            getChildFragmentManager().popBackStack();
            return true;
        } else if (manager2 != null) {
            getChildFragmentManager().popBackStack();
            return true;
/*        } else if (manager3 != null) {
            getChildFragmentManager().popBackStack();
            return true;*/
        } else if (Utils.backWillExit(mProjectPath, getPath())) {
            requireActivity().finish();
            return true;
        } else {
            setPath(new File(Utils.downDir(1, getPath())));
            refresh();
            return true;
        }

    }

    @Override
    public void onFileClick(FileHolder item, int position) {
        File file = null;
        if (getFileAdapter() != null && position >= 0) {
            file = item.getFile();
            if (getFileAdapter().anySelected()) {
                getFileAdapter().toggle(position);
                return;
            }
        }

        if (file != null) {
            if (file.isDirectory()) {
                if (file.canRead()) {
                    setPath(file);
                    savePosition(true);
                    refresh();
                } else {
                    UIUtils.toast(requireContext(), R.string.cannt_open_directory);
                }
            } else {
                switch (FileUtil.FileType.getFileType(file)) {
                    case TXT:
                    case SMALI:
                    case JS:
                    case JSON:
                    case HTM:
                    case HTML:
                    case INI:
                    case XML:
                        if (file.getName().startsWith("colors")) {
                            Fragment colorEditorFragment = ColorEditorFragment.newInstance(file.getAbsolutePath());
                            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, colorEditorFragment, ColorEditorFragment.TAG).addToBackStack(null).commit();
/*                        } else if (file.getName().equals("dimens.xml")) {
                            Fragment dimensEditorFragment = DimensEditorFragment.newInstance(file.getAbsolutePath());
                            getChildFragmentManager().beginTransaction().replace(R.id.fragment_container, dimensEditorFragment, DimensEditorFragment.TAG).addToBackStack(null).commit();*/
                        } else if ((mCurrentDirectory.getName().startsWith("drawable") || mCurrentDirectory.getName().startsWith("mipmap")) && new VectorMasterDrawable(requireContext(), file).isVector()) {
                            ImageViewerActivity.setViewerData(getContext(), getFileAdapter(), file);
                            startActivity(new Intent(getActivity(), ImageViewerActivity.class));
                        } else {
                            Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
                            intent.putExtra("filePath", file.getAbsolutePath());
                            intent.putExtra("currentDirectory", mCurrentDirectory.getAbsolutePath());
                            startActivity(intent);
                        }
                        break;
                    case IMAGE:
//                    case TTF:
                        ImageViewerActivity.setViewerData(getContext(), getFileAdapter(), file);
                        startActivity(new Intent(getActivity(), ImageViewerActivity.class));
                        break;
                    default:
                        startActivity(IntentUtils.openFileWithIntent(file));
                        break;
                }
            }
        }
    }

    @Override
    public void onLongClick(FileHolder item, int position) {
        getFileAdapter().toggle(position);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_copy:
                if (getFileAdapter().getSelectedItemCount() > 1) {
                    mCopyHelper.copy(getFileAdapter().getSelectedItems());
                } else {
                    mCopyHelper.copy(getFileAdapter().getSelectedItems().get(0));
                }
                isCanPaste = mCopyHelper.canPaste();
                invalidateClipboardPanel();
                break;
            case R.id.action_delete:
                UIUtils.showConfirmDialog(requireContext(), R.string.confirm_delete, new UIUtils.OnClickCallback() {
                    @Override
                    public void onOkClick() {
                        FileHolder[] params = getFileAdapter().getSelectedItems().toArray(new FileHolder[getFileAdapter().getSelectedItemCount()]);
                        mActions.actionDelete(params);
                        getFileAdapter().clearSelection();
                    }
                });
                break;
            case R.id.action_share:
                mActions.actionShare(getFileAdapter().getSelectedItems().get(0));
                break;
            case R.id.action_rename:
                if (getFileAdapter().getSelectedItemCount() > 1) {
                    Toast.makeText(requireContext(), "Multiple rename doesent supported!", Toast.LENGTH_SHORT).show();
                    // FragmentUtils.add(new BatchRenameFragment(), getParentFragmentManager(), android.R.id.content);
                } else {
                    mActions.actionRenameFile(getFileAdapter().getSelectedItems().get(0).getFile());
                }
                break;
            case R.id.action_cut:
                if (getFileAdapter().getSelectedItemCount() > 1) {
                    mCopyHelper.cut(getFileAdapter().getSelectedItems());
                } else {
                    mCopyHelper.cut(getFileAdapter().getSelectedItems().get(0));
                }
                isCanPaste = mCopyHelper.canPaste();
                invalidateClipboardPanel();
                break;
            case R.id.action_paste:
                if (mCopyHelper.canPaste()) {
                    mCopyHelper.paste(requireContext(), new File(getPath()));
                } else {
                    // Toast.makeText(requireContext(), R.string.nothing_to_paste, Toast.LENGTH_LONG).show();
                }
                isCanPaste = false;
                invalidateClipboardPanel();
                break;
            case R.id.action_overflow:
                if (getFileAdapter().getSelectedItemCount() == 1) {
                    FileHolder fileHolder = getFileAdapter().getSelectedItems().get(0);
                    mSelectedFile = fileHolder.getFile();
                    PopupMenu popupMenu = new PopupMenu(requireContext(), v);
                    popupMenu.inflate(R.menu.filemanager_project_menu);
                    popupMenu.setOnMenuItemClickListener(this);
                    popupMenu.getMenu().findItem(R.id.action_open_with).setVisible(mSelectedFile.isFile());
                    popupMenu.getMenu().findItem(R.id.action_open_in_editor).setVisible(mSelectedFile.isFile());
                    popupMenu.getMenu().findItem(R.id.action_share).setVisible(mSelectedFile.isFile());
                    popupMenu.getMenu().findItem(R.id.action_copy_id).setVisible(mSelectedFile.isFile() && mSelectedFile.getAbsolutePath().contains(mProjectPath + "/res/"));
                    popupMenu.show();
                } else {

                }
                break;
            case R.id.fab_search:
                mFab.close(true);
                Bundle bundle = new Bundle();
                bundle.putString("curDirect", getPath());
                SearchFragment searchFragment = new SearchFragment();
                searchFragment.setArguments(bundle);
                FragmentUtils.add(searchFragment, getChildFragmentManager(), R.id.fragment_container, SearchFragment.TAG);
                break;
            case R.id.fab_add_file:
                mFab.close(true);
                mActions.actionCreateFile(new File(getPath()));
                break;
            case R.id.action_new_folder:
            case R.id.fab_add_folder:
                mFab.close(true);
                mActions.actionCreateNewDirectory(new File(getPath()));
                break;
            case R.id.fab_copy_folder:
                mFab.close(true);
                new FilePickerDialog(requireContext())
                        .setTitleText(getString(R.string.select_directory))
                        .setSelectMode(FilePickerDialog.MODE_SINGLE)
                        .setSelectType(FilePickerDialog.TYPE_DIR)
                        .setRootDir(FileUtil.getInternalStorage().getAbsolutePath())
                        .setBackCancelable(true)
                        .setOutsideCancelable(true)
                        .setDialogListener(getString(R.string.choose_button_label), getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                            @Override
                            public void onSelectedFilePaths(String[] filePaths) {
                                new Thread(() -> {
                                    for (String dir : filePaths) {
                                        try {
                                            FileUtil.copyFile(new File(dir), new File(getPath()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                                refresh();
                            }

                            @Override
                            public void onCanceled() {
                            }
                        })
                        .show();
                break;
            case R.id.fab_copy_file:
                mFab.close(true);
                new FilePickerDialog(requireContext())
                        .setTitleText(getString(R.string.select_file))
                        .setSelectMode(FilePickerDialog.MODE_MULTI)
                        .setSelectType(FilePickerDialog.TYPE_FILE)
                        .setRootDir(FileUtil.getInternalStorage().getAbsolutePath())
                        .setBackCancelable(true)
                        .setOutsideCancelable(true)
                        .setDialogListener(getString(R.string.choose_button_label), getString(R.string.cancel_button_label), new FilePickerDialog.FileDialogListener() {
                            @Override
                            public void onSelectedFilePaths(String[] filePaths) {
                                new Thread(() -> {
                                    for (String dir : filePaths) {
                                        try {
                                            FileUtil.copyFile(new File(dir), new File(getPath()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).start();
                                refresh();
                            }

                            @Override
                            public void onCanceled() {
                            }
                        })
                        .show();
                break;
            case R.id.home_folder_app:
                setPath(new File(mProjectPath));
                refresh();
                break;
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_open_with:
                startActivity(IntentUtils.openFileWithIntent(mSelectedFile));
                //refresh();
                return true;
            case R.id.action_open_in_editor:
                Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
                intent.putExtra("filePath", mSelectedFile.getAbsolutePath());
                startActivity(intent);
                //refresh();
                return true;
            case R.id.action_share:
                mActions.actionShare(getFileAdapter().getSelectedItems().get(0));
                //refresh();
                return true;
            case R.id.action_copy_name:
                StringUtils.setClipboard(requireContext(), mSelectedFile.getName());
                UIUtils.toast(requireContext(), getString(R.string.toast_copy_to_clipboard));
                return false;
            case R.id.action_copy_path:
                StringUtils.setClipboard(requireContext(), mSelectedFile.getAbsolutePath());
                UIUtils.toast(requireContext(), getString(R.string.toast_copy_to_clipboard));
                return false;
            case R.id.action_copy_id:

                return false;
        }
        return false;
    }

}
