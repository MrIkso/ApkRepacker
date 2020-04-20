package com.mrikso.apkrepacker.ui.findresult;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.recycler.OnItemClickListener;
import com.mrikso.apkrepacker.recycler.OnItemSelectedListener;
import com.mrikso.apkrepacker.recycler.ViewHolder;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FilesAdapter extends   RecyclerView.Adapter<ViewHolder> {
    private List<File> items;
    private Context context;
    private final SparseBooleanArray selectedItems;
    private OnItemClickListener onItemClickListener;

    public FilesAdapter(Context context, List<File> mFileList) {
        this.items = mFileList;
        this.context = context;
        this.selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_project_file, parent, false);
        return new ViewHolderFile(context, onItemClickListener, itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull com.mrikso.apkrepacker.recycler.ViewHolder holder, int position) {
        holder.setData(get(position), getSelected(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
    public void refresh() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i);
        }
    }

    public void select(ArrayList<Integer> positions) {
        selectedItems.clear();
        for (int i : positions) {
            selectedItems.append(i, true);
            notifyItemChanged(i);
        }

    }

    private boolean getSelected(int position) {
        return selectedItems.get(position);
    }

    public int indexOf(File file) {
        return items.indexOf(file);
    }

    public File get(int index) {
        return items.get(index);
    }
}
