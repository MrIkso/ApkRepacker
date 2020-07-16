package com.mrikso.apkrepacker.ui.findresult;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter;
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup;

import java.util.List;

public class FoundStringsAdapter extends ExpandableRecyclerViewAdapter<ParentViewHolder, ChildViewHolders> {

    private Context context;
    private ParentViewHolder.ItemClickListener mListener;

    public FoundStringsAdapter(Context context, ParentViewHolder.ItemClickListener listener, List<? extends ExpandableGroup> groups) {
        super(groups);
        this.context = context;
        mListener = listener;
    }

    @Override
    public ParentViewHolder onCreateGroupViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.find_in_files_item, parent, false);
        return new ParentViewHolder(view, mListener);
    }

    @Override
    public ChildViewHolders onCreateChildViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.find_in_files_item_child, parent, false);
        return new ChildViewHolders(view);
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolders holder, int flatPosition, ExpandableGroup group, int childIndex) {
        final ChildData childData = ((ParentData) group).getItems().get(childIndex);
        holder.setText(childData.getSpannableName(), TextView.BufferType.SPANNABLE);

        holder.view.setOnClickListener(view -> {
            try {
                Intent intent = new Intent(context, CodeEditorActivity.class);
                intent.putExtra("offset", childData.getOffset());
                intent.putExtra("filePath", childData.getPath());
                context.startActivity(intent);
            } catch (Exception ex) {
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


