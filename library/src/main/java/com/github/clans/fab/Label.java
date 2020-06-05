package com.github.clans.fab;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public class Label extends MaterialCardView {

    private int mShadowRadius;
    private int mShadowXOffset;
    private int mShadowYOffset;
    private int mShadowColor;
    private Drawable mBackgroundDrawable;
    private boolean mShowShadow = true;
    private int mRawWidth;
    private int mRawHeight;
    private int mColorNormal;
    private int mColorPressed;
    private int mColorRipple;
    private int mCornerRadius;
    private FloatingActionButton mFab;
    private Animation mShowAnimation;
    private Animation mHideAnimation;
    private boolean mUsingStyle;
    private boolean mHandleVisibilityChanges = true;
    private TextView mTextView = new TextView(getContext());
    private String mText;
    private int mTextColor;

    public Label(Context context) {
        super(context);
        init();
    }

    public Label(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Label(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        int[][] states = new int[][]{new int[]{android.R.attr.state_enabled}, new int[]{android.R.attr.state_pressed}};
        int[] colors = new int[]{mColorNormal, mColorPressed, mColorRipple};

        ColorStateList colorStateList = new ColorStateList(states, colors);
        setCardBackgroundColor(colorStateList);
        setRippleColor(colorStateList);

        int padding = (int) TypedValue.applyDimension(8, 1.0f, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        mTextView.setLayoutParams(layoutParams2);
        mTextView.setPadding(20, 10, 20, 10);

        addView(mTextView);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);

    }

/*    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(calculateMeasuredWidth(), calculateMeasuredHeight());
    }*/

/*    private int calculateMeasuredWidth() {
        if (mRawWidth == 0) {
            mRawWidth = getMeasuredWidth();
        }
        return getMeasuredWidth() + calculateShadowWidth();
    }*/

/*    private int calculateMeasuredHeight() {
        if (mRawHeight == 0) {
            mRawHeight = getMeasuredHeight();
        }
        return getMeasuredHeight() + calculateShadowHeight();
    }*/

/*    int calculateShadowWidth() {
        return mShowShadow ? (mShadowRadius + Math.abs(mShadowXOffset)) : 0;
    }*/

/*    int calculateShadowHeight() {
        return mShowShadow ? (mShadowRadius + Math.abs(mShadowYOffset)) : 0;
    }*/

/*    void updateBackground() {
        LayerDrawable layerDrawable;
        if (mShowShadow) {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    new Shadow(),
                    createFillDrawable()
            });

            int leftInset = mShadowRadius + Math.abs(mShadowXOffset);
            int topInset = mShadowRadius + Math.abs(mShadowYOffset);
            int rightInset = (mShadowRadius + Math.abs(mShadowXOffset));
            int bottomInset = (mShadowRadius + Math.abs(mShadowYOffset));

            layerDrawable.setLayerInset(
                    1,
                    leftInset,
                    topInset,
                    rightInset,
                    bottomInset
            );
        } else {
            layerDrawable = new LayerDrawable(new Drawable[]{
                    createFillDrawable()
            });
        }

        setBackgroundCompat(layerDrawable);
    }*/

/*    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private Drawable createFillDrawable() {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_pressed}, createRectDrawable(mColorPressed));
        drawable.addState(new int[]{}, createRectDrawable(mColorNormal));

        if (Util.hasLollipop()) {
            RippleDrawable ripple = new RippleDrawable(new ColorStateList(new int[][]{{}},
                    new int[]{mColorRipple}), drawable, null);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            setClipToOutline(true);
            mBackgroundDrawable = ripple;
            return ripple;
        }

        mBackgroundDrawable = drawable;
        return drawable;
    }*/

/*    private Drawable createRectDrawable(int color) {
        RoundRectShape shape = new RoundRectShape(
                new float[]{
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius,
                        mCornerRadius
                },
                null,
                null);
        ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }*/

    private void setShadow(FloatingActionButton fab) {
        mShadowColor = fab.getShadowColor();
        mShadowRadius = fab.getShadowRadius();
        mShadowXOffset = fab.getShadowXOffset();
        mShadowYOffset = fab.getShadowYOffset();
        mShowShadow = fab.hasShadow();

        setShowShadow(mShowShadow);
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setBackgroundCompat(Drawable drawable) {
        if (Util.hasJellyBean()) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    public void setBackgroundColor(@ColorInt int backgroundColor) {
        super.setCardBackgroundColor(backgroundColor);
    }

    private void playShowAnimation() {
        if (mShowAnimation != null) {
            mHideAnimation.cancel();
            startAnimation(mShowAnimation);
        }
    }

    private void playHideAnimation() {
        if (mHideAnimation != null) {
            mShowAnimation.cancel();
            startAnimation(mHideAnimation);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionDown() {
        if (mUsingStyle) {
            mBackgroundDrawable = getBackground();
        }

        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{android.R.attr.state_pressed});
        } else if (Util.hasLollipop() && mBackgroundDrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed});
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
//        setPressed(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void onActionUp() {
        if (mUsingStyle) {
            mBackgroundDrawable = getBackground();
        }

        if (mBackgroundDrawable instanceof StateListDrawable) {
            StateListDrawable drawable = (StateListDrawable) mBackgroundDrawable;
            drawable.setState(new int[]{});
        } else if (Util.hasLollipop() && mBackgroundDrawable instanceof RippleDrawable) {
            RippleDrawable ripple = (RippleDrawable) mBackgroundDrawable;
            ripple.setState(new int[]{});
            ripple.setHotspot(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            ripple.setVisible(true, true);
        }
//        setPressed(false);
    }

    void setFab(FloatingActionButton fab) {
        mFab = fab;
        setShadow(fab);
    }

    void setShowShadow(boolean show) {
        mShowShadow = show;

        if (mShowShadow) {
            setCardElevation(mShadowRadius);
        } else {
            setCardElevation(0);
        }
    }

    void setCornerRadius(int cornerRadius) {
        mCornerRadius = cornerRadius;
        setRadius(mCornerRadius);
    }

    void setColors(int colorNormal, int colorPressed, int colorRipple) {
        mColorNormal = colorNormal;
        mColorPressed = colorPressed;
        mColorRipple = colorRipple;

        int[][] states = new int[][]{new int[]{android.R.attr.state_enabled}, new int[]{android.R.attr.state_pressed}};
        int[] colors = new int[]{mColorNormal, mColorPressed};

        ColorStateList colorStateList = new ColorStateList(states, colors);
        setCardBackgroundColor(colorStateList);
    }

    void show(boolean animate) {
        if (animate) {
            playShowAnimation();
        }
        setVisibility(VISIBLE);
    }

    void hide(boolean animate) {
        if (animate) {
            playHideAnimation();
        }
        setVisibility(INVISIBLE);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
/*        requestLayout();
        invalidateOutline();
        requestApplyInsets();

        layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setLayoutParams(layoutParams);*/

        setClipToOutline(true);
        setClipToPadding(true);
    }

    void setShowAnimation(Animation showAnimation) {
        mShowAnimation = showAnimation;
    }

    void setHideAnimation(Animation hideAnimation) {
        mHideAnimation = hideAnimation;
    }

    void setUsingStyle(boolean usingStyle) {
        mUsingStyle = usingStyle;
    }

    void setHandleVisibilityChanges(boolean handle) {
        mHandleVisibilityChanges = handle;
    }

    boolean isHandleVisibilityChanges() {
        return mHandleVisibilityChanges;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mFab == null || mFab.getOnClickListener() == null || !mFab.isEnabled()) {
            return super.onTouchEvent(event);
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                onActionUp();
                mFab.onActionUp();
                break;

            case MotionEvent.ACTION_CANCEL:
                onActionUp();
                mFab.onActionUp();
                break;
        }

        mGestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    GestureDetector mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onDown(MotionEvent e) {
            onActionDown();
            if (mFab != null) {
                mFab.onActionDown();
            }
            return super.onDown(e);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onActionUp();
            if (mFab != null) {
                mFab.onActionUp();
            }
            return super.onSingleTapUp(e);
        }
    });

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        this.mText = text;
        this.mTextView.setText(text);
        setClipToOutline(true);
        setClipToPadding(true);
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(@ColorInt int textColor) {
        this.mTextColor = textColor;
        this.mTextView.setTextColor(textColor);
    }

    public void setTextColor(ColorStateList colors) {
        this.mTextView.setTextColor(colors);
    }

    public void setTextColorFromRes(@ColorRes int textColor) {
        setTextColor(ContextCompat.getColor(getContext(), textColor));
    }

    public void setTextAppearance(Context context, @StyleRes int resID) {
        this.mTextView.setTextAppearance(context, resID);
    }

    public void setMaxLines(int maxLines) {
        this.mTextView.setMaxLines(maxLines);
    }

    public void setTextSize(int unit, float size) {
        this.mTextView.setTextSize(unit, size);
    }

    public void setSingleLine(boolean singleLine) {
        this.mTextView.setSingleLine(singleLine);
    }

    public void setTypeface(Typeface tf) {
        this.mTextView.setTypeface(tf);
    }

    public void setEllipsize(TextUtils.TruncateAt where) {
        this.mTextView.setEllipsize(where);
    }

/*    private class Shadow extends Drawable {

        private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private Paint mErase = new Paint(Paint.ANTI_ALIAS_FLAG);

        private Shadow() {
            this.init();
        }

        private void init() {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mColorNormal);

            mErase.setXfermode(PORTER_DUFF_CLEAR);

            if (!isInEditMode()) {
                mPaint.setShadowLayer(mShadowRadius, mShadowXOffset, mShadowYOffset, mShadowColor);
            }
        }*/

/*        @Override
        public void draw(Canvas canvas) {
            RectF shadowRect = new RectF(
                    mShadowRadius + Math.abs(mShadowXOffset),
                    mShadowRadius + Math.abs(mShadowYOffset),
                    mRawWidth,
                    mRawHeight
            );

            canvas.drawRoundRect(shadowRect, mCornerRadius, mCornerRadius, mPaint);
            canvas.drawRoundRect(shadowRect, mCornerRadius, mCornerRadius, mErase);
        }

        @Override
        public void setAlpha(int alpha) {

        }

        @Override
        public void setColorFilter(ColorFilter cf) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }
    }*/
}
