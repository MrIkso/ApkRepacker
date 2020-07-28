package com.mrikso.apkrepacker.ui.filemanager.holder;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.filemanager.misc.ThumbnailHelper;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.File;

public class FileListViewHolder extends RecyclerView.ViewHolder {

    private ImageView mFileIcon;
    private TextView mFileName;
    public TextView mFileSize;
    private TextView mFileDate;

    public FileListViewHolder(ViewGroup parent) {
        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_file, parent, false));

        mFileIcon = itemView.findViewById(R.id.list_item_image);
        mFileName = itemView.findViewById(R.id.list_item_name);
        mFileSize = itemView.findViewById(R.id.list_item_size);
        mFileDate = itemView.findViewById(R.id.list_item_date);
    }

    public void bind(File filePath, int position, OnItemClickListener listener, boolean selected, boolean projectMode) {
        Context context = itemView.getContext();
        FileHolder item = new FileHolder(filePath, context);
        boolean isDirectory = filePath.isDirectory();


        int color = ContextCompat.getColor(context, FileUtil.getColorResource(filePath));
        final Drawable drawable = item.getBestIcon();
        DrawableCompat.setTint(drawable, color);

        //mFileIcon.setClipShadow(false);
        mFileIcon.setImageDrawable(drawable);

        ThumbnailHelper.requestIcon(item, mFileIcon, projectMode);

        mFileName.setText(item.getName());
        mFileSize.setText(isDirectory ? context.getString(R.string.directory) : item.getFormattedSize(context, false));
        mFileDate.setText(item.getFormattedModificationDate(context));

        itemView.setSelected(selected);
        itemView.setOnClickListener(view -> listener.onFileClick(item, position));
        itemView.setOnLongClickListener(view -> {
            listener.onLongClick(item, position);
            return false;
        });
        mFileIcon.setOnClickListener(view -> listener.onLongClick(item, position));
    }

    public interface OnItemClickListener {
        void onFileClick(FileHolder item, int position);

        void onLongClick(FileHolder item, int position);
    }
}
