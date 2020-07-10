/*
 * Copyright (C) 2018 Tran Le Duy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.mrikso.apkrepacker.ide.editor.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrikso.apkrepacker.ide.editor.theme.model.EditorTheme;
import com.mrikso.codeeditor.view.ColorScheme;

public class CodeEditor extends HighlightEditorView {
    /**
     * Editor color schemes, include text color, text background and more color attrs
     */
    @Nullable
    private EditorTheme mEditorTheme;

    public CodeEditor(Context context) {
        super(context);
        init(context);
    }

    public CodeEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CodeEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setTheme(mEditorPreferences.getEditorTheme());
    }

    @Override
    public void setTheme(@NonNull EditorTheme editorTheme) {
        mEditorTheme = editorTheme;

        setBackgroundColor(getColor(editorTheme.getThemeModel().getViewBackgroundColor()));
        setTextColor(getColor(editorTheme.getThemeModel().getViewDefault()));
        setTextHighlightColor(getColor(editorTheme.getThemeModel().getViewSelectionColor()));
        setGutterBackgroundColor(getColor(editorTheme.getThemeModel().getViewGutterBackgroundColor()));
        setLineNumberTextColor(getColor(editorTheme.getThemeModel().getViewGutterForegroundColor()));
       // setLineHighlightColor(editorTheme.getLineHighlightColor());
        setWhiteSpaceColor(getColor(editorTheme.getThemeModel().getViewWhitespaceColor()));

        //syntax
        setKeywordColor(getColor(editorTheme.getThemeModel().getViewKeyword()));
        setBaseWordColor(getColor(editorTheme.getThemeModel().getViewName()));
        setCommentColor(getColor(editorTheme.getThemeModel().getViewComment()));
        setLiteralColor(getColor(editorTheme.getThemeModel().getViewLiteral()));
        setOperatorColor(getColor(editorTheme.getThemeModel().getViewOperator()));
        setTypeColor(getColor(editorTheme.getThemeModel().getViewOperator()));
        setSeparatorColor(getColor(editorTheme.getThemeModel().getViewSeparator()));
        setPackageColor(getColor(editorTheme.getThemeModel().getViewPackage()));
        setErrorColor(getColor(editorTheme.getThemeModel().getViewError()));

        postInvalidate();
    }

    @Override
    public EditorTheme getEditorTheme() {
        return mEditorTheme;
    }

    protected int getColor(String attr) {
        int color = Color.parseColor(attr);
        if (Color.alpha(color) == 0 && color != Color.TRANSPARENT) {
            return Color.rgb(Color.red(color), Color.green(color), Color.blue(color));
        }
        return color;
    }
    public void setGutterBackgroundColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.GUTTER_FOREGROUND, color);
    }

    public void setKeywordColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.KEYWORD, color);
    }

    public void setBaseWordColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.NAME, color);
    }

    public void setLiteralColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.LITERAL, color);
    }

    public void setOperatorColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.OPERATOR, color);
    }

    public void setSeparatorColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.SEPARATOR, color);
    }

    public void setTypeColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.TYPE, color);
    }

    public void setErrorColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.ERROR, color);
    }

    public void setPackageColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.PACKAGE, color);
    }

    public void setCommentColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.COMMENT, color);
    }

    public void setBackgroundColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.BACKGROUND, color);
    }

    public void setTextColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.TEXT, color);
    }

    public void setLineNumberTextColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.GUTTER_LINENUMBER, color);
    }

    public void setLineHighlightColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.LINE_HIGHLIGHT, color);
    }

    public void setWhiteSpaceColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.NON_PRINTING_GLYPH, color);
    }

    public void setTextHighlightColor(int color) {
        getColorScheme().setColor(ColorScheme.Colorable.SELECTION_BACKGROUND, color);
    }
    /*
    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return super.requestFocus(direction, previouslyFocusedRect);
    }

     */
}
