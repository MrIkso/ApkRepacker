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
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.utils.ViewUtils;
import com.mrikso.codeeditor.util.Document;
import com.mrikso.codeeditor.util.DocumentProvider;
import com.mrikso.codeeditor.view.FreeScrollingTextField;
import com.mrikso.codeeditor.view.YoyoNavigationMethod;

public abstract class HighlightEditorView extends FreeScrollingTextField
        implements IEditAreaView, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final char CURSOR = '\u2622';
    private static final String TAG = "EditAreaView2";
    protected EditorPreferences mEditorPreferences;
    private Document _inputtingDoc;
    private boolean _isWordWrap;
    private Context mContext;
    private boolean editable = true;
    private String _lastSelectFile;
    private int _index;
    private OnEditStateChangedListener listener;
    private boolean mIsAutoIndent = true;
    private boolean mIsAutoPair;
    HighlightEditorView view;

    public HighlightEditorView(Context context) {
        super(context);
        init(context);
    }

    public HighlightEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public HighlightEditorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        view = this;
        mContext = context;
        if (isInEditMode()) {
            return;
        }
        //avoid crash with large data
        setSaveEnabled(false);

        //setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        //  setInputType(InputType.TYPE_CLASS_TEXT
        //     | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        //       | InputType.TYPE_TEXT_FLAG_MULTI_LINE
        //    | InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);


        mEditorPreferences = EditorPreferences.getInstance(getContext());
        mEditorPreferences.registerOnSharedPreferenceChangeListener(this);

        //  TextPaint gutterForegroundPaint = new TextPaint(getPaint());
        // gutterForegroundPaint.setTextSize(getTextSize() * LayoutContext.LINE_NUMBER_FACTOR);


      //  setVerticalScrollBarEnabled(true);
        setTypeface(Typeface.MONOSPACE);
        //  DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        //设置字体大小
        //float size = TypedValue.applyDimension(2, BASE_TEXT_SIZE_PIXELS, dm);
        // setTextSize((int) ViewUtils.dpToPx(BASE_TEXT_SIZE_PIXELS,mContext));
        //setShowLineNumbers(true);
        //setAutoCompete(true);
        setHighlightCurrentRow(true);
        //setWordWrap(true);
        setAutoComplete(false);
        // setAutoIndent(true);
        setUseGboard(true);
        setAutoIndentWidth(2);
        // setLexTask(new SmaliLexTask());
        setNavigationMethod(new YoyoNavigationMethod(this));
        // int textColor = Color.BLACK;// 默认文字颜色
        //  int selectionText = Color.argb(255, 0, 120, 215);//选择文字颜色
        //setTextColor(textColor);
        // setTextHighlightColor(selectionText);
        //  setTextSize(getTextSize());


        onSharedPreferenceChanged(null, EditorPreferences.KEY_FONT_SIZE);
        onSharedPreferenceChanged(null, EditorPreferences.KEY_SHOW_LINE_NUMBER);
        onSharedPreferenceChanged(null, EditorPreferences.KEY_WORD_WRAP);
        onSharedPreferenceChanged(null, EditorPreferences.KEY_SHOW_WHITESPACE);
        onSharedPreferenceChanged(null, EditorPreferences.KEY_TAB_SIZE);
        onSharedPreferenceChanged(null, EditorPreferences.KEY_AUTO_INDENT);
        onSharedPreferenceChanged(null, EditorPreferences.KEY_AUTO_PAIR);
        onSharedPreferenceChanged(null, EditorPreferences.KEY_AUTO_CAPITALIZE);

        //setDefaultFilters();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        // TODO: Implement this method
        super.onLayout(changed, left, top, right, bottom);
        if (_index != 0 && right > 0) {
            moveCaret(_index);
            _index = 0;
        }
    }

    /*
    private void setDefaultFilters() {
        //indent filters
        final InputFilter indentFilter = (source, start, end, dest, dstart, dend) -> {
            if (mIsAutoIndent) {
                if (!(source.length() == 1 && source.charAt(0) == '\n')) {
                    return null;
                }
                int startIndex = dstart - 1;
                if (startIndex < 0 || startIndex >= dest.length())
                    return null;

                char ch;
                for (; startIndex >= 0; startIndex--) {
                    ch = dest.charAt(startIndex);
                    if (ch != '\r')
                        break;
                }

                StringBuilder indent = new StringBuilder();
                for (int i = startIndex; i >= 0; i--) {
                    ch = dest.charAt(i);
                    if (ch == '\n' || ch == '\r') {
                        break;
                    } else if (ch == ' ' || ch == '\t') {
                        indent.append(ch);
                    } else {
                        indent.setLength(0);
                    }
                }
                indent.reverse();

                //bad code
                //common support java,c and c++
                // TODO: 08-Jun-18 dynamic change
                if (dend < dest.length() && dest.charAt(dend) == '}'
                        && dstart - 1 >= 0 && dest.charAt(dstart - 1) == '{') {
                    int mstart = dstart - 2;
                    while (mstart >= 0 && dest.charAt(mstart) != '\n') {
                        mstart--;
                    }
                    String closeIndent = "";
                    if (mstart >= 0) {
                        mstart++;
                        int zstart = mstart;
                        while (zstart < dest.length() && dest.charAt(zstart) == ' ') {
                            zstart++;
                        }
                        closeIndent = dest.toString().substring(mstart, zstart);
                    }
                    return source +
                            (indent.toString() + "  ") +
                            CURSOR + "\n" + closeIndent;
                }

                return "\n" + indent.toString();
            }
            return null;
        };

        //end line filter, only support \n
        InputFilter newLineFilter = (source, start, end, dest, dstart, dend) -> {
            final String s = source.toString();
            if (s.contains("\r")) {
                return s.replace("\r", "");
            }
            return null;
        };

        //bracket filter, auto add close bracket if auto pair is enable
        final InputFilter bracketFilter = (source, start, end, dest, dstart, dend) -> {
            if (mIsAutoPair) {
                if (end - start == 1 && start < source.length() && dstart < dest.length()) {
                    char c = source.charAt(start);
                    if (c == '(' || c == '{' || c == '[' || c == '"' || c == '\'') {
                        return addBracket(source, start);
                    }
                }
            }
            return null;
        };

        //setFilters(new InputFilter[]{indentFilter, newLineFilter, bracketFilter});

        //auto add bracket
        addTextChangedListener(new TextWatcher() {
            private int start;
            private int count;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                this.start = start;
                this.count = count;
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > start && count > 1) {
                    for (int i = start; i < start + count; i++) {
                        if (editable.charAt(i) == CURSOR) {
                            editable.delete(i, i + 1);
                            setSelection(start);
                            break;
                        }
                    }
                }
            }
        });
    }

     */
    @Nullable
    private CharSequence addBracket(CharSequence source, int start) {
        switch (source.charAt(start)) {
            case '"':
                return "\"" + CURSOR + "\"";
            case '\'':
                return "'" + CURSOR + "'";
            case '(':
                return "(" + CURSOR + ")";
            case '{':
                return "{" + CURSOR + "}";
            case '[':
                return "[" + CURSOR + "]";
        }
        return null;
    }

    @Override
    public void setWordWrap(boolean enable) {
        // TODO: Implement this method
        _isWordWrap = enable;
        super.setWordWrap(enable);
    }

    @Override
    public CharSequence getSelectedText() {
        // TODO: Implement this method
        return hDoc.subSequence(getSelectionStart(), getSelectionEnd() - getSelectionStart()).toString();
    }

    @Override
    public void gotoLine(int line) {
        if (line > hDoc.getRowCount()) {
            line = hDoc.getRowCount();

        }
        int i = getText().getLineOffset(line - 1);
        setSelection(i);
    }

    public void setSelection(int index) {
        selectText(false);
        if (!hasLayout())
            moveCaret(index);
        else
            _index = index;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case EditorPreferences.KEY_FONT_SIZE:
                setTextSize((int) ViewUtils.dpToPx(mEditorPreferences.getFontSize(), mContext));
                break;
            case EditorPreferences.KEY_SHOW_LINE_NUMBER:
                setShowLineNumbers(mEditorPreferences.isShowLineNumber());
                break;
            case EditorPreferences.KEY_WORD_WRAP:
                setWordWrap(mEditorPreferences.isWordWrap());
                break;
            case EditorPreferences.KEY_SHOW_WHITESPACE:
                setNonPrintingCharVisibility(mEditorPreferences.isShowWhiteSpace());
                break;
            case EditorPreferences.KEY_TAB_SIZE:
                //updateTabChar();
                break;
            case EditorPreferences.KEY_AUTO_INDENT:
                setAutoIndent(mEditorPreferences.isAutoIndent());
                break;
            case EditorPreferences.KEY_AUTO_PAIR:
                mIsAutoPair = mEditorPreferences.isAutoPair();
                break;
            case EditorPreferences.KEY_AUTO_CAPITALIZE:
                if (!mEditorPreferences.isAutoCapitalize()) {
                    //   setInputType(getInputType() & ~EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
                } else {
                    //    setInputType(getInputType() | EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);
                }
                break;
        }
    }

    @Override
    public DocumentProvider getText() {
        return createDocumentProvider();
    }

    @Override
    public void setText(CharSequence c) {
        Document doc = new Document(this);
        doc.setWordWrap(_isWordWrap);
        doc.setText(c);
        setDocumentProvider(new DocumentProvider(doc));
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        super.showIME(editable);
    }

    @Override
    public boolean isChanged() {
        return isEdited();
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        setEditable(!readOnly);
    }

    @Override
    protected void showIME(boolean show) {
        if (!editable)
            show = false;
        super.showIME(show);
        getParent().requestLayout();
    }

    @Override
    public void onRestoreInstanceState(Parcelable editorState) {
        super.onRestoreInstanceState(editorState);
    }

    @Override
    public void setEdited(boolean set)
    {
        super.setEdited(set);
        if (listener != null)
            listener.onEditStateChanged();
    }

    @Override
    public void setOnEditStateChangedListener(OnEditStateChangedListener listener)
    {
        this.listener = listener;
    }

    public interface OnEditStateChangedListener
    {
        void onEditStateChanged();
    }

    @Override
    public boolean hasSelection() {
        return mFieldController.isSelectText();
    }

    @Override
    public void gotoTop() {
        gotoLine(1);
    }

    @Override
    public void gotoEnd() {
        gotoLine(getLineNum());
    }

    @Override
    public int length() {
        return hDoc.length();
    }

    @Override
    public void setFreezesText(boolean b) {

    }

    @Override
    public Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
        // return null;
    }

    @Override
    public void undo() {
        DocumentProvider doc = createDocumentProvider();
        int newPosition = doc.undo();

        if (newPosition >= 0) {
            //TODO editor.setEdited(false);
            // if reached original condition of file
            setEdited(true);
            respan();
            selectText(false);
            moveCaret(newPosition);
            invalidate();
        }

    }

    @Override
    public void redo() {
        DocumentProvider doc = createDocumentProvider();
        int newPosition = doc.redo();

        if (newPosition >= 0) {
            setEdited(true);

            respan();
            selectText(false);
            moveCaret(newPosition);
            invalidate();
        }
    }

    @Override
    public boolean doCut() {
        return false;
    }

    @Override
    public boolean doCopy() {
        return false;
    }

    @Override
    public boolean doPaste() {
        return false;
    }


    @Override
    public boolean doCanUndo() {
        DocumentProvider doc = createDocumentProvider();
        return doc.canUndo();
    }

    @Override
    public boolean doCanRedo() {
        DocumentProvider doc = createDocumentProvider();
        return doc.canRedo();
    }

    @Override
    public void insert(@NonNull CharSequence text) {
        selectText(false);
        // moveCaret(_index);
        paste(text.toString());
    }

    @Override
    public HighlightEditorView getEditorView(){
        return view;
    }

    @Override
    public String getLang(){
        return getLexTask().getLanguageType();
    }
}
