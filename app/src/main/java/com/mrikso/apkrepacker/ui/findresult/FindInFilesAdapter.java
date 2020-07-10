package com.mrikso.apkrepacker.ui.findresult;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.jecelyin.editor.v2.utils.ExtGrep;
import com.mrikso.apkrepacker.App;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.activity.CodeEditorActivity;

import java.util.ArrayList;
import java.util.List;

public class FindInFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements getItem {
    private List<ExtGrep.Result> data;
    private int findResultsKeywordColor;

    public FindInFilesAdapter(){
        data = new ArrayList<>();
    }

    @Override
    public ExtGrep.Result getItem(int position) {
        return data.get(position);
    }

    public void setResults(List<ExtGrep.Result> results) {
        this.data = results;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.find_in_files_item, parent, false);
        return new RecyclerViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(data!= null){
            TypedArray a = App.getContext().obtainStyledAttributes(new int[]{
                    R.attr.findResultsPath,
                    R.attr.findResultsKeyword,
            });

            findResultsKeywordColor = a.getColor(a.getIndex(1), Color.BLACK);
            a.recycle();
            ExtGrep.Result item = getItem(position);
            SpannableStringBuilder ssb = new SpannableStringBuilder();
            ssb.append(String.format("%1$4d :", item.lineNumber));
            int start = ssb.length();
            ssb.append(item.line);

            ssb.setSpan(new ForegroundColorSpan(findResultsKeywordColor),  start + item.matchStart,  start +item.matchEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ((RecyclerViewHolder) holder).mFileTextView.setText(item.file.getPath());
            ((RecyclerViewHolder) holder).mMatchLineTextView.setText(ssb, TextView.BufferType.SPANNABLE);
            ((RecyclerViewHolder)holder).mFindItem.setOnClickListener(v -> {
                try{
                    Intent intent = new Intent(App.getContext(), CodeEditorActivity.class);
                    intent.putExtra("filePath", item.file.getAbsolutePath());
                    App.getContext().startActivity(intent);
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            });
        }
        else {
            Log.i("FindAdapter", "data is null!" );
        }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    private class RecyclerViewHolder extends RecyclerView.ViewHolder {
        TextView mFileTextView;
        TextView mMatchLineTextView;
        LinearLayout mFindItem;

        RecyclerViewHolder(View view) {
            super(view);
            mFindItem = view.findViewById(R.id.find_item);
            mFileTextView = view.findViewById(R.id.file_text_view);
            mMatchLineTextView = view.findViewById(R.id.match_line_text_view);
        }
    }
}
