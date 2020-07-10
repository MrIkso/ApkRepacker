package com.mrikso.apkrepacker.ide.editor.theme.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ThemeModel {

    @SerializedName("theme_name")
    @Expose
    private String themeName;
    @SerializedName("theme_type")
    @Expose
    private String themeType;
    @SerializedName("dropdown_background")
    @Expose
    private String dropdownBackground;
    @SerializedName("dropdown_border")
    @Expose
    private String dropdownBorder;
    @SerializedName("dropdown_foreground")
    @Expose
    private String dropdownForeground;
    @SerializedName("view_background_color")
    @Expose
    private String viewBackgroundColor;
    @SerializedName("view_caret_color")
    @Expose
    private String viewCaretColor;
    @SerializedName("view_gutter_background_color")
    @Expose
    private String viewGutterBackgroundColor;
    @SerializedName("view_gutter_foreground_color")
    @Expose
    private String viewGutterForegroundColor;
    @SerializedName("view_selection_color")
    @Expose
    private String viewSelectionColor;
    @SerializedName("view_comment")
    @Expose
    private String viewComment;
    @SerializedName("view_keyword")
    @Expose
    private String viewKeyword;
    @SerializedName("view_name")
    @Expose
    private String viewName;
    @SerializedName("view_literal")
    @Expose
    private String viewLiteral;
    @SerializedName("view_operator")
    @Expose
    private String viewOperator;
    @SerializedName("view_separator")
    @Expose
    private String viewSeparator;
    @SerializedName("view_package")
    @Expose
    private String viewPackage;
    @SerializedName("view_type")
    @Expose
    private String viewType;
    @SerializedName("view_error")
    @Expose
    private String viewError;
    @SerializedName("view_string")
    @Expose
    private String viewString;
    @SerializedName("view_default")
    @Expose
    private String viewDefault;
    @SerializedName("view_constant")
    @Expose
    private String viewConstant;
    @SerializedName("view_whitespace_color")
    @Expose
    private String viewWhitespaceColor;

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public String getThemeType() {
        return themeType;
    }

    public void setThemeType(String themeType) {
        this.themeType = themeType;
    }

    public String getDropdownBackground() {
        return dropdownBackground;
    }

    public void setDropdownBackground(String dropdownBackground) {
        this.dropdownBackground = dropdownBackground;
    }

    public String getDropdownBorder() {
        return dropdownBorder;
    }

    public void setDropdownBorder(String dropdownBorder) {
        this.dropdownBorder = dropdownBorder;
    }

    public String getDropdownForeground() {
        return dropdownForeground;
    }

    public void setDropdownForeground(String dropdownForeground) {
        this.dropdownForeground = dropdownForeground;
    }

    public String getViewBackgroundColor() {
        return viewBackgroundColor;
    }

    public void setViewBackgroundColor(String viewBackgroundColor) {
        this.viewBackgroundColor = viewBackgroundColor;
    }

    public String getViewCaretColor() {
        return viewCaretColor;
    }

    public void setViewCaretColor(String viewCaretColor) {
        this.viewCaretColor = viewCaretColor;
    }

    public String getViewGutterBackgroundColor() {
        return viewGutterBackgroundColor;
    }

    public void setViewGutterBackgroundColor(String viewGutterBackgroundColor) {
        this.viewGutterBackgroundColor = viewGutterBackgroundColor;
    }

    public String getViewGutterForegroundColor() {
        return viewGutterForegroundColor;
    }

    public void setViewGutterForegroundColor(String viewGutterForegroundColor) {
        this.viewGutterForegroundColor = viewGutterForegroundColor;
    }

    public String getViewSelectionColor() {
        return viewSelectionColor;
    }

    public void setViewSelectionColor(String viewSelectionColor) {
        this.viewSelectionColor = viewSelectionColor;
    }

    public String getViewComment() {
        return viewComment;
    }

    public void setViewComment(String viewComment) {
        this.viewComment = viewComment;
    }

    public String getViewKeyword() {
        return viewKeyword;
    }

    public void setViewKeyword(String viewKeyword) {
        this.viewKeyword = viewKeyword;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getViewLiteral() {
        return viewLiteral;
    }

    public void setViewLiteral(String viewLiteral) {
        this.viewLiteral = viewLiteral;
    }

    public String getViewOperator() {
        return viewOperator;
    }

    public void setViewOperator(String viewOperator) {
        this.viewOperator = viewOperator;
    }

    public String getViewSeparator() {
        return viewSeparator;
    }

    public void setViewSeparator(String viewSeparator) {
        this.viewSeparator = viewSeparator;
    }

    public String getViewPackage() {
        return viewPackage;
    }

    public void setViewPackage(String viewPackage) {
        this.viewPackage = viewPackage;
    }

    public String getViewType() {
        return viewType;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    public String getViewError() {
        return viewError;
    }

    public void setViewError(String viewError) {
        this.viewError = viewError;
    }

    public String getViewString() {
        return viewString;
    }

    public void setViewString(String viewString) {
        this.viewString = viewString;
    }

    public String getViewDefault() {
        return viewDefault;
    }

    public void setViewDefault(String viewDefault) {
        this.viewDefault = viewDefault;
    }

    public String getViewConstant() {
        return viewConstant;
    }

    public void setViewConstant(String viewConstant) {
        this.viewConstant = viewConstant;
    }

    public String getViewWhitespaceColor() {
        return viewWhitespaceColor;
    }

    public void setViewWhitespaceColor(String viewWhitespaceColor) {
        this.viewWhitespaceColor = viewWhitespaceColor;
    }
}
