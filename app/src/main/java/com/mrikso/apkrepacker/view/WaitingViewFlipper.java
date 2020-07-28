package com.mrikso.apkrepacker.view;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

/**
 * Children must always follow the PAGE_INDEX_* indexing.
 */
public class WaitingViewFlipper extends ViewFlipper {
    public static final int PAGE_INDEX_CONTENT = 0;
    public static final int PAGE_INDEX_LOADING = 1;
    public static final int PAGE_INDEX_PERMISSION_DENIED = 2;
    public static final int ANIM_START_DELAY = 0;

    private Handler mWaiterHandler = new Handler();
    private Runnable mWaiter;
    private int mWaitingChild = -1;

    public WaitingViewFlipper(Context context) {
        super(context);
    }

    public WaitingViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDisplayedChildDelayed(final int child) {
        if (mWaiter != null) {
            mWaiterHandler.removeCallbacks(mWaiter);
        }

        mWaiter = () -> WaitingViewFlipper.this.setDisplayedChild(child);
        mWaiterHandler.postDelayed(mWaiter, ANIM_START_DELAY);
        mWaitingChild = child;
    }

    public void setDisplayedChild(int whichChild) {
        if (mWaitingChild != -1) {
            mWaitingChild = -1;
            mWaiterHandler.removeCallbacks(mWaiter);
        }

        if (getDisplayedChild() != whichChild)
            super.setDisplayedChild(whichChild);
    }
}
