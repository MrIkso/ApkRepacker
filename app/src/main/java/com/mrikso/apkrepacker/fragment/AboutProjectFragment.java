package com.mrikso.apkrepacker.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.task.base.CoroutinesAsyncTask;
import com.mrikso.apkrepacker.ui.projectlist.ProjectItem;
import com.mrikso.apkrepacker.database.ITabDatabase;
import com.mrikso.apkrepacker.database.JsonDatabase;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.ProjectUtils;

import java.io.File;

public class AboutProjectFragment extends Fragment {
    public static final String TAG = "full_screen_dialog";
    private static ProjectItem mProjectItem;
    private TextView patchPrj, lastWrite, createDate, size, appName, appVersion, appPackage;
    private ImageView apkIcon;
    private Toolbar toolbar;
    private TextInputEditText mProjectNotes;
    private ITabDatabase mDatabase;
    private FloatingActionButton mFabSave;
    private Context mContext;

    public static AboutProjectFragment newInstance(ProjectItem projectItem) {
        AboutProjectFragment aboutProjectFragment = new AboutProjectFragment();
       // fullScreenDialogFragment.(fragmentManager, TAG);
        mProjectItem = projectItem;
        return aboutProjectFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fullscreenDialog = inflater.inflate(R.layout.fragment_about_project, container, false);
        mContext = fullscreenDialog.getContext();
        mDatabase = JsonDatabase.getInstance(mContext);
        toolbar = fullscreenDialog.findViewById(R.id.toolbar);
        apkIcon = fullscreenDialog.findViewById(R.id.tv_about_app_icon);
        appName = fullscreenDialog.findViewById(R.id.tv_about_app_name);
        appVersion = fullscreenDialog.findViewById(R.id.tv_about_version);
        patchPrj = fullscreenDialog.findViewById(R.id.label_path_prj);
        lastWrite = fullscreenDialog.findViewById(R.id.label_date_write);
        // createDate = fullscreenDialog.findViewById(R.id.label_date_prj);
        size = fullscreenDialog.findViewById(R.id.label_project_size);
        appPackage = fullscreenDialog.findViewById(R.id.label_app_pkg_prj);
        mFabSave = fullscreenDialog.findViewById(R.id.fab_save_notes);
        mProjectNotes = fullscreenDialog.findViewById(R.id.note_project);
        return fullscreenDialog;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
//        setStyle(DialogFragment.STYLE_NORMAL, !Theme.getInstance(mContext).getCurrentTheme().isDark() ? R.style.DialogFullscreen : R.style.DialogFullscreen_Dark);
        apkIcon.setImageDrawable(ProjectUtils.getProjectIconDrawable(mProjectItem.getAppIcon(), mContext));
        patchPrj.setText(mProjectItem.getAppProjectPath());
        appPackage.setText(mProjectItem.getAppPackage());
        appName.setText(mProjectItem.getAppName());
        appVersion.setText(getString(R.string.about_version, mProjectItem.getAppVersionName(), mProjectItem.getAppVersionCode()));
        lastWrite.setText(FileUtil.getLastModified(new File(mProjectItem.getAppProjectPath())));
        mProjectNotes.setText(mDatabase.getProjectNotes(mProjectItem.getAppProjectPath()));
        new GetProjectSizeTask().execute(mProjectItem.getAppProjectPath());
        mFabSave.setOnClickListener(v ->{
            mDatabase.addProjectNotes(mProjectNotes.getText().toString(), mProjectItem.getAppProjectPath());
        });
        //createDate.setText(FileUtil.getCreateTime(new File(projectPath)));
        toolbar.setNavigationOnClickListener(v1 -> FragmentUtils.remove(this));
    }
    @SuppressLint("StaticFieldLeak")
    class GetProjectSizeTask extends CoroutinesAsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        public String doInBackground(String... projectPath) {
            return FileUtil.getFormatFolderSize(getContext(), new File(projectPath[0]));
        }

        @Override
        public void onPreExecute() {
            super.onPreExecute();
            size.setText("....  ....");
        }

        @Override
        public void onPostExecute(String result) {
            super.onPostExecute(result);
            size.setText(result);
        }
    }
}
