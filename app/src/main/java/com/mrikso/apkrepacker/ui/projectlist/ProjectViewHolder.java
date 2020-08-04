package com.mrikso.apkrepacker.ui.projectlist;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.ProjectUtils;

public class ProjectViewHolder extends RecyclerView.ViewHolder {

    private View menuContainer;
    private TextView appName, appPackage, appPatch;
    private ImageView appIcon;
    private MaterialButton menuBuild, menuAbout, menuDelete;

    public ProjectViewHolder(View parent) {
        super(parent);
        menuContainer = itemView.findViewById(R.id.menu_expand_container);
        appIcon = itemView.findViewById(R.id.icon_app);
        appName = itemView.findViewById(R.id.app_name);
        appPackage = itemView.findViewById(R.id.app_pkg);
        appPatch = itemView.findViewById(R.id.app_patch);
        menuBuild = itemView.findViewById(R.id.action_build);
        menuAbout = itemView.findViewById(R.id.action_about_project);
        menuDelete = itemView.findViewById(R.id.action_delete);
    }

    void bind(ProjectItem projectItem, OnItemClickListener listener, int position) {
        appName.setText(projectItem.getAppName());
        appPackage.setText(projectItem.getAppPackage());
        appPackage.setVisibility(projectItem.getAppPackage() == null ? View.GONE : View.VISIBLE);
        appIcon.setImageDrawable(ProjectUtils.getProjectIconDrawable(projectItem.getAppIcon(), itemView.getContext()));
        appPatch.setText(projectItem.getAppProjectPath());

        menuContainer.setVisibility(projectItem.isChecked() ? View.VISIBLE : View.GONE);

        itemView.setOnClickListener(view -> listener.onProjectClick(projectItem, position));

        itemView.setOnLongClickListener(v -> {
            projectItem.setChecked(!projectItem.isChecked());
            menuContainer.setVisibility(projectItem.isChecked() ? View.VISIBLE : View.GONE);
            listener.onProjectLongClick(projectItem,position);
            return true;
        });

        menuBuild.setOnClickListener(v -> listener.onProjectMenuClick(v, projectItem,position));
        menuAbout.setOnClickListener(v -> listener.onProjectMenuClick(v, projectItem,position));
        menuDelete.setOnClickListener(v -> listener.onProjectMenuClick(v, projectItem,position));
    }

    public interface OnItemClickListener {
        void onProjectClick(ProjectItem item, int position);

        void onProjectMenuClick(View view,ProjectItem item, int position);

        void onProjectLongClick( ProjectItem item, int position);
    }
}
