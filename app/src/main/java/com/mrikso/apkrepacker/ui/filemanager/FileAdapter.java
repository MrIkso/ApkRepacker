package com.mrikso.apkrepacker.ui.filemanager;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.recycler.OnItemSelectedListener;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.holder.FileListViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class FileAdapter extends RecyclerView.Adapter<FileListViewHolder> {

    private final Context context;
    private final ArrayList<FileHolder> items;
    private final SparseBooleanArray selectedItems;

    private FileListViewHolder.OnItemClickListener onItemClickListener;
    private OnItemSelectedListener onItemSelectedListener;
    private boolean mProjectMode;

    public FileAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
        this.selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public FileListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileListViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull FileListViewHolder holder, int position) {
        holder.bind(items.get(position).getFile(), position, onItemClickListener, getSelected(position), mProjectMode);
    }

    public void setProjectMode(boolean enable){
        mProjectMode = enable;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setOnItemClickListener(FileListViewHolder.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void add(FileHolder file) {
        items.add(file);
    }

    public void addAll(FileHolder... files) {
        try {
            items.addAll(Arrays.asList(files));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addAll(Collection<FileHolder> files) {
        items.addAll(files);
    }

    public void clear() {
        if (items != null)
            while (items.size() > 0) items.remove(items.size() - 1);
    }

    public void refresh() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i);
        }
    }

    public void removeAll(Collection<File> files) {
        for (File file : files) items.remove(file);
    }


    public void selectAll() {
        selectedItems.clear();
        for (int i = 0; i < getItemCount(); i++) {
            selectedItems.append(i, true);
            notifyItemChanged(i);
        }
        onItemSelectedListener.onItemSelected();
    }

    public void clearSelection() {
        ArrayList<Integer> selectedPositions = getSelectedPositions();
        selectedItems.clear();
        for (int i : selectedPositions) notifyItemChanged(i);
        onItemSelectedListener.onItemSelected();
    }


    public void select(ArrayList<Integer> positions) {
        selectedItems.clear();
        for (int i : positions) {
            selectedItems.append(i, true);
            notifyItemChanged(i);
        }
        onItemSelectedListener.onItemSelected();
    }

    public void toggle(int position) {
        if (getSelected(position)) selectedItems.delete(position);
        else selectedItems.append(position, true);
        notifyItemChanged(position);
        onItemSelectedListener.onItemSelected();
    }

    public boolean anySelected() {
        return selectedItems.size() > 0;
    }

    private boolean getSelected(int position) {
        return selectedItems.get(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public int indexOf(FileHolder file) {
        return items.indexOf(file);
    }

    public ArrayList<FileHolder> getSelectedItems() {
        ArrayList<FileHolder> list = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if (getSelected(i)) list.add(get(i));
        }
        return list;
    }

    private ArrayList<FileHolder> getItems() {
        ArrayList<FileHolder> list = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            list.add(get(i));
        }
        return list;
    }

    public ArrayList<Integer> getSelectedPositions() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if (getSelected(i)) list.add(i);
        }
        return list;
    }

    public FileHolder get(int index) {
        return items.get(index);
    }

}
