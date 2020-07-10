package com.mrikso.codeeditor.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;

import com.mrikso.codeeditor.R;

public class HelperUtils {

    //get AccentColor
    public static int fetchAccentColor(Context context) {
        TypedArray typedArray = context.obtainStyledAttributes(new TypedValue().data, new int[]{R.attr.colorAccent});
        int color = typedArray.getColor(0, 0);
        typedArray.recycle();
        return color;
    }

    public static float getDpi(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    // create bitmap from vector drawable
    public static Bitmap getBitmap(Context context, int res) {
        Bitmap bitmap = null;
        Drawable vectorDrawable = ContextCompat.getDrawable(context, res);
        if (vectorDrawable != null) {
            vectorDrawable.setAlpha(210);
            vectorDrawable.setTint(fetchAccentColor(context));
            bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);
            return bitmap;
        }
        return bitmap;
    }
}
