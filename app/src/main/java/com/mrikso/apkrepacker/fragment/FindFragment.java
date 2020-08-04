package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.FindFileOptionDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.ReplaceInFileDialogFragment;
import com.mrikso.apkrepacker.model.SearchFinder;

import com.mrikso.apkrepacker.task.SearchFilesTask;
import com.mrikso.apkrepacker.task.SearchStringsTask;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileListViewHolder;
import com.mrikso.apkrepacker.ui.findresult.FoundStringsAdapter;
import com.mrikso.apkrepacker.ui.findresult.ParentData;
import com.mrikso.apkrepacker.ui.findresult.ParentViewHolder;
import com.mrikso.apkrepacker.ui.findresult.files.SearchListAdapter;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class FindFragment extends Fragment implements ProgressDialogFragment.ProgressDialogFragmentListener, FindFileOptionDialogFragment.ItemClickListener,
        FileListViewHolder.OnItemClickListener, ParentViewHolder.ItemClickListener, ReplaceInFileDialogFragment.OnReplacedInterface {

    protected ParentViewHolder mHolder;
    private RecyclerView mRecyclerView;
    private boolean mFindFiles;
    private String mPath, mSearchText, mSearchFilename;
    private ArrayList<String> mExt;
    private DialogFragment mDialogProgress;
    private Bundle mBundle;
    private Context mContext;
    private File mSelectedFile;

    public FindFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initValue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_list, container, false);
        mContext = view.getContext();
        //Bundle bundle = this.getArguments();
        mRecyclerView = view.findViewById(R.id.find_list);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        new FastScrollerBuilder(mRecyclerView).useMd2Style().build();

        if (!mFindFiles) {
            SearchStringsTask searchStringsTask = new SearchStringsTask(mContext, this);
            searchStringsTask.execute();
        } else {
            SearchFilesTask task = new SearchFilesTask(mContext, this, new SearchFinder());
            task.setArguments(mPath, mSearchFilename, mExt);
            task.execute();
        }
    }

    public void setResult(List<File> mFileList) {
        SearchListAdapter mFilesAdapter = new SearchListAdapter(this);
        if (!mFileList.isEmpty()) {
            mFilesAdapter.notifyDataUpdated(mFileList);
            mRecyclerView.setAdapter(mFilesAdapter);
        } else {
            UIUtils.toast(mContext, R.string.find_not_found);
        }
    }

    public void setStringResult(List<ParentData> mParentList) {
        if (!mParentList.isEmpty()) {
            FoundStringsAdapter mFindStringsAdapter = new FoundStringsAdapter(mContext, this, mParentList);
            mRecyclerView.setAdapter(mFindStringsAdapter);
        } else {
            UIUtils.toast(mContext, R.string.find_not_found);
        }
    }

    private void initValue() {
        mBundle = this.getArguments();
        if (mBundle != null) {
            mFindFiles = mBundle.getBoolean("findFiles");
            mPath = mBundle.getString("curDirect");

            mSearchFilename = mBundle.getString("searchFileName");
            mSearchText = mBundle.getString("searchText");
            mExt = mBundle.getStringArrayList("expensions");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // initValue();
        // if (adapter != null){
        //  adapter.refresh();
        // }
    }

    @Override
    public void onDestroy() {
        mBundle = null;
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        mBundle = null;
        super.onDestroyView();
    }

    @Override
    public void onProgressCancelled() {

    }

    public void showProgress() {
        Bundle args = new Bundle();
        args.putString(ProgressDialogFragment.TITLE, getResources().getString(R.string.dialog_find));
        args.putString(ProgressDialogFragment.MESSAGE, getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        //  args.putInt(ProgressDialogFragment.MAX, 100);
        mDialogProgress = ProgressDialogFragment.newInstance();
        mDialogProgress.setArguments(args);
        mDialogProgress.show(getChildFragmentManager(), ProgressDialogFragment.TAG);
    }

    public void updateProgress(Integer... values) {
        ProgressDialogFragment progress = getProgressDialogFragment();
        if (progress == null) {
            return;
        }
        progress.updateProgress(values[0]);
    }

    public void hideProgress() {
        mDialogProgress.dismiss();
    }

    private ProgressDialogFragment getProgressDialogFragment() {
        Fragment fragment = getChildFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        return (ProgressDialogFragment) fragment;
    }

    @Override
    public void onFileItemClick(int item) {
        switch (item) {
            case R.id.open_with:
                startActivity(IntentUtils.openFileWithIntent(mSelectedFile));
                break;
            case R.id.open_in_editor:
                Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
                intent.putExtra("filePath", mSelectedFile.getAbsolutePath());
                startActivity(intent);
            case R.id.copy_path:
                StringUtils.setClipboard(mContext, mSelectedFile.getAbsolutePath());
                UIUtils.toast(requireContext(), getString(R.string.toast_copy_to_clipboard));
                break;
            case R.id.replace_in_file:
                ReplaceInFileDialogFragment replaceInFileDialogFragment = ReplaceInFileDialogFragment.newInstance(mSearchText, mSelectedFile.getAbsolutePath());
                replaceInFileDialogFragment.setItemClickListener(this);
                replaceInFileDialogFragment.show(getParentFragmentManager(), ReplaceInFileDialogFragment.TAG);
                break;
        }
    }

    @Override
    public void onTitleClick(String file, int position, ParentViewHolder holder) {
      //  UIUtils.toast(requireContext(), String.valueOf(position));
        mSelectedFile = new File(file);
        mHolder = holder;
        FindFileOptionDialogFragment fragment = FindFileOptionDialogFragment.newInstance();
        fragment.setItemClickListener(this);
        fragment.setIsStringMode(true);
        fragment.show(getParentFragmentManager(), FindFileOptionDialogFragment.TAG);
    }

    @Override
    public void onReplaced() {
       mHolder.changeTextColor();
    }

    @Override
    public void onFileClick(FileHolder item, int position) {
        final File file = item.getFile();
        switch (FileUtil.FileType.getFileType(file)) {
            case TXT:
            case SMALI:
            case JS:
            case JSON:
            case HTM:
            case HTML:
            case INI:
            case XML:
                Intent intent = new Intent(getActivity(), CodeEditorActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                startActivity(intent);
                break;
            default:
                startActivity(IntentUtils.openFileWithIntent(file));
        }
    }

    @Override
    public void onLongClick(FileHolder item, int position) {
        mSelectedFile = item.getFile();
        FindFileOptionDialogFragment fragment = FindFileOptionDialogFragment.newInstance();
        fragment.setItemClickListener(this);
        fragment.show(getChildFragmentManager(), FindFileOptionDialogFragment.TAG);
    }
}
