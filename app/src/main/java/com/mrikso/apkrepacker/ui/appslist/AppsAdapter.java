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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.adapter.RecyclerViewAdapter;
import com.mrikso.apkrepacker.fragment.AppsFragment;
import com.mrikso.apkrepacker.utils.PackageMeta;
import com.mrikso.apkrepacker.utils.ViewUtils;

import java.util.List;

public class AppsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private static List<PackageMeta> mPackages;
    private static OnItemInteractionListener mListener;

    public AppsAdapter(Context c) {
        this.context = c;
        setHasStableIds(true);
    }

    public void setData(List<PackageMeta> packages) {
        mPackages = packages;
        notifyDataSetChanged();
    }

    public void setInteractionListener(OnItemInteractionListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_apps, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AppsFragment.loadingView.setVisibility(View.GONE);
        PackageMeta packageMeta = mPackages.get(position);
        if (holder instanceof ViewHolder) {
            final ViewHolder recyclerViewHolder = (ViewHolder) holder;
            recyclerViewHolder.bindTo(packageMeta);
        }
    }

    @Override
    public int getItemCount() {
        return mPackages == null ? 0 : mPackages.size();
    }

    @Override
    public long getItemId(int position) {
        return mPackages.get(position).packageName.hashCode();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
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

            itemView.setOnClickListener((v) -> {
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

            Glide
                    .with(mAppIcon)
                    .load(packageMeta.iconDrawable == null ? R.drawable.default_app_icon : packageMeta.iconDrawable)
                    .placeholder(android.R.color.transparent)
                    .transition(DrawableTransitionOptions.withCrossFade(ViewUtils.getShortAnimTime(mAppIcon)))
                    .centerInside()
                    .into(mAppIcon);
        }

        void recycle() {
            Glide.with(mAppIcon).clear(mAppIcon);
        }
    }

    public interface OnItemInteractionListener {
        void onBackupButtonClicked(PackageMeta packageMeta);
    }
}

