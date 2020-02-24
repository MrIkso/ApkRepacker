package com.mrikso.apkrepacker.ui.findresult;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.core.text.SpannableStringBuilder;
import android.graphics.Color;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.jecelyin.editor.v2.ui.activities.MainActivity;
import com.jecelyin.editor.v2.utils.ExtGrep;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class MyAdapter extends ExpandableRecyclerViewAdapter<ParentViewHolder,ChildViewHolders>{

    public Context context;
    private List<ExtGrep.Result> data;

    public MyAdapter(Context context,List<? extends ExpandableGroup> groups) {
        super(groups);
        this.context = context;
    }


    @Override
    public ParentViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.find_in_files_item,parent,false);
        return new ParentViewHolder(view);
    }

    @Override
    public ChildViewHolders onCreateChildViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.find_in_files_item_child,parent,false);
        return new ChildViewHolders(view);
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolders holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final ChildData childData = ((ParentData)group).getItems().get(childIndex);
        holder.setText(childData.getSpannableName(), TextView.BufferType.SPANNABLE);

        holder.textView_child.setOnClickListener(view ->{
            try{
                Intent intent = new Intent(App.getContext(), MainActivity.class);
                intent.putExtra("filePath", childData.getPath());
                App.getContext().startActivity(intent);
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
                });
             //   Toast.makeText(context, "Selected : " + childData.getName(), Toast.LENGTH_SHORT).show()

    }

    @Override
    public void onBindGroupViewHolder(ParentViewHolder holder, int flatPosition, ExpandableGroup group) {
        holder.setGroupName(group);
    }
}


