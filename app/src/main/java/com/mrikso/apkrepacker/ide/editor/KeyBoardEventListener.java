package com.mrikso.apkrepacker.ide.editor;

import android.graphics.Rect;
import android.view.ViewTreeObserver;

import com.mrikso.apkrepacker.activity.IdeActivity;

public class KeyBoardEventListener implements ViewTreeObserver.OnGlobalLayoutListener {

   private IdeActivity activity;

    public KeyBoardEventListener(IdeActivity activityIde) {
        this.activity = activityIde;
    }

    public void onGlobalLayout() {
        int i = 0;
        int navHeight = this.activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        navHeight = navHeight > 0 ? this.activity.getResources().getDimensionPixelSize(navHeight) : 0;
        int statusBarHeight = this.activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarHeight > 0) {
            i = this.activity.getResources().getDimensionPixelSize(statusBarHeight);
        }
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        if (activity.mDrawerLayout.getRootView().getHeight() - ((navHeight + i) + rect.height()) <= 0) {
            activity.onHideKeyboard();
        } else {
            activity.onShowKeyboard();
        }
    }
}
