/*
 * Copyright (C) 2014 George Venios
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mrikso.apkrepacker.ui.filemanager.misc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.mrikso.apkrepacker.ui.filemanager.holder.FileHolder;
import com.mrikso.apkrepacker.ui.filemanager.utils.FileUtils;
import com.mrikso.apkrepacker.ui.filemanager.utils.Utils;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.Theme;
import com.mrikso.apkrepacker.utils.ViewUtils;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecoder;
import com.nostra13.universalimageloader.core.decode.ImageDecodingInfo;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.sdsmdg.harjot.vectormaster.VectorMasterDrawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.net.Uri.decode;
import static com.mrikso.apkrepacker.ui.filemanager.utils.FileUtils.getViewIntentFor;
import static com.nostra13.universalimageloader.core.ImageLoader.getInstance;
import static com.nostra13.universalimageloader.core.assist.ImageScaleType.EXACTLY;


public class ThumbnailHelper {
    private static final int FADE_IN_DURATION = 400;

    private static ImageDecoder sDecoder = null;
    private static DisplayImageOptions.Builder sDefaultImageOptionsBuilder;
    private static boolean sProjectMode = false;

    public static void requestIcon(FileHolder holder, ImageView imageView, boolean projectMode) {
        sProjectMode = projectMode;
        Uri uri = Uri.fromFile(holder.getFile());
        DisplayImageOptions options = defaultOptionsBuilder()
                .extraForDownloader(holder)
                .build();

        getInstance().displayImage(decode(uri.toString()), imageView, options);
    }

    /**
     * Unfortunately getting the default is not straightforward..
     * See https://groups.google.com/forum/#!topic/android-developers/UkfP70MtjGA
     */
    private static Drawable getAssociatedAppIconDrawable(FileHolder holder, Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = getViewIntentFor(holder, context);
        Drawable icon = null;

        // Contrary to queryIntentActivities documentation, the first item IS NOT the same
        // as the one returned by resolveActivity.
        ResolveInfo resolveInfo = pm.resolveActivity(intent, MATCH_DEFAULT_ONLY);
        if (!FileUtils.isResolverActivity(resolveInfo)) {
            icon = resolveInfo.loadIcon(pm);
        } else if (!holder.getMimeType().equals("*/*")) {
            final List<ResolveInfo> lri = pm.queryIntentActivities(intent,
                    MATCH_DEFAULT_ONLY);
            if (lri != null && lri.size() > 0) {
                // Again, contrary to documentation, best match is actually the last item.
                icon = lri.get(lri.size() - 1).loadIcon(pm);
            }
        }

        return icon;
    }

    private static Drawable getApkIconDrawable(FileHolder holder, Context context) {
        PackageManager pm = context.getPackageManager();
        String path = holder.getFile().getPath();
        PackageInfo pInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (pInfo != null) {
            ApplicationInfo aInfo = pInfo.applicationInfo;

            // Bug in SDK versions >= 8. See here:
            // http://code.google.com/p/android/issues/detail?id=9151
            aInfo.sourceDir = path;
            aInfo.publicSourceDir = path;

            return aInfo.loadIcon(pm);
        }

        return null;
    }

    private static Drawable getVectorIconDrawable(FileHolder holder, Context context) {
        boolean isLight = !Theme.getInstance(context).getCurrentTheme().isDark();
        VectorMasterDrawable vectorDrawable = new VectorMasterDrawable(context, holder.getFile(), isLight);
        if (vectorDrawable.isVector()) {
            return vectorDrawable;
        }

        return null;
    }

    public static ImageDecoder imageDecoder(final Context context) {
        if (sDecoder == null) {
            sDecoder = new ImageDecoder() {
                private BaseImageDecoder internal = new BaseImageDecoder(false);

                @Override
                public Bitmap decode(ImageDecodingInfo idi) throws IOException {
                    FileHolder holder = (FileHolder) idi.getExtraForDownloader();
                    Bitmap bitmap = null;

                    if (!holder.getFile().isDirectory()) {
                        FileUtil.FileType fileType = FileUtil.FileType.getFileType(holder.getFile());
                        switch (fileType) {
                            case IMAGE:
                                try {
                                    ImageDecodingInfo info = new ImageDecodingInfo(
                                            idi.getImageKey(), idi.getImageUri(),
                                            idi.getOriginalImageUri(), idi.getTargetSize(),
                                            ViewScaleType.CROP,
                                            idi.getDownloader(), defaultOptionsBuilder().build()
                                    );
                                    Bitmap bmp = internal.decode(info);

                                    bitmap = bmp;
                                /*// Make bmp round
                                int targetHeight = idi.getTargetSize().getHeight();
                                int targetWidth = idi.getTargetSize().getWidth();
                                int radius = targetWidth / 2;
                                bitmap = Bitmap.createBitmap(targetWidth, targetHeight, ARGB_8888);
                                BitmapShader shader = new BitmapShader(bmp, CLAMP, CLAMP);
                                Canvas canvas = new Canvas(bitmap);
                                Paint paint = new Paint();
                                paint.setAntiAlias(true);
                                paint.setShader(shader);
                                canvas.drawCircle(radius, radius, radius, paint);*/

                                    //    bmp.recycle();
                                } catch (FileNotFoundException ex) {
                                    DLog.e(ex);
                                    return null;
                                    // Fail silently.
                                }
                                break;
                            case APK:
                                Drawable drawable = getApkIconDrawable(holder, context);
                                if (drawable != null) {
                                    bitmap = Utils.bitmapFrom(drawable);
                                }
                                break;
                            case TTF:
                                bitmap = textToBitmap(context, holder.getFile());
                                break;
                            case XML:
                                if (sProjectMode) {
                                    Drawable vectorIconDrawable = getVectorIconDrawable(holder, context);
                                    if (vectorIconDrawable != null) {
                                        bitmap = ((BitmapDrawable) vectorIconDrawable).getBitmap();
                                    }
                                }
                                break;
                        }
                    }

                    return bitmap;
                }
            };
        }

        return sDecoder;
    }

    private static Bitmap textToBitmap(Context context, File file) {
        TextView tv = new TextView(context);
        tv.setText("Aa");
        tv.setTextColor(!Theme.getInstance(context).getCurrentTheme().isDark() ? Color.BLACK : Color.LTGRAY);
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

    private static DisplayImageOptions.Builder defaultOptionsBuilder() {
        if (sDefaultImageOptionsBuilder == null) {
            sDefaultImageOptionsBuilder = new DisplayImageOptions.Builder()
                    .displayer(new FadeInBitmapDisplayer(FADE_IN_DURATION, true, true, false))
                    .cacheInMemory(true)
                    .cacheOnDisk(false)
                    .delayBeforeLoading(125)
                    .imageScaleType(EXACTLY);
        }

        return sDefaultImageOptionsBuilder;
    }

}
