package com.mrikso.apkrepacker.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.StringUtils;
import com.mrikso.apkrepacker.utils.ThemeWrapper;
import com.mrikso.apkrepacker.utils.view.Colors;

import java.util.List;
import java.util.Objects;

public class LogAdapter extends ArrayAdapter<String> {
    private List<String> objects;
    private Context mContext;
    private int textSize;

    public LogAdapter(Context context, int textviewid, List<String> objects, int textSize) {
        super(context, textviewid, objects);
        this.objects = objects;
        mContext = context;
        textSize(textSize);
    }

    @Override
    public int getCount() {
        return ((null != objects) ? objects.size() : 0);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private int getLogColor(String s) {
        int color;
        boolean lt = ThemeWrapper.isLightTheme();
        switch (s.charAt(0)) {
            case 'I':
                color = (lt ? Colors.VERY_DARK_GREEN : Colors.VERY_LIGHT_GREEN);
                break;
            case 'V':
                color = (lt ? Colors.BLACK : Colors.WHITE);
                break;
            case 'E':
                color = (lt ? Colors.DARK_RED : Colors.LIGHT_RED);
                break;
            case 'S':
                color = (lt ? Colors.VERY_DARK_RED : Colors.VERY_LIGHT_RED);
                break;
            case 'W':
                color = (lt ? Colors.DARK_BROWN : Colors.VERY_LIGHT_YELLOW);
                break;
            case 'D':
                color = (lt ? Colors.VERY_DARK_BLUE : Colors.VERY_LIGHT_BLUE);
                break;
            case 'A':
                color = Colors.PURPLE;
                break;
            default:
                color = (lt ? Colors.BLACK : Colors.WHITE);
                break;
        }
        return color;
    }

    @Override
    public String getItem(int position) {
        return ((null != objects) ? objects.get(position) : null);
    }

    public void textSize(int size) {
        this.textSize = size;
    }

    @SuppressLint("InflateParams")
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (null == view) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = Objects.requireNonNull(vi).inflate(R.layout.log_view_item, null);
        }
        view.setMinimumHeight(0);
        String data = objects.get(position);
        if (null != data && !TextUtils.isEmpty(data)) {
            final TextView textview = view.findViewById(R.id.logitemText);
            textview.setText(data);
            textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
            textview.setTextColor(getLogColor(data));
            textview.setOnClickListener(v -> StringUtils.setClipboard(mContext, textview.getText().toString()));
        }
        return view;
    }
}
