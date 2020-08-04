package com.mrikso.apkrepacker.fragment;

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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.base.BaseFilesFragment;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.ApkOptionsDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.DecompileOptionsDialogFragment;
import com.mrikso.apkrepacker.recycler.OnItemSelectedListener;
import com.mrikso.apkrepacker.task.ImportFrameworkTask;
import com.mrikso.apkrepacker.task.SignTask;
import com.mrikso.apkrepacker.ui.filemanager.PanelActions;
import com.mrikso.apkrepacker.ui.filemanager.PathButtonAdapter;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.utils.CopyHelper;
import com.mrikso.apkrepacker.ui.filemanager.utils.Utils;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.SignUtil;
import com.mrikso.apkrepacker.utils.ViewUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MyFilesFragment extends BaseFilesFragment implements View.OnClickListener, ApkOptionsDialogFragment.ItemClickListener,
        DecompileOptionsDialogFragment.ItemClickListener, OnItemSelectedListener, OnBackPressedListener,
        PopupMenu.OnMenuItemClickListener {

    public static final String TAG = "MyFilesFragment";

    private static File selectedApk;
    public File signedApk;
    private FloatingActionMenu mFab;
    private RecyclerView mPathBar;
    private LinearLayout mSelectionBar;
    private LinearLayout mPathBarContainer;
    private LinearLayout mBottomMenuContainer;
    private PathButtonAdapter mPathAdapter;
    private DecompileFragment mDecompileFragment;
    private DialogFragment dialog;
    private AppCompatTextView mSubtitle;
    private AppCompatTextView mSelectedCount, mPaste;
    private AppCompatImageButton mClearSelection;
    private AppCompatImageButton mSelectAll, mCut, mDelete, mCopy, mRename, mMoreMenuBottomBar, mCreateDirectory;

    private ArrayList<FileHolder> mSelected = new ArrayList<>();
    private PanelActions mActions;
    private CopyHelper mCopyHelper;

    private boolean signedMode = false;
    private boolean isCanPaste = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filelist_simple, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mActions = PanelActions.getInstance(requireContext(), this);
        mPathBar = view.findViewById(R.id.pathScrollView);
        mFab = view.findViewById(R.id.fab_menu);
        mSubtitle = view.findViewById(R.id.ab_subtitle);
        mBottomMenuContainer = view.findViewById(R.id.bottom_menu);
        FloatingActionButton addFile = view.findViewById(R.id.fab_add_file);
        addFile.setOnClickListener(this);
        FloatingActionButton addFolder = view.findViewById(R.id.fab_add_folder);
        addFolder.setOnClickListener(this);

        mPathAdapter = new PathButtonAdapter();
        initView(view);
    }

    private void initView(View view) {
        AppCompatImageButton mSearchIb = view.findViewById(R.id.action_search);
        mSearchIb.setOnClickListener(this);
        AppCompatImageButton mMoreMenuToolBar = view.findViewById(R.id.action_overflow_toolbar);
        mMoreMenuToolBar.setOnClickListener(this);
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
        });
        //  mPathBar.setAdapter(mPathAdapter);
        mFab.setClosedOnTouchOutside(true);
    }


    @Override
    protected void onDataApplied() {
        super.onDataApplied();
        // savePosition(true);
        if (mDirsCount > 0 && mFilesCount > 0) {
            mSubtitle.setText(String.format(Locale.ENGLISH, "%1d folders, %2d files", mDirsCount, mFilesCount));
        } else if (mDirsCount == 0 && mFilesCount > 0) {
            mSubtitle.setText(String.format(Locale.ENGLISH, "%d files", mFilesCount));
        } else if (mFilesCount == 0 && mDirsCount > 0) {
            mSubtitle.setText(String.format(Locale.ENGLISH, "%d folders", mDirsCount));
        } else {
            mSubtitle.setText(getString(R.string.this_folder_is_empty));
        }

        mPathAdapter.setPath(new File(getPath()), false);
        mPathBar.setAdapter(mPathAdapter);
        getFileAdapter().setOnItemSelectedListener(this);
    }

    /**
     * Point this Fragment to show the contents of the passed file.
     *
     * @param f If same as current, does nothing.
     */
    private void open(FileHolder f) {
        if (!f.getFile().exists())
            return;

        if (f.getFile().isDirectory()) {
            openDir(f);
        } else if (f.getFile().isFile()) {
            openFile(f);
        }
    }

    private void openFile(FileHolder fileholder) {
        if ("apk".equals(fileholder.getExtension())) {
            selectedApk = fileholder.getFile();
            ApkOptionsDialogFragment fragment = ApkOptionsDialogFragment.newInstance();
            fragment.show(getChildFragmentManager(), ApkOptionsDialogFragment.TAG);
        } else {
            startActivity(IntentUtils.openFileWithIntent(fileholder.getFile()));
        }
    }

    /**
     * Attempts to open a directory for browsing.
     * Override this to handle folder click behavior.
     *
     * @param fileHolder The holder of the directory to open.
     */
    private void openDir(FileHolder fileHolder) {
        // Avoid unnecessary attempts to load.
        if (fileHolder.getFile().getAbsolutePath().equals(getPath()))
            return;


        // Load
        setPath(fileHolder.getFile());
        refresh();
    }

    @Override
    public void onApkItemClick(Integer item) {
        switch (item) {
            case R.id.decompile_app:
                PreferenceHelper preferenceHelper = PreferenceHelper.getInstance(requireContext());
                int mode = preferenceHelper.getDecodingMode();
                if (mode == 3) {
                    DecompileOptionsDialogFragment decompileOptionsDialogFragment = DecompileOptionsDialogFragment.newInstance();
                    decompileOptionsDialogFragment.show(getChildFragmentManager(), DecompileOptionsDialogFragment.TAG);
                } else {
                    mDecompileFragment = DecompileFragment.newInstance(selectedApk.getAbsolutePath(), mode == 0 ? 3 : mode == 1 ? 2 : mode == 2 ? 1 : 0);
                    FragmentUtils.add(mDecompileFragment, getParentFragmentManager(), R.id.fragment_container, DecompileFragment.TAG);
                }
                break;
            case R.id.simple_edit_apk:
                SimpleEditorFragment simpleEditorFragment = SimpleEditorFragment.newInstance(selectedApk.getAbsolutePath());
                FragmentUtils.add(simpleEditorFragment, getParentFragmentManager(), R.id.fragment_container, SimpleEditorFragment.TAG);
                break;
            case R.id.install_app:
                UIUtils.toast(requireContext(), getString(R.string.install_app));
                AppUtils.installApk(requireContext(), selectedApk);
                break;
            case R.id.sign_app:
                signedMode = true;
                Runnable build = () -> SignUtil.loadKey(requireContext(), signTool -> new SignTask(requireContext(), this, signTool).execute(selectedApk));
                build.run();
                break;
            case R.id.set_as_framework_app:
                Runnable frame = () -> new ImportFrameworkTask(requireContext(), this).execute(selectedApk);
                frame.run();
                break;
            case R.id.delete_item:
                try {
                    FileUtil.deleteFile(selectedApk);
                    UIUtils.toast(requireContext(), getString(R.string.toast_deleted_item, selectedApk.getName()));
                } catch (Exception e) {
                    UIUtils.toast(requireContext(), getString(R.string.error));
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onModeItemClick(Integer item) {
        switch (item) {
            case R.id.decompile_all:
                mDecompileFragment = DecompileFragment.newInstance(selectedApk.getAbsolutePath(), 3);
                FragmentUtils.add(mDecompileFragment, getParentFragmentManager(), R.id.fragment_container, DecompileFragment.TAG);
                break;
            case R.id.decompile_all_res:
                mDecompileFragment = DecompileFragment.newInstance(selectedApk.getAbsolutePath(), 2);
                FragmentUtils.add(mDecompileFragment, getParentFragmentManager(), R.id.fragment_container, DecompileFragment.TAG);
                break;
            case R.id.decompile_all_dex:
                mDecompileFragment = DecompileFragment.newInstance(selectedApk.getAbsolutePath(), 1);
                FragmentUtils.add(mDecompileFragment, getParentFragmentManager(), R.id.fragment_container, DecompileFragment.TAG);
                break;
        }
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
                FileHolder[] params = mSelected.toArray(new FileHolder[mSelected.size()]);
                mActions.actionDelete(params);
                getFileAdapter().clearSelection();
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
                break;
            case R.id.action_overflow_toolbar:
                PopupMenu popupMenu = new PopupMenu(requireContext(), v);
                popupMenu.inflate(R.menu.filemanager_menu);
                popupMenu.setOnMenuItemClickListener(this);
                popupMenu.getMenu().findItem(R.id.action_sd_card).setVisible(mSdCard != null).setTitle(mFlag ? R.string.intenal_sd_card : R.string.action_sd_card);
                popupMenu.show();
                break;
            case R.id.action_search:
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
        }
    }

    @Override
    public void onFileClick(FileHolder item, int position) {
        if (getFileAdapter().anySelected()) {
            getFileAdapter().toggle(position);
        } else {
            open(item);
        }
    }

    @Override
    public void onLongClick(FileHolder item, int position) {
        getFileAdapter().toggle(position);
    }

    @Override
    public void onItemSelected() {
        invalidatePathAdapter();
    }

    @Override
    public boolean onBackPressed() {
        //  savePosition(false);
        Fragment mSimpleEditor = getParentFragmentManager().findFragmentByTag(SimpleEditorFragment.TAG);
        Fragment decompile = getParentFragmentManager().findFragmentByTag(DecompileFragment.TAG);

        if (getFileAdapter().anySelected()) {
            getFileAdapter().clearSelection();
            return true;
        } else if (Utils.backWillExit(FileUtil.getInternalStorage().getAbsolutePath(), getPath())) {
            FragmentUtils.remove(this);
            return true;
        } else if (mSdCard != null && Utils.backWillExit(mSdCard.getAbsolutePath(), getPath())) {
            FragmentUtils.remove(this);
            return true;
        } else if (mSimpleEditor != null) {
            getParentFragmentManager().popBackStack();
            // FragmentUtils.remove(mSimpleEditor);
            return true;
        } else if (decompile != null) {
            getParentFragmentManager().popBackStack();
            // FragmentUtils.remove(mSimpleEditor);
            return true;
        } else {
            setPath(new File(Utils.downDir(1, getPath())));
            refresh();
            return true;
        }
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
                mSelected.addAll(getFileAdapter().getSelectedItems());
                mSelectAll.setOnClickListener(v -> getFileAdapter().selectAll());
                mClearSelection.setOnClickListener(v -> getFileAdapter().clearSelection());
                mSelectedCount.setText(getString(R.string.selected, getFileAdapter().getSelectedItemCount()));
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sd_card:
                actionGotoSDCard();
                return true;
            case R.id.action_select_all:
                getFileAdapter().selectAll();
                return false;
            case R.id.action_sort:
                actionSort();
                return false;
        }
        return false;
    }

    public void showProgress() {
        Bundle args = new Bundle();
        if (signedMode) {
            args.putString(ProgressDialogFragment.TITLE, getResources().getString(R.string.dialog_sign));
        } else {
            args.putString(ProgressDialogFragment.TITLE, getResources().getString(R.string.dialog_import));
        }
        args.putString(ProgressDialogFragment.MESSAGE, getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        //  args.putInt(ProgressDialogFragment.MAX, 100);
        dialog = ProgressDialogFragment.newInstance();
        dialog.setArguments(args);
        dialog.show(getParentFragmentManager(), ProgressDialogFragment.TAG);
    }

    public void updateProgress(Integer... values) {
        ProgressDialogFragment progress = getProgressDialogFragment();
        if (progress == null) {
            return;
        }
        progress.updateProgress(values[0]);
    }

    public void hideProgress() {
        dialog.dismiss();
        if (signedMode) {
            setPath(new File(Objects.requireNonNull(selectedApk.getParent())));
            UIUtils.toast(requireContext(), getString(R.string.toast_sign_done));
            signedMode = false;
        } else {
            UIUtils.toast(requireContext(), getString(R.string.toast_import_framework_done));
        }
        refresh();
    }

    private ProgressDialogFragment getProgressDialogFragment() {
        Fragment fragment = getParentFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        return (ProgressDialogFragment) fragment;
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
        ViewUtils.setVisibleOrGone(mSelectAll, !isCanPaste);
        ViewUtils.setVisibleOrGone(mDelete, !isCanPaste);
        ViewUtils.setVisibleOrGone(mMoreMenuBottomBar, !isCanPaste);
        //  ViewUtils.setVisibleOrGone(mSelectAll, !isCanPaste);
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
}
