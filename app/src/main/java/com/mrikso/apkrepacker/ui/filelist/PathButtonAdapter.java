package com.mrikso.apkrepacker.ui.filelist;

import android.util.Log;
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
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        File path = pathList.get(position);
        String name = path.getName();
        holder.textView.setText(name);
        holder.textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, position != pathList.size() - 1 ? R.drawable.ic_chevron_right : 0, 0);
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
            try {
                String pathName = path.toString();
                if (pathName.endsWith(pathName.substring(pathName.indexOf("projects/"))))
                    pathList.add(path);
            } catch (Exception e) {
                e.printStackTrace();
            }
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