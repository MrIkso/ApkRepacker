package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.style.CubeGrid;
import com.google.android.material.button.MaterialButton;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.AppEditorActivity;
import com.mrikso.apkrepacker.adapter.LogAdapter;
import com.mrikso.apkrepacker.task.DecodeTask;
import com.mrikso.apkrepacker.ui.prererence.Preference;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

import java.io.File;
import java.util.ArrayList;

public class DecompileFragment extends Fragment {

    private ListView listView;
    private LogAdapter adapter;
    private ArrayList<String> logarray;
    private Context mContext;

    private File selectedApk;
    private String nameApk;
    private boolean apkMode;
    private MaterialButton mClose, mOpen, mCopylog;
    private ProgressBar mProgress;
    private TextView mTextProgress;
    private AppCompatImageView mImageResult;
    private int mMode = -1;

    public DecompileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedApk = new File(getArguments().getString("selected"));
        nameApk = getArguments().getString("name");
        apkMode = getArguments().getBoolean("mode");
        mMode = getArguments().getInt("decMode");
        setRetainInstance(true);
    }

    public static DecompileFragment newInstance(String name, String selected, boolean f) {
        DecompileFragment fragment = new DecompileFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("selected", selected);
        args.putBoolean("mode", f);
        fragment.setArguments(args);
        return fragment;
    }

    public static DecompileFragment newInstance(String name, String selected, boolean f, int mode) {
        DecompileFragment fragment = new DecompileFragment();
        Bundle args = new Bundle();
        args.putString("name", name);
        args.putString("selected", selected);
        args.putBoolean("mode", f);
        args.putInt("decMode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    public static DecompileFragment newInstance(String selected, int mode) {
        DecompileFragment fragment = new DecompileFragment();
        Bundle args = new Bundle();
        args.putString("selected", selected);
        args.putInt("decMode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    public static DecompileFragment newInstance(String selected) {
        DecompileFragment fragment = new DecompileFragment();
        Bundle args = new Bundle();
        args.putString("selected", selected);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_decompile, container, false);
        mContext = view.getContext();
        listView = view.findViewById(R.id.log);
        mProgress = view.findViewById(R.id.progressBar);

        mTextProgress = view.findViewById(R.id.progress_tip);
        mImageResult = view.findViewById(R.id.image_error);
        mOpen = view.findViewById(R.id.btn_open_project);
        mCopylog = view.findViewById(R.id.btn_copy);
        mClose = view.findViewById(R.id.btn_close);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        CubeGrid cubeGrid = new CubeGrid();
        cubeGrid.setBounds(0, 0, 100, 100);
        cubeGrid.setColor(ThemeWrapper.isLightTheme() ? mContext.getResources().getColor(R.color.light_accent) :
                mContext.getResources().getColor(R.color.dark_accent));
        cubeGrid.setAlpha(0);
        mProgress.setIndeterminateDrawable(cubeGrid);
        logarray = new ArrayList<>();
        adapter = new LogAdapter(mContext, R.id.logitemText, logarray, 12);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        Preference preference = Preference.getInstance(mContext);
        String decodeAppName = AppUtils.getApkName(mContext, selectedApk.getAbsolutePath()).replace("/", "_");
        if (!apkMode) {
            if (mMode != -1) {
                switch (mMode) {
                    case 1://Decompile dex
                        new DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk);
                        break;
                    case 2://Decompile only res
                        new DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk);
                        break;
                    case 3://Decompile all
                        new DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk);
                        break;
                }
            } else {
                switch (preference.getDecodingMode()) {
                    case 0://Decompile all
                        new DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk);
                        break;
                    case 1://Decompile only res
                        new DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk);
                        break;
                    case 2://Decompile dex
                        new DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk);
                        break;
                }
            }
        } else {
            if (mMode != -1) {
                switch (mMode) {
                    case 1://Decompile dex
                        new DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk);
                        break;
                    case 2://Decompile only res
                        new DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk);
                        break;
                    case 3://Decompile all
                        new DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk);
                        break;
                }
            } else {
                switch (preference.getDecodingMode()) {
                    case 0://Decompile all
                        new DecodeTask(mContext, 3, decodeAppName, this).execute(selectedApk);
                        break;
                    case 1://Decompile only res
                        new DecodeTask(mContext, 2, decodeAppName, this).execute(selectedApk);
                        break;
                    case 2://Decompile dex
                        new DecodeTask(mContext, 1, decodeAppName, this).execute(selectedApk);
                        break;
                }
            }
        }
    }

    public void append(CharSequence s) {
        logarray.add(s.toString());
        listView.setSelection(adapter.getCount() - 1);
    }

    public void append(ArrayList<String> list) {
        adapter = new LogAdapter(mContext, R.id.logitemText, list, 12);
        listView.setAdapter(adapter);
    }

    public CharSequence getText() {
        return listToString(logarray);
    }

    public ArrayList<String> getTextArray() {
        return logarray;
    }

    private String listToString(ArrayList<String> list) {
        StringBuilder listString = new StringBuilder();
        for (String s : list) {
            listString.append(s).append("\n");
        }
        return listString.toString();
    }

    public void decompileResult(File result) {
        if (result != null) {
            mProgress.setVisibility(View.GONE);
            mImageResult.setVisibility(View.VISIBLE);
            mOpen.setVisibility(View.VISIBLE);
            mImageResult.setImageResource(R.drawable.ic_done);
            mTextProgress.setText(R.string.decompile_finished);
            mOpen.setOnClickListener(v -> {

                File dataFile = new File(result.getAbsolutePath(), "apktool.json");
                String apkFileName = FileUtil.readJson(dataFile, "apkFileName");

                Intent intent = new Intent(mContext, AppEditorActivity.class);
                intent.putExtra("projectPatch", result.getAbsolutePath());
                intent.putExtra("apkFileIcon", FileUtil.readJson(dataFile, "apkFileIcon"));
                intent.putExtra("apkFileName", (apkFileName != null) ? apkFileName : result.getName());
                intent.putExtra("apkFilePackageName", FileUtil.readJson(dataFile, "apkFilePackageName"));
                mContext.startActivity(intent);
            });
        }
        /*
            При декомпиляции произошла ошибка
         */
        else {
            mProgress.setVisibility(View.GONE);
            mOpen.setVisibility(View.GONE);
            mImageResult.setVisibility(View.VISIBLE);
            mCopylog.setVisibility(View.VISIBLE);
            mCopylog.setOnClickListener(v -> {
                StringUtils.setClipboard(mContext, getText().toString());
            });
            mTextProgress.setText(R.string.error_decompilation_failed);
            mTextProgress.setTextColor(mContext.getResources().getColor(R.color.google_red));
        }
        mClose.setOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }
}
