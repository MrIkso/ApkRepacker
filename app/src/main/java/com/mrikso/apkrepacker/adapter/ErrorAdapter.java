package com.mrikso.apkrepacker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.jecelyin.common.utils.UIUtils;
import com.mrikso.apkrepacker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ErrorAdapter extends RecyclerView.Adapter<ErrorAdapter.ViewHolder> {
    private List<String> mErrorLines;
    private static OnItemInteractionListener mItemInteractionListener;

    public ErrorAdapter() {
        mErrorLines = new ArrayList<>();
    }

    @NonNull
    @Override
    public ErrorAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_error, parent, false);
        return new ErrorAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ErrorAdapter.ViewHolder holder, int position) {
        if (!mErrorLines.isEmpty())
            holder.bindTo(mErrorLines.get(position));
    }

    @Override
    public int getItemCount() {
        return mErrorLines.size();
    }

    public void setItemInteractionListener(OnItemInteractionListener itemInteractionListener) {
        mItemInteractionListener = itemInteractionListener;
    }

    public void updateMessage(String errorMessage) {
        //mErrorLines.clear();
        if (errorMessage != null) {
            String[] array = errorMessage.split("\\r?\\n");
            for (String line : array) {
                if (!"".equals(line)) {
                    mErrorLines.add(line);
                }
            }
        }
        notifyDataSetChanged();
    }

    public List<String> getErrorLines() {
        return mErrorLines;
    }

    public interface OnItemInteractionListener {
        void OnItemClicked(String filePath, int lineNumber);

        void OnItemLongClick(String message);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView mErrorText;
        AppCompatImageButton mViewFile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mErrorText = itemView.findViewById(R.id.message);
            mViewFile = itemView.findViewById(R.id.btn_preview);
        }

        public void bindTo(String message) {
            boolean viewable = false;
            int commaPos = message.indexOf(58);
            if (commaPos != -1) {
                final String filePath = message.substring(0, commaPos);
                if (new File(filePath).exists()) {
                    viewable = true;
                    mViewFile.setVisibility(View.VISIBLE);
                    int lineNum = -1;
                    int nextCommaPos = message.indexOf(58, commaPos + 1);
                    if (nextCommaPos != -1) {
                        try {
                            lineNum = Integer.parseInt(message.substring(commaPos + 1, nextCommaPos));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    int finalLineNum = lineNum;
                    mViewFile.setOnClickListener(v -> {
                        if (finalLineNum > 0) {
                            mItemInteractionListener.OnItemClicked(filePath, finalLineNum);
                        }
                    });
                }
            }
            if (!viewable) {
                mViewFile.setVisibility(View.GONE);
            }
            mErrorText.setText(message);
            itemView.setOnLongClickListener(view -> {
                mItemInteractionListener.OnItemLongClick(message);
                return true;
            });
        }
    }
}
