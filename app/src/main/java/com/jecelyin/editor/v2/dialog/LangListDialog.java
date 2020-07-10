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

package com.jecelyin.editor.v2.dialog;

import android.app.AlertDialog;
import android.content.Context;


import com.jecelyin.editor.v2.common.Command;
import com.mrikso.apkrepacker.R;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Jecelyin Peng <jecelyin@gmail.com>
 */
public class LangListDialog extends AbstractDialog {
    private String[] langList;
    private int currentLangIndex = -1;

    public LangListDialog(Context context) {
        super(context);
        initGrammarInfo();
    }

    private void initGrammarInfo() {
        ArrayList<String> list = new ArrayList<>();
        list.add("C++");
        list.add("Java");
        list.add("Smali");
        list.add("Html");
        list.add("Json");
        list.add("Xml");
        list.add("Css");
        list.add("None");
        Collections.sort(list, String::compareToIgnoreCase);

        String currLang = getMainActivity().getCurrentLang();

        int size = list.size();
        langList = new String[size];

        for (int i = 0; i < size; i++) {
            String name = list.get(i);
            langList[i] = name;

            if (currLang != null && currLang.equals(name)) {
                currentLangIndex = i;
            }
        }
    }

    @Override
    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.select_lang_to_highlight)
                .setSingleChoiceItems(langList, currentLangIndex, (dialog, which) -> {
                    Command command = new Command(Command.CommandEnum.HIGHLIGHT);
                    command.object = langList[which];
                    getMainActivity().doCommand(command);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        AlertDialog dlg = builder.create();
        dlg.show();
        handleDialog(dlg);
    }
}
