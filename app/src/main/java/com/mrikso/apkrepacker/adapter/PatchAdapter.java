package com.mrikso.apkrepacker.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.mrikso.apkrepacker.R;

import java.util.ArrayList;
import java.util.List;

public class PatchAdapter extends RecyclerView.Adapter<PatchAdapter.ViewHolder> {

    private List<PatchItem> mPatchData = new ArrayList<>();
    private final SparseBooleanArray selectedItems;

    public PatchAdapter(Context context){
        selectedItems = new SparseBooleanArray();
    }

    public void setData(List<PatchItem> data){
        mPatchData.addAll(data);
        notifyDataSetChanged();
    }

    public void addItem(PatchItem item){
        mPatchData.add(item);
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectedItems.clear();
        for (int i = 0; i < getItemCount(); i++) {
            selectedItems.append(i, true);
            notifyItemChanged(i);
        }
    }

    public void clearSelection() {
        ArrayList<Integer> selectedPositions = getSelectedPositions();
        selectedItems.clear();
        for (int i : selectedPositions) notifyItemChanged(i);
    }

    public void toggle(int position) {
        if (getSelected(position)) selectedItems.delete(position);
        else selectedItems.append(position, true);
        notifyItemChanged(position);

    }

    public ArrayList<Integer> getSelectedPositions() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if (getSelected(i)) list.add(i);
        }
        return list;
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
        public void deleteItem(PatchItem item){
        mPatchData.remove(item);
    }

    public void deleteItem(int index){
        mPatchData.remove(index);
    }

    public List<PatchItem> getPatchData() {
        return mPatchData;
    }

    @NonNull
    @Override
    public PatchAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_patch, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatchAdapter.ViewHolder holder, int position) {
        holder.bindTo(mPatchData.get(position), getSelected(position));
    }

    @Override
    public int getItemCount() {
        return mPatchData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView mIcon;
        AppCompatTextView mPatchName;
        AppCompatImageView mPatchPreview;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.img_icon);
            mPatchName = itemView.findViewById(R.id.patch_name);
            mPatchPreview = itemView.findViewById(R.id.btn_preview);
            int color = ContextCompat.getColor(itemView.getContext(), R.color.zip);
            Drawable drawable = ContextCompat.getDrawable(itemView.getContext(), R.drawable.ic_zip);
            DrawableCompat.setTint(drawable, color);
            mIcon.setImageDrawable(drawable);
        }

        private void bindTo(PatchItem item, boolean selected) {
            itemView.setSelected(selected);
            mPatchName.setText(item.mPatchName);
        }
    }

}
