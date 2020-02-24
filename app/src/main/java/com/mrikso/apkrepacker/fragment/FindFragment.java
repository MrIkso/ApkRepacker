package com.mrikso.apkrepacker.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.core.text.SpannableStringBuilder;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.ui.activities.MainActivity;
import com.jecelyin.editor.v2.utils.ExtGrep;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.model.SearchFinder;
import com.mrikso.apkrepacker.ui.findresult.ChildData;
import com.mrikso.apkrepacker.ui.findresult.FilesAdapter;
import com.mrikso.apkrepacker.ui.findresult.FindInFilesAdapter;
import com.mrikso.apkrepacker.ui.findresult.MyAdapter;
import com.mrikso.apkrepacker.ui.findresult.ParentData;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.StringUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;

public class FindFragment extends Fragment implements ProgressDialogFragment.ProgressDialogFragmentListener {

    private RecyclerView recyclerView;
    private FindInFilesAdapter findInFilesAdapter;
    private ExtGrep extGrep;
    private boolean findFiles;
    private String path, searchText;
    private SearchFinder mFinder;
    private FilesAdapter adapter;
    private ArrayList<String> ext;
    private DialogFragment dialog;
    private SearchTask task;
    private Bundle mBundle;
    private MyAdapter myAdapter;
    private int findResultsKeywordColor;


    public FindFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initValue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_list, container, false);
        //Bundle bundle = this.getArguments();
        recyclerView = view.findViewById(R.id.find_list);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);

        recyclerView.setLayoutManager(new LinearLayoutManager(App.getContext()));
        new FastScrollerBuilder(recyclerView).build();
        task = new SearchTask();
        if (!findFiles) {
            extGrep = StringUtils.extGreps;
            if (extGrep != null) {
                SearchStringsTask searchStringsTask = new SearchStringsTask();
                searchStringsTask.execute();
            }
        } else {
            task.execute();
        }
    }

    private void initValue() {
        mBundle = this.getArguments();
        if (mBundle != null) {
            findFiles = mBundle.getBoolean("findFiles");
            path = mBundle.getString("curDirect");

            searchText = mBundle.getString("searchFileName");
            ext = mBundle.getStringArrayList("expensions");
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
    public void onDetach() {
        mBundle = null;
        super.onDetach();
    }

    @Override
    public void onProgressCancelled() {

    }

    @SuppressLint("DefaultLocale")
    private List<ParentData> getList(List<ExtGrep.Result> results) {
        File file = null;
        TypedArray a = App.getContext().obtainStyledAttributes(new int[]{
                R.attr.findResultsKeyword
        });

        findResultsKeywordColor = a.getColor(a.getIndex(0), Color.BLACK);
        a.recycle();
        List<ParentData> parentDataList = new ArrayList<>();
        List<ChildData> childDataList = null;

        for (ExtGrep.Result res : results) {
            if (!res.file.equals(file)) {
                file = res.file;
                childDataList = new ArrayList<>();
                parentDataList.add(new ParentData(file.getAbsolutePath().substring((FileUtil.getProjectPath() + "/").length()), childDataList));
            }

            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(String.format("%1$4d :", res.lineNumber));
            int start = ssb.length();
            ssb.append(res.line);

            ssb.setSpan(new ForegroundColorSpan(findResultsKeywordColor), start + res.matchStart, start + res.matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            // if (!childDataList.contains(new ChildData(ssb, file.getAbsolutePath()))) {
            childDataList.add(new ChildData(ssb, file.getAbsolutePath()));
            // } else {
            //   childDataList.remove(new ChildData(ssb, file.getAbsolutePath()));
            ///     ssb = null;
            // }
            //    }
            //parentDataList.add(new ParentData(file.getName(), childDataList));
        }

        return parentDataList;
    }

    private void showProgress() {
        Bundle args = new Bundle();
        args.putString(ProgressDialogFragment.TITLE, getResources().getString(R.string.dialog_find));
        args.putString(ProgressDialogFragment.MESSAGE, getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        //  args.putInt(ProgressDialogFragment.MAX, 100);
        dialog = ProgressDialogFragment.newInstance();
        dialog.setArguments(args);
        dialog.show(getChildFragmentManager(), ProgressDialogFragment.TAG);
    }

    private void updateProgress(Integer... values) {
        ProgressDialogFragment progress = getProgressDialogFragment();
        if (progress == null) {
            return;
        }
        progress.updateProgress(values[0]);
    }

    private void hideProgress() {
        dialog.dismiss();
    }

    private ProgressDialogFragment getProgressDialogFragment() {
        assert getFragmentManager() != null;
        Fragment fragment = getFragmentManager().findFragmentByTag(ProgressDialogFragment.TAG);
        return (ProgressDialogFragment) fragment;
    }

    private final class OnItemClickListener implements com.mrikso.apkrepacker.recycler.OnItemClickListener {

        private final Context context;

        private OnItemClickListener(Context context) {

            this.context = context;
        }

        @Override
        public void onItemClick(int position) {
            final File file = adapter.get(position);
            switch (FileUtil.FileType.getFileType(file)) {
                case TXT:
                case SMALI:
                case JS:
                case JSON:
                case HTM:
                case HTML:
                case INI:
                case XML:
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra("filePath", file.getAbsolutePath());
                    startActivity(intent);
                    break;
                default:
                    try {
                        if (Build.VERSION.SDK_INT >= 24) {
                            try {
                                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                                m.invoke(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Intent adctionView = new Intent(Intent.ACTION_VIEW);
                        adctionView.setData(Uri.fromFile(file));
                        adctionView.setFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        startActivity(adctionView);
                    } catch (Exception e) {
                        UIUtils.toast(App.getContext(), R.string.cannt_open_file);
                        Log.d("OPENERROR", e.toString());
                        // showMessage(String.format("Cannot open %s", getName(file)));
                    }
            }
                /*
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                startActivity(intent);

                 */

        }

        @Override
        public boolean onItemLongClick(int position) {
            return true;
        }
    }

    class SearchTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Void doInBackground(String... strings) {
            mFinder = new SearchFinder();
            mFinder.setCurrentPath(new File(path));
            mFinder.setExtensions(ext);
            mFinder.query(searchText);
            adapter = new FilesAdapter(App.getContext(), mFinder.getFileList());
            adapter.setOnItemClickListener(new OnItemClickListener(App.getContext()));
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... items) {
            updateProgress(items);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (!findFiles) {
                findInFilesAdapter = new FindInFilesAdapter();
                recyclerView.setAdapter(findInFilesAdapter);
            } else {
                if (mFinder != null && adapter != null) {
                    recyclerView.setAdapter(adapter);
                }
            }
            hideProgress();
        }
    }

    class SearchStringsTask extends AsyncTask<String, Integer, Void> {
        @Override
        protected Void doInBackground(String... extGreps) {
            List<ParentData> list = getList(extGrep.execute());
            if(list.isEmpty()){
                UIUtils.toast(App.getContext(), R.string.find_not_found);
            }
            myAdapter = new MyAdapter(App.getContext(), list);

            //findInFilesAdapter = new FindInFilesAdapter();
            // findInFilesAdapter.setResults(extGrep.execute());
            return null;
        }

        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected void onProgressUpdate(Integer... items) {
            updateProgress(items);
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            recyclerView.setAdapter(myAdapter);
            hideProgress();
        }
    }
}
