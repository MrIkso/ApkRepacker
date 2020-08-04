package com.mrikso.apkrepacker.task;

import android.content.Context;

import com.mrikso.apkrepacker.fragment.FindFragment;
import com.mrikso.apkrepacker.model.SearchFinder;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;

import java.io.File;
import java.util.ArrayList;

public class SearchFilesTask extends CoroutinesAsyncTask<String, Integer, Void> {

    private FindFragment mFindFragment;
    private Context mContext;
    private SearchFinder mFinder;
    private String mPath;
    private String mSearchText;
    private ArrayList<String> mExt;

    public SearchFilesTask(Context context, FindFragment findFragment, SearchFinder searchFinder){
        mFindFragment = findFragment;
        mContext = context;
        mFinder = searchFinder;
    }

    public void setArguments(String path, String searchText, ArrayList<String> ext){
        mPath = path;
        mSearchText = searchText;
        mExt = new ArrayList<>();
        mExt.addAll(ext);
    }

    @Override
    public void onPreExecute() {
        mFindFragment.showProgress();
    }

    @Override
    public Void doInBackground(String... strings) {
        mFinder.setCurrentPath(new File(mPath));
        mFinder.setExtensions(mExt);
        mFinder.query(mSearchText);
        return null;
    }

    @Override
    public void onProgressUpdate(Integer... items) {
        mFindFragment.updateProgress(items);
    }

    @Override
    public void onPostExecute(Void result) {
        super.onPostExecute(result);
        if (mFinder != null){
            mFindFragment.setResult(mFinder.getFileList());
        }
        mFindFragment.hideProgress();
    }
}

