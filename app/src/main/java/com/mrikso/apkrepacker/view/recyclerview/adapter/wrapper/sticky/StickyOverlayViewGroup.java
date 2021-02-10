package com.mrikso.apkrepacker.view.recyclerview.adapter.wrapper.sticky;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The overlay class. It seems like a {@link View#getOverlay()} But It's for me to manager all the children in View.
 * We are not actually add this view to View. Instead we use {@link RecyclerView#addItemDecoration(RecyclerView.ItemDecoration)} draw the ViewGroup.
 *
 * This class responsible for manager all the sticky header view. Simulated user gesture for all the views.
 *
 * @see StickyAdapter#onViewAttachedToWindow(RecyclerView.ViewHolder)
 * @see #findClickableViewInternal(View, float, float) find the clickable view by point.
 * @see #getOverlayView() Return the container of the extra view.
 */
public class StickyOverlayViewGroup {
    /**
     * The actual container for the drawables (and views, if it's a ViewGroupOverlay).
     * All of the management and rendering details for the overlay are handled in
     * OverlayViewGroup.
     */
    private OverlayViewGroup overlayViewGroup;

    public StickyOverlayViewGroup(RecyclerView hostView) {
        this.overlayViewGroup = new OverlayViewGroup(hostView.getContext(), hostView);
    }

    /**
     * Used internally by View and ViewGroup to handle drawing and invalidation
     * of the overlay
     *
     * @return
     */
    public ViewGroup getOverlayView() {
        return overlayViewGroup;
    }

    public void add(@NonNull View view) {
        overlayViewGroup.add(view);
    }

    public void remove(@NonNull View view) {
        overlayViewGroup.remove(view);
    }

    public int getChildCount(){
        return overlayViewGroup.getChildCount();
    }

    public View getChildAt(int index){
        return overlayViewGroup.getChildAt(index);
    }

    public <T extends View>T findViewById(@IdRes int id){
        return (T) overlayViewGroup.findViewById(id);
    }

    public void addView(View child) {
        overlayViewGroup.addView(child, -1);
    }

    public void addView(View child, int index) {
        overlayViewGroup.addView(child, index);
    }

    public void addView(View child, int width, int height) {
        overlayViewGroup.addView(child,width,height);
    }

    public void addView(View child, ViewGroup.LayoutParams params) {
        overlayViewGroup.addView(child,params);
    }

    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        overlayViewGroup.addView(child,index,params);
    }

    public View findStickyView(int position){
        View adapterView=null;
        int childCount = getChildCount();
        for(int i=0;i<childCount;i++){
            View childView = getChildAt(i);
            LayoutParams layoutParams= (LayoutParams) childView.getLayoutParams();
            if(layoutParams.position==position){
                adapterView=childView;
                break;
            }
        }
        return adapterView;
    }

    View findViewInternal(float x,float y){
        View findView=null;
        int childCount = getChildCount();
        for(int i=childCount-1;i>=0;i--){
            View childView = getChildAt(i);
            //inside the rect.
            float left=childView.getLeft()+childView.getTranslationX();
            float top=childView.getTop()+childView.getTranslationY();
            float right=childView.getRight()+childView.getTranslationX();
            float bottom=childView.getBottom()+childView.getTranslationY();
            if(left <= x && top <= y && right >= x && bottom >= y) {
                findView=findClickableViewInternal(childView,x,y);
                break;
            }
        }
        return findView;
    }

    private View findClickableViewInternal(View view,float x,float y){
        View findView=null;
        if(view.isClickable()||view.isLongClickable()){
            return view;
        }
        if(view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for(int i=childCount-1;i>=0;i--){
                View childView = viewGroup.getChildAt(i);
                float left=childView.getLeft()+childView.getTranslationX();
                float top=childView.getTop()+childView.getTranslationY();
                float right=childView.getRight()+childView.getTranslationX();
                float bottom=childView.getBottom()+childView.getTranslationY();
                //inside the rect.
                if(left <= x && top <= y && right >= x && bottom >= y) {
                    findView=findClickableViewInternal(childView,x,y);
                    break;
                }
            }
        }
        return findView;
    }

    /**
     * Removes all content from the overlay.
     */
    public void removeAllViews() {
        overlayViewGroup.removeAllViews();
    }

    public void removeView(View view) {
        overlayViewGroup.removeView(view);
    }

    public void removeViewAt(int index) {
        overlayViewGroup.removeViewAt(index);
    }

    public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        overlayViewGroup.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
    }

    public boolean isEmpty() {
        return overlayViewGroup.isEmpty();
    }

    public class OverlayViewGroup extends ViewGroup implements GestureDetector.OnGestureListener, RecyclerView.OnItemTouchListener {
        private final GestureDetectorCompat gestureDetector;
        /**
         * The View for which this is an overlay. Invalidations of the overlay are redirected to
         * this host view.
         */
        final RecyclerView hostView;

        public OverlayViewGroup(Context context, RecyclerView hostView) {
            super(context);
            this.hostView = hostView;
            this.hostView.addOnItemTouchListener(this);
            this.gestureDetector= new GestureDetectorCompat(context, this);
        }

        public void add(@NonNull View child) {
            if (child == null) {
                throw new IllegalArgumentException("view must be non-null");
            }
            if (child.getParent() instanceof ViewGroup) {
                ViewGroup parent = (ViewGroup) child.getParent();
                parent.removeView(child);
            }
            super.addView(child);
            invalidate();
        }

        @Override
        public void setPressed(boolean pressed) {
            super.setPressed(pressed);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
        }

        @Override
        public void setActivated(boolean activated) {
            super.setActivated(activated);
        }

        @Override
        public void setClickable(boolean clickable) {
            super.setClickable(clickable);
        }

        public void remove(@NonNull View view) {
            if (view == null) {
                throw new IllegalArgumentException("view must be non-null");
            }
            super.removeView(view);
            invalidate();
        }

        @Override
        public void invalidate() {
            hostView.invalidate();
        }

        public void removeAllViews() {
            removeAllViews();
            invalidate();
        }

        @Override
        public void removeView(View view) {
            super.removeView(view);
        }

        @Override
        public void removeViewAt(int index) {
            super.removeViewAt(index);
        }

        public boolean isEmpty() {
            return 0 == getChildCount();
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
        }

        @Override
        public void childDrawableStateChanged(View child) {
            super.childDrawableStateChanged(child);
            invalidate();
        }

        @Override
        protected void drawableStateChanged() {
            super.drawableStateChanged();
            invalidate();
        }

        @Override
        public void refreshDrawableState() {
            super.refreshDrawableState();
            invalidate();
        }

        @Override
        public void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
            super.measureChild(child, parentWidthMeasureSpec, parentHeightMeasureSpec);
        }

        /**
         * If we click on our view. on intercept this touch event.
         * @param rv
         * @param event
         * @return
         */
        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
            int action = event.getActionMasked();
            //Here we release the press state.
            if(action==MotionEvent.ACTION_DOWN){
                float x=event.getX();
                float y=event.getY();
                View view = findClickableViewInternal(this,x, y);
                if(null!=view&&view.isEnabled()){
                    view.setPressed(true);
                    invalidate();
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            int action = event.getActionMasked();
            if(action==MotionEvent.ACTION_UP||
                    action==MotionEvent.ACTION_CANCEL||
                    action==MotionEvent.ACTION_POINTER_UP){
                float x=event.getX();
                float y=event.getY();
                View view = findClickableViewInternal(this,x, y);
                if(null!=view&&view.isEnabled()){
                    view.setPressed(false);
                    invalidate();
                }
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            float x=e.getX();
            float y=e.getY();
            View view = findClickableViewInternal(this,x, y);
            if(null!=view){
                view.setPressed(false);
                if(view.isEnabled()&&view.isClickable()){
                    return view.performClick();
                }
                invalidate();
            }
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            float x=e.getX();
            float y=e.getY();
            View view = findClickableViewInternal(this,x, y);
            if(null!=view){
                view.setPressed(false);
                if(view.isEnabled()&&view.isLongClickable()){
                    view.performLongClick();
                }
                invalidate();
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        protected boolean checkLayoutParams(LayoutParams p) {
            return p instanceof StickyOverlayViewGroup.LayoutParams;
        }

        /**
         * Generate a default layout params.
         * When you call {@link ViewGroup#addView(View)}.
         * It will ask for a default LayoutParams
         * @return
         */
        protected LayoutParams generateDefaultLayoutParams() {
            return new StickyOverlayViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        }

        /**
         * Create a layout params from a giving one.
         * @param p
         * @return
         */
        protected LayoutParams generateLayoutParams(LayoutParams p) {
            return new StickyOverlayViewGroup.LayoutParams(p);
        }

        public LayoutParams generateLayoutParams(AttributeSet attrs) {
            Context context = getContext();
            return new StickyOverlayViewGroup.LayoutParams(context,attrs);
        }
    }
    /**
     * Our custom LayoutParams object
     */
    public class LayoutParams extends ViewGroup.MarginLayoutParams {
        public int position;

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
    }

}
