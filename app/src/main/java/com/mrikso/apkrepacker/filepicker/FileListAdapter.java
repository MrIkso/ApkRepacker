
package com.mrikso.apkrepacker.filepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.mrikso.apkrepacker.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import static com.mrikso.apkrepacker.utils.FileUtil.getColorResource;
import static com.mrikso.apkrepacker.utils.FileUtil.getImageResource;
import static com.mrikso.apkrepacker.utils.PreferenceUtils.getBoolean;

/**
 * Adapter Class that extends {@link BaseAdapter} that is
 * used to populate {@link ListView} with file info.
 */
public class FileListAdapter extends BaseAdapter {
    private ArrayList<FileListItem> listItem;
    private Context context;
    private FileItemSelectedListener fileItemSelectedListener;
    private int selectType;
    private int selectMode;

    public FileListAdapter(Context context, ArrayList<FileListItem> listItem, int selectType, int selectMode) {
        this.context = context;
        this.listItem = listItem;
        this.selectType = selectType;
        this.selectMode = selectMode;
    }

    @Override
    public int getCount() {
        return listItem.size();
    }

    @Override
    public FileListItem getItem(int i) {
        return listItem.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        final ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.dialog_file_list_item, viewGroup, false);
            holder = new ViewHolder();
            holder.tvName = view.findViewById(R.id.tv_file_name);
            holder.tvInfo = view.findViewById(R.id.tv_file_info);
            holder.ivType = view.findViewById(R.id.iv_file_type);
            holder.cbMark = view.findViewById(R.id.cb_file_mark);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        final FileListItem item = listItem.get(position);

        String name = item.getName();
        boolean isDirectory = item.isDirectory();
       // String ext = FileUtil.getExtension(item.getName());
        if (getBoolean(context, "pref_icon", true)) {
            //   holder.ivType.setImageResource(isDirectory ? R.drawable.ic_directory : R.drawable.ic_misc_file);
            // holder.ivType.setColorFilter(context.getResources().getColor(isDirectory ? R.color.directory : R.color.misc_file, context.getTheme()));
            int color = ContextCompat.getColor(context, getColorResource(new File(item.getPath())));
            holder.ivType.setBackground(getBackground(color));
            Drawable drawable = ContextCompat.getDrawable(context, getImageResource(new File(item.getPath())));
            DrawableCompat.setTint(drawable, Color.rgb(255, 255, 255));
         //   if(ext.endsWith("apk")){
          //      holder.ivType.setImageDrawable(AppUtils.getApkIcon(context,item.getPath()));
          //  }else {
                holder.ivType.setImageDrawable(drawable);
          //  }
        }
        else {
            int color = ContextCompat.getColor(context, getColorResource(new File(item.getPath())));
            holder.ivType.setBackground(null);
            Drawable drawable = ContextCompat.getDrawable(context, getImageResource(new File(item.getPath())));
            DrawableCompat.setTint(drawable, color);
           // if(ext.endsWith("apk")){
            //    holder.ivType.setImageDrawable(AppUtils.getApkIcon(context,item.getPath()));
           // }else {
                holder.ivType.setImageDrawable(drawable);
          //  }
        }

        holder.ivType.setContentDescription(name);
        holder.tvName.setText(name);

        if (position == 0 && name.startsWith(context.getString(R.string.label_parent_dir))) {
            holder.tvInfo.setText(R.string.label_parent_directory);
            holder.cbMark.setVisibility(View.INVISIBLE);
        } else {
            holder.tvInfo.setText(new SimpleDateFormat("yyyy/dd/MM HH:mm", Locale.getDefault()).format(item.getTime()));
            switch (selectType) {
                case FilePickerDialog.TYPE_FILE:
                    holder.cbMark.setVisibility(item.isDirectory() ? View.INVISIBLE : View.VISIBLE);
                    break;
                case FilePickerDialog.TYPE_DIR:
                    holder.cbMark.setVisibility(item.isDirectory() ? View.VISIBLE : View.INVISIBLE);
                    break;
                case FilePickerDialog.TYPE_ALL:
                default:
                    holder.cbMark.setVisibility(View.VISIBLE);
                    break;
            }
        }

        holder.cbMark.setOnCheckedChangeListener(null);
        if (MarkedItemList.hasItem(item.getPath())) {
            holder.cbMark.setChecked(true);
        } else {
            holder.cbMark.setChecked(false);
        }

        holder.cbMark.setOnCheckedChangeListener((checkbox, isChecked) -> {
            if (isChecked) {
                if (selectMode == FilePickerDialog.MODE_MULTI) {
                    String[] paths = MarkedItemList.getSelectedPaths();
                    for (String path : paths) {
                        if (path != null && item.getPath().startsWith(path + "/")) {
                            // 已经勾选了item的父目录，则勾选无效
                            Toast.makeText(context, R.string.toast_error_not_selectable, Toast.LENGTH_SHORT).show();
                            checkbox.setChecked(false);
                            return;
                        } else if (path != null && path.startsWith(item.getPath() + "/")) {
                            // item包含已勾选的其他子路径，子路径移除
                            MarkedItemList.removeSelectedItem(path);
                        }
                    }
                    MarkedItemList.addMultiItem(item);
                } else {
                    MarkedItemList.addSingleFile(item);
                }
            } else {
                MarkedItemList.removeSelectedItem(item.getPath());
            }
            item.setMarked(isChecked);
            fileItemSelectedListener.onFileItemSelected();
            notifyDataSetChanged();
        });
        return view;
    }

    private static class ViewHolder {
        ImageView ivType;
        TextView tvName, tvInfo;
        CheckBox cbMark;
    }
    private ShapeDrawable getBackground(int color) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        int size = (int) context.getResources().getDimension(R.dimen.avatar_size);
        shapeDrawable.setIntrinsicWidth(size);
        shapeDrawable.setIntrinsicHeight(size);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }

    public void setFileItemSelectedListener(FileItemSelectedListener notifyItemChecked) {
        this.fileItemSelectedListener = notifyItemChecked;
    }


    public interface FileItemSelectedListener {

        /**
         * Called when a checkbox is checked.
         */
        void onFileItemSelected();
    }
}
