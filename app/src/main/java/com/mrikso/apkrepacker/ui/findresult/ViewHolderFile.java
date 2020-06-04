package com.mrikso.apkrepacker.ui.findresult;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.recycler.OnItemClickListener;
import com.mrikso.apkrepacker.recycler.ViewHolder;
import com.mrikso.apkrepacker.utils.FileUtil;

import java.io.File;

import static com.mrikso.apkrepacker.utils.FileUtil.getColorResource;
import static com.mrikso.apkrepacker.utils.FileUtil.getImageResource;
import static com.mrikso.apkrepacker.utils.PreferenceUtils.getBoolean;

public final class ViewHolderFile extends ViewHolder {

    private TextView name;
   // private TextView date;
   // private TextView size;

    public ViewHolderFile(Context context, OnItemClickListener listener, View view) {
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
       // date = itemView.findViewById(R.id.list_item_date);
       // size = itemView.findViewById(R.id.list_item_size);
    }

    @Override
    protected void bindIcon(File file) {
        if (getBoolean(context, "pref_icon", true)) {
            image.setOnClickListener(onActionClickListener);
            image.setOnLongClickListener(onActionLongClickListener);
/*            if (selected) {
                int color = ContextCompat.getColor(context, R.color.misc_file);
                image.setBackground(getBackground(color));
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.ic_selected);
                DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
                image.setImageDrawable(drawable);
            }
            else { */
                int color = ContextCompat.getColor(context, getColorResource(file));
                image.setBackground(getBackground(color));
                Drawable drawable = ContextCompat.getDrawable(context, getImageResource(file));
                DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
                image.setImageDrawable(drawable);
//            }
        } else {
            int color = ContextCompat.getColor(context, getColorResource(file));
            image.setBackground(null);
            Drawable drawable = ContextCompat.getDrawable(context, getImageResource(file));
            DrawableCompat.setTint(drawable, color);
            image.setImageDrawable(drawable);
        }
    }

    @Override
    protected void bindName(File file) {
        name.setText(/*extension ? getName(file) :*/file.getAbsolutePath().substring((FileUtil.getProjectPath() + "/").length()));
    }

    @Override
    protected void bindInfo(File file) {

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
