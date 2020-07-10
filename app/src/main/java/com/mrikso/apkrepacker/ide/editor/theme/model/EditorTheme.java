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

package com.mrikso.apkrepacker.ide.editor.theme.model;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EditorTheme extends ColorScheme {
    private static final String TAG = "EditorTheme";

    /**
     * File name in assets
     */
    private String fileName;

    private String name;
    private ThemeModel themeModel;

    @Override
    public void load(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        themeModel = gson.fromJson(json, ThemeModel.class);
    }

    public ThemeModel getThemeModel() {
        return themeModel;
    }

    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        if (name.isEmpty()) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append(name.replace("-", " "));
        builder.setCharAt(0, Character.toUpperCase(builder.charAt(0)));
        for (int i = 0; i < builder.length(); i++) {
            if (builder.charAt(i) == ' ' && i + 1 < builder.length()) {
                builder.setCharAt(i + 1, Character.toUpperCase(builder.charAt(i + 1)));
            }
        }
        this.name = builder.toString();
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}
