package com.jecelyin.editor.v2.manager;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class EditorPager extends ViewPager {

    public EditorPager(Context context) {
        super(context);
        setFocusable(true);
    }

    public EditorPager(Context context, AttributeSet attributes) {
        super(context, attributes);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
       return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev){
        return false;
    }

}
