package com.mrikso.apkrepacker.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.Matrix4f;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.ScriptIntrinsicColorMatrix;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import org.jetbrains.annotations.NotNull;

public class ElevationImageView extends AppCompatImageView {
    private boolean clipShadow = false;
    private Bitmap shadowBitmap;
    private float customElevation = 20;
    private Rect rect;
    private boolean forceClip = false;
    private boolean isTranslucent = false;
    private boolean isBlurShadow = true;
    private RenderScript rs;
    private ScriptIntrinsicBlur blurScript;
    private ScriptIntrinsicColorMatrix colorMatrixScript;

    public ElevationImageView(@NotNull Context context) {
        super(context);
        this.rect = new Rect();
        this.init(null);
    }

    public ElevationImageView(@NotNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.rect = new Rect();
        this.init(attrs);
    }

    public ElevationImageView(@NotNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.rect = new Rect();
        this.init(attrs);
    }

    private final void init(AttributeSet attrs) {
//        TypedArray a = this.getContext().obtainStyledAttributes(attrs, R.styleable.ElevationImageView);
//        int elevation = Build.VERSION.SDK_INT >= 21 ? (int) this.customElevation : 0;
//        this.customElevation = (float) a.getDimensionPixelSize(R.styleable.ElevationImageView_compatEvelation, elevation);
//        this.clipShadow = a.getBoolean(R.styleable.ElevationImageView_clipShadow, false);
//        this.setTranslucent(a.getBoolean(R.styleable.ElevationImageView_isTranslucent, false));
//        this.setForceClip(a.getBoolean(R.styleable.ElevationImageView_forceClip, false));
//        a.recycle();
    }

    public final void setClipShadow(boolean value) {
        this.clipShadow = value;
        this.invalidate();
    }

    public final void setForceClip(boolean value) {
        this.forceClip = value;
        this.invalidate();
    }

    public final boolean isTranslucent() {
        return this.isTranslucent;
    }

    public final void setTranslucent(boolean value) {
        this.isTranslucent = value;
        this.invalidate();
    }

    public final boolean isBlurShadow() {
        return this.isBlurShadow;
    }

    public final void setBlurShadow(boolean value) {
        this.isBlurShadow = value;
        this.invalidate();
    }

    public void setElevation(float elevation) {
        this.customElevation = elevation;
        this.invalidate();
    }

    public final void setElevationDp(float elevation) {
        this.customElevation = TypedValue.applyDimension(1, elevation, getResources().getDisplayMetrics());
        this.invalidate();
    }

    protected void onDraw(@Nullable Canvas canvas) {
        if (!isInEditMode() && canvas != null) {
            if (shadowBitmap == null && customElevation > 0) {
                generateShadow();
            }
            Drawable drawable = this.getDrawable();
            Rect bounds = drawable.copyBounds();
            if (shadowBitmap != null) {
                canvas.save();

                if (!clipShadow) {
                    canvas.getClipBounds(rect);
                    rect.inset(-2 * (int) getBlurRadius(), -2 * (int) getBlurRadius());
                    if (forceClip) {
                        canvas.clipRect(rect);
                    } else {
                        canvas.save();
                        canvas.clipRect(rect);
                    }
                    canvas.drawBitmap(shadowBitmap, bounds.left - getBlurRadius(), bounds.top - getBlurRadius() / 2f, null);
                }

                canvas.restore();
            }
        }

        super.onDraw(canvas);
    }


    public void invalidate() {
        this.shadowBitmap = null;
        super.invalidate();
    }

    protected void onDetachedFromWindow() {
        if (!isInEditMode()) {
            blurScript.destroy();
            colorMatrixScript.destroy();
            rs.destroy();
        }
        super.onDetachedFromWindow();
    }

    protected void onAttachedToWindow() {
        if (!isInEditMode()) {

            if (forceClip) {
                ((ViewGroup) getParent()).setClipChildren(false);
            }
            rs = RenderScript.create(getContext());
            Element element = Element.U8_4(rs);
            blurScript = ScriptIntrinsicBlur.create(rs, element);
            colorMatrixScript = ScriptIntrinsicColorMatrix.create(rs, element);
        }
        super.onAttachedToWindow();
    }

    private float getBlurRadius() {
        float maxElevation = TypedValue.applyDimension(1, 24.0F, getResources().getDisplayMetrics());
        return Math.min(25.0F * (this.customElevation / maxElevation), 25.0F);
    }

    private Bitmap getShadowBitmap(Bitmap bitmap) {
        Allocation allocationIn = Allocation.createFromBitmap(rs, bitmap);
        Allocation allocationOut = Allocation.createTyped(rs, allocationIn.getType());

        Matrix4f matrix;
        if (isTranslucent) {
            matrix = new Matrix4f(new float[]{
                    0.4f, 0f, 0f, 0f,
                    0f, 0.4f, 0f, 0f,
                    0f, 0f, 0.4f, 0f,
                    0f, 0f, 0f, 0.6f});
        } else if (isBlurShadow) {
            matrix = new Matrix4f(new float[]{
                    0.8f, 0f, 0f, 0f,
                    0f, 0.8f, 0f, 0f,
                    0f, 0f, 0.8f, 0f,
                    0f, 0f, 0f, 1f});
        } else {
            matrix = new Matrix4f(new float[]{
                    0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0f,
                    0f, 0f, 0f, 0.3f});
        }

        colorMatrixScript.setColorMatrix(matrix);
        colorMatrixScript.forEach(allocationIn, allocationOut);

        blurScript.setRadius(getBlurRadius());

        blurScript.setInput(allocationOut);
        blurScript.forEach(allocationIn);

        allocationIn.copyTo(bitmap);

        allocationIn.destroy();
        allocationOut.destroy();

        return bitmap;
    }

    private void generateShadow() {
        if (this.getDrawable() != null) {
            this.shadowBitmap = this.getShadowBitmap(this.getBitmapFromDrawable());
        }
    }

    private Bitmap getBitmapFromDrawable() {
        Drawable drawable = this.getDrawable();
        float blurRadius = this.getBlurRadius();
        int width = this.getWidth() + 2 * (int) blurRadius;
        int height = this.getHeight() + 2 * (int) blurRadius;
        Bitmap bitmap = width > 0 && height > 0 ? Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888) : Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Matrix imageMatrix = this.getImageMatrix();
        canvas.translate((float) this.getPaddingLeft() + blurRadius, (float) this.getPaddingTop() + blurRadius);
        if (imageMatrix != null) {
            canvas.concat(imageMatrix);
        }

        drawable.draw(canvas);
        return bitmap;
    }
}
