package com.mrikso.apkrepacker.ui.themelist;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.android.material.card.MaterialCardView;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.utils.AppUtils;
import com.mrikso.apkrepacker.utils.Theme;
import com.mrikso.apkrepacker.utils.ViewUtils;

public class ThemeView extends MaterialCardView {

    private TextView mThemeTitle;
    private TextView mThemeMessage;

    public ThemeView(Context context) {
        super(context);
        init();
    }

    public ThemeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThemeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LinearLayoutCompat container = new LinearLayoutCompat(getContext());
        container.setOrientation(LinearLayoutCompat.VERTICAL);
        LayoutParams containerLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        containerLayoutParams.gravity = Gravity.CENTER;
        addView(container, containerLayoutParams);

        mThemeTitle = new AppCompatTextView(getContext());
        mThemeTitle.setGravity(Gravity.CENTER);
        mThemeTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        LinearLayoutCompat.LayoutParams titleLayoutParams = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(mThemeTitle, titleLayoutParams);

        mThemeMessage = new AppCompatTextView(getContext());
        mThemeMessage.setGravity(Gravity.CENTER);
        mThemeMessage.setVisibility(GONE);
        LinearLayoutCompat.LayoutParams messageLayoutParams = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        container.addView(mThemeMessage, messageLayoutParams);
    }

    public void setTheme(Theme.ThemeDescriptor theme) {
        mThemeTitle.setText(theme.getName(getContext()));

        Context themedContext = new ContextThemeWrapper(getContext(), theme.getTheme());
        setCardBackgroundColor(ViewUtils.getThemeColor(themedContext, R.attr.colorPrimary));

        int accentColor = ViewUtils.getThemeColor(themedContext, R.attr.colorAccent);
        setStrokeColor(accentColor);
        mThemeTitle.setTextColor(accentColor);

        if (AppUtils.apiIsAtLeast(Build.VERSION_CODES.M)) {
            setRippleColor(themedContext.getColorStateList(R.color.md_selector_color));
        }

        mThemeMessage.setTextColor(ViewUtils.getThemeColor(themedContext, android.R.attr.textColorPrimary));
    }

    public void setMessage(@Nullable CharSequence message) {
        if (message == null) {
            mThemeMessage.setVisibility(GONE);
        } else {
            mThemeMessage.setVisibility(VISIBLE);
            mThemeMessage.setText(message);
        }
    }

    public void setMessage(@StringRes int message) {
        mThemeMessage.setVisibility(VISIBLE);
        mThemeMessage.setText(message);
    }
}
