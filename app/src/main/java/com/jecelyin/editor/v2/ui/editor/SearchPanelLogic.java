/*
 * Copyright (C) 2016 Jecelyin Peng <jecelyin@gmail.com>
 *
 * This file is part of 920 Text Editor.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jecelyin.editor.v2.ui.editor;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.widget.PopupMenu;

import com.mrikso.apkrepacker.R;
import com.jecelyin.common.task.TaskListener;
import com.jecelyin.common.utils.DLog;
import com.jecelyin.common.utils.UIUtils;
import com.jecelyin.common.widget.DrawClickableEditText;
import com.jecelyin.editor.v2.utils.DBHelper;
import com.jecelyin.editor.v2.utils.ExtGrep;
import com.jecelyin.editor.v2.utils.GrepBuilder;
import com.jecelyin.editor.v2.utils.MatcherResult;

public class SearchPanelLogic {

    private EditorDelegate fragment;
    private static final int ID_FIND_PREV = 1;
    private static final int ID_FIND_NEXT = 2;
    private static final int ID_REPLACE = 3;
    private static final int ID_REPLACE_ALL = 4;
    private static final int ID_FIND_TEXT = 5;

    private MatcherResult lastResults;
    private ExtGrep grep;
    private String findText, replaceText;
    private Context context;
    private SearchPanelLogic.ViewHolder holder;

    public void init(EditorDelegate delegate){
        fragment = delegate;
        context = delegate.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.search_panel, null);
        holder = new SearchPanelLogic.ViewHolder(view);
        initSearchPanel(false);
    }

    private void initSearchPanel(boolean replaceMode){

      //  this.lastResults = match;
       // this.findText = searchText;
      //  this.replaceText = replacesText;
        holder.searchPanel.setVisibility(View.VISIBLE);
        holder.replaceRow.setVisibility(replaceMode ?  View.VISIBLE: View.GONE);

      //  holder.searchEditText.setText(findText);
      //  holder.replaceEditText.setText(replaceText!=null ? replaceText : "");

        holder.moreoption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                popupMenu.getMenu().add(Menu.NONE,1,1, R.string.close);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case 1:
                                holder.searchPanel.setVisibility(View.GONE);
                                break;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        holder.nextResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!onFindButtonClick(holder)){
                    doFind(ID_FIND_NEXT,grep, fragment);
                }
            }
        });


    }

    private void initSearchButton (boolean replaceMode){
        holder.replaceRow.setVisibility(replaceMode ?  View.VISIBLE: View.GONE);
        holder.prevResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doFind(ID_FIND_PREV,grep,fragment);
            }
        });
        holder.replaceOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (lastResults != null) {
                    fragment.getEditableText().replace(lastResults.start(), lastResults.end(), ExtGrep.parseReplacement(lastResults, replaceText));
                    lastResults = null;
                }
            }
        });
        holder.allReplace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                grep.replaceAll(fragment.getEditableText(), replaceText);
            }
        });

        holder.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() !=0){
                    findText = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        holder.replaceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.length() !=0){
                    replaceText = charSequence.toString();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }
    private boolean onFindButtonClick(SearchPanelLogic.ViewHolder holder) {

        findText = holder.searchEditText.getText().toString();
        if (TextUtils.isEmpty(findText)) {
            holder.searchEditText.setError(context.getString(R.string.cannot_be_empty));
            return false;
        }

       // String replaceText = holder.mReplaceCheckBox.isChecked() ? holder.replaceEditText.getText().toString() : null;


        GrepBuilder builder = GrepBuilder.start();
        //if (!holder.mCaseSensitiveCheckBox.isChecked()) {
            builder.ignoreCase();
       // }
      //  if (holder.mWholeWordsOnlyCheckBox.isChecked()) {
            builder.wordRegex();
      //  }
      //  builder.setRegex(findText, holder.mRegexCheckBox.isChecked());

        ExtGrep grep = builder.build();

        DBHelper.getInstance(context).addFindKeyword(findText, false);
        DBHelper.getInstance(context).addFindKeyword(replaceText, true);

        findNext(holder, grep,findText,replaceText);

        return true;
    }

    private void findNext(final SearchPanelLogic.ViewHolder holder, final ExtGrep grep, final String searchText, final String replaceText) {
        grep.grepText(ExtGrep.GrepDirect.NEXT,
                fragment.getEditableText(),
                fragment.getCursorOffset(),
                new TaskListener<MatcherResult>() {
                    @Override
                    public void onCompleted() {
                        //  UIUtils.toast(context, "Done");
                    }

                    @Override
                    public void onSuccess(MatcherResult match) {
                        if (match == null) {
                            UIUtils.toast(context, R.string.find_not_found);
                            return;
                        }
                        fragment.clearSelectable();
                        fragment.addHightlight(match.start(), match.end());
                        initSearchButton(false);
                       // initSearchPanel(false);
                       // initSearchPanel(holder.mReplaceCheckBox.isChecked(),searchText,replaceText, grep, match, fragment);
                        //getMainActivity().startSupportActionMode(new FindTextActionModeCallback(replaceText, fragment, grep, match));
                    }

                    @Override
                    public void onError(Exception e) {
                        DLog.e(e);
                        UIUtils.toast(context, e.getMessage());
                    }
                }
        );
    }

    private void doFind(int id, ExtGrep grep, final EditorDelegate editorDelegate ) {
        id = id == ID_FIND_PREV ? ID_FIND_PREV : ID_FIND_NEXT;
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
                            UIUtils.toast(context, R.string.find_not_found);
                            return;
                        }
                        editorDelegate.clearSelectable();
                        editorDelegate.addHightlight(match.start(), match.end());
                        lastResults = match;
                    }

                    @Override
                    public void onError(Exception e) {
                        DLog.e(e);
                        UIUtils.toast(context, e.getMessage());
                    }
                });
    }

    static class ViewHolder {
        TableRow replaceRow;
        TextView nextResult;
        TextView prevResult;
        TextView replaceOption;
        TextView allReplace;
        TextView moreoption;
        DrawClickableEditText searchEditText;
        DrawClickableEditText replaceEditText;
        LinearLayout searchPanel;

        ViewHolder(View view) {
            searchPanel = view.findViewById(R.id.search_panel);
            replaceRow = view.findViewById(R.id.replace_row);
            nextResult = view.findViewById(R.id.search_next_result);
            prevResult = view.findViewById(R.id.search_prev_result);
            replaceOption = view.findViewById(R.id.search_replace_option);
            allReplace = view.findViewById(R.id.all_replace_option);
            moreoption = view.findViewById(R.id.search_more_option);
            searchEditText = view.findViewById(R.id.search_text);
            replaceEditText = view.findViewById(R.id.replace_text);
        }
    }
}
