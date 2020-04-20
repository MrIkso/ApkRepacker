package com.mrikso.apkrepacker.ui.findresult;

import android.view.View;
import android.widget.TextView;

import com.mrikso.apkrepacker.R;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder;

public class ParentViewHolder extends GroupViewHolder{

    public TextView textView_parent;

    public ParentViewHolder(View itemView) {
        super(itemView);
        textView_parent = itemView.findViewById(R.id.file_text_view);
    }

    @Override
    public void expand() {
        super.expand();
        textView_parent.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.ic_down_arrow,0);
    }

    @Override
    public void collapse() {
        super.collapse();
        textView_parent.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_up_arrow,0);
    }

    //private void animationExpand() { }
    //private void animationCollapse() { }


    public void setGroupName(ExpandableGroup groupName){
        textView_parent.setText(groupName.getTitle());
    }
}

