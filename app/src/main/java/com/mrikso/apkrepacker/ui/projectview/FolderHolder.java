package com.mrikso.apkrepacker.ui.projectview;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.unnamed.b.atv.model.TreeNode;

import java.io.File;

import static com.mrikso.apkrepacker.utils.FileUtil.getColorResource;
import static com.mrikso.apkrepacker.utils.FileUtil.getColorResourceSimple;
import static com.mrikso.apkrepacker.utils.FileUtil.getImageResource;

public class FolderHolder extends TreeNode.BaseNodeViewHolder<FolderHolder.TreeItem> {
    private static final String TAG = "FolderHolder";
    private AppCompatTextView txtName;
    private LayoutInflater inflater;
    private AppCompatImageView imgArrow;
    private boolean leaf = false;

    public FolderHolder(Context context) {
        super(context);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View createNodeView(final TreeNode node, final TreeItem item) {
        View view = inflater.inflate(R.layout.list_tree_item_file, null, false);
        txtName = view.findViewById(R.id.node_value);
        txtName.setText(item.getFile().getName());
        txtName.setContentDescription(item.getFile().getPath());

        imgArrow = view.findViewById(R.id.img_arrow);
        this.leaf = node.isLeaf();
        View imgNew = view.findViewById(R.id.img_add);
        View imgDelete = view.findViewById(R.id.img_delete);

        if (item.getFile().isDirectory() && !node.isRoot()) {
            imgNew.setVisibility(View.VISIBLE);
        } else {
            imgNew.setVisibility(View.GONE);
        }
        if (FileUtil.isRoot(item.getProjectFile(), item.getFile())) {
            imgDelete.setVisibility(View.GONE);
        } else {
            imgDelete.setVisibility(View.VISIBLE);
        }
        if (node.isLeaf()) {
            imgArrow.setVisibility(View.INVISIBLE);
        }

        final File file = item.getFile();
        setIcon(view.findViewById(R.id.img_icon), file);

        final ProjectFileContract.FileActionListener listener = item.getListener();

        imgDelete.setOnClickListener(view1 -> {
            if (listener != null) {
                listener.clickRemoveFile(file, new ProjectFileContract.Callback() {
                    @Override
                    public void onSuccess(File old) {
                        getTreeView().removeNode(node);
                    }

                    @Override
                    public void onFailed(@Nullable Exception e) {

                    }
                });
            }
        });
        imgNew.setOnClickListener(view12 -> {
            if (listener != null) {
                listener.onClickNewButton(file, new ProjectFileContract.Callback() {
                    @Override
                    public void onSuccess(File newf) {
                        TreeNode child = new TreeNode(new TreeItem(item.getProjectFile(), newf, listener));
                        getTreeView().addNode(node, child);
                    }

                    @Override
                    public void onFailed(@Nullable Exception e) {

                    }
                });
            }
        });
        return view;
    }

    private void setIcon(AppCompatImageView view, File fileDetail) {
        view.setBackground(null);
        int color = ContextCompat.getColor(context, getColorResourceSimple(fileDetail));
        Drawable drawable = ContextCompat.getDrawable(context, getImageResource(fileDetail));
        DrawableCompat.setTint(drawable, color);
        view.setImageDrawable(drawable);
    }

    public ShapeDrawable getBackground(int color) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        int size = (int) context.getResources().getDimension(R.dimen.avatar_size);
        shapeDrawable.setIntrinsicWidth(size);
        shapeDrawable.setIntrinsicHeight(size);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    @Override
    public void toggle(boolean active) {
        if (!leaf) {
            imgArrow.setImageResource(active ? R.drawable.ic_keyboard_arrow_down_white : R.drawable.ic_arrow_right);
        }
    }

    public static class TreeItem {
        private File projectFile;
        @NonNull
        private File file;
        @Nullable
        private ProjectFileContract.FileActionListener listener;

        public TreeItem(@NonNull File projectFile, @Nullable File file,
                        @Nullable ProjectFileContract.FileActionListener listener) {
            this.projectFile = projectFile;
            this.file = file;
            this.listener = listener;
        }

        public File getProjectFile() {
            return projectFile;
        }

        @Nullable
        public ProjectFileContract.FileActionListener getListener() {
            return listener;
        }

        @NonNull
        public File getFile() {
            return file;
        }
    }
}
