package com.mrikso.apkrepacker.ui.filelist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.recycler.OnItemClickListener;
import com.mrikso.apkrepacker.recycler.ViewHolder;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.PreferenceUtils;
import com.mrikso.apkrepacker.utils.Theme;
import com.mrikso.apkrepacker.utils.ViewUtils;
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;

import java.io.File;

public final class ViewHolderFile extends ViewHolder {

    private TextView name;
    private boolean isLight;
    // private TextView date;
    // private TextView size;

    public ViewHolderFile(Context context, OnItemClickListener listener, View view) {
        super(context, listener, view);
        isLight = !Theme.getInstance(context).getCurrentTheme().isDark();
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
        // date = itemView.findViewById(R.id.list_item_date);
        // size = itemView.findViewById(R.id.list_item_size);
    }

    @Override
    protected void bindIcon(File file) {
            image.setOnClickListener(onActionClickListener);
            image.setOnLongClickListener(onActionLongClickListener);
            setFileIcon(file);
    }

    void recycle() {
        Glide.with(image).clear(image);
    }

    @Override
    protected void bindName(File file) {
        name.setText(file.getName());
    }

    @Override
    protected void bindInfo(File file) {}

    private void createGlide(Object loadObject) {
        Glide
                .with(image)
                .load(loadObject instanceof File ? ((File) loadObject).getPath() : loadObject)
//                .placeholder(android.R.color.transparent)
                .transition(DrawableTransitionOptions.withCrossFade(ViewUtils.getShortAnimTime(image)))
                .centerInside()
                .into(image);
    }

    private void setFileIcon(File file) {
        FileUtil.FileType fileType = FileUtil.FileType.getFileType(file);
        if (fileType.equals(FileUtil.FileType.IMAGE)) {
            image.setBackground(null);
            createGlide(file);

        } else if (fileType.equals(FileUtil.FileType.XML) && new VectorMasterDrawable(context, file, isLight).isVector()) {
            image.setBackground(null);
            VectorMasterDrawable vectorDrawable = new VectorMasterDrawable(context, file, isLight);
            createGlide(vectorDrawable);

        } else if (fileType.equals(FileUtil.FileType.TTF)) {
            image.setBackground(null);
            createGlide(textToBitmap(file));

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

    private Bitmap textToBitmap(File file) {
        TextView tv = new TextView(context);
        tv.setText("Aa");
        tv.setTextColor(isLight ? Color.BLACK : Color.LTGRAY);
        tv.setTypeface(Typeface.createFromFile(file));
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(20);
        tv.setPadding(0, ViewUtils.dpToPxOffset(5, context), 0, 0);

        Bitmap bitmap = Bitmap.createBitmap(ViewUtils.dpToPxOffset(32, context), ViewUtils.dpToPxOffset(32, context), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tv.layout(0, 0, ViewUtils.dpToPxOffset(32, context), ViewUtils.dpToPxOffset(32, context));
        tv.draw(canvas);
        return bitmap;
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
