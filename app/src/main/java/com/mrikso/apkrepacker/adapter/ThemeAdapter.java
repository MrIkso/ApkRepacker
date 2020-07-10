package com.mrikso.apkrepacker.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.themelist.ThemeView;
import com.mrikso.apkrepacker.utils.Theme;

import java.util.List;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {

    private List<Theme.ThemeDescriptor> mThemes;

    private Context mContext;
    private LayoutInflater mInflater;

    private OnThemeInteractionListener mListener;

    public ThemeAdapter(Context c) {
        mContext = c;
        mInflater = LayoutInflater.from(c);
        setHasStableIds(true);
    }

    public void setThemes(List<Theme.ThemeDescriptor> themes) {
        mThemes = themes;
        notifyDataSetChanged();
    }

    public void setOnThemeInteractionListener(OnThemeInteractionListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bindTo(mThemes.get(position));
    }

    @Override
    public int getItemCount() {
        return mThemes != null ? mThemes.size() : 0;
    }

    @Override
    public long getItemId(int position) {
        return mThemes.get(position).getTheme();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private ThemeView mThemeView;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.requestFocus();

            mThemeView = itemView.findViewById(R.id.theme_view_theme_item);

            mThemeView.setOnClickListener(v -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                if (mListener != null)
                    mListener.onThemeClicked(mThemes.get(adapterPosition));
            });
        }

        private void bindTo(Theme.ThemeDescriptor theme) {
            mThemeView.setTheme(theme);

        }
    }

    public interface OnThemeInteractionListener {

        void onThemeClicked(Theme.ThemeDescriptor theme);

    }
}
