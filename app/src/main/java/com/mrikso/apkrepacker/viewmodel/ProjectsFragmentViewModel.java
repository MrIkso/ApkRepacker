package com.mrikso.apkrepacker.viewmodel;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mrikso.apkrepacker.ui.projectlist.ProjectItem;
import com.mrikso.apkrepacker.utils.AppExecutor;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.viewmodel.projects.ProjectLoader;

import java.io.File;
import java.util.List;

public class ProjectsFragmentViewModel extends AndroidViewModel {

    private ProjectLoader mLoader;
    private MutableLiveData<List<ProjectItem>> projectItemLiveData = new MutableLiveData();
    private Context mContext;

    public ProjectsFragmentViewModel(@NonNull Application application) {
        super(application);
        mContext = application;
    }

    public void startLoad(){
        mLoader = ProjectLoader.getInstance(mContext);
    }

    public LiveData<List<ProjectItem>> getProjects() {
      //  projectItemLiveData.setValue();
      return mLoader.getProjectItems();
    }

    public void deleteProject(int position){
        AppExecutor.getInstance().getDiskIO().execute(() -> {
            try {
                FileUtil.deleteFile(new File(getProjects().getValue().get(position).getAppProjectPath()));
                //getProjects();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
     //   mLoader.getProjects().removeObserver(mBackupRepoPackagesObserver);
    }
}
