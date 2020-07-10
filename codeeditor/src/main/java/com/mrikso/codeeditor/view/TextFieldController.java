package com.mrikso.codeeditor.view;

import android.content.Context;
import android.text.ClipboardManager;
import android.view.inputmethod.InputMethodManager;

import com.mrikso.codeeditor.lang.Language;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.Pair;
import com.mrikso.codeeditor.util.TextWarriorException;

import java.util.List;

import static com.mrikso.codeeditor.util.DLog.log;

//*********************************************************************
//************************ Controller logic ***************************
//*********************************************************************
public class TextFieldController implements Lexer.LexCallback {
    public final Lexer _lexer;
    public boolean _isInSelectionMode = false;
    private boolean _isInSelectionMode2;
    private FreeScrollingTextField mTextField;

    public TextFieldController(FreeScrollingTextField textField) {
        mTextField = textField;
        _lexer = new Lexer(this, mTextField.getLexTask());
    }

    /**
     * Analyze the text for programming language keywords and redraws the
     * text view when done. The global programming language used is set with
     * the static method Lexer.setLanguage(Language)
     * <p>
     * Does nothing if the Lexer language is not a programming language
     */
    public void determineSpans() {
        _lexer.tokenize(mTextField.hDoc);
    }

    public void cancelSpanning() {
        _lexer.cancelTokenize();
    }

    @Override
    //This is usually called from a non-UI thread
    public void lexDone(final List<Pair> results) {
        mTextField.post(new Runnable() {
            @Override
            public void run() {
                mTextField.hDoc.setSpans(results);
                mTextField.invalidate();
            }
        });
    }

    //- TextFieldController -----------------------------------------------
    //---------------------------- Key presses ----------------------------

    //TODO minimise invalidate calls from moveCaret(), insertion/deletion and word wrap
    public void onPrintableChar(char c) {
        // delete currently selected text, if any
        boolean selectionDeleted = false;
        if (_isInSelectionMode) {
            selectionDelete();
            selectionDeleted = true;
        }

        int originalRow = mTextField.mCaretRow;
        int originalOffset = mTextField.hDoc.getRowOffset(originalRow);

        switch (c) {
            case Language.BACKSPACE:
                if (selectionDeleted) {
                    break;
                }
                if (mTextField.mCaretPosition > 0) {
                    mTextField.hDoc.deleteAt(mTextField.mCaretPosition - 1, System.nanoTime());
                    if (mTextField.hDoc.charAt(mTextField.mCaretPosition - 2) == 0xd83d || mTextField.hDoc.charAt(mTextField.mCaretPosition - 2) == 0xd83c) {
                        mTextField.hDoc.deleteAt(mTextField.mCaretPosition - 2, System.nanoTime());
                        moveCaretLeft(true);
                    }

                    mTextField.mTextListener.onDel(c + "", mTextField.mCaretPosition, 1);
                    moveCaretLeft(true);

                    if (mTextField.mCaretRow < originalRow) {
                        // either a newline was deleted or the caret was on the
                        // first word and it became short enough to fit the prev
                        // row
                        mTextField.invalidateFromRow(mTextField.mCaretRow);
                    } else if (mTextField.hDoc.isWordWrap()) {
                        if (originalOffset != mTextField.hDoc.getRowOffset(originalRow)) {
                            //invalidate previous row too if its wrapping changed
                            --originalRow;
                        }
                        //TODO invalidate damaged rows only
                        mTextField.invalidateFromRow(originalRow);
                    }
                }
                break;

            case Language.NEWLINE:
                if (mTextField.isAutoIndent) {
                    char[] indent = createAutoIndent();
                    mTextField.hDoc.insertBefore(indent, mTextField.mCaretPosition, System.nanoTime());
                    moveCaret(mTextField.mCaretPosition + indent.length);
                } else {
                    mTextField.hDoc.insertBefore(c, mTextField.mCaretPosition, System.nanoTime());
                    moveCaretRight(true);
                }

                if (mTextField.hDoc.isWordWrap() && originalOffset != mTextField.hDoc.getRowOffset(originalRow)) {
                    //invalidate previous row too if its wrapping changed
                    --originalRow;
                }

                mTextField.mTextListener.onNewLine(c + "", mTextField.mCaretPosition, 1);
                mTextField.invalidateFromRow(originalRow);
                break;

            default:
                mTextField.hDoc.insertBefore(c, mTextField.mCaretPosition, System.nanoTime());
                moveCaretRight(true);
                mTextField.mTextListener.onAdd(c + "", mTextField.mCaretPosition, 1);
                if (mTextField.hDoc.isWordWrap()) {
                    if (originalOffset != mTextField.hDoc.getRowOffset(originalRow)) {
                        //invalidate previous row too if its wrapping changed
                        --originalRow;
                    }
                    //TODO invalidate damaged rows only
                    mTextField.invalidateFromRow(originalRow);
                }
                break;
        }

        mTextField.setEdited(true);
        determineSpans();
    }

    /**
     * Return a char[] with a newline as the 0th element followed by the
     * leading spaces and tabs of the line that the caret is on
     * 创建自动缩进
     */
    private char[] createAutoIndent() {
        int lineNum = mTextField.hDoc.findLineNumber(mTextField.mCaretPosition);
        int startOfLine = mTextField.hDoc.getLineOffset(lineNum);
        int whitespaceCount = 0;
        mTextField.hDoc.seekChar(startOfLine);
        //查找上一行的空白符个数
        while (mTextField.hDoc.hasNext()) {
            char c = mTextField.hDoc.next();
            if ((c != ' ' && c != Language.TAB) || startOfLine + whitespaceCount >= mTextField.mCaretPosition) {
                break;
            }
            if (c == Language.TAB) {
                whitespaceCount += mTextField.getAutoIndentWidth();
            }
            if (c == ' ')
                ++whitespaceCount;
        }
        //寻找最后字符
        mTextField.hDoc.seekChar(startOfLine);
        int endChar = 0;
        while (mTextField.hDoc.hasNext()) {
            char c = mTextField.hDoc.next();
            if (c == Language.NEWLINE) {
                break;
            }
            endChar = c;
        }
        //最后字符为'{',缩进
        if (endChar == '{')
            whitespaceCount += mTextField.getAutoIndentWidth();
        if (whitespaceCount < 0)
            return new char[]{Language.NEWLINE};

        char[] indent = new char[1 + whitespaceCount];
        indent[0] = Language.NEWLINE;

        mTextField.hDoc.seekChar(startOfLine);
        for (int i = 0; i < whitespaceCount; ++i) {
            indent[1 + i] = ' ';
        }
        return indent;
    }

    public void moveCaretDown() {
        if (!mTextField.caretOnLastRowOfFile()) {
            int currCaret = mTextField.mCaretPosition;
            int currRow = mTextField.mCaretRow;
            int newRow = currRow + 1;
            int currColumn = mTextField.getColumn(currCaret);
            int currRowLength = mTextField.hDoc.getRowSize(currRow);
            int newRowLength = mTextField.hDoc.getRowSize(newRow);

            if (currColumn < newRowLength) {
                // Position at the same column as old row.
                mTextField.mCaretPosition += currRowLength;
            } else {
                // Column does not exist in the new row (new row is too short).
                // Position at end of new row instead.
                mTextField.mCaretPosition +=
                        currRowLength - currColumn + newRowLength - 1;
            }
            ++mTextField.mCaretRow;

            updateSelectionRange(currCaret, mTextField.mCaretPosition);
            if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
                mTextField.invalidateRows(currRow, newRow + 1);
            }
            // 拖动yoyo球滚动时，保证yoyo球的坐标与光标一致
            mTextField.mCaretListener.updateCaret(mTextField.mCaretPosition);
            mTextField.mRowListener.onRowChanged(newRow);
            stopTextComposing();
        }
    }

    public void moveCaretUp() {
        if (!mTextField.caretOnFirstRowOfFile()) {
            int currCaret = mTextField.mCaretPosition;
            int currRow = mTextField.mCaretRow;
            int newRow = currRow - 1;
            int currColumn = mTextField.getColumn(currCaret);
            int newRowLength = mTextField.hDoc.getRowSize(newRow);

            if (currColumn < newRowLength) {
                // Position at the same column as old row.
                mTextField.mCaretPosition -= newRowLength;
            } else {
                // Column does not exist in the new row (new row is too short).
                // Position at end of new row instead.
                mTextField.mCaretPosition -= (currColumn + 1);
            }
            --mTextField.mCaretRow;

            updateSelectionRange(currCaret, mTextField.mCaretPosition);
            if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
                mTextField.invalidateRows(newRow, currRow + 1);
            }
            // 拖动yoyo球滚动时，保证yoyo球的坐标与光标一致
            mTextField.mCaretListener.updateCaret(mTextField.mCaretPosition);
            mTextField.mRowListener.onRowChanged(newRow);
            stopTextComposing();
        }
    }

    /**
     * @param isTyping Whether caret is moved to a consecutive position as
     *                 a result of entering text
     */
    public void moveCaretRight(boolean isTyping) {
        if (!mTextField.caretOnEOF()) {
            int originalRow = mTextField.mCaretRow;
            ++mTextField.mCaretPosition;
            updateCaretRow();
            updateSelectionRange(mTextField.mCaretPosition - 1, mTextField.mCaretPosition);
            if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
                mTextField.invalidateRows(originalRow, mTextField.mCaretRow + 1);
            }

            if (!isTyping) {
                stopTextComposing();
            }
            // 拖动yoyo球滚动时，保证yoyo球的坐标与光标一致
            mTextField.mCaretListener.updateCaret(mTextField.mCaretPosition);
        }
    }

    /**
     * @param isTyping Whether caret is moved to a consecutive position as
     *                 a result of deleting text
     */
    public void moveCaretLeft(boolean isTyping) {
        if (mTextField.mCaretPosition > 0) {
            int originalRow = mTextField.mCaretRow;
            --mTextField.mCaretPosition;
            updateCaretRow();
            updateSelectionRange(mTextField.mCaretPosition + 1, mTextField.mCaretPosition);
            if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
                mTextField.invalidateRows(mTextField.mCaretRow, originalRow + 1);
            }

            if (!isTyping) {
                stopTextComposing();
            }
            // 拖动yoyo球滚动时，保证yoyo球的坐标与光标一致
            mTextField.mCaretListener.updateCaret(mTextField.mCaretPosition);
        }
    }

    public void moveCaret(int i) {
        if (i < 0 || i >= mTextField.hDoc.docLength()) {
            TextWarriorException.fail("Invalid caret position");
            return;
        }
        updateSelectionRange(mTextField.mCaretPosition, i);

        mTextField.mCaretPosition = i;
        updateAfterCaretJump();
    }

    private void updateAfterCaretJump() {
        int oldRow = mTextField.mCaretRow;
        updateCaretRow();
        if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
            mTextField.invalidateRows(oldRow, oldRow + 1); //old caret row
            mTextField.invalidateCaretRow(); //new caret row
        }
        stopTextComposing();
    }

    /**
     * This helper method should only be used by internal methods after setting
     * mTextFiledl.mCaretPosition, in order to to recalculate the new row the caret is on.
     */
    void updateCaretRow() {
        int newRow = mTextField.hDoc.findRowNumber(mTextField.mCaretPosition);
        if (mTextField.mCaretRow != newRow) {
            mTextField.mCaretRow = newRow;
            mTextField.mRowListener.onRowChanged(newRow);
        }
    }

    public void stopTextComposing() {
        InputMethodManager im = (InputMethodManager) mTextField.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        // This is an overkill way to inform the InputMethod that the caret
        // might have changed position and it should re-evaluate the
        // caps mode to use.
        im.restartInput(mTextField);

        if (mTextField.mInputConnection != null && mTextField.mInputConnection.isComposingStarted()) {
            mTextField.mInputConnection.resetComposingState();
        }
    }

    //- TextFieldController -----------------------------------------------
    //-------------------------- Selection mode ---------------------------
    public final boolean isSelectText() {
        return _isInSelectionMode;
    }

    /**
     * Enter or exit select mode.
     * Does not invalidate view.
     *
     * @param mode If true, enter select mode; else exit select mode
     */
    public void setSelectText(boolean mode) {
        if (mode == _isInSelectionMode) {
            return;
        }

        if (mode) {
            mTextField.mSelectionAnchor = mTextField.mCaretPosition;
            mTextField.mSelectionEdge = mTextField.mCaretPosition;
        } else {
            mTextField.mSelectionAnchor = -1;
            mTextField.mSelectionEdge = -1;
        }
        _isInSelectionMode = mode;
        _isInSelectionMode2 = mode;
        mTextField.mSelModeListener.onSelectionChanged(mode, mTextField.getSelectionStart(), mTextField.getSelectionEnd());
    }

    public final boolean isSelectText2() {
        return _isInSelectionMode2;
    }

    public boolean inSelectionRange(int charOffset) {
        if (mTextField.mSelectionAnchor < 0) {
            return false;
        }
        return (mTextField.mSelectionAnchor <= charOffset && charOffset < mTextField.mSelectionEdge);
    }

    /**
     * Selects numChars count of characters starting from beginPosition.
     * Invalidates necessary areas.
     *
     * @param beginPosition
     * @param numChars
     * @param scrollToStart If true, the start of the selection will be scrolled
     *                      into view. Otherwise, the end of the selection will be scrolled.
     */

    public void setSelectionRange(int beginPosition, int numChars, boolean scrollToStart, boolean mode) {
        TextWarriorException.assertVerbose(
                (beginPosition >= 0) && numChars <= (mTextField.hDoc.docLength() - 1) && numChars >= 0,
                "Invalid range to select");

        if (_isInSelectionMode) {
            // unhighlight previous selection
            mTextField.invalidateSelectionRows();
        } else {
            // unhighlight caret
            mTextField.invalidateCaretRow();
            if (mode)
                setSelectText(true);
            else
                _isInSelectionMode = true;
        }

        mTextField.mSelectionAnchor = beginPosition;
        mTextField.mSelectionEdge = mTextField.mSelectionAnchor + numChars;

        mTextField.mCaretPosition = mTextField.mSelectionEdge;
        stopTextComposing();
        updateCaretRow();
        if (mode)
            mTextField.mSelModeListener.onSelectionChanged(isSelectText(), mTextField.mSelectionAnchor, mTextField.mSelectionEdge);
        boolean scrolled = mTextField.makeCharVisible(mTextField.mSelectionEdge);

        if (scrollToStart) {
            //TODO reduce unnecessary scrolling and write a method to scroll
            // the beginning of multi-line selections as far left as possible
            scrolled = mTextField.makeCharVisible(mTextField.mSelectionAnchor);
        }

        if (!scrolled) {
            mTextField.invalidateSelectionRows();
        }
    }

    /**
     * Moves the caret to an edge of selected text and scrolls it to view.
     *
     * @param start If true, moves the caret to the beginning of
     *              the selection. Otherwise, moves the caret to the end of the selection.
     *              In all cases, the caret is scrolled to view if it is not visible.
     */
    public void focusSelection(boolean start) {
        if (_isInSelectionMode) {
            if (start && mTextField.mCaretPosition != mTextField.mSelectionAnchor) {
                mTextField.mCaretPosition = mTextField.mSelectionAnchor;
                updateAfterCaretJump();
            } else if (!start && mTextField.mCaretPosition != mTextField.mSelectionEdge) {
                mTextField.mCaretPosition = mTextField.mSelectionEdge;
                updateAfterCaretJump();
            }
        }
    }


    /**
     * Used by internal methods to update selection boundaries when a new
     * caret position is set.
     * Does nothing if not in selection mode.
     */
    private void updateSelectionRange(int oldCaretPosition, int newCaretPosition) {

        if (!_isInSelectionMode) {
            return;
        }

        if (oldCaretPosition < mTextField.mSelectionEdge) {
            if (newCaretPosition > mTextField.mSelectionEdge) {
                mTextField.mSelectionAnchor = mTextField.mSelectionEdge;
                mTextField.mSelectionEdge = newCaretPosition;
            } else {
                mTextField.mSelectionAnchor = newCaretPosition;
            }

        } else {
            if (newCaretPosition < mTextField.mSelectionAnchor) {
                mTextField.mSelectionEdge = mTextField.mSelectionAnchor;
                mTextField.mSelectionAnchor = newCaretPosition;
            } else {
                mTextField.mSelectionEdge = newCaretPosition;
            }
        }
    }

    //- TextFieldController -----------------------------------------------
    //------------------------ Cut, copy, paste, delete ---------------------------

    /**
     * Convenience method for consecutive copy and paste calls
     */
    public void cut(ClipboardManager cb) {
        copy(cb);
        selectionDelete();
    }

    /**
     * Copies the selected text to the clipboard.
     * <p>
     * Does nothing if not in select mode.
     */
    public void copy(ClipboardManager cb) {
        //TODO catch OutOfMemoryError
        if (_isInSelectionMode &&
                mTextField.mSelectionAnchor < mTextField.mSelectionEdge) {
            CharSequence contents = mTextField.hDoc.subSequence(mTextField.mSelectionAnchor,
                    mTextField.mSelectionEdge - mTextField.mSelectionAnchor);
            cb.setText(contents);
        }
    }

    /**
     * Inserts text at the caret position.
     * Existing selected text will be deleted and select mode will end.
     * The deleted area will be invalidated.
     * <p>
     * After insertion, the inserted area will be invalidated.
     */
    public void paste(String text) {
        if (text == null) {
            return;
        }

        mTextField.hDoc.beginBatchEdit();
        selectionDelete();

        int originalRow = mTextField.mCaretRow;
        int originalOffset = mTextField.hDoc.getRowOffset(originalRow);
        mTextField.hDoc.insertBefore(text.toCharArray(), mTextField.mCaretPosition, System.nanoTime());
        mTextField.mTextListener.onAdd(text, mTextField.mCaretPosition, text.length());
        //_textLis.onAdd(text, mTextFiledl.mCaretPosition, text.length());
        mTextField.hDoc.endBatchEdit();

        mTextField.mCaretPosition += text.length();
        updateCaretRow();

        mTextField.setEdited(true);
        determineSpans();
        stopTextComposing();

        if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
            int invalidateStartRow = originalRow;
            //invalidate previous row too if its wrapping changed
            if (mTextField.hDoc.isWordWrap() &&
                    originalOffset != mTextField.hDoc.getRowOffset(originalRow)) {
                --invalidateStartRow;
            }

            if (originalRow == mTextField.mCaretRow && !mTextField.hDoc.isWordWrap()) {
                //pasted text only affects caret row
                mTextField.invalidateRows(invalidateStartRow, invalidateStartRow + 1);
            } else {
                //TODO invalidate damaged rows only
                mTextField.invalidateFromRow(invalidateStartRow);
            }
        }
    }

    /**
     * Deletes selected text, exits select mode and invalidates deleted area.
     * If the selected range is empty, this method exits select mode and
     * invalidates the caret.
     * <p>
     * Does nothing if not in select mode.
     */
    public void selectionDelete() {
        if (!_isInSelectionMode) {
            return;
        }

        int totalChars = mTextField.mSelectionEdge - mTextField.mSelectionAnchor;

        if (totalChars > 0) {
            int originalRow = mTextField.hDoc.findRowNumber(mTextField.mSelectionAnchor);
            int originalOffset = mTextField.hDoc.getRowOffset(originalRow);
            boolean isSingleRowSel = mTextField.hDoc.findRowNumber(mTextField.mSelectionEdge) == originalRow;
            mTextField.hDoc.deleteAt(mTextField.mSelectionAnchor, totalChars, System.nanoTime());
            mTextField.mTextListener.onDel("", mTextField.mCaretPosition, totalChars);
            mTextField.mCaretPosition = mTextField.mSelectionAnchor;
            updateCaretRow();
            mTextField.setEdited(true);
            determineSpans();
            setSelectText(false);
            stopTextComposing();

            if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
                int invalidateStartRow = originalRow;
                //invalidate previous row too if its wrapping changed
                if (mTextField.hDoc.isWordWrap() &&
                        originalOffset != mTextField.hDoc.getRowOffset(originalRow)) {
                    --invalidateStartRow;
                }

                if (isSingleRowSel && !mTextField.hDoc.isWordWrap()) {
                    //pasted text only affects current row
                    mTextField.invalidateRows(invalidateStartRow, invalidateStartRow + 1);
                } else {
                    //TODO invalidate damaged rows only
                    mTextField.invalidateFromRow(invalidateStartRow);
                }
            }
        } else {
            setSelectText(false);
            mTextField.invalidateCaretRow();
        }
    }

    void replaceText(int from, int charCount, String text) {
        int invalidateStartRow, originalOffset;
        boolean isInvalidateSingleRow = true;
        boolean dirty = false;
        //delete selection
        if (_isInSelectionMode) {
            invalidateStartRow = mTextField.hDoc.findRowNumber(mTextField.mSelectionAnchor);
            originalOffset = mTextField.hDoc.getRowOffset(invalidateStartRow);

            int totalChars = mTextField.mSelectionEdge - mTextField.mSelectionAnchor;

            if (totalChars > 0) {
                mTextField.mCaretPosition = mTextField.mSelectionAnchor;
                mTextField.hDoc.deleteAt(mTextField.mSelectionAnchor, totalChars, System.nanoTime());

                if (invalidateStartRow != mTextField.mCaretRow) {
                    isInvalidateSingleRow = false;
                }
                dirty = true;
            }

            setSelectText(false);
        } else {
            invalidateStartRow = mTextField.mCaretRow;
            originalOffset = mTextField.hDoc.getRowOffset(mTextField.mCaretRow);
        }

        //delete requested chars
        if (charCount > 0) {
            int delFromRow = mTextField.hDoc.findRowNumber(from);
            if (delFromRow < invalidateStartRow) {
                invalidateStartRow = delFromRow;
                originalOffset = mTextField.hDoc.getRowOffset(delFromRow);
            }

            if (invalidateStartRow != mTextField.mCaretRow) {
                isInvalidateSingleRow = false;
            }

            mTextField.mCaretPosition = from;
            mTextField.hDoc.deleteAt(from, charCount, System.nanoTime());
            dirty = true;
        }

        //insert
        if (text != null && text.length() > 0) {
            int insFromRow = mTextField.hDoc.findRowNumber(from);
            if (insFromRow < invalidateStartRow) {
                invalidateStartRow = insFromRow;
                originalOffset = mTextField.hDoc.getRowOffset(insFromRow);
            }

            mTextField.hDoc.insertBefore(text.toCharArray(), mTextField.mCaretPosition, System.nanoTime());
            mTextField.mCaretPosition += text.length();
            dirty = true;
        }

        if (dirty) {
            mTextField.setEdited(true);
            determineSpans();
        }

        int originalRow = mTextField.mCaretRow;
        updateCaretRow();
        if (originalRow != mTextField.mCaretRow) {
            isInvalidateSingleRow = false;
        }

        if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
            //invalidate previous row too if its wrapping changed
            if (mTextField.hDoc.isWordWrap() &&
                    originalOffset != mTextField.hDoc.getRowOffset(invalidateStartRow)) {
                --invalidateStartRow;
            }

            if (isInvalidateSingleRow && !mTextField.hDoc.isWordWrap()) {
                //replaced text only affects current row
                mTextField.invalidateRows(mTextField.mCaretRow, mTextField.mCaretRow + 1);
            } else {
                //TODO invalidate damaged rows only
                mTextField.invalidateFromRow(invalidateStartRow);
            }
        }
    }

    //- TextFieldController -----------------------------------------------
    //----------------- Helper methods for InputConnection ----------------

    /**
     * Deletes existing selected text, then deletes charCount number of
     * characters starting at from, and inserts text in its place.
     * <p>
     * Unlike paste or selectionDelete, does not signal the end of
     * text composing to the IME.
     */
    void replaceComposingText(int from, int charCount, String text) {
        int invalidateStartRow, originalOffset;
        boolean isInvalidateSingleRow = true;
        boolean dirty = false;

        //delete selection
        if (_isInSelectionMode) {
            invalidateStartRow = mTextField.hDoc.findRowNumber(mTextField.mSelectionAnchor);
            originalOffset = mTextField.hDoc.getRowOffset(invalidateStartRow);

            int totalChars = mTextField.mSelectionEdge - mTextField.mSelectionAnchor;

            if (totalChars > 0) {
                mTextField.mCaretPosition = mTextField.mSelectionAnchor;
                mTextField.hDoc.deleteAt(mTextField.mSelectionAnchor, totalChars, System.nanoTime());

                if (invalidateStartRow != mTextField.mCaretRow) {
                    isInvalidateSingleRow = false;
                }
                dirty = true;
            }

            setSelectText(false);
        } else {
            invalidateStartRow = mTextField.mCaretRow;
            originalOffset = mTextField.hDoc.getRowOffset(mTextField.mCaretRow);
        }

        //delete requested chars
        if (charCount > 0) {
            int delFromRow = mTextField.hDoc.findRowNumber(from);
            if (delFromRow < invalidateStartRow) {
                invalidateStartRow = delFromRow;
                originalOffset = mTextField.hDoc.getRowOffset(delFromRow);
            }

            if (invalidateStartRow != mTextField.mCaretRow) {
                isInvalidateSingleRow = false;
            }

            mTextField.mCaretPosition = from;
            mTextField.hDoc.deleteAt(from, charCount, System.nanoTime());
            dirty = true;
        }

        //insert
        if (text != null && text.length() > 0) {
            int insFromRow = mTextField.hDoc.findRowNumber(from);
            if (insFromRow < invalidateStartRow) {
                invalidateStartRow = insFromRow;
                originalOffset = mTextField.hDoc.getRowOffset(insFromRow);
            }

            log("inserted text:" + text);
            mTextField.hDoc.insertBefore(text.toCharArray(), mTextField.mCaretPosition, System.nanoTime());
            mTextField.mCaretPosition += text.length();
            dirty = true;

        }

        mTextField.mTextListener.onAdd(text, mTextField.mCaretPosition, text.length() - charCount);
        if (dirty) {
            mTextField.setEdited(true);
            determineSpans();
        }

        int originalRow = mTextField.mCaretRow;
        updateCaretRow();
        if (originalRow != mTextField.mCaretRow) {
            isInvalidateSingleRow = false;
        }

        if (!mTextField.makeCharVisible(mTextField.mCaretPosition)) {
            //invalidate previous row too if its wrapping changed
            if (mTextField.hDoc.isWordWrap() &&
                    originalOffset != mTextField.hDoc.getRowOffset(invalidateStartRow)) {
                --invalidateStartRow;
            }

            if (isInvalidateSingleRow && !mTextField.hDoc.isWordWrap()) {
                //replaced text only affects current row
                mTextField.invalidateRows(mTextField.mCaretRow, mTextField.mCaretRow + 1);
            } else {
                //TODO invalidate damaged rows only
                mTextField.invalidateFromRow(invalidateStartRow);
            }
        }
    }

    /**
     * Delete leftLength characters of text before the current caret
     * position, and delete rightLength characters of text after the current
     * cursor position.
     * <p>
     * Unlike paste or selectionDelete, does not signal the end of
     * text composing to the IME.
     */
    void deleteAroundComposingText(int left, int right) {
        int start = mTextField.mCaretPosition - left;
        if (start < 0) {
            start = 0;
        }
        int end = mTextField.mCaretPosition + right;
        int docLength = mTextField.hDoc.docLength();
        if (end > (docLength - 1)) { //exclude the terminal EOF
            end = docLength - 1;
        }
        replaceComposingText(start, end - start, "");
    }

    String getTextAfterCursor(int maxLen) {
        int docLength = mTextField.hDoc.docLength();
        if ((mTextField.mCaretPosition + maxLen) > (docLength - 1)) {
            //exclude the terminal EOF
            return mTextField.hDoc.subSequence(mTextField.mCaretPosition, docLength - mTextField.mCaretPosition - 1).toString();
        }

        return mTextField.hDoc.subSequence(mTextField.mCaretPosition, maxLen).toString();
    }

    String getTextBeforeCursor(int maxLen) {
        int start = mTextField.mCaretPosition - maxLen;
        if (start < 0) {
            start = 0;
        }
        return mTextField.hDoc.subSequence(start, mTextField.mCaretPosition - start).toString();
    }
}//end inner controller class
