package com.mrikso.apkrepacker.ui.findresult;

import android.view.View;
import android.widget.TextView;

import com.mrikso.apkrepacker.R;
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder;

public class ChildViewHolders extends ChildViewHolder{

    public TextView textView_child;

    public ChildViewHolders(View itemView) {
        super(itemView);
        textView_child = itemView.findViewById(R.id.match_line_text_view);
    }

    public void setChildText(String name){
        textView_child.setText(name);
    }


    public void setText(CharSequence text, TextView.BufferType type) {
        textView_child.setText(text, type);

    }
}
