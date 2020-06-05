package com.mrikso.apkrepacker.recycler;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.PreferenceUtils;
import com.mrikso.apkrepacker.utils.ViewUtils;
import com.mrikso.apkrepacker.utils.view.ElevationImageView;

import java.io.File;

public final class ViewHolder0 extends ViewHolder {

    private TextView name;
    private TextView date;
    private TextView size;

    ViewHolder0(Context context, OnItemClickListener listener, View view) {
        super(context, listener, view);
    }

    @Override
    protected void loadIcon() {
        image = itemView.findViewById(R.id.list_item_image);
    }

    @Override
    protected void loadName() {
        name = itemView.findViewById(R.id.list_item_name);
    }

    @Override
    protected void loadInfo() {
        date = itemView.findViewById(R.id.list_item_date);
        size = itemView.findViewById(R.id.list_item_size);
    }

    @Override
    protected void bindIcon(File file) {
        image.setOnClickListener(onActionClickListener);
        image.setOnLongClickListener(onActionLongClickListener);
        ((ElevationImageView) image).setClipShadow(true);
        setFileIcon(file);
    }

    @Override
    protected void bindName(File file) {
//        boolean extension = PreferenceUtils.getBoolean(context, "pref_extension", true);
        name.setText(/*extension ? getName(file) :*/ file.getName());
    }

    @Override
    protected void bindInfo(File file) {
        date.setText(FileUtil.getLastModified(file));
        size.setText(FileUtil.getSize(context, file));
        setVisibility(date, PreferenceUtils.getBoolean(context, "pref_date", true));
        setVisibility(size, PreferenceUtils.getBoolean(context, "pref_size", false));
    }

    private void createIconGlide(Drawable icon) {
        Glide
                .with(image)
                .load(icon != null ? icon : R.drawable.default_app_icon)
                .placeholder(android.R.color.transparent)
                .transition(DrawableTransitionOptions.withCrossFade(ViewUtils.getShortAnimTime(image)))
                .centerInside()
                .into(image);
    }

    private void setFileIcon(File file) {
        if (FileUtil.FileType.getFileType(file).equals(FileUtil.FileType.APK)) {
            image.setBackground(null);
            ((ElevationImageView) image).setClipShadow(false);
            createIconGlide(AppUtils.getApkIcon(context, file.getAbsolutePath()));
        } else {
            int color = ContextCompat.getColor(context, FileUtil.getColorResource(file));
            final Drawable drawable = ContextCompat.getDrawable(context, FileUtil.getImageResource(file));
            if (PreferenceUtils.getBoolean(context, "pref_icon", true)) {
                DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
                image.setBackground(getBackground(color));
                image.setImageDrawable(drawable);
            } else {
                image.setBackground(null);
                DrawableCompat.setTint(drawable, color);
                image.setImageDrawable(drawable);
            }
        }
    }

    private ShapeDrawable getBackground(int color) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        int size = (int) context.getResources().getDimension(R.dimen.avatar_size);
        shapeDrawable.setIntrinsicWidth(size);
        shapeDrawable.setIntrinsicHeight(size);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }
}