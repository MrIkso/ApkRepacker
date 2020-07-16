package com.mrikso.apkrepacker.ui.filelist;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.mrikso.apkrepacker.recycler.Callback;
import com.mrikso.apkrepacker.recycler.OnItemClickListener;
import com.mrikso.apkrepacker.recycler.OnItemSelectedListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class FileAdapter extends RecyclerView.Adapter<ViewHolderFile> {

    private final Context context;
    private final SortedList<File> items;
    private final SparseBooleanArray selectedItems;
    private final Callback callback;
    @LayoutRes
    private int itemLayout;

    private int spanCount;
    private OnItemClickListener onItemClickListener;
    private OnItemSelectedListener onItemSelectedListener;
    private boolean mFind;

    public FileAdapter(Context context) {
        this.context = context;
        this.callback = new Callback(context, this);
        this.items = new SortedList<>(File.class, callback);
        this.selectedItems = new SparseBooleanArray();
    }

    @NonNull
    @Override
    public ViewHolderFile onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(itemLayout, parent, false);
        return new ViewHolderFile(parent.getContext(), onItemClickListener, itemView);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderFile holder, int position) {
        holder.setFind(mFind);
        holder.setData(get(position), getSelected(position));
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolderFile holder) {
        holder.recycle();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItemLayout(int itemLayout) {
        this.itemLayout = itemLayout;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemSelectedListener(OnItemSelectedListener onItemSelectedListener) {
        this.onItemSelectedListener = onItemSelectedListener;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public void setIsFind(boolean find){
        mFind = find;
    }

    public void add(File file) {
        items.add(file);
    }

    public void addAll(File... files) {
        try {
            items.addAll(files);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addAll(Collection<File> files) {
        items.addAll(files);
    }

    public void clear() {
        if (items != null)
            while (items.size() > 0) items.removeItemAt(items.size() - 1);
    }

    public void refresh() {
        for (int i = 0; i < getItemCount(); i++) {
            notifyItemChanged(i);
        }
    }

    public void removeAll(Collection<File> files) {
        for (File file : files) items.remove(file);
    }

    public void updateItemAt(int index, File file) {
        items.updateItemAt(index, file);
    }

    public void removeItemAt(int index) {
        items.removeItemAt(index);
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

    public void update(int criteria) {
        if (callback.update(criteria)) {
            ArrayList<File> list = getItems();
            clear();
            addAll(list);
        }
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

    public int indexOf(File file) {
        return items.indexOf(file);
    }

    public ArrayList<File> getSelectedItems() {
        ArrayList<File> list = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if (getSelected(i)) list.add(get(i));
        }
        return list;
    }

    private ArrayList<File> getItems() {
        ArrayList<File> list = new ArrayList<>();
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

    public File get(int index) {
        return items.get(index);
    }

}
