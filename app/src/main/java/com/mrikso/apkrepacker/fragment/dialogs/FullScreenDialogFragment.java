package com.mrikso.apkrepacker.fragment.dialogs;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

import java.io.File;

public class FullScreenDialogFragment extends DialogFragment {
    public  static final String TAG = "full_screen_dialog";
    private static String apkPath;
    private static String projectPath;
    TextView patchPrj, lastWrite, createDate, size, appName, appPackage;
    ImageView apkIcon;
    Toolbar toolbar;

    public static FullScreenDialogFragment display (FragmentManager fragmentManager, String project, String apk){
        FullScreenDialogFragment fullScreenDialogFragment = new FullScreenDialogFragment();
        fullScreenDialogFragment.show(fragmentManager, TAG);
        apkPath = apk;
        projectPath = project;
        return fullScreenDialogFragment;
    }

    @Override
    public void onCreate(Bundle bundle){
        super.onCreate(bundle);
        setStyle(DialogFragment.STYLE_NORMAL, ThemeWrapper.isLightTheme() ? R.style.DialogFullscreen : R.style.DialogFullscreen_Dark);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fullscreenDialog = inflater.inflate(R.layout.dialog_about_project, container, false);
        toolbar = fullscreenDialog.findViewById(R.id.toolbar);
        apkIcon = fullscreenDialog.findViewById(R.id.project_icon);
        patchPrj = fullscreenDialog.findViewById(R.id.label_path_prj);
        lastWrite = fullscreenDialog.findViewById(R.id.label_date_write);
       // createDate = fullscreenDialog.findViewById(R.id.label_date_prj);
        size = fullscreenDialog.findViewById(R.id.label_project_size);
        appName = fullscreenDialog.findViewById(R.id.app_name_prj);
        appPackage= fullscreenDialog.findViewById(R.id.label_app_pkg_prj);
        return fullscreenDialog;
    }
    @Override
    public void onViewCreated(View view, Bundle bundle){
        super.onViewCreated(view,bundle);
        Object[] info = AppUtils.getApkInfo(view.getContext(), apkPath);
        apkIcon.setImageDrawable(AppUtils.getApkIcon(view.getContext(),apkPath ));
        patchPrj.setText(projectPath);
        appPackage.setText(AppUtils.getApkPackage(view.getContext(), apkPath));
        if (info != null) {
            appName.setText(info[1].toString() + " v" +info[3].toString() + "(" + info[4].toString() + ")");
        }
        lastWrite.setText(FileUtil.getLastModified(new File(projectPath)));
        size.setText(FileUtil.getFolderSize(view.getContext(), new File(projectPath)));
        GetProjectSizeTask sizeTask = new GetProjectSizeTask();
        sizeTask.execute();
        //createDate.setText(FileUtil.getCreateTime(new File(projectPath)));
        toolbar.setNavigationOnClickListener(v1 -> dismiss());
    }
    class GetProjectSizeTask extends AsyncTask<String, Integer, Void> {
        String sizeT;
        @Override
        protected Void doInBackground(String... extGreps) {
            sizeT= FileUtil.getFolderSize(App.getContext(), new File(projectPath));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            size.setText(sizeT);
        }
    }
}
