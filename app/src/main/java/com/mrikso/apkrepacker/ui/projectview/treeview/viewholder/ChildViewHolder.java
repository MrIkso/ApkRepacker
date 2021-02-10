package com.mrikso.apkrepacker.ui.projectview.treeview.viewholder;

import java.io.File;
import java.lang.reflect.Method;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.projectview.treeview.interfaces.ItemFileClickListener;
import com.mrikso.apkrepacker.ui.projectview.treeview.model.ItemData;

/**
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description
 */
public class ChildViewHolder extends BaseViewHolder {

	public TextView text;
	public ImageView image;
	public RelativeLayout relativeLayout;
	private ItemFileClickListener itemFileClickListener;
	private final int itemMargin;
	private final int offsetMargin;

	public ChildViewHolder(View itemView) {
		super(itemView);
		text = itemView.findViewById(R.id.list_item_name);
		image = itemView.findViewById(R.id.img_icon);
		relativeLayout = itemView.findViewById(R.id.container);
		itemMargin = itemView.getContext().getResources()
				.getDimensionPixelSize(R.dimen.item_margin);
		offsetMargin = itemView.getContext().getResources()
				.getDimensionPixelSize(R.dimen.expand_size);
	}

	public void setItemFileClickListener(ItemFileClickListener listener){
		itemFileClickListener = listener;
	}

	public void bindView(final ItemData itemData, int position) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) image
				.getLayoutParams();
		params.leftMargin = itemMargin * itemData.getTreeDepth() + offsetMargin;
		image.setLayoutParams(params);
		text.setText(itemData.getText());
		if(itemFileClickListener !=null){
			relativeLayout.setOnClickListener(view -> itemFileClickListener.onFileClick(itemData.getPath()));
			relativeLayout.setOnLongClickListener(v -> {
				itemFileClickListener.onFileLongClick(itemData.getPath());
				return true;
			});
		}

	}

}
