package com.mrikso.apkrepacker.ui.findresult;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.FileUtil;
import com.mrikso.apkrepacker.utils.common.DLog;
import com.thoughtbot.expandablerecyclerview.listeners.OnGroupClickListener;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class ParentViewHolder extends GroupViewHolder implements View.OnClickListener {

    private TextView textView_parent;
    private ImageView imageView;
    private OnGroupClickListener listener;
    private ItemClickListener mListener;

    public ParentViewHolder(View itemView, ItemClickListener listener) {
        super(itemView);
        mListener = listener;
        textView_parent = itemView.findViewById(R.id.file_text_view);
        imageView = itemView.findViewById(R.id.mark);
    }

    @Override
    public void expand() {
        super.expand();
        textView_parent.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0);
    }

    @Override
    public void collapse() {
        super.collapse();
        textView_parent.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0);
    }

    public void changeTextColor() {
        imageView.setVisibility(View.VISIBLE);
        textView_parent.setTextColor(textView_parent.getContext().getResources().getColor(R.color.google_green));
    }

    @Override
    public void setOnGroupClickListener(OnGroupClickListener listener) {
        super.setOnGroupClickListener(listener);
        this.listener = listener;
    }

    @SuppressLint("ResourceType")
    public void setGroupName(ExpandableGroup groupName) {
        textView_parent.setText(groupName.getTitle());
        textView_parent.setOnClickListener(v -> {
            if (listener != null) {
                DLog.d("setGroupName", getAdapterPosition());
                listener.onGroupClick(getAdapterPosition());
            }
        });

        textView_parent.setOnLongClickListener(v -> {
            mListener.onTitleClick(FileUtil.getProjectPath() + "/" + groupName.getTitle(), getAdapterPosition(), this);
            return true;
        });
    }

    public interface ItemClickListener {
        void onTitleClick(String file, int id, ParentViewHolder holder);
    }
}

