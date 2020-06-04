package com.mrikso.apkrepacker.ui.imageviewer;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.github.chrisbanes.photoview.PhotoView;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.ThemeWrapper;
import com.mrikso.apkrepacker.utils.ViewUtils;
import com.sdsmdg.harjot.vectormaster.VectorMasterView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImageViewerAdapter extends ViewPagerAdapter {

    public static int width = -1, height = -1;

    @NonNull
    private final List<String> mPaths = new ArrayList<>();

/*    @NonNull
    private final View.OnClickListener mListener;*/

/*    ImageViewerAdapter(@NonNull View.OnClickListener listener) {
        mListener = listener;
    }*/

    public void replace(@NonNull List<String> paths) {
        mPaths.clear();
        mPaths.addAll(paths);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mPaths.size();
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull ViewGroup container, int position) {
        View view = LayoutInflater.from(container.getContext()).inflate(R.layout.image_viewer_item, container, false);
        String currentPath = mPaths.get(position);
        FileUtil.FileType fileType = FileUtil.FileType.getFileType(new File(currentPath));

        ViewHolder holder = new ViewHolder(view, currentPath);
        view.setTag(holder);
//        holder.vector.setOnClickListener(v -> mListener.onClick(v));
//        holder.image.setOnClickListener((v) -> mListener.onClick(v));
        container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        if (!fileType.equals(FileUtil.FileType.IMAGE) && fileType.equals(FileUtil.FileType.XML) && new VectorMasterView(container.getContext(), new File(currentPath)).isVector()) {
            loadVector(holder, currentPath);
        } else if (fileType.equals(FileUtil.FileType.IMAGE)) {
            loadImage(holder, currentPath);
        } /*else {
            loadFonts(holder, currentPath);
        }*/
        return view;
    }

    private static void loadImage(@NonNull ViewHolder holder, @NonNull Object object) {
        ViewUtils.fadeIn(holder.progress);
        Glide
                .with(holder.progress)
                .load(object)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(true)
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        showError(holder, e);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .centerInside()
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        loadImageWithInfo(holder, object);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
    }

    private static void loadImageWithInfo(@NonNull ViewHolder holder, @NonNull Object object) {
        ViewUtils.setVisibleOrGone(holder.image, true);
        Glide.with(holder.image)
                .load(object)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .skipMemoryCache(true)
                .dontTransform()
                .placeholder(android.R.color.transparent)
                .transition(DrawableTransitionOptions.withCrossFade(ViewUtils.getShortAnimTime(holder.image)))
                .addListener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onResourceReady(@NonNull Drawable drawable, @NonNull Object model, @NonNull Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        ViewUtils.fadeOut(holder.progress);
                        ViewUtils.setVisibleOrGone(holder.image, true);
                        return false;
                    }

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @NonNull Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        showError(holder, e);
                        return false;
                    }
                })
                .centerInside()
                .into(holder.image);
    }


    private static void loadVector(@NonNull ViewHolder holder, @NonNull String path) {
        ViewUtils.setVisibleOrGone(holder.vector, true);

//        holder.vector.setProjectPatch(FileUtil.getProjectPath());
//        holder.vector.setUseLightTheme(ThemeWrapper.isLightTheme());
        holder.vector.setVectorFile(new File(path));
    }

/*    private static void loadFonts(@NonNull ViewHolder holder, @NonNull String path) {
        ViewUtils.setVisibleOrGone(holder.errorText, true);

        holder.errorText.setText("A B C D E F G H I J K L M N O P Q R S T U V W X Y Z" + "\n" + "a b c d e f g h i j k l m n o p q r s t u v w x y z");
        holder.errorText.setTextColor(Color.BLACK);
        holder.errorText.setTypeface(Typeface.createFromFile(path));
    }*/

    private static void showError(@NonNull ViewHolder holder, @Nullable Exception e) {
        if (e == null) {
            e = new GlideException(null);
        }
        holder.errorText.setText(e.getLocalizedMessage());
        ViewUtils.crossfade(holder.progress, holder.errorText);
    }

    @Override
    public void onDestroyView(@NonNull ViewGroup container, int position, @NonNull View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        Glide.with(container).clear(holder.image);
        container.removeView(view);
    }

    @Override
    public int getViewPosition(@NonNull View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        int index = mPaths.indexOf(holder.path);
        if (index == -1) {
            return POSITION_NONE;
        }
        return index;
    }

    static class ViewHolder {
        public PhotoView image;
        public VectorMasterView vector;
        TextView errorText;
        ProgressBar progress;
        public final String path;

        ViewHolder(@NonNull View view, @NonNull String path) {
            this.path = path;
            image = view.findViewById(R.id.imageView);
            vector = view.findViewById(R.id.vectorView);
            errorText = view.findViewById(R.id.error);
            progress = view.findViewById(R.id.progress);
        }
    }
}