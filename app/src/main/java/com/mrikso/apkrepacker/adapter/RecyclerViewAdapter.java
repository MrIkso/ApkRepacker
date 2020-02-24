package com.mrikso.apkrepacker.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.AppEditorActivity;
import com.mrikso.apkrepacker.fragment.CompileFragment;
import com.mrikso.apkrepacker.fragment.dialogs.FullScreenDialogFragment;
import com.mrikso.apkrepacker.task.BuildTask;
import com.mrikso.apkrepacker.utils.SignUtil;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements onMoveAndSwipedListener {

    private Context context;
    private List<ProjectItem> mItems;
    private View parentView;


    public RecyclerViewAdapter(Context context) {
        this.context = context;
        mItems = new ArrayList<>();
    }

    public void setItems(List<ProjectItem> data) {
        this.mItems.addAll(data);
        notifyDataSetChanged();
    }

    public void clear(){
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
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        ProjectItem projectItem = mItems.get(position);
        if (holder instanceof RecyclerViewHolder) {
            final RecyclerViewHolder recyclerViewHolder = (RecyclerViewHolder) holder;
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.anim_recycler_item_show);
            recyclerViewHolder.mView.startAnimation(animation);
            AlphaAnimation aa = new AlphaAnimation(0.1f, 1.0f);
            aa.setDuration(400);

            recyclerViewHolder.mView.setOnClickListener(view -> {
               Intent intent = new Intent(context, AppEditorActivity.class);
                intent.putExtra("projectPatch", projectItem.getAppProjectPatch());
                intent.putExtra("apkPatch", projectItem.getApkPatch());
               context.startActivity(intent);
            });
            ((RecyclerViewHolder) holder).appPackage.setText(projectItem.getAppPackage());
            ((RecyclerViewHolder) holder).appName.setText(projectItem.getAppName());
            ((RecyclerViewHolder) holder).appIcon.setImageDrawable(projectItem.getAppIcon());
            ((RecyclerViewHolder) holder).appPatch.setText(projectItem.getAppProjectPatch());
            ((RecyclerViewHolder) holder).optionMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(parentView.getContext(), v);
                /*try{
                    Class<?> classPopupMenu = Class.forName(popup.getClass().getName());
                    Field mPoup = classPopupMenu.getDeclaredField("mPopup");
                    mPoup.setAccessible(true);
                    Object menuPopupupHelper = mPoup.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupupHelper.getClass().getName());
                    Method setForceicon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceicon.invoke(menuPopupupHelper, true);
                }catch (Exception e){
                    e.printStackTrace();
                }*/
                UIUtils.showIconInPopup(popup);
                popup.inflate(R.menu.options_menu_poject);
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.action_build:
                            Fragment compileFragment = new CompileFragment(projectItem.getAppProjectPatch());
                            //  compileFragment.setArguments(bundle);
                            FragmentManager fm = ((FragmentActivity)parentView.getContext()).getSupportFragmentManager();
                            fm.beginTransaction().replace(android.R.id.content, compileFragment).addToBackStack(null).commit();
                            //Runnable build = () -> SignUtil.loadKey(parentView.getContext(), signTool -> new BuildTask(parentView.getContext(), signTool).execute(new File(projectItem.getAppProjectPatch())));
                           // build.run();
                           // Toast.makeText(parentView.getContext(), R.string.menu_build_app, Toast.LENGTH_LONG).show();
                            //new BuildTask(parentView.getContext()).execute(new File(projectItem.getAppProjectPatch()));
                            break;
                        case R.id.action_delete:
                            try {
                               // new Runnable()
                                FileUtils.deleteDirectory(new File(projectItem.getAppProjectPatch()));
                                onItemDismiss(position);
                                Toast.makeText(context, context.getResources().getString(R.string.toast_project_deleted,projectItem.getAppName() ), Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            break;
                        case R.id.action_about_project:
                            String path =projectItem.getAppProjectPatch();
                            String apkpath =projectItem.getApkPatch();
                            FullScreenDialogFragment.display(((FragmentActivity)parentView.getContext()).getSupportFragmentManager(),path, apkpath);
                            break;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onItemDismiss(final int position) {
        if(mItems.size() == 1){
            mItems.clear();
            notifyDataSetChanged();
        }else {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        private View mView;
        private TextView appName, appPackage, appPatch;
        private ImageView appIcon, optionMenu;

        private RecyclerViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            appName = itemView.findViewById(R.id.app_name);
            appPackage = itemView.findViewById(R.id.app_pkg);
            appPatch = itemView.findViewById(R.id.app_patch);
            appIcon = itemView.findViewById(R.id.icon_app);
            optionMenu = itemView.findViewById(R.id.optionMenu);
        }
    }

}

