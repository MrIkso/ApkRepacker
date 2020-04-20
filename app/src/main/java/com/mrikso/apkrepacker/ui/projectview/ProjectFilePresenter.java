package com.mrikso.apkrepacker.ui.projectview;

import java.io.File;

public class ProjectFilePresenter implements ProjectFileContract.Presenter {
    private ProjectFileContract.View view;

    public ProjectFilePresenter(ProjectFileContract.View view) {
        view.setPresenter(this);
        this.view = view;
    }

    @Override
    public void show(File projectFile, boolean expand) {
        view.display(projectFile, expand);
    }

    @Override
    public void refresh(File projectFile) {
        view.refresh();
    }
}
