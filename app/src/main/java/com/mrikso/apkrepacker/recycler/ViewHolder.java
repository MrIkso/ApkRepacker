package com.mrikso.apkrepacker.recycler;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.mrikso.apkrepacker.utils.view.ElevationImageView;

import java.io.File;

public abstract class ViewHolder extends RecyclerView.ViewHolder {

    public final Context context;

    public ImageView image;
    public View.OnClickListener onActionClickListener;
    public View.OnLongClickListener onActionLongClickListener;
    public View.OnClickListener onClickListener;
    public View.OnLongClickListener onLongClickListener;

    //----------------------------------------------------------------------------------------------

    public ViewHolder(Context context, OnItemClickListener listener, View view) {
        super(view);
        this.context = context;
        setClickListener(listener);
        loadIcon();
        loadName();
        loadInfo();
    }

    //----------------------------------------------------------------------------------------------

    protected abstract void loadIcon();

    protected abstract void loadName();

    protected abstract void loadInfo();

    protected abstract void bindIcon(File file);

    protected abstract void bindName(File file);

    protected abstract void bindInfo(File file);

    //----------------------------------------------------------------------------------------------

    private void setClickListener(final OnItemClickListener listener) {
        this.onActionClickListener = v -> listener.onItemLongClick(getAdapterPosition());
        this.onActionLongClickListener = v -> listener.onItemLongClick(getAdapterPosition());
        this.onClickListener = v -> listener.onItemClick(getAdapterPosition());
        this.onLongClickListener = v -> listener.onItemLongClick(getAdapterPosition());
    }

    public void setData(final File file, Boolean selected) {
        itemView.setOnClickListener(onClickListener);
        itemView.setOnLongClickListener(onLongClickListener);
        itemView.setSelected(selected);
        bindIcon(file);
        bindName(file);
        bindInfo(file);
    }

    void setVisibility(View view, Boolean visibility) {
        view.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }
}