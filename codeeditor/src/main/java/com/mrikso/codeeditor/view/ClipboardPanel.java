package com.mrikso.codeeditor.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.RequiresApi;

import com.mrikso.codeeditor.R;

public class ClipboardPanel {
    protected FreeScrollingTextField _textField;
    private Context _context;

    private ActionMode _clipboardActionMode;
    private ActionMode.Callback2 _clipboardActionModeCallback2;
    private Rect caret;

    public ClipboardPanel(FreeScrollingTextField textField) {
        _textField = textField;
        _context = textField.getContext();
    }

    public Context getContext() {
        return _context;
    }

    public void show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            initData();
            startClipboardActionNew();
        } else {
            startClipboardAction();
        }

    }

    public void hide() {
        stopClipboardAction();
    }

    public void startClipboardAction() {
        // TODO: Implement this method
        if (_clipboardActionMode == null)
            _textField.startActionMode(new ActionMode.Callback() {

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    // TODO: Implement this method
                    _clipboardActionMode = mode;
                    mode.setTitle(android.R.string.selectTextMode);
                    TypedArray array = _context.getTheme().obtainStyledAttributes(new int[]{
                            android.R.attr.actionModeSelectAllDrawable,
                            android.R.attr.actionModeCutDrawable,
                            android.R.attr.actionModeCopyDrawable,
                            android.R.attr.actionModePasteDrawable,
                    });
                    menu.add(0, 0, 0, _context.getString(android.R.string.selectAll))
                            .setShowAsActionFlags(2)
                            .setAlphabeticShortcut('a')
                            .setIcon(array.getDrawable(0));

                    menu.add(0, 1, 0, _context.getString(android.R.string.cut))
                            .setShowAsActionFlags(2)
                            .setAlphabeticShortcut('x')
                            .setIcon(array.getDrawable(1));

                    menu.add(0, 2, 0, _context.getString(android.R.string.copy))
                            .setShowAsActionFlags(2)
                            .setAlphabeticShortcut('c')
                            .setIcon(array.getDrawable(2));

                    menu.add(0, 3, 0, _context.getString(android.R.string.paste))
                            .setShowAsActionFlags(2)
                            .setAlphabeticShortcut('v')
                            .setIcon(array.getDrawable(3));
                    array.recycle();
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    // TODO: Implement this method
                    return false;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    // TODO: Implement this method
                    switch (item.getItemId()) {
                        case 0:
                            _textField.selectAll();
                            break;
                        case 1:
                            _textField.cut();
                            mode.finish();
                            break;
                        case 2:
                            _textField.copy();
                            mode.finish();
                            break;
                        case 3:
                            _textField.paste();
                            mode.finish();
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode p1) {
                    // TODO: Implement this method
                    _textField.selectText(false);
                    _clipboardActionMode = null;
                }
            });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void startClipboardActionNew() {
        // TODO: Implement this method
        if (_clipboardActionMode == null)
            _textField.startActionMode(_clipboardActionModeCallback2, ActionMode.TYPE_FLOATING);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initData(){
        _clipboardActionModeCallback2 = new ActionMode.Callback2() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO: Implement this method
                _clipboardActionMode = mode;
                menu.add(0, 0, 0, _context.getString(android.R.string.selectAll))
                        .setShowAsActionFlags(2)
                        .setAlphabeticShortcut('a');

                menu.add(0, 1, 0, _context.getString(android.R.string.cut))
                        .setShowAsActionFlags(2)
                        .setAlphabeticShortcut('x');
                menu.add(0, 2, 0, _context.getString(android.R.string.copy))
                        .setShowAsActionFlags(2)
                        .setAlphabeticShortcut('c');
                menu.add(0, 3, 0, _context.getString(android.R.string.paste))
                        .setShowAsActionFlags(2)
                        .setAlphabeticShortcut('v');
                menu.add(0, 4, 0, "Delete")
                        .setShowAsActionFlags(2)
                        .setAlphabeticShortcut('d');
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO: Implement this method
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO: Implement this method
                switch (item.getItemId()) {
                    case 0:
                        _textField.selectAll();
                        break;
                    case 1:
                        _textField.cut();
                        mode.finish();
                        break;
                    case 2:
                        _textField.copy();
                        mode.finish();
                        break;
                    case 3:
                        _textField.paste();
                        mode.finish();
                    case 4:
                        _textField.delete();
                        mode.finish();
                }
                return false;


            }

            @Override
            public void onDestroyActionMode(ActionMode p1) {
                // TODO: Implement this method
                _textField.selectText(false);
                _clipboardActionMode = null;
                caret = null;
            }

            @Override
            public void onGetContentRect(ActionMode mode, View view, Rect outRect){
                caret = _textField.getBoundingBox(_textField.getCaretPosition());
                int x = outRect.left + _textField.getPaddingLeft();
                int y = outRect.bottom + _textField.getPaddingTop();
                outRect = caret;
                super.onGetContentRect(mode, view, outRect);
            }
        };

    }

    public void stopClipboardAction() {
        if (_clipboardActionMode != null) {
            //_clipboardActionModeCallback2.onDestroyActionMode(_clipboardActionMode);
            _clipboardActionMode.finish();
            _clipboardActionMode = null;

        }
    }

}
