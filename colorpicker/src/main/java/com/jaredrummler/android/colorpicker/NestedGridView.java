package com.jaredrummler.android.colorpicker;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;
import androidx.annotation.RestrictTo;

@RestrictTo(RestrictTo.Scope.LIBRARY)
public class NestedGridView extends GridView {

  public NestedGridView(Context context) {
    super(context);
  }

  public NestedGridView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public NestedGridView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
    super.onMeasure(widthMeasureSpec, expandSpec);
  }
}
