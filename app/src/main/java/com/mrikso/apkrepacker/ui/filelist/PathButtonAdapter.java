package com.mrikso.apkrepacker.ui.filelist;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class PathButtonAdapter extends RecyclerView.Adapter<PathButtonAdapter.ViewHolder> {
    private ArrayList<File> pathList;
    private OnItemClickListener onItemClickListener;

    public File getItem(int position) {
        return pathList.get(position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.patch_button_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        File path = pathList.get(position);
        String name = path.getName();
        if ("/".equals(name) || TextUtils.isEmpty(name))
            name = holder.textView.getContext().getString(R.string.root_path);
        holder.textView.setText(name);
        holder.textView.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(position, v);
        });
    }

    @Override
    public int getItemCount() {
        return pathList == null ? 0 : pathList.size();
    }

    public void setPath(File path) {
        if (pathList == null)
            pathList = new ArrayList<>();
        else
            pathList.clear();

        for (; path != null; ) {
            pathList.add(path);
            path = path.getParentFile();
        }

        Collections.reverse(pathList);
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView;
        }
    }
}