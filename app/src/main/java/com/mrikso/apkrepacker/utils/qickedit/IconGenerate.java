package com.mrikso.apkrepacker.utils.qickedit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class IconGenerate {
    public static final int[] mSizes = {36, 48, 72, 96, 144, 192};
    public static final String[] mDens = {"ldpi", "mdpi", "hdpi", "xhdpi", "xxhdpi", "xxxhdpi"};

    public static void generate(String path, Bitmap bm, String name) {
        Bitmap bitmap = createSquaredBitmap(bm);
        for (int i = 0; i < mSizes.length; i++) {
            File dir = new File(path, mDens[i]);
            if (!dir.exists())
                dir.mkdir();
            Bitmap temp = Bitmap.createScaledBitmap(bitmap, mSizes[i], mSizes[i], false);
            savebitmap(temp, new File(dir, name + ".png"));
        }
    }

    public static synchronized Bitmap createSquaredBitmap(Bitmap srcBmp) {
        srcBmp = resizeBitmap(srcBmp, 192);
        int dim = Math.max(srcBmp.getWidth(), srcBmp.getHeight());
        Bitmap dstBmp = Bitmap.createBitmap(dim, dim, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(dstBmp);
        canvas.drawColor(Color.TRANSPARENT);
        canvas.drawBitmap(srcBmp, (dim - srcBmp.getWidth()) / 2, (dim - srcBmp.getHeight()) / 2, null);
        return dstBmp;
    }

    public static synchronized File savebitmap(Bitmap bmp, File f) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            fo.close();
            //bmp.recycle();
            return f;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static synchronized Bitmap resizeBitmap(File srcBmp, int maxSize) {
        Bitmap bitmap = BitmapFactory.decodeFile(srcBmp.getAbsolutePath());
        return resizeBitmap(bitmap, maxSize);
    }

    public static synchronized Bitmap resizeBitmap(Bitmap srcBmp, int maxSize) {
        int outWidth;
        int outHeight;
        int inWidth = srcBmp.getWidth();
        int inHeight = srcBmp.getHeight();
        if (inWidth > inHeight) {
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }
        Bitmap out = Bitmap.createScaledBitmap(srcBmp, outWidth, outHeight, false);
        //myBitmap.recycle();
        return out;
    }

    public static synchronized Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static synchronized int getDominantColor(Drawable drawable) {
        if (drawable == null)
            return Color.DKGRAY;
        Bitmap bitmap = drawableToBitmap(drawable);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int size = width * height;
        int pixels[] = new int[size];
        Bitmap bitmap2 = bitmap.copy(Bitmap.Config.ARGB_4444, false);
        bitmap2.getPixels(pixels, 0, width, 0, 0, width, height);
        HashMap<Integer, Integer> colorMap = new HashMap<>();
        int color = 0;
        Integer count = 0;
        for (int pixel : pixels) {
            color = pixel;
            count = colorMap.get(color);
            if (count == null)
                count = 0;
            colorMap.put(color, ++count);
        }
        int dominantColor = 0;
        int max = 0;
        for (Map.Entry<Integer, Integer> entry : colorMap.entrySet()) {
            if (entry.getValue() > max) {
                max = entry.getValue();
                dominantColor = entry.getKey();
            }
        }
        return (dominantColor == Color.TRANSPARENT ? Color.WHITE : dominantColor);
    }
}

