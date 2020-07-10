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
import android.os.Parcelable;

import com.mrikso.codeeditor.util.DocumentProvider;
import com.mrikso.codeeditor.util.LexTask;

public interface IEditAreaView extends IEditActionSupport, IdeEditor {

   CharSequence getSelectedText();

    DocumentProvider getText();

    HighlightEditorView getEditorView();

    boolean isChanged();

    void setText(CharSequence spannable);

    void setEnabled(boolean enable);

    boolean post(Runnable runnable);

    void setLexTask(LexTask lexer);

    void setSelection(int start, int end);

    void setReadOnly(boolean readOnly);

    String getLang();

    void setOnEditStateChangedListener(HighlightEditorView.OnEditStateChangedListener listener);


    void onRestoreInstanceState(Parcelable editorState);

    //void addTextChangedListener(TextWatcher textWatcher);

   // void removeTextChangedListener(TextWatcher textWatcher);

    boolean hasSelection();

    int getSelectionStart();

    int getSelectionEnd();

    void gotoLine(int line);

    void gotoTop();

    void gotoEnd();

    boolean requestFocus();

    int length();

    void setFreezesText(boolean b);

    Parcelable onSaveInstanceState();

    Context getContext();

    void clearFocus();

}
