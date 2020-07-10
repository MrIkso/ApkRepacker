package com.mrikso.codeeditor.view.autocomplete;

import android.graphics.Bitmap;

public class ListItem {
    private Bitmap bitmap;
    private String text;

    public ListItem(Bitmap bitmap, String text) {
        this.bitmap = bitmap;
        this.text = text;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}