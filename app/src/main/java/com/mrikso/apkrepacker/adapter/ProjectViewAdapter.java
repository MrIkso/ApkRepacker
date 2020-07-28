package com.mrikso.apkrepacker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.AppEditorActivity;
import com.mrikso.apkrepacker.activity.MainActivity;
import com.mrikso.apkrepacker.fragment.CompileFragment;
import com.mrikso.apkrepacker.fragment.AboutProjectFragment;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;
import com.mrikso.apkrepacker.recycler.OnMoveAndSwipedListener;
import com.mrikso.apkrepacker.ui.preferences.PreferenceHelper;
import com.mrikso.apkrepacker.utils.FragmentUtils;
import com.mrikso.apkrepacker.utils.ProjectUtils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements OnMoveAndSwipedListener {

    private static Context context;
    private List<ProjectItem> mItems;
    private View parentView;
    private DialogFragment dialog;
    private Toolbar mainToolBar;
    private BottomAppBar bottomAppBar;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;

    public ProjectViewAdapter(Context context) {
        ProjectViewAdapter.context = context;
        mainToolBar = MainActivity.getInstance().findViewById(R.id.toolbar);
        bottomAppBar = MainActivity.getInstance().findViewById(R.id.bottom_App_bar);
        floatingActionButton = MainActivity.getInstance().findViewById(R.id.fab_bottom_appbar);
        recyclerView = MainActivity.getInstance().findViewById(R.id.recycler_view_bottom_appbar);
        mItems = new ArrayList<>();
    }

    public void setItems(List<ProjectItem> data) {
        this.mItems.addAll(data);
        notifyDataSetChanged();
    }

    public void clear() {
        this.mItems.clear();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        parentView = parent;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_view, parent, false);
        return new RecyclerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        ProjectItem projectItem = mItems.get(position);
        if (holder instanceof RecyclerViewHolder) {
            final RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
//            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
//            recyclerViewHolder.mView.startAnimation(animation);
//            AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
//            aa.setDuration(400);

            recyclerViewHolder.mView.setOnClickListener(view -> {
                Intent intent = new Intent(context, AppEditorActivity.class);
                intent.putExtra("apkFileIcon", projectItem.getAppIcon());
                intent.putExtra("apkFileName", projectItem.getAppName());
                intent.putExtra("apkFilePackageName", projectItem.getAppPackage());
                intent.putExtra("projectPatch", projectItem.getAppProjectPatch());
                context.startActivity(intent);
            });
            recyclerViewHolder.appName.setText(projectItem.getAppName());
            recyclerViewHolder.appPackage.setText(projectItem.getAppPackage());
            recyclerViewHolder.appPackage.setVisibility(projectItem.getAppPackage() == null ? View.GONE : View.VISIBLE);
            recyclerViewHolder.appIcon.setImageDrawable(ProjectUtils.getProjectIconDrawable(projectItem.getAppIcon(), context));
            recyclerViewHolder.appPatch.setText(projectItem.getAppProjectPatch());

            mainToolBar.setTitle(context.getResources().getString(R.string.projects_count, getItemCount()));
            recyclerViewHolder.menuContainer.setVisibility(projectItem.isChecked() ? View.VISIBLE : View.GONE);

            recyclerViewHolder.mView.setOnLongClickListener(v -> {
                notifyItemChanged(position);
                projectItem.setChecked(!projectItem.isChecked());
                recyclerViewHolder.menuContainer.setVisibility(projectItem.isChecked() ? View.VISIBLE : View.GONE);
                return true;
            });

            View.OnClickListener listener = v -> {
                switch (v.getId()) {
                    case R.id.action_build:
                        if(PreferenceHelper.getInstance(context).isConfirmBuild()){
                            UIUtils.showConfirmDialog(context, context.getString(R.string.confirm_build_title),new UIUtils.OnClickCallback() {
                                @Override
                                public void onOkClick() {
                                    showCompileFragment(projectItem);
                                }
                            });
                        }
                        else {
                            showCompileFragment(projectItem);
                        }
                        break;
                    case R.id.action_about_project:
                        AboutProjectFragment about = AboutProjectFragment.newInstance(projectItem);
                        FragmentUtils.add(about, ((FragmentActivity) parentView.getContext()).getSupportFragmentManager(), android.R.id.content);
                        break;
                    case R.id.action_delete:
                        onItemDismiss(position);
                        new deleteProjectTask().execute(projectItem);
//                        mainToolBar.setTitle(context.getResources().getString(R.string.projects_count, getItemCount()));
                        break;
                }
            };

            recyclerViewHolder.menuBuild.setOnClickListener(listener);
            recyclerViewHolder.menuAbout.setOnClickListener(listener);
            recyclerViewHolder.menuDelete.setOnClickListener(listener);
        }
    }

    private void showProgress() {
        Bundle args = new Bundle();
        args.putString(ProgressDialogFragment.TITLE, context.getResources().getString(R.string.dialog_deleting));
        args.putString(ProgressDialogFragment.MESSAGE, context.getResources().getString(R.string.dialog_please_wait));
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);
        dialog = ProgressDialogFragment.newInstance();
        dialog.setArguments(args);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), ProgressDialogFragment.TAG);
    }

    private void fixBottomAppBar() {
        if (!recyclerView.canScrollVertically(-1) && bottomAppBar.isShown() && !floatingActionButton.isShown()) {
            bottomAppBar.performShow();
            floatingActionButton.show();
        }
    }

    private void showCompileFragment(ProjectItem projectItem){
        Fragment compileFragment = CompileFragment.newInstance(projectItem.getAppProjectPatch());
        FragmentManager fm = ((FragmentActivity) parentView.getContext()).getSupportFragmentManager();
        FragmentUtils.replace(compileFragment, fm, android.R.id.content);
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
        if (mItems.size() == 1) {
            mItems.clear();
        } else {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("StaticFieldLeak")
    class deleteProjectTask extends AsyncTask<ProjectItem, Integer, ProjectItem> {

        @SuppressLint("WrongThread")
        @Override
        protected ProjectItem doInBackground(ProjectItem... patch) {
            try {
                FileUtils.deleteDirectory(new File(patch[0].getAppProjectPatch()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return patch[0];
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgress();
        }

        @Override
        protected void onPostExecute(ProjectItem result) {
            super.onPostExecute(result);
            if (getItemCount() == 0) {
                MainActivity.getInstance().refreshAdapter(false);
                mainToolBar.setTitle("");
            }
            fixBottomAppBar();
            UIUtils.toast(context, context.getResources().getString(R.string.toast_project_deleted, result.getAppName()));
            dialog.dismissAllowingStateLoss();
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private static class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private View mView, menuContainer;
        private TextView appName, appPackage, appPatch;
        private ImageView appIcon;
        private MaterialButton menuBuild, menuAbout, menuDelete;

        private RecyclerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            menuContainer = itemView.findViewById(R.id.menu_expand_container);
            appIcon = itemView.findViewById(R.id.icon_app);
            appName = itemView.findViewById(R.id.app_name);
            appPackage = itemView.findViewById(R.id.app_pkg);
            appPatch = itemView.findViewById(R.id.app_patch);
            menuBuild = itemView.findViewById(R.id.action_build);
            menuAbout = itemView.findViewById(R.id.action_about_project);
            menuDelete = itemView.findViewById(R.id.action_delete);
        }
    }
}