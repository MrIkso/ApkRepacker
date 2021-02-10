package com.mrikso.apkrepacker.ui.projectview.treeview.viewholder;

import java.util.List;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ui.projectview.treeview.interfaces.ItemDataClickListener;
import com.mrikso.apkrepacker.ui.projectview.treeview.interfaces.ItemFileClickListener;
import com.mrikso.apkrepacker.ui.projectview.treeview.model.ItemData;

/**
 * @Author Zheng Haibo
 * @PersonalWebsite http://www.mobctrl.net
 * @Description
 */

public class ParentViewHolder extends BaseViewHolder {

	//public ImageView image;
	public TextView text;
	public ImageView expand;
	//public TextView count;
	public RelativeLayout relativeLayout;
	private final int itemMargin;
	private ItemFileClickListener itemFileClickListener;

	public ParentViewHolder(View itemView) {
		super(itemView);
	//	image = itemView.findViewById(R.id.image);
		text = itemView.findViewById(R.id.list_item_name);
		expand = itemView.findViewById(R.id.img_arrow);
		//count = itemView.findViewById(R.id.count);
		relativeLayout = itemView.findViewById(R.id.container);
		itemMargin = itemView.getContext().getResources().getDimensionPixelSize(R.dimen.item_margin);
	}

	public void setItemFileClickListener(ItemFileClickListener listener){
		itemFileClickListener = listener;
	}

	public void bindView(final ItemData itemData, final int position,
						 final ItemDataClickListener imageClickListener) {
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) expand
				.getLayoutParams();
		params.leftMargin = itemMargin * itemData.getTreeDepth();
		expand.setLayoutParams(params);
		text.setText(itemData.getText());
		if (itemData.isExpand()) {
			expand.setRotation(45);
			/*List<ItemData> children = itemData.getChildren();
			if (children != null) {
				count.setText(String.format("(%s)", itemData.getChildren()
						.size()));
			}
			count.setVisibility(View.VISIBLE);*/
		} else {
			expand.setRotation(0);
			//count.setVisibility(View.GONE);
		}
		relativeLayout.setOnClickListener(v -> {
			if (imageClickListener != null) {
				if (itemData.isExpand()) {
					imageClickListener.onHideChildren(itemData);
					itemData.setExpand(false);
					rotationExpandIcon(45, 0);
					//count.setVisibility(View.GONE);
				} else {
					imageClickListener.onExpandChildren(itemData);
					itemData.setExpand(true);
					rotationExpandIcon(0, 45);
					/*List<ItemData> children = itemData.getChildren();
					if (children != null) {
						count.setText(String.format("(%s)", itemData
								.getChildren().size()));
					}*/
					//count.setVisibility(View.VISIBLE);
				}
			}

		});
		if(itemFileClickListener!=null){
			relativeLayout.setOnLongClickListener(v -> {
				itemFileClickListener.onFileLongClick(itemData.getPath());
				return true;
			});
		}
		relativeLayout.setOnLongClickListener(view -> {
			Toast.makeText(view.getContext(), "longclick",
					Toast.LENGTH_SHORT).show();
			return true;
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void rotationExpandIcon(float from, float to) {
		ValueAnimator valueAnimator = ValueAnimator.ofFloat(from, to);
		valueAnimator.setDuration(150);
		valueAnimator.setInterpolator(new DecelerateInterpolator());
		valueAnimator.addUpdateListener(valueAnimator1 -> expand.setRotation((Float) valueAnimator1.getAnimatedValue()));
		valueAnimator.start();
	}
}
