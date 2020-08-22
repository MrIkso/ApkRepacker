package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.bottomsheet.FindFileOptionDialogFragment;
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
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.IntentUtils;
import com.mrikso.apkrepacker.utils.ScrollingViewOnApplyWindowInsetsListener;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.utils.ViewUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.fastscroll.FastScroller;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class FindFragment extends Fragment implements ProgressDialogFragment.ProgressDialogFragmentListener, FindFileOptionDialogFragment.ItemClickListener,
        FileListViewHolder.OnItemClickListener, ParentViewHolder.ItemClickListener, ReplaceInFileDialogFragment.OnReplacedInterface, View.OnClickListener, SearchSettingsFragment.ItemClickListener, OnBackPressedListener {

    protected ParentViewHolder mHolder;
    private RecyclerView mRecyclerView;
    private String mSearchText;
    private ArrayList<String> mFiles = new ArrayList<>();
    private DialogFragment mDialogProgress;
    private Context mContext;
    private File mSelectedFile;
    private LinearLayout mReplaceBtn;
    private LinearLayout mClearBtn;
    private SearchListAdapter mFilesAdapter;
    private FoundStringsAdapter mFindStringsAdapter;
    private AppCompatTextView mCounter;

    public FindFragment() {
        // Required empty public constructor
    }

    public static FindFragment getInstance() {
        return new FindFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_list, container, false);
        mContext = view.getContext();
        mRecyclerView = view.findViewById(R.id.find_list);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        view.findViewById(R.id.action_search).setOnClickListener(this);
        mReplaceBtn = view.findViewById(R.id.action_find_replace);
        mReplaceBtn.setOnClickListener(this);
        mClearBtn = view.findViewById(R.id.action_clear);
        mClearBtn.setOnClickListener(this);

        mCounter = view.findViewById(R.id.search_result_count);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        FastScroller fastScroller = new FastScrollerBuilder(mRecyclerView).useMd2Style().build();
        mRecyclerView.setOnApplyWindowInsetsListener(new ScrollingViewOnApplyWindowInsetsListener(mRecyclerView, fastScroller));
    }

    public void setResult(List<File> mFileList) {
        mFilesAdapter = new SearchListAdapter(this);
        ViewUtils.setVisibleOrGone(mReplaceBtn, false);
        if (!mFileList.isEmpty()) {
            ViewUtils.setVisibleOrGone(mClearBtn, true);
            mCounter.setText(getString(R.string.search_result, mFileList.size(), mSearchText));
            mFilesAdapter.notifyDataUpdated(mFileList);
            mRecyclerView.setAdapter(mFilesAdapter);
        } else {
            mCounter.setText(getString(R.string.search_result, 0, mSearchText));
            UIUtils.toast(mContext, R.string.find_not_found);
        }
    }

    public void setStringResult(List<ParentData> parentList, ArrayList<String> files) {
        if (!parentList.isEmpty()) {
            ViewUtils.setVisibleOrGone(mClearBtn, true);
            ViewUtils.setVisibleOrGone(mReplaceBtn, true);
            mFiles = files;
            mCounter.setText(getString(R.string.search_result, parentList.size(), mSearchText));
            mFindStringsAdapter = new FoundStringsAdapter(mContext, this, parentList);
            mRecyclerView.setAdapter(mFindStringsAdapter);
        } else {
            mCounter.setText(getString(R.string.search_result, 0, mSearchText));
            UIUtils.toast(mContext, R.string.find_not_found);
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
                break;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.action_search:
                SearchSettingsFragment searchFragment = new SearchSettingsFragment();
                searchFragment.setItemClickListener(this);
                FragmentUtils.add(searchFragment, getChildFragmentManager(), R.id.fragment_container, SearchSettingsFragment.TAG);
                break;
            case R.id.action_find_replace:
                ReplaceInFileDialogFragment replaceInFileDialogFragment = ReplaceInFileDialogFragment.newInstance(mSearchText, mFiles);
                replaceInFileDialogFragment.setItemClickListener(this);
                replaceInFileDialogFragment.show(getParentFragmentManager(), ReplaceInFileDialogFragment.TAG);
                break;
            case R.id.action_clear:
                if (mFilesAdapter != null) {
                    ViewUtils.setVisibleOrGone(mClearBtn, false);
                    ViewUtils.setVisibleOrGone(mReplaceBtn, false);
                    mFilesAdapter.clear();
                    mCounter.setText("");
                }

                if(mFindStringsAdapter != null){
                    ViewUtils.setVisibleOrGone(mClearBtn, false);
                    ViewUtils.setVisibleOrGone(mReplaceBtn, false);
                    mFindStringsAdapter.clearData();
                    mCounter.setText("");
                }
                break;
        }
    }

    @Override
    public void onStartSearch(boolean filesMode, String path, String searchText, ArrayList<String> ext) {
        mSearchText = searchText;
        if (filesMode) {
            SearchFilesTask task = new SearchFilesTask(requireContext(), this, new SearchFinder());
            task.setArguments(path, searchText, ext);
            task.execute();
        } else {
            SearchStringsTask searchStringsTask = new SearchStringsTask(requireContext(), this);
            searchStringsTask.execute();
        }
    }

    @Override
    public void onBackPressed() {
        Fragment find = getChildFragmentManager().findFragmentByTag(SearchSettingsFragment.TAG);
        if (find != null) {
            getChildFragmentManager().popBackStack();
        }
    }
}
