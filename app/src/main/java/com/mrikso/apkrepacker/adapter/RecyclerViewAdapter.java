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

import com.google.android.material.button.MaterialButton;
import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.AppEditorActivity;
import com.mrikso.apkrepacker.activity.MainActivity;
import com.mrikso.apkrepacker.fragment.CompileFragment;
import com.mrikso.apkrepacker.fragment.dialogs.FullScreenDialogFragment;
import com.mrikso.apkrepacker.fragment.dialogs.ProgressDialogFragment;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements onMoveAndSwipedListener {

    private static Context context;
    private List<ProjectItem> mItems;
    private View parentView;
    private DialogFragment dialog;

    private static ProjectItem projectItem;

    public RecyclerViewAdapter(Context context) {
        RecyclerViewAdapter.context = context;
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
//        ((Toolbar) ((AppCompatActivity) context).findViewById(R.id.toolbar)).setTitle("Position bind view holder: " + position);
        ProjectItem projectItem = mItems.get(position);
        RecyclerViewAdapter.projectItem = projectItem;
        if (holder instanceof RecyclerViewHolder) {
            final RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
//            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
//            recyclerViewHolder.mView.startAnimation(animation);
//            AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
//            aa.setDuration(400);

            recyclerViewHolder.mView.setOnClickListener(view -> {
                Intent intent = new Intent(context, AppEditorActivity.class);
                intent.putExtra("projectPatch", projectItem.getAppProjectPatch());
                intent.putExtra("apkPatch", projectItem.getApkPatch());
                context.startActivity(intent);
            });
            recyclerViewHolder.appName.setText(projectItem.getAppName());
            recyclerViewHolder.appPackage.setText(projectItem.getAppPackage());
            recyclerViewHolder.appPackage.setVisibility(projectItem.getAppPackage() == null ? View.GONE : View.VISIBLE);
            recyclerViewHolder.appIcon.setImageDrawable(projectItem.getAppIcon());
            recyclerViewHolder.appPatch.setText(projectItem.getAppProjectPatch());

            ((Toolbar) ((AppCompatActivity) context).findViewById(R.id.toolbar)).setTitle(context.getResources().getString(R.string.projects_count, getItemCount()));
            recyclerViewHolder.menuContainer.setVisibility(projectItem.isChecked() ? View.VISIBLE : View.GONE);

            recyclerViewHolder.mView.setOnLongClickListener(v -> {

                notifyItemChanged(position);
                projectItem.setChecked(!projectItem.isChecked());
                recyclerViewHolder.menuContainer.setVisibility(projectItem.isChecked() ? View.VISIBLE : View.GONE);
//                ((Toolbar) ((AppCompatActivity) context).findViewById(R.id.toolbar)).setTitle("Position long click: " + position);
                return true;
            });

            View.OnClickListener listener = v -> {
                switch (v.getId()) {
                    case R.id.action_build:
                        Fragment compileFragment = CompileFragment.newInstance(projectItem.getAppProjectPatch());
                        //  compileFragment.setArguments(bundle);
                        FragmentManager fm = ((FragmentActivity) parentView.getContext()).getSupportFragmentManager();
                        fm.beginTransaction().replace(android.R.id.content, compileFragment).addToBackStack(null).commit();
                        //Runnable build = () -> SignUtil.loadKey(parentView.getContext(), signTool -> new BuildTask(parentView.getContext(), signTool).execute(new File(projectItem.getAppProjectPatch())));
                        // build.run();
                        // Toast.makeText(parentView.getContext(), R.string.menu_build_app, Toast.LENGTH_LONG).show();
                        //new BuildTask(parentView.getContext()).execute(new File(projectItem.getAppProjectPatch()));
                        break;
                    case R.id.action_about_project:
                        FullScreenDialogFragment.display(((FragmentActivity) parentView.getContext()).getSupportFragmentManager(), projectItem.getAppProjectPatch(), projectItem.getApkPatch());
                        break;
                    case R.id.action_delete:
                        onItemDismiss(position);
                        new DeleteProjectTask().execute(projectItem);
                        ((Toolbar) ((AppCompatActivity) context).findViewById(R.id.toolbar)).setTitle(context.getResources().getString(R.string.projects_count, getItemCount()));
//                        ((Toolbar) ((AppCompatActivity) context).findViewById(R.id.toolbar)).setTitle("Position: " + position);
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

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
//        notifyItemChanged(position);
        if (mItems.size() == 1) {
            mItems.clear();
//            notifyDataSetChanged();
        } else {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
        notifyDataSetChanged();
    }

    @SuppressLint("StaticFieldLeak")
    class DeleteProjectTask extends AsyncTask<ProjectItem, Integer, ProjectItem> {

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
                ((Toolbar) ((AppCompatActivity) context).findViewById(R.id.toolbar)).setTitle("");
            }
            UIUtils.toast(context, context.getResources().getString(R.string.toast_project_deleted, result.getAppName()));
            dialog.dismiss();
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