package com.mrikso.apkrepacker.ui.stringlist;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.mrikso.apkrepacker.autotranslator.translator.TranslateItem;

import java.util.List;

public class StringsDiffCallback extends DiffUtil.Callback {

    private List<TranslateItem> oldList;
    private List<TranslateItem> newList;

    public StringsDiffCallback(List<TranslateItem> oldList, List<TranslateItem> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        TranslateItem old = oldList.get(oldItemPosition);
        TranslateItem newItem = newList.get(newItemPosition);
        if(old !=null && newItem !=null){
            return old.equals(newItem);
        }
        return false;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
    }

    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        return super.getChangePayload(oldItemPosition, newItemPosition);
    }
}
