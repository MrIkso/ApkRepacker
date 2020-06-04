package com.mrikso.apkrepacker.autotranslator.translator;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.mrikso.apkrepacker.R;

import java.util.ArrayList;
import java.util.List;

public class TranslateStringsAdapter extends RecyclerView.Adapter<TranslateStringsAdapter.ViewHolder> {

    private List<TranslateItem> mTranslateItemList;
    private Context mContext;
    private boolean translateFinished = false;

    public TranslateStringsAdapter(Context context) {
        this.mContext = context;
        this.mTranslateItemList = new ArrayList<>();
    }

    public void setItems(List<TranslateItem> data) {
        this.mTranslateItemList.addAll(data);
        notifyDataSetChanged();
    }

    public void setItem(TranslateItem data) {
        this.mTranslateItemList.add(data);
        notifyDataSetChanged();
    }

    public void setTranslatedFinished(boolean translateFinished) {
        this.translateFinished = translateFinished;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_stringvalue_translate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TranslateItem mTranslateItem = mTranslateItemList.get(position);
        holder.bindTo(mTranslateItem);
        holder.mRestoreOriginalValue.setEnabled(translateFinished);
        holder.mTranslatedValue.setEnabled(translateFinished);
    }

    public List<TranslateItem> getStringItems() {
        return mTranslateItemList;
    }

    @Override
    public int getItemCount() {
        return mTranslateItemList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageButton mRestoreOriginalValue;
        private AppCompatTextView mKey, mOriginalValue;
        private TextInputEditText mTranslatedValue;

        private ViewHolder(View itemView) {
            super(itemView);
            mRestoreOriginalValue = itemView.findViewById(R.id.restore_string);
            mKey = itemView.findViewById(R.id.string_name);
            mOriginalValue = itemView.findViewById(R.id.origin_value);
            mTranslatedValue = itemView.findViewById(R.id.translated_value);
        }

        void bindTo(TranslateItem item) {
            mRestoreOriginalValue.setOnClickListener(v -> {
                mTranslatedValue.setText(mOriginalValue.getText());
                item.translatedValue = mOriginalValue.getText().toString();
            });
            mKey.setText(item.name);
            mOriginalValue.setText(item.originValue);
            mTranslatedValue.setText(item.translatedValue);
            mTranslatedValue.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (translateFinished && mTranslatedValue.isFocused()) {
                        item.translatedValue = s.toString();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }
    }
}
