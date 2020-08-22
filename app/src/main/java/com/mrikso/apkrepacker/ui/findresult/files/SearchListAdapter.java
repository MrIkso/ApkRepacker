package com.mrikso.apkrepacker.ui.findresult.files;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.ui.filemanager.holder.FileListViewHolder;
import com.mrikso.apkrepacker.ui.filemanager.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static java.util.Collections.emptyList;

public class SearchListAdapter extends RecyclerView.Adapter<SearchListViewHolder> {
    private List<File> data = emptyList();
    private FileListViewHolder.OnItemClickListener onItemClickListener;

    public SearchListAdapter(FileListViewHolder.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void notifyDataUpdated(@NonNull List<File> updatedData) {
        int firstUpdatedIndex = Utils.firstDifferentItemIndex(data, updatedData);
        if (firstUpdatedIndex != -1) {  // Lists are not equal
            int oldCount = data.size();
            int newCount = updatedData.size();
            data = updatedData;

            if (firstUpdatedIndex == oldCount) {  // Data appended
                notifyItemRangeInserted(firstUpdatedIndex, updatedData.size() - oldCount);
            } else {
                notifyDataSetChanged();
            }
        }
    }


    @NotNull
    @Override
    public SearchListViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new SearchListViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(SearchListViewHolder holder, int position) {
        holder.bind(data.get(position).getAbsoluteFile(), position, onItemClickListener, false, true);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void clear() {
        data.clear();
        notifyDataSetChanged();
    }
}

