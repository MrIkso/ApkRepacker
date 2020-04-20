package com.mrikso.apkrepacker.ui.imageview;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mrikso.apkrepacker.ui.filelist.FileAdapter;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerActivity extends AppCompatActivity {

    private static List<String> mPaths;
    private static int mPosition;

    public static void setViewerData(Context context, @NonNull FileAdapter adapter, @NonNull File path) {
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < adapter.getItemCount(); ++i) {
            File file = adapter.get(i);
            String filePath = file.getAbsolutePath();

            boolean isVector;
            if (!FileUtil.FileType.getFileType(file).equals(FileUtil.FileType.IMAGE)) {
                isVector = new VectorMasterView(context, file).isVector();
            } else {
                isVector = false;
            }

            if (FileUtil.FileType.getFileType(file).equals(FileUtil.FileType.IMAGE) || filePath.equals(path.getPath()) || isVector) {
                paths.add(filePath);
                Log.i("ImageViewer", "Image added: " + filePath + " >>>>>>> " + "isVector = " + isVector);
            }
        }
        int position = paths.indexOf(path.getPath());
        Log.i("ImageViewer", "position == " + position);
        if (position == -1) {
            Log.i("ImageViewer", "position == -1");
            return;
        }
        mPaths = paths;
        mPosition = position;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Calls ensureSubDecor().
        findViewById(android.R.id.content);

        if (savedInstanceState == null) {
            ImageViewerFragment fragment = ImageViewerFragment.newInstance(mPaths, mPosition);
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, fragment, "ImageViewerFragment").commit();
        }
    }
}
