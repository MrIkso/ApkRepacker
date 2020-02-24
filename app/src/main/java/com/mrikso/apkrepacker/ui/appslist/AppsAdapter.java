package com.mrikso.apkrepacker.ui.appslist;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.PackageMeta;

import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<AppsAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private List<PackageMeta> mPackages;
    private OnItemInteractionListener mListener;
    public boolean changed;

    public AppsAdapter(Context c) {
        mInflater = LayoutInflater.from(c);
        setHasStableIds(true);
    }

    public void setData(List<PackageMeta> packages) {
        mPackages = packages;
        notifyDataSetChanged();
    }

    public boolean isChanged(){
        return changed;
    }

    public void setInteractionListener(OnItemInteractionListener listener) {
        mListener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_apps, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        changed = true;
        PackageMeta packageMeta = mPackages.get(position);
        holder.bindTo(packageMeta);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.recycle();
    }

    @Override
    public int getItemCount() {
        return mPackages == null ? 0 : mPackages.size();
    }

    @Override
    public long getItemId(int position) {
        return mPackages.get(position).packageName.hashCode();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mAppName;
        private TextView mAppVersion;
        private TextView mAppPackage;
        private AppCompatImageView mAppIcon;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);

            mAppName = itemView.findViewById(R.id.tv_app_name);
            mAppVersion = itemView.findViewById(R.id.tv_app_version);
            mAppPackage = itemView.findViewById(R.id.tv_app_package);
            mAppIcon = itemView.findViewById(R.id.tv_app_icon);

            itemView.findViewById(R.id.app_item).setOnClickListener((v) -> {
                int adapterPosition = getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION)
                    return;

                if (mListener != null)
                    mListener.onBackupButtonClicked(mPackages.get(adapterPosition));
            });
        }

        @SuppressLint("DefaultLocale")
        void bindTo(PackageMeta packageMeta) {
            mAppName.setText(packageMeta.label);
            mAppVersion.setText(String.format("%s (%d)", packageMeta.versionName, packageMeta.versionCode));
            mAppPackage.setText(packageMeta.packageName);

            Glide.with(mAppIcon)
                    .load(packageMeta.iconUri != null ? packageMeta.iconUri : R.drawable.default_app_icon)
                    .placeholder(R.drawable.default_app_icon)
                    .into(mAppIcon);
        }

        void recycle() {
            Glide.with(mAppIcon)
                    .clear(mAppIcon);
        }
    }

    public interface OnItemInteractionListener {
        void onBackupButtonClicked(PackageMeta packageMeta);
    }

}

