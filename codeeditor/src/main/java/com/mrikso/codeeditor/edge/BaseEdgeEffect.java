package com.mrikso.codeeditor.edge;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import android.widget.EdgeEffect;

import androidx.annotation.FloatRange;
import androidx.core.widget.EdgeEffectCompat;

/**
 * @author Liujin 2018-11-07:16:33
 */
public abstract class BaseEdgeEffect {

    /**
     * 效果尺寸
     */
    protected int mWidth;
    protected int mHeight;
    /**
     * 需要绘制效果的view
     */
    protected View mView;
    /**
     * 是否已经释放
     */
    protected boolean isReleased;
    protected EdgeEffect mEffect;

    /**
     * @param view 需要效果的view
     */
    @SuppressWarnings("SuspiciousNameCombination")
    protected BaseEdgeEffect(View view) {

        Context context = view.getContext();

        mEffect = new EdgeEffect(context);
        mView = view;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    public void setSize(int width, int height) {

        mWidth = width;
        mHeight = height;
    }

    /**
     * 在{@link #mView#onDraw(Canvas)}中回调,绘制效果
     */
    public void onDraw(Canvas canvas) {

        if (isReleased) {

            boolean isInvalidate = false;
            if (!mEffect.isFinished()) {
                isInvalidate = true;
            }
            if (isInvalidate) {
                mView.invalidate();
            } else {
                isReleased = false;
            }
        }
    }

    public void release() {

        if (!mEffect.isFinished()) {
            mEffect.onRelease();
            mView.invalidate();
            isReleased = true;
        }
    }

    /**
     * 强制结束
     */
    public void forceStop() {

        mEffect.onPull(0);
        mView.invalidate();
    }

    /**
     * 在需要上边效果的时候调用
     *
     * @param deltaDistanceY 0时没有效果,1时满效果
     */
    public void pull(@FloatRange(from = 0, to = 1) float deltaDistanceY) {

        mEffect.onPull(deltaDistanceY);
        mView.invalidate();
    }

    /**
     * 在需要上边效果的时候调用
     *
     * @param deltaDistanceY 0时没有效果,1时满效果
     */
    public void pull(
            @FloatRange(from = 0, to = 1) float deltaDistanceY,
            @FloatRange(from = 0, to = 1) float displacement) {

        EdgeEffectCompat.onPull(mEffect, deltaDistanceY, displacement);
        mView.invalidate();
    }
}
