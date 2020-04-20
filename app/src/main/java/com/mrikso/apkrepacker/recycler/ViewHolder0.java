package com.mrikso.apkrepacker.recycler;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.bumptech.glide.Glide;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.mrikso.apkrepacker.utils.FileUtil.getColorResource;
import static com.mrikso.apkrepacker.utils.FileUtil.getImageResource;
import static com.mrikso.apkrepacker.utils.FileUtil.getLastModified;
import static com.mrikso.apkrepacker.utils.FileUtil.getSize;
import static com.mrikso.apkrepacker.utils.PreferenceUtils.getBoolean;


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
    protected void bindIcon(File file, Boolean selected) {
        //String ext = FileUtil.getExtension(file.getName());
        if (getBoolean(context, "pref_icon", true)) {
            image.setOnClickListener(onActionClickListener);
            image.setOnLongClickListener(onActionLongClickListener);
            if (selected) {
                int color = ContextCompat.getColor(context, R.color.misc_file);
                image.setBackground(getBackground(color));
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_selected);
                DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
                image.setImageDrawable(drawable);
            } else {
                int color = ContextCompat.getColor(context, getColorResource(file));
                image.setBackground(getBackground(color));
                Drawable drawable = ContextCompat.getDrawable(context, getImageResource(file));
                DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
                if (FileUtil.FileType.getFileType(file).equals(FileUtil.FileType.APK)) {

                    InputStream inputStream = null;
                    try {
                        inputStream = new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap imageBitmap = BitmapFactory.decodeStream(inputStream, null, options);

                    Glide.with(image).load(imageBitmap != null ? imageBitmap : getImageResource(file)).fitCenter().placeholder(getImageResource(file)).into(image);

                    image.setBackground(null);
                    image.setImageDrawable(AppUtils.getApkIcon(context, file.getAbsolutePath()));
                }/* else if (FileUtil.FileType.getFileType(file).equals(FileUtil.FileType.IMAGE)) {

//                    InputStream is = (InputStream) url.getContent();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.RGB_565;
                    Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

//                    Glide.with(image).load(imageBitmap != null ? imageBitmap : getImageResource(file)).fitCenter().placeholder(getImageResource(file)).into(image);

                    image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    image.setImageBitmap(imageBitmap);
                    image.setBackground(null);
                }*/ else {
                    image.setImageDrawable(drawable);
                }
            }
        } else {
            int color = ContextCompat.getColor(context, getColorResource(file));
            image.setBackground(null);
            Drawable drawable = ContextCompat.getDrawable(context, getImageResource(file));
            DrawableCompat.setTint(drawable, color);
            // if(ext.endsWith("apk")) {
            //   image.setImageDrawable(AppUtils.getApkIcon(context, file.getAbsolutePath()));
            ////  }
            // else {
            image.setImageDrawable(drawable);
            // }
        }
    }

    @Override
    protected void bindName(File file) {
        boolean extension = getBoolean(context, "pref_extension", true);
        name.setText(/*extension ? getName(file) :*/ file.getName());
    }

    @Override
    protected void bindInfo(File file) {
        date.setText(getLastModified(file));
        size.setText(getSize(context, file));
        setVisibility(date, getBoolean(context, "pref_date", true));
        setVisibility(size, getBoolean(context, "pref_size", false));
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