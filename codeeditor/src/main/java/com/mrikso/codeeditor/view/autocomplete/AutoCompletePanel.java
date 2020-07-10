package com.mrikso.codeeditor.view.autocomplete;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Filter;
import android.widget.ListPopupWindow;
import android.widget.TextView;

import com.mrikso.codeeditor.R;
import com.mrikso.codeeditor.lang.Language;
import com.mrikso.codeeditor.lang.LanguageNonProg;
import com.mrikso.codeeditor.view.FreeScrollingTextField;

public class AutoCompletePanel {

    public static Language _globalLanguage = LanguageNonProg.getInstance();
    public CharSequence _constraint;
    private FreeScrollingTextField _textField;
    private Context _context;
    private ListPopupWindow _autoCompletePanel;
    private AutoPanelAdapter _adapter;
    private Filter _filter;
    private int _verticalOffset;
    private int _height;
    private int _horizontal;
    private int _backgroundColor;
    private GradientDrawable gd;
    public int _textColor;
    private boolean isShow = false;

    public AutoCompletePanel(FreeScrollingTextField textField) {
        _textField = textField;
        _context = textField.getContext();
        initAutoCompletePanel();

    }

    synchronized public static Language getLanguage() {
        return _globalLanguage;
    }

    synchronized public static void setLanguage(Language lang) {
        _globalLanguage = lang;
    }

    public void setTextColor(int color) {
        _textColor = color;
        gd.setStroke(1, color);
        _autoCompletePanel.setBackgroundDrawable(gd);
    }

    public void setBackgroundColor(int color) {
        _backgroundColor = color;
        gd.setColor(color);
        _autoCompletePanel.setBackgroundDrawable(gd);
    }

    public void setBackground(Drawable color) {
        _autoCompletePanel.setBackgroundDrawable(color);
    }

    @SuppressWarnings("ResourceType")
    private void initAutoCompletePanel() {
        _autoCompletePanel = new ListPopupWindow(_context);
        _autoCompletePanel.setAnchorView(_textField);
        _adapter = new AutoPanelAdapter(_context, this, _textField);
        _autoCompletePanel.setAdapter(_adapter);
        _filter = _adapter.getFilter();
        //_autoCompletePanel.setContentWidth(ListPopupWindow.WRAP_CONTENT);
        setHeight(300);

        TypedArray array = _context.getTheme().obtainStyledAttributes(new int[]{
                android.R.attr.colorBackground,
                android.R.attr.textColorPrimary,
        });
        int backgroundColor = array.getColor(0, 0xFF00FF);
        int textColor = array.getColor(1, 0xFF00FF);
        array.recycle();
        gd = new GradientDrawable();
        gd.setColor(backgroundColor);
        gd.setCornerRadius(4);
        gd.setStroke(1, textColor);
        setTextColor(textColor);
        _autoCompletePanel.setBackgroundDrawable(gd);
        _autoCompletePanel.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
                select(p3);
            }
        });
    }

    public void selectFirst() {
        select(0);
    }

    public void select(int pos) {
        View view = _adapter.getView(pos, null, null);
        TextView textView = view.findViewById(R.id.auto_panel_text);
        String text = textView.getText().toString();
        String commitText = null;
        boolean isFunc = text.contains("(");
        if (isFunc) {
            commitText = text.substring(0, text.indexOf('(')) + "()";
        } else {
            commitText = text;
        }
        _textField.replaceText(_textField.getCaretPosition() - _constraint.length(), _constraint.length(), commitText);
        _adapter.abort();
        dismiss();
        if (isFunc) {
            _textField.moveCaretLeft();
        }

    }

    public void setWidth(int width) {
        _autoCompletePanel.setWidth(width);
    }

    public void setHeight(int height) {
        if (_height != height) {
            _height = height;
            _autoCompletePanel.setHeight(height);
        }
    }

    public void setHorizontalOffset(int horizontal) {
        horizontal = Math.min(horizontal, _textField.getWidth() / 2);
        if (_horizontal != horizontal) {
            _horizontal = horizontal;
            _autoCompletePanel.setHorizontalOffset(horizontal);
        }
    }

    void setVerticalOffset(int verticalOffset) {
        //verticalOffset=Math.min(verticalOffset,_textField.getWidth()/2);
        int max = 0 - _autoCompletePanel.getHeight();
        if (verticalOffset > max) {
            _textField.scrollBy(0, verticalOffset - max);
            verticalOffset = max;
        }
        if (_verticalOffset != verticalOffset) {
            _verticalOffset = verticalOffset;
            _autoCompletePanel.setVerticalOffset(verticalOffset);
        }
    }

    public void update(CharSequence constraint) {
        _adapter.restart();
        _filter.filter(constraint);
    }

    public void show() {
        if (!_autoCompletePanel.isShowing())
            _autoCompletePanel.show();
        _autoCompletePanel.getListView().setFadingEdgeLength(0);
        isShow = true;
    }

    public void dismiss() {
        if (_autoCompletePanel.isShowing()) {
            isShow = false;
            _autoCompletePanel.dismiss();
        }

    }

    public boolean isShow() {
        return _autoCompletePanel.isShowing();
    }

}
