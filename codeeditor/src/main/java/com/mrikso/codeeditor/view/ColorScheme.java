/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */

package com.mrikso.codeeditor.view;

import android.graphics.Color;

import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.TextWarriorException;

import java.util.HashMap;

public abstract class ColorScheme {

    public enum Colorable {
      //  FOREGROUND,
        BACKGROUND,//цвет фона редактора
        SELECTION_FOREGROUND,//цвет выделеного текста
        SELECTION_BACKGROUND,//цвет выделения текста
        GUTTER_FOREGROUND,//цвет фона номера строк
        GUTTER_LINENUMBER,//цвет текста номеров строк
        CARET_BACKGROUND,//цвет handle
        //CARET_DISABLED,
        LINE_HIGHLIGHT,//цвет строки, когда там находится курсор
        NON_PRINTING_GLYPH,//цвет не печатываемых символов
        //синтаксис
        COMMENT,//комментарии
        KEYWORD,//зарезервированные слова
        NAME,//имена
        LITERAL,//цифры
        OPERATOR,//операторы
        SEPARATOR,//разделители
        PACKAGE,//пакет
        TYPE,//тип
        ERROR,//ошибка
        STRING,//строка
        TEXT;//обычный текст
    }

    protected HashMap<Colorable, Integer> _colors = generateDefaultColors();

    public void setColor(Colorable colorable, int color) {
        _colors.put(colorable, color);
    }

    public int getColor(Colorable colorable) {
        Integer color = _colors.get(colorable);
        if (color == null) {
            TextWarriorException.fail("Color not specified for " + colorable);
            return 0;
        }
        return color;
    }

    // Currently, color scheme is tightly coupled with semantics of the token types
    public int getTokenColor(int tokenType) {
        Colorable element;
        switch (tokenType) {
            case Lexer.NORMAL:
                element = Colorable.TEXT;
                break;
            case Lexer.KEYWORD:
                element = Colorable.KEYWORD;
                break;
            case Lexer.NAME:
                element = Colorable.NAME;
                break;
            case Lexer.COMMENT:
                element = Colorable.COMMENT;
                break;
            case Lexer.SEPARATOR:
                element = Colorable.SEPARATOR;
                break;
            case Lexer.LITERAL:
                element = Colorable.LITERAL;
                break;
            case Lexer.OPERATOR:
                element = Colorable.OPERATOR;
                break;
            case Lexer.UNKNOWN:
            case Lexer.ERROR:
                element = Colorable.ERROR;
                break;
            case Lexer.PACKAGE:
                element = Colorable.PACKAGE;
                break;
            case Lexer.TYPE:
                element = Colorable.TYPE;
                break;
            case Lexer.TEXT:
                element = Colorable.TEXT;
                break;
            default:
                TextWarriorException.fail("Invalid token type");
                element = Colorable.TEXT;
                break;
        }
        return getColor(element);
    }

    /**
     * Whether this color scheme uses a dark background, like black or dark grey.
     */
    public boolean isDark() {
        return false;
    }

    private HashMap<Colorable, Integer> generateDefaultColors() {
        // High-contrast, black-on-white color scheme
        HashMap<Colorable, Integer> colors = new HashMap<>(Colorable.values().length);
        colors.put(Colorable.BACKGROUND, Color.WHITE);
        colors.put(Colorable.SELECTION_FOREGROUND, Color.WHITE);
        colors.put(Colorable.SELECTION_BACKGROUND, 0x2097C024);
        colors.put(Colorable.GUTTER_FOREGROUND, Color.WHITE);
        colors.put(Colorable.GUTTER_LINENUMBER, 0xFFC0C0C0);
        colors.put(Colorable.CARET_BACKGROUND, 0xFF40B0FF);
        colors.put(Colorable.LINE_HIGHLIGHT, 0x20888888);
        colors.put(Colorable.NON_PRINTING_GLYPH, 0xFFC0C0C0);
        //синтаксис
        colors.put(Colorable.COMMENT, 0xFF3F7F5F);
        colors.put(Colorable.KEYWORD, 0xFFD040DD);
        colors.put(Colorable.NAME, Color.BLACK);
        colors.put(Colorable.LITERAL, 0xFF6080FF);
        colors.put(Colorable.OPERATOR, 0xFFDD4488);
        colors.put(Colorable.SEPARATOR, 0xff0096ff);
        colors.put(Colorable.PACKAGE, 0xff5d5d5d);
        colors.put(Colorable.TYPE, 0xff0096ff);
        colors.put(Colorable.ERROR, 0xffff0000);
        colors.put(Colorable.TEXT, Color.BLACK);
        return colors;
    }
}

