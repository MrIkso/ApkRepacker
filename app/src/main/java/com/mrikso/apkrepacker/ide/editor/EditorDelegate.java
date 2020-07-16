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

package com.mrikso.apkrepacker.ide.editor;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.ide.editor.lexer.CssLexTask;
import com.mrikso.apkrepacker.utils.common.ShareUtil;
import com.mrikso.apkrepacker.activity.IdeActivity;

import com.mrikso.apkrepacker.ide.editor.lexer.CppLexTask;
import com.mrikso.apkrepacker.ide.editor.lexer.HtmlLexTask;
import com.mrikso.apkrepacker.ide.editor.lexer.JavaLexTask;
import com.mrikso.apkrepacker.ide.editor.lexer.JsonLexTask;
import com.mrikso.apkrepacker.ide.editor.lexer.LexerUtil;
import com.mrikso.apkrepacker.ide.editor.lexer.SmaliLexTask;
import com.mrikso.apkrepacker.ide.editor.lexer.XmlLexTask;
import com.mrikso.apkrepacker.ide.editor.text.InputMethodManagerCompat;
import com.mrikso.apkrepacker.ide.editor.view.HighlightEditorView;
import com.mrikso.apkrepacker.ide.editor.view.IEditAreaView;
import com.mrikso.apkrepacker.ide.file.SaveListener;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.EditorPreferences;
import com.jecelyin.editor.v2.common.Command;
import com.jecelyin.editor.v2.dialog.DocumentInfoDialog;
import com.jecelyin.editor.v2.widget.menu.MenuDef;
import com.mrikso.codeeditor.util.NonProgLexTask;

import java.io.File;
import java.util.Locale;

public class EditorDelegate implements  IEditorDelegate, HighlightEditorView.OnEditStateChangedListener {
    public final static String KEY_CLUSTER = "is_cluster";
    private static final String TAG = "EditorDelegate";
    private final Handler mHandler = new Handler();
    private Context mContext;
    private Document mDocument;
    @NonNull
    private SavedState savedState;
    private int mOrientation;
    private boolean loaded = true;
    @Nullable
    private IEditAreaView mEditText;


    EditorDelegate(@NonNull SavedState ss) {
        savedState = ss;
    }

    EditorDelegate(@NonNull File file, int offset, String encoding) {
        savedState = new SavedState();
        savedState.encoding = encoding;
        savedState.cursorOffset = offset;
        setCurrentFileToEdit(file);
    }

    private void setCurrentFileToEdit(File file) {
        savedState.file = file;
        savedState.title = savedState.file.getName();
    }

    void onLoadStart() {
        loaded = false;
        assert mEditText != null;
        mEditText.setEnabled(false);
    }

    void onLoadFinish() {
        assert mEditText != null;
        mEditText.setEnabled(true);
        mEditText.setOnEditStateChangedListener(this);
        mEditText.post(() -> {
            mEditText.setLexTask(LexerUtil.createLexer(mDocument.getFile().getName(), mDocument.getFile()));
            if (savedState.cursorOffset < mEditText.getText().length() && savedState.cursorOffset != -1) {
                mEditText.gotoLine(savedState.cursorOffset);
            }
        });

        onDocumentChanged();
        loaded = true;

        String fileName = mDocument.getFile().getPath().replaceAll("[^A-Za-z0-9_]", "_");
        SharedPreferences historyData = mContext.getSharedPreferences(
                fileName, Context.MODE_PRIVATE);
        //mEditText.restoreEditHistory(historyData);
    }

    public Context getContext() {
        return mContext;
    }

    private IdeActivity getActivity() {
        return (IdeActivity) mContext;
    }

    public String getTitle() {
        return savedState.title;
    }

    public String getPath() {
        return mDocument == null ? savedState.file.getPath() : mDocument.getPath();
    }

    public String getEncoding() {
        return mDocument == null ? null : mDocument.getEncoding();
    }

    public String getText() {
        return mEditText.getText().toString();
    }

    public Editable getEditableText() {
        return null;// mEditText.getText().;
    }

    public IEditAreaView getEditText() {
        return mEditText;
    }

    public void onCreate(IEditAreaView editorView) {
        mContext = editorView.getContext();
        mEditText = editorView;

        mOrientation = mContext.getResources().getConfiguration().orientation;

        mDocument = new Document(mContext, this, savedState.file);
        mEditText.setReadOnly(EditorPreferences.getInstance(mContext).isReadOnly());
        //mEditText.setCustomSelectionActionModeCallback(new EditorSelectionActionModeCallback());

        if (savedState.editorState != null)
            try {
                mDocument.onRestoreInstanceState(savedState);
                mEditText.onRestoreInstanceState(savedState.editorState);
            } catch (Exception e) {
                //wrong state
                e.printStackTrace();
            }
        else {
            mDocument.loadFile(savedState.file, savedState.encoding);
        }

        onDocumentChanged();
    }

    public void onDestroy() {
/*
        String fileName = mDocument.getFile().getPath().replaceAll("[^A-Za-z0-9_]", "_");
        SharedPreferences historyData = mContext.getSharedPreferences(
                fileName, Context.MODE_PRIVATE);
        mEditText.saveHistory(historyData);

 */
        if (isChanged() && EditorPreferences.getInstance(getContext()).isAutoSave()) {
            saveInBackground();
        }

    }

    public boolean isChanged() {
        return mEditText != null && mEditText.isChanged();
    }

    @NonNull
    public CharSequence getToolbarText() {
        try {
            String encode = mDocument == null ? "UTF-8" : mDocument.getEncoding();
            String fileMode = mDocument == null || mDocument.getModeName() == null
                    ? "" : mDocument.getModeName();
            String title = getTitle();
            String changed = isChanged() ? "*" : "";
            String cursor = "";
            if (mEditText != null && getCursorOffset() >= 0) {
                int cursorOffset = getCursorOffset();
             //   int line = mDocument.getBuffer().getLineManager().getLineOfOffset(cursorOffset);
             //   cursor += line + ":" + cursorOffset;
            }
            return String.format(Locale.US, "%s%s  \t|\t  %s \t %s \t %s",
                    changed, title, encode, fileMode, cursor);
        } catch (Exception e) {
            return "";
        }
    }
/*
    private void startSaveFileSelectorActivity() {
        if (mDocument != null) {
           // getActivity().startPickPathActivity(mDocument.getPath(), mDocument.getEncoding());
        }
    }


 */
    /**
     * Write out content of editor to file in background thread
     *
     * @param file     - File to write
     * @param encoding - file encoding
     */
    public void saveInBackground(File file, String encoding) {
        if (mDocument != null) {
            mDocument.saveInBackground(file, encoding == null ? mDocument.getEncoding() : encoding,
                    new SaveListener() {
                        @Override
                        public void onSavedSuccess() {
                            onDocumentChanged();
                        }

                        @Override
                        public void onSaveFailed(Exception e) {
                           UIUtils.alert(mContext, e.getMessage());
                        }
                    });
        }
    }

    /**
     * Write current content of editor to file
     */
    @Override
    public void saveCurrentFile() throws Exception {
        if (mDocument.isChanged()) {
            mDocument.writeToFile(mDocument.getFile(), mDocument.getEncoding());
        }
    }

    /**
     * Write out content of editor to file in background thread
     */
    @Override
    public void saveInBackground() {
        if (mDocument.isChanged()) {
            saveInBackground(mDocument.getFile(), mDocument.getEncoding());
        } else {
            if (DLog.DEBUG) DLog.d(TAG, "saveInBackground: document not changed, no need to save");
        }
    }

    public int getCursorOffset() {
        if (mEditText == null) {
            return -1;
        }
        return mEditText.getSelectionEnd();
    }

    @Override
    public void doCommand(Command command) {
        if (mEditText == null)
            return;
        boolean readonly = EditorPreferences.getInstance(mContext).isReadOnly();
        switch (command.what) {
            case HIDE_SOFT_INPUT:
                InputMethodManagerCompat.hideSoftInput((View) mEditText);
                break;
            case SHOW_SOFT_INPUT:
                InputMethodManagerCompat.showSoftInput((View) mEditText);
                break;
            case UNDO:
                if (!readonly) {
                    mEditText.undo();
                }
                break;
            case REDO:
                if (!readonly) {
                    mEditText.redo();
                }
                break;
            case CUT:
                if (!readonly) {
                    mEditText.doCut();
                    return;
                }
            case COPY:
                mEditText.doCopy();
                return;
            case PASTE:
                if (!readonly) {
                    mEditText.doPaste();
                    return;
                }
            case SELECT_ALL:
                mEditText.selectAll();
                return;
            case DUPLICATION:
                if (!readonly)
                  //  mEditText.duplicateSelection();
                break;
            case GOTO_INDEX:
                int col = command.args.getInt("col", -1);
                int line = command.args.getInt("line", -1);
                mEditText.gotoLine(line);
                break;
            case GOTO_TOP:
                mEditText.gotoTop();
                break;
            case GOTO_END:
                mEditText.gotoEnd();
                break;
            case DOC_INFO:
                DocumentInfoDialog documentInfoDialog = new DocumentInfoDialog(mContext);
                documentInfoDialog.setDocument(mDocument);
                documentInfoDialog.setEditAreaView(mEditText);
                documentInfoDialog.setPath(mDocument.getPath());
                documentInfoDialog.show();
                break;
            case READONLY_MODE:
                EditorPreferences editorPreferences = EditorPreferences.getInstance(mContext);
                boolean readOnly = editorPreferences.isReadOnly();
                mEditText.setReadOnly(readOnly);
                break;
            case SAVE:
                if (!readonly) {
                    saveInBackground();
                }
                break;
            case SAVE_AS:
                //startSaveFileSelectorActivity();
                break;
            case FIND:
                SearchPanel panel = SearchPanel.getInstance(mContext);
                panel.setActivity(getActivity());
                panel.initSearchPanel(this);
                break;
            case HIGHLIGHT:
                String scope = (String) command.object;
                setMode(scope);
                break;
            case INSERT_TEXT:
                if (!readonly) {
                    mEditText.insert((CharSequence) command.object);
                }
                break;
            case RELOAD_WITH_ENCODING:
                reOpenWithEncoding((String) command.object);
                break;
            case REQUEST_FOCUS:
                mEditText.requestFocus();
                break;
            case SHARE_CODE:
                shareCurrentContent();
                break;
            case FORMAT_SOURCE:
                //formatSource();

                break;
            case REFRESH_THEME:
                if (mEditText != null) {
                    mEditText.setTheme(EditorPreferences.getInstance(mContext).getEditorTheme());
                }
                break;
        }
    }

    /**
     * Format current source
     */
    /*private void formatSource() {
        if (mCodeFormatProvider == null) {
            Toast.makeText(mContext, R.string.unsupported_format_source, Toast.LENGTH_SHORT).show();
            return;
        }
        CodeFormatter formatter = mCodeFormatProvider.getFormatterForFile(mDocument.getFile(), this);
        if (formatter == null) {
            Toast.makeText(mContext, R.string.unsupported_format_source, Toast.LENGTH_SHORT).show();
            return;
        }

        FormatSourceTask formatSourceTask = new FormatSourceTask(mContext, mEditText, formatter);
        formatSourceTask.execute();
    }
*/
    private void shareCurrentContent() {
        ShareUtil.shareText(mContext, mEditText.getText().toString());
    }

    private void reOpenWithEncoding(final String encoding) {
        final File file = mDocument.getFile();
        if (mDocument.isChanged()) {
            new AlertDialog.Builder(mContext)
                    .setTitle(R.string.document_changed)
                    .setMessage(R.string.give_up_document_changed_message)
                    .setPositiveButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                    .setNegativeButton(R.string.ok, (dialog, which) -> {
                        dialog.cancel();
                        mDocument.loadFile(file, encoding);
                    })
                    .create()
                    .show();
            return;
        }
        mDocument.loadFile(file, encoding);
    }

    /**
     * This method will be called when document changed file
     */
    @MainThread
    public void onDocumentChanged() {
        setCurrentFileToEdit(mDocument.getFile());
        noticeMenuChanged();
    }


    private void noticeMenuChanged() {
        //MainActivity mainActivity = (MainActivity) this.context;
        getActivity().setMenuStatus(R.id.action_save, isChanged() ? MenuDef.STATUS_NORMAL : MenuDef.STATUS_DISABLED);
        getActivity().setMenuStatus(R.id.action_undo, mEditText != null && mEditText.doCanUndo() ? MenuDef.STATUS_NORMAL : MenuDef.STATUS_DISABLED);
        getActivity().setMenuStatus(R.id.action_redo, mEditText != null && mEditText.doCanRedo() ? MenuDef.STATUS_NORMAL : MenuDef.STATUS_DISABLED);
        getActivity().getTabManager().onDocumentChanged();
    }

    void setMode(String name) {
        switch (name){
            case"C++":
                mEditText.setLexTask(new CppLexTask());
                break;
            case"Java":
                mEditText.setLexTask(new JavaLexTask());
            break;
            case"Smali":
                mEditText.setLexTask(new SmaliLexTask());
            break;
            case"Html":
                mEditText.setLexTask(new HtmlLexTask());
            break;
            case "Json":
                mEditText.setLexTask(new JsonLexTask());
            break;
            case "Xml":
                mEditText.setLexTask(new XmlLexTask());
            break;
            case"Css":
                mEditText.setLexTask(new CssLexTask());
            break;
            case "None":
                mEditText.setLexTask(NonProgLexTask.instance);
                break;
            default:
                mEditText.setLexTask(NonProgLexTask.instance);
                break;
        }

    }
    /*
    private void convertSelectedText(int id) {
        if (mEditText == null || !mEditText.hasSelection()) {
            return;
        }

        int start = mEditText.getSelectionStart();
        int end = mEditText.getSelectionEnd();

        String selectedText = getEditableText().subSequence(start, end).toString();

        if (id == R.id.action_convert_to_uppercase) {
            selectedText = selectedText.toUpperCase();

        } else if (id == R.id.action_convert_to_lowercase) {
            selectedText = selectedText.toLowerCase();

        }
        getEditableText().replace(start, end, selectedText);
    }
     */
    Parcelable onSaveInstanceState() {
        if (mDocument != null) {
            mDocument.onSaveInstanceState(savedState);
        }
        if (mEditText != null) {
            mEditText.setFreezesText(true);
        }

        if (loaded && mDocument != null) {
            if (EditorPreferences.getInstance(mContext).isAutoSave()) {
                int newOrientation = mContext.getResources().getConfiguration().orientation;
                if (mOrientation != newOrientation) {
                    DLog.d("current is screen orientation, discard auto save!");
                    mOrientation = newOrientation;
                } else {
                    try {
                        saveCurrentFile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return savedState;
    }

    @Override
    public Document getDocument() {
        return mDocument;
    }

    @Override
    public void onEditStateChanged() {
        onDocumentChanged();
    }


    public static class SavedState implements Parcelable {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source) {
                return new SavedState(source);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int cursorOffset;
        int lineNumber;
        File file;
        String title;
        String encoding;
        String modeName;
        Parcelable editorState;
        byte[] textMd5;
        int textLength;

        SavedState() {
        }

        SavedState(Parcel in) {
            this.cursorOffset = in.readInt();
            this.lineNumber = in.readInt();
            String file = in.readString();
            this.file = new File(file);
            this.title = in.readString();
            this.encoding = in.readString();
            this.modeName = in.readString();
            int hasState = in.readInt();
            if (hasState == 1) {
                this.editorState = in.readParcelable(Parcelable.class.getClassLoader());
            }
            this.textMd5 = in.createByteArray();
            this.textLength = in.readInt();
        }


        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(this.cursorOffset);
            dest.writeInt(this.lineNumber);
            dest.writeString(this.file.getPath());
            dest.writeString(this.title);
            dest.writeString(this.encoding);
            dest.writeString(this.modeName);
            dest.writeInt(this.editorState == null ? 0 : 1);
            if (this.editorState != null) {
                dest.writeParcelable(this.editorState, flags);
            }
            dest.writeByteArray(this.textMd5);
            dest.writeInt(textLength);
        }
    }

    /*
    private class EditorSelectionActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            final TypedArray arr = mContext.obtainStyledAttributes(
                    R.styleable.SelectionModeDrawables);

            boolean readOnly = Preferences.getInstance(mContext).isReadOnly();
            boolean selected = mEditText.hasSelection();
            if (selected) {
                menu.add(0, R.id.action_find_replace, 6, R.string.find).
                       // setIcon(R.drawable.ic_find_replace_white).
                        setAlphabeticShortcut('f').
                        setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                if (!readOnly) {
                    menu.add(0, R.id.action_convert_to_uppercase, 6, R.string.convert_to_uppercase)
                         //   .setIcon(R.drawable.m_uppercase)
                            .setAlphabeticShortcut('U')
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                    menu.add(0, R.id.action_convert_to_lowercase, 6, R.string.convert_to_lowercase)
                            //.setIcon(R.drawable.m_lowercase)
                            .setAlphabeticShortcut('L')
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
                }
            }

            if (!readOnly) {
                menu.add(0, R.id.action_duplicate, 6, selected ? R.string.duplication_text : R.string.duplication_line)
                       // .setIcon(R.drawable.ic_control_point_duplicate_white)
                        .setAlphabeticShortcut('L')
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
            }

            arr.recycle();
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int i = item.getItemId();
            if (i == R.id.action_find_replace) {
                doCommand(new Command(Command.CommandEnum.FIND));
                return true;
            } else if (i == R.id.action_convert_to_uppercase || i == R.id.action_convert_to_lowercase) {
                convertSelectedText(item.getItemId());
                return true;
            } else if (i == R.id.action_duplicate) {
                mEditText.duplicateSelection();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

     */
}
