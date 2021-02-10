package com.mrikso.apkrepacker.ide.editor;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.textfield.TextInputLayout;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.editor.v2.EditorPreferences;
import com.mrikso.apkrepacker.activity.TextEditorActivity;
import com.mrikso.apkrepacker.utils.grep.ExtGrep;
import com.mrikso.apkrepacker.utils.grep.MatcherResult;
import com.mrikso.apkrepacker.R;
import com.mrikso.apkrepacker.database.ITabDatabase;
import com.mrikso.apkrepacker.database.JsonDatabase;
import com.mrikso.apkrepacker.ui.autocompleteeidttext.CustomAdapter;
import com.mrikso.codeeditor.util.FindThread;
import com.mrikso.codeeditor.util.HelperUtils;
import com.mrikso.codeeditor.util.ProgressObserver;
import com.mrikso.codeeditor.util.ProgressSource;

import java.util.List;

public class SearchPanel implements ProgressObserver {
    private static final int ID_FIND_PREV = 1;
    private static final int ID_FIND_NEXT = 2;
    private static final int ID_REPLACE = 3;
    private static final int ID_REPLACE_ALL = 4;
    private static final int ID_FIND_TEXT = 5;
    private LinearLayout mSearchPanel;
    private String mFindText, mReplaceText;
    private MatcherResult mLastResults;
    private ExtGrep grep;
    private boolean mCaseSensitive;
    private boolean mWholeWordsOnly;
    private boolean mRegex, mReplaceMode;
    private Context mContext;
    private TextEditorActivity mActivity;
    private EditorPreferences mEditorPreferences;
    private ITabDatabase mDatabase;
    private FindThread mTaskFind = null;
    CustomAdapter replaceAdapter;
    CustomAdapter searchAdapter;

    public SearchPanel(Context context) {
        mContext = context;
        mEditorPreferences = EditorPreferences.getInstance(mContext);
        mDatabase = JsonDatabase.getInstance(mContext);
    }

    public static SearchPanel getInstance(Context context) {
        return new SearchPanel(context);
    }

    public void setActivity(TextEditorActivity activity) {
        mActivity = activity;
    }

    //search text logic
    public void initSearchPanel(final EditorDelegate editorDelegate) {
//        final TableRow replaceRow = findViewById(R.id.replace_row);
        TextInputLayout replaceTextInputLayout = mActivity.findViewById(R.id.replace_text_input_layout);
        mSearchPanel = mActivity.findViewById(R.id.search_panel);
        mSearchPanel.setVisibility(View.VISIBLE);
        mRegex = mEditorPreferences.isRegexMode();
        mCaseSensitive = mEditorPreferences.isMatchCaseMode();
        mWholeWordsOnly = mEditorPreferences.isWholeWordsOnlyMode();
        replaceTextInputLayout.setVisibility(mReplaceMode ? View.VISIBLE : View.GONE);
        TextView nextResult = mActivity.findViewById(R.id.search_next_result);
        TextView prevResult = mActivity.findViewById(R.id.search_prev_result);
        TextView replaceOption = mActivity.findViewById(R.id.search_replace_option);
        TextView allReplace = mActivity.findViewById(R.id.all_replace_option);
        nextResult.setTextColor(HelperUtils.fetchAccentColor(mContext));
        prevResult.setTextColor(HelperUtils.fetchAccentColor(mContext));
        replaceOption.setTextColor(HelperUtils.fetchAccentColor(mContext));
        allReplace.setEnabled(false);
        allReplace.setTextColor(mContext.getResources().getColor(R.color.color_gray_text_disabled));
        ImageButton moreOption = mActivity.findViewById(R.id.search_more_option);
        searchAdapter = new CustomAdapter(mContext, getSearchData());
        replaceAdapter = new CustomAdapter(mContext, getReplaceData());
        final AppCompatAutoCompleteTextView searchET = mActivity.findViewById(R.id.search_text);
        final AppCompatAutoCompleteTextView replaceET = mActivity.findViewById(R.id.replace_text);
        searchET.setAdapter(searchAdapter);
        replaceET.setAdapter(replaceAdapter);
        searchET.setText(mFindText);
        replaceET.setText(mReplaceText != null ? mReplaceText : "");
        moreOption.setOnClickListener(view -> {
            PopupMenu popupMenu = new PopupMenu(mContext, view);
            popupMenu.inflate(R.menu.search_panel);
            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.regex_check_menu) {
                    if (item.isChecked()) {
                        mRegex = false;
                    } else {
                        mRegex = true;
                        UIUtils.toast(mContext, R.string.use_regex_to_find_tip);
                    }
                } else if (id == R.id.whole_words_only_menu) {
                    mWholeWordsOnly = !item.isChecked();
                } else if (id == R.id.match_case_check) {
                    mCaseSensitive = !item.isChecked();
                } else if (id == R.id.close_search_panel) {
                    mSearchPanel.setVisibility(View.GONE);
                }
                return true;
            });

            popupMenu.getMenu().findItem(R.id.regex_check_menu).setChecked(mRegex);
            popupMenu.getMenu().findItem(R.id.whole_words_only_menu).setChecked(mWholeWordsOnly);
            popupMenu.getMenu().findItem(R.id.match_case_check).setChecked(mCaseSensitive);
            mEditorPreferences.setRegexMode(mRegex);
            mEditorPreferences.setWholeWordsOnlyMode(mWholeWordsOnly);
            mEditorPreferences.setMatchCaseMode(mCaseSensitive);
            popupMenu.show();
        });

        nextResult.setOnClickListener(view -> {
            onFindButtonClick(searchET, replaceET, editorDelegate);// {
            // doFind(ID_FIND_NEXT, grep, editorDelegate);
            //}
        });

        prevResult.setOnClickListener(view -> {
            findBackwards(mFindText, editorDelegate, mCaseSensitive, mWholeWordsOnly);
            // if (lastResults != null) {
            // doFind(ID_FIND_PREV, grep, editorDelegate);
            // }
        });

        replaceOption.setOnClickListener(view -> {
            mReplaceMode = true;
            replaceTextInputLayout.setVisibility(View.VISIBLE);
            allReplace.setEnabled(true);
            allReplace.setTextColor(HelperUtils.fetchAccentColor(mContext));
            if (mReplaceText != null) {
                if (mLastResults != null) {
                    replaceSelection(mReplaceText, editorDelegate);
                    // editorDelegate.getEditableText().replace(lastResults.start(), lastResults.end(), ExtGrep.parseReplacement(lastResults, replaceText));
                    mLastResults = null;
                }
            }
        });
        allReplace.setOnClickListener(view -> {
            replaceAll(mFindText, mReplaceText, editorDelegate, mCaseSensitive, mWholeWordsOnly);
            // if (editorDelegate.getEditText() != null && !TextUtils.isEmpty(searchET.getText().toString()) && !TextUtils.isEmpty(replaceET.getText().toString()) && onFindButtonClick(searchET, replaceET, editorDelegate)) {
            //   grep.replaceAll(editorDelegate.getEditText(), replaceText);
            // } else {
            //   UIUtils.toast(this, R.string.cannot_be_empty);
            // }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    mFindText = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        replaceET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() != 0) {
                    mReplaceText = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void onFindButtonClick(AppCompatAutoCompleteTextView find, AppCompatAutoCompleteTextView replae, EditorDelegate delegate) {
        //注意不要trim
        mFindText = find.getText().toString();
        if (TextUtils.isEmpty(mFindText)) {
            UIUtils.toast(mContext, R.string.cannot_be_empty);
            return;
        }
        //   GrepBuilder builder = GrepBuilder.start();
        if (!mCaseSensitive) {
            // builder.ignoreCase();
        }
        if (mWholeWordsOnly) {
            //  builder.wordRegex();
        }
        // builder.setRegex(findText, mRegex);
        // grep = builder.build();
        find(mFindText, delegate, mCaseSensitive, mWholeWordsOnly);

        searchAdapter.addValue(mFindText);
        //mDatabase.addFindKeyword(mFindText, false);
        mDatabase.addFindKeyword(searchAdapter.getDataList(), false);

        if (mReplaceMode) {
            replaceAdapter.addValue(mReplaceText);
            mDatabase.addFindKeyword(replaceAdapter.getDataList(), true);
        }
        /*
        grep = builder.build();
        SQLHelper dbHelper = new SQLHelper(this);

        searchAdapter.addValue(findText);
        dbHelper.clearFindKeywords(false);
        for (String item : searchAdapter.getDataList()) {
            dbHelper.addFindKeyword(item, false);
        }
        if (mReplaceMode) {
            repaceAdapter.addValue(replaceText);
            dbHelper.clearFindKeywords(true);
            for (String item : repaceAdapter.getDataList()) {
                dbHelper.addFindKeyword(item, true);
            }
        }

         */

        // return findNext(delegate, grep);
    }

    /*
        private boolean findNext(final EditorDelegate delegate, final ExtGrep grep) {
            grep.grepText(ExtGrep.GrepDirect.NEXT,
                    delegate.getEditableText(),
                    delegate.getCursorOffset(),
                    new TaskListener<MatcherResult>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onSuccess(MatcherResult match) {
                            if (match == null) {
                                UIUtils.toast(IdeActivity.this, R.string.find_not_found);
                                return;
                            }
                            //delegate.clearSelectable();
                            getCurrentEditorDelegate().getEditText().requestFocus();
                            delegate.addHighlight(match.start(), match.end());

                            //     getMainActivity().initSearchPanel(holder.mReplaceCheckBox.isChecked(),searchText,replaceText, grep, match, fragment);
                            //getMainActivity().startSupportActionMode(new FindTextActionModeCallback(replaceText, fragment, grep, match));
                        }

                        @Override
                        public void onError(Exception e) {
                            DLog.e(e);
                            UIUtils.toast(IdeActivity.this, e.getMessage());
                        }
                    }
            );
            return true;
        }

        private void doFind(int id, ExtGrep grep, final EditorDelegate editorDelegate) {
            id = id == ID_FIND_PREV ? ID_FIND_PREV : ID_FIND_NEXT;
            if (grep != null) {
                grep.grepText(id == ID_FIND_PREV ? ExtGrep.GrepDirect.PREV : ExtGrep.GrepDirect.NEXT,
                        editorDelegate.getEditableText(),
                        editorDelegate.getCursorOffset(),
                        new TaskListener<MatcherResult>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onSuccess(MatcherResult match) {
                                if (match == null) {
                                    UIUtils.toast(IdeActivity.this, R.string.find_not_found);
                                    return;
                                }
                                editorDelegate.addHighlight(match.start(), match.end());
                                lastResults = match;
                            }

                            @Override
                            public void onError(Exception e) {
                                DLog.e(e);
                                UIUtils.toast(IdeActivity.this, e.getMessage());
                            }
                        });
            }

        }

        /**
         * Switch focus between the find panel and the main editing area
         *
         * @return If the focus was switched successfully between the
         * find panel and main editing area
         */
    @Override
    //This method is called by various worker threads
    public void onComplete(final int requestCode, final Object result) {
        mActivity.runOnUiThread(() -> {
            if (requestCode == ProgressSource.FIND
                    || requestCode == ProgressSource.FIND_BACKWARDS) {
                final int foundIndex = ((FindThread.FindResults) result).foundOffset;
                final int length = ((FindThread.FindResults) result).searchTextLength;

                if (foundIndex != -1) {
                    mActivity.getCurrentEditorDelegate().getEditText().getEditorView().setSelectionRange(foundIndex, length);
                } else {
                    UIUtils.toast(mContext, R.string.find_not_found);
                }
                mTaskFind = null;
            } else if (requestCode == ProgressSource.REPLACE_ALL) {
                final int replacementCount = ((FindThread.FindResults) result).replacementCount;
                final int newCaretPosition = ((FindThread.FindResults) result).newStartPosition;
                if (replacementCount > 0) {
                    mActivity.getCurrentEditorDelegate().getEditText().getEditorView().setEdited(true);
                    mActivity.getCurrentEditorDelegate().getEditText().getEditorView().selectText(false);
                    mActivity.getCurrentEditorDelegate().getEditText().getEditorView().moveCaret(newCaretPosition);
                    mActivity.getCurrentEditorDelegate().getEditText().getEditorView().respan();
                    mActivity.getCurrentEditorDelegate().getEditText().getEditorView().invalidate(); //TODO reduce invalidate calls
                }
                UIUtils.toast(mContext, mContext.getResources().getQuantityString(R.plurals.x_text_replaced, replacementCount, replacementCount));
                mTaskFind = null;
            }

        });
    }

    public void find(String what, EditorDelegate editorDelegate, boolean isCaseSensitive, boolean isWholeWord) {
        if (what.length() > 0) {
            int startingPosition = editorDelegate.getEditText().getEditorView().isSelectText()
                    ? editorDelegate.getEditText().getEditorView().getSelectionStart() + 1
                    : editorDelegate.getEditText().getEditorView().getCaretPosition() + 1;

            mTaskFind = FindThread.createFindThread(
                    editorDelegate.getEditText().getEditorView().createDocumentProvider(),
                    what,
                    startingPosition,
                    true,
                    isCaseSensitive,
                    isWholeWord, editorDelegate.getEditText().getEditorView().getLexTask().getLanguage());
            mTaskFind.registerObserver(this);

            mTaskFind.start();
        }
    }

    public void findBackwards(String what, EditorDelegate editorDelegate, boolean isCaseSensitive, boolean isWholeWord) {
        if (what.length() > 0) {
            int startingPosition = editorDelegate.getEditText().getEditorView().isSelectText()
                    ? editorDelegate.getEditText().getEditorView().getSelectionStart() - 1
                    : editorDelegate.getEditText().getEditorView().getCaretPosition() - 1;

            mTaskFind = FindThread.createFindThread(
                    editorDelegate.getEditText().getEditorView().createDocumentProvider(),
                    what,
                    startingPosition,
                    false,
                    isCaseSensitive,
                    isWholeWord, editorDelegate.getEditText().getEditorView().getLexTask().getLanguage());
            mTaskFind.registerObserver(this);


            mTaskFind.start();
        }
    }

    public void replaceSelection(String replacementText, EditorDelegate editorDelegate) {
        if (editorDelegate.getEditText().getEditorView().isSelectText()) {
            editorDelegate.getEditText().getEditorView().paste(replacementText);
        } else {
           //  Toast.makeText(mContext,R.string.dialog_replace_no_selection,Toast.LENGTH_SHORT).show();
        }
    }

    public void replaceAll(String what, String replacementText, EditorDelegate editorDelegate,
                           boolean isCaseSensitive, boolean isWholeWord) {
        if (what.length() > 0) {
            int startingPosition = editorDelegate.getEditText().getEditorView().getCaretPosition();
            mTaskFind = FindThread.createReplaceAllThread(
                    editorDelegate.getEditText().getEditorView().createDocumentProvider(),
                    what,
                    replacementText,
                    startingPosition,
                    isCaseSensitive,
                    isWholeWord, editorDelegate.getEditText().getEditorView().getLexTask().getLanguage());
            mTaskFind.registerObserver(this);
            mTaskFind.start();
        }
    }

    private List<String> getSearchData() {
        /*List<String> dataList = new ArrayList<>();

        for (String item : mDatabase.getFindKeywords(false)) {
            dataList.add(item);
        }*/
//        searchAdapter.notifyDataSetChanged();
        return mDatabase.getFindKeywords(false);
    }

    private List<String> getReplaceData() {
        /*List<String> dataList = new ArrayList<>();
        for (String item : mDatabase.getFindKeywords(true)) {
            dataList.add(item);
        }*/
        //      replaceAdapter.notifyDataSetChanged();
        return mDatabase.getFindKeywords(true);
    }
}
