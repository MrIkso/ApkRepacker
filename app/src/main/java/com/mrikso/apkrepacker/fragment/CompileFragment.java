package com.mrikso.apkrepacker.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.style.CubeGrid;
import com.google.android.material.button.MaterialButton;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.LogAdapter;
import com.mrikso.apkrepacker.task.BuildTask;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.SignUtil;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

import java.io.File;
import java.util.ArrayList;

public class CompileFragment extends Fragment {

    private ListView listView;
    private LogAdapter adapter;
    private ArrayList<String> logarray;
    private Context mContext;

    private String projectDir;
    private LinearLayout layoutApkCompiling, layoutApkCompiled;
    private MaterialButton uninstallApp, installApp, closeFragment, copyLog;
    private ProgressBar progressBar;
    private TextView progressTip;
    private AppCompatTextView savedFileMsg;
    private AppCompatImageView imageError;

    public CompileFragment() {
        // projectDir = project;
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectDir = getArguments() != null ? getArguments().getString("project") : null;
        setRetainInstance(true);
    }

    public static CompileFragment newInstance(String param1) {
        CompileFragment fragment = new CompileFragment();
        Bundle args = new Bundle();
        args.putString("project", param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //savedInstanceState.putParcelableArrayList("logArr", logarray);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compile, container, false);
        mContext = view.getContext();
        listView = view.findViewById(R.id.log);
        layoutApkCompiling = view.findViewById(R.id.layout_apk_compiling);
        layoutApkCompiled = view.findViewById(R.id.layout_apk_compiled);
        uninstallApp = view.findViewById(R.id.btn_remove);
        installApp = view.findViewById(R.id.btn_install);
        closeFragment = view.findViewById(R.id.btn_close);
        copyLog = view.findViewById(R.id.btn_copy);
        progressBar = view.findViewById(R.id.progressBar);
        progressTip = view.findViewById(R.id.progress_tip);
        imageError = view.findViewById(R.id.image_error);
        savedFileMsg = view.findViewById(R.id.message_build_file_saved);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        CubeGrid cubeGrid = new CubeGrid();
        cubeGrid.setBounds(0, 0, 100, 100);
        cubeGrid.setColor(ThemeWrapper.isLightTheme() ? mContext.getResources().getColor(R.color.light_accent) : mContext.getResources().getColor(R.color.dark_accent));
        cubeGrid.setAlpha(0);
        progressBar.setIndeterminateDrawable(cubeGrid);
        logarray = new ArrayList<>();
        adapter = new LogAdapter(mContext, R.id.logitemText, logarray, 12);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setDividerHeight(0);
        Runnable build = () -> SignUtil.loadKey(mContext, signTool -> new BuildTask(mContext, signTool, this).execute(new File(projectDir)));
        build.run();
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
        String listString = "";
        for (String s : list) {
            listString += s + "\n";
        }
        return listString;
    }

    public void builded(File result) {
        if (result != null) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.about_card_show);
            layoutApkCompiling.setVisibility(View.GONE);
            layoutApkCompiled.setVisibility(View.VISIBLE);
            layoutApkCompiled.startAnimation(animation);
            String pkg = AppUtils.getApkPackage(mContext, result.getAbsolutePath());
            if (AppUtils.checkAppInstalled(mContext, pkg)) {
                uninstallApp.setVisibility(View.VISIBLE);
                uninstallApp.setOnClickListener(v -> AppUtils.uninstallApp(mContext, pkg));
            }
            savedFileMsg.setText(mContext.getResources().getString(R.string.build_apk_saved_to, result.getAbsolutePath()));
            installApp.setOnClickListener(v -> FileUtil.installApk(mContext, result));
        } else {
            copyLog.setVisibility(View.VISIBLE);
            copyLog.setOnClickListener(v -> {
                StringUtils.setClipboard(mContext, getText().toString());
            });
            progressBar.setVisibility(View.GONE);
            imageError.setVisibility(View.VISIBLE);
            progressTip.setText(R.string.error_build_failed);
            progressTip.setTextColor(mContext.getResources().getColor(R.color.google_red));
        }
        closeFragment.setOnClickListener(v -> getActivity().onBackPressed());
    }
}
