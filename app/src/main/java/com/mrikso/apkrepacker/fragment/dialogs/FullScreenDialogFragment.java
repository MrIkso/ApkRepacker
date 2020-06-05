package com.mrikso.apkrepacker.fragment.dialogs;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.ProjectItem;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ThemeWrapper;

import java.io.File;

public class FullScreenDialogFragment extends DialogFragment {
    public static final String TAG = "full_screen_dialog";
    private static ProjectItem mProjectItem;
    TextView patchPrj, lastWrite, createDate, size, appName, appVersion, appPackage;
    ImageView apkIcon;
    Toolbar toolbar;

    public static FullScreenDialogFragment display(FragmentManager fragmentManager, ProjectItem projectItem) {
        FullScreenDialogFragment fullScreenDialogFragment = new FullScreenDialogFragment();
        fullScreenDialogFragment.show(fragmentManager, TAG);
        mProjectItem = projectItem;
        return fullScreenDialogFragment;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setStyle(DialogFragment.STYLE_NORMAL, ThemeWrapper.isLightTheme() ? R.style.DialogFullscreen : R.style.DialogFullscreen_Dark);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fullscreenDialog = inflater.inflate(R.layout.dialog_about_project, container, false);
        toolbar = fullscreenDialog.findViewById(R.id.toolbar);
        apkIcon = fullscreenDialog.findViewById(R.id.tv_about_app_icon);
        appName = fullscreenDialog.findViewById(R.id.tv_about_app_name);
        appVersion = fullscreenDialog.findViewById(R.id.tv_about_version);
        patchPrj = fullscreenDialog.findViewById(R.id.label_path_prj);
        lastWrite = fullscreenDialog.findViewById(R.id.label_date_write);
        // createDate = fullscreenDialog.findViewById(R.id.label_date_prj);
        size = fullscreenDialog.findViewById(R.id.label_project_size);
        appPackage = fullscreenDialog.findViewById(R.id.label_app_pkg_prj);
        return fullscreenDialog;
    }

    @Override
    public void onViewCreated(View view, Bundle bundle) {
        super.onViewCreated(view, bundle);
        apkIcon.setImageDrawable(FileUtil.getProjectIconDrawable(mProjectItem.getAppIcon()));
        patchPrj.setText(mProjectItem.getAppProjectPatch());
        appPackage.setText(mProjectItem.getAppPackage());
        appName.setText(mProjectItem.getAppName());
        appVersion.setText(getString(R.string.about_version, mProjectItem.getAppVersionName(), mProjectItem.getAppVersionCode()));
        lastWrite.setText(FileUtil.getLastModified(new File(mProjectItem.getAppProjectPatch())));
        new GetProjectSizeTask().execute(mProjectItem.getAppProjectPatch());
        //createDate.setText(FileUtil.getCreateTime(new File(projectPath)));
        toolbar.setNavigationOnClickListener(v1 -> dismiss());
    }

/*    private static Drawable getBitmapDrawableBase64(String appIconBase64) {
        return (decodeBase64(appIconBase64) != null) ? new BitmapDrawable(decodeBase64(appIconBase64)) : null;
    }

    private static Bitmap decodeBase64(String input) {
        try {
            byte[] decodedBytes = Base64.decode(input, 0);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            return null;
        }
    }*/

    @SuppressLint("StaticFieldLeak")
    class GetProjectSizeTask extends AsyncTask<String, String, String> {

        @SuppressLint("WrongThread")
        @Override
        protected String doInBackground(String... projectPath) {
            return FileUtil.getFormatFolderSize(getContext(), new File(projectPath[0]));
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            size.setText("....  ....");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            size.setText(result);
        }
    }
}
