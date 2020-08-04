package com.mrikso.apkrepacker.ui.projectlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.recycler.OnMoveAndSwipedListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProjectViewAdapter extends RecyclerView.Adapter<ProjectViewHolder> implements OnMoveAndSwipedListener {

    private Context mContext;
    private List<ProjectItem> mItems;
    private ProjectViewHolder.OnItemClickListener onItemClickListener;

    public ProjectViewAdapter(Context context) {
        mContext = context;
        mItems = new ArrayList<>();
    }

    public void setData(List<ProjectItem> data) {
        this.mItems.addAll(data);
        notifyDataSetChanged();
    }

    public void clear() {
        this.mItems.clear();
    }

    public void setOnItemClickListener(ProjectViewHolder.OnItemClickListener listener){
        onItemClickListener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_view, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectItem projectItem = mItems.get(position);
        holder.bind(projectItem, onItemClickListener, position);
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

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}