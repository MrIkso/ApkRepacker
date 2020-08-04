package com.mrikso.apkrepacker.fragment.base;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.filemanager.FileAdapter;
import com.mrikso.apkrepacker.ui.filemanager.utils.FileUtils;
import com.mrikso.apkrepacker.ui.filemanager.holder.DirectoryHolder;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileListViewHolder;
import com.mrikso.apkrepacker.ui.filemanager.misc.DirectoryScanner;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.IntegerArray;
import com.mrikso.apkrepacker.utils.PermissionsUtils;
import com.mrikso.apkrepacker.utils.PreferenceUtils;
import com.mrikso.apkrepacker.view.WaitingViewFlipper;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public abstract class BaseFilesFragment extends Fragment implements FileListViewHolder.OnItemClickListener{

    private static final String INSTANCE_STATE_PATH = "path";
    private static final String INSTANCE_STATE_FILES = "files";
    private static final String INSTANCE_STATE_NEEDS_LOADING = "needsLoading";
    private FileAdapter mAdapter;
    private DirectoryScanner mScanner;
    private ArrayList<FileHolder> mFiles = new ArrayList<>();
    private String mPath;
    private String mFilename;
    private FileObserver mFileObserver;
    private RecyclerView mFileList;

    private WaitingViewFlipper mFlipper;
    public int mDirsCount;
    public int mFilesCount;
    private IntegerArray integerArray = new IntegerArray();
    private int lastFirstVisiblePosition = 0;
    private int selectedPosition = RecyclerView.NO_POSITION;
    public File mSdCard;
    public boolean mFlag = false;

    public BaseFilesFragment (){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        return inflater.inflate(R.layout.base_files_fragment,null);
    }

    @Override
    public void onDestroy() {
        stopScanner();
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(INSTANCE_STATE_PATH, mPath);
        outState.putInt(INSTANCE_STATE_NEEDS_LOADING, isScannerRunning() ? 1 : 0);
        outState.putParcelableArrayList(INSTANCE_STATE_FILES, mFiles);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFlipper = (WaitingViewFlipper) view.findViewById(R.id.flipper);
        mSdCard = FileUtil.getExternalStorage();

        // Get arguments
        boolean needsLoading = true;
        if (savedInstanceState == null) {
            setPath(FileUtil.getInternalStorage());
            mFilename = FileUtil.getInternalStorage().getName();
        } else {
            setPath(new File(Objects.requireNonNull(savedInstanceState.getString(INSTANCE_STATE_PATH))));
            mFiles = savedInstanceState
                    .getParcelableArrayList(INSTANCE_STATE_FILES);
            needsLoading = savedInstanceState.getInt(INSTANCE_STATE_NEEDS_LOADING) != 0;
        }
        pathCheckAndFix();

        if (needsLoading) {
            refresh();
        }

        mFileList = view.findViewById(R.id.file_list);
        mAdapter = new FileAdapter(view.getContext());
        mAdapter.addAll(mFiles);
        mAdapter.notifyDataSetChanged();
        mAdapter.setOnItemClickListener(this);
        mFileList.setAdapter(mAdapter);
        new FastScrollerBuilder(mFileList).useMd2Style().build();
    }


    /**
     * Reloads {@link #mPath}'s contents.
     */
    public void refresh() {
        if (hasPermissions()) {
            showLoading(true);
            getActivity().runOnUiThread(() -> {
                renewScanner().start();
            });
        } else {
            requestPermissions();
        }
    }

    /**
     * Will request a refresh for all active FileListFragment instances currently displaying "directory".
     * @param directory The directory to refresh.
     */
    public void refresh(Context c, File directory) {
        setPath(directory);
        refresh();
    }

    private boolean hasPermissions() {
        if (getActivity() == null) return false;

        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        showLoading(true);
        if(!PermissionsUtils.checkAndRequestStoragePermissions(this));{
            showPermissionDenied();
        }
    }


    /**
     * Switch to permission request mode.
     */
    private void showPermissionDenied() {
        mFlipper.setDisplayedChild(WaitingViewFlipper.PAGE_INDEX_PERMISSION_DENIED);

    }

    /**
     * Make the UI indicate loading.
     */
    private void showLoading(boolean loading) {

        if (loading) {
            mFlipper.setDisplayedChildDelayed(WaitingViewFlipper.PAGE_INDEX_LOADING);
        } else {
            mFlipper.setDisplayedChild(WaitingViewFlipper.PAGE_INDEX_CONTENT);
        }

    }

    /**
     * Recreates the {@link #mScanner} using the previously set arguments and
     * {@link #mPath}.
     *
     * @return {@link #mScanner} for convenience.
     */
    protected DirectoryScanner renewScanner() {
        // Cancel previous scanner so that it doesn't load on top of the new list.
        stopScanner();

        String filetypeFilter = null;
        String mimetypeFilter = "*/*";
        boolean writeableOnly = false;
        boolean directoriesOnly = false;

        mScanner = new DirectoryScanner(new File(mPath),
                getActivity(),
                new FileListMessageHandler(),
                filetypeFilter == null ? "" : filetypeFilter,
                mimetypeFilter == null ? "" : mimetypeFilter,
                writeableOnly,
                directoriesOnly);
        return mScanner;
    }

    private void stopScanner() {
        if (hasScanner()) {
            mScanner.cancel();
        }
    }

    public boolean isScannerRunning() {
        return hasScanner()
                && mScanner.isAlive()
                && mScanner.isRunning();
    }


    protected boolean hasScanner() {
        return mScanner != null;
    }


    @SuppressLint("HandlerLeak")
    private class FileListMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DirectoryScanner.MESSAGE_SHOW_DIRECTORY_CONTENTS:
                    DirectoryHolder c = (DirectoryHolder) msg.obj;
                    mFiles.clear();

                    mFiles.addAll(c.listSdCard);
                    mFiles.addAll(c.listDir);
                    mFiles.addAll(c.listFile);
                    //onDataReady();

                    if (mAdapter.anySelected()) {
                        mAdapter.clear();
                        mAdapter.clearSelection();}
                    else {
                        mAdapter.clear();
                        mAdapter.addAll(mFiles);
                    }
                    mAdapter.notifyDataSetChanged();

                    mDirsCount = c.listDir.size();
                    mFilesCount = c.listFile.size();
                        showLoading(false);
                        onDataApplied();

                    break;
                case DirectoryScanner.MESSAGE_SET_PROGRESS:
                    // Irrelevant.
                    break;
            }
        }
    }

    /**
     * @return The currently displayed directory's absolute path.
     */
    public final String getPath() {
        return mPath;
    }

    /**
     * This will be ignored if path doesn't pass check as valid.
     *
     * @param dir The path to set.
     */
    public final void setPath(File dir) {
        mPath = dir.getAbsolutePath();

        if (dir.exists()){
            // Observe the path
            if (mFileObserver != null) {
                mFileObserver.stopWatching();
            }
            mFileObserver = generateFileObserver(mPath);
            mFileObserver.startWatching();
        }

    }

    public void savePosition(boolean save) {
        if (save) {
            integerArray.add(((LinearLayoutManager) Objects.requireNonNull(mFileList.getLayoutManager())).findFirstCompletelyVisibleItemPosition());
        } else {
            lastFirstVisiblePosition = integerArray.getSize();
            lastFirstVisiblePosition--;
            if (lastFirstVisiblePosition >= 0) {
                ((LinearLayoutManager) Objects.requireNonNull(mFileList.getLayoutManager())).scrollToPositionWithOffset(integerArray.get(lastFirstVisiblePosition), 0);
            } else {
                lastFirstVisiblePosition = 0;
                integerArray.clear();
            }
        }
    }

    public void actionSort() {
        int checkedItem = PreferenceUtils.getInteger(requireContext(), "pref_sort", 0);
        CharSequence[] sorting = {getString(R.string.sort_by_name), getString(R.string.sort_by_date), getString(R.string.sort_by_size)};

        UIUtils.showListSingleChoiceDialog(requireContext(), R.string.sort_by, 0, sorting, checkedItem, new UIUtils.OnSingleChoiceCallback() {
            @Override
            public void onSelect(MaterialDialog dialog, int which) {
               refresh();
                PreferenceUtils.putInt(requireContext(), "pref_sort", which);
            }
        }, null);
    }

    public void actionGotoSDCard() {
        if (mFlag) {
            mFlag = false;
            setPath(FileUtil.getInternalStorage());
            refresh();
        } else {
            if (mSdCard != null) {
                mFlag = true;
                setPath(mSdCard);
                refresh();
            }
        }
    }

    public FileAdapter getFileAdapter(){
        return mAdapter;
    }

    private FileObserver generateFileObserver(String pathToObserve) {
        return new FileObserver(pathToObserve,
                FileObserver.CREATE
                        | FileObserver.DELETE
                        | FileObserver.CLOSE_WRITE // Removed since in case of continuous modification
                        // (copy/compress) we would flood with events.
                        | FileObserver.MOVED_FROM
                        | FileObserver.MOVED_TO) {
            private static final long MIN_REFRESH_INTERVAL = 2 * 1000;

            private long lastUpdate = 0;

            @Override
            public void onEvent(int event, String path) {
                if (System.currentTimeMillis() - lastUpdate <= MIN_REFRESH_INTERVAL
                        || event == 32768) { // See https://code.google.com/p/android/issues/detail?id=29546
                    return;
                }

             //   Logger.logV(Logger.TAG_OBSERVER, "Observed event " + event + ", refreshing list..");
                lastUpdate = System.currentTimeMillis();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            refresh();
                        }
                    });
                }
            }
        };
    }

    private void pathCheckAndFix() {
        File dir = new File(mPath);
        // Sanity check that the path (coming from extras_dir_path) is indeed a
        // directory
        if (!dir.isDirectory() && dir.getParentFile() != null) {
            // remember the filename for picking.
            mFilename = dir.getName();
            setPath(dir.getParentFile());
        }
    }

    public String getFilename() {
        return mFilename;
    }


    /**
     * Use this callback to handle UI state when the new list data is ready and the UI
     * has been refreshed.
     */
    protected void onDataApplied() {}

    protected void onFileClicked(FileHolder holder) {}
}
