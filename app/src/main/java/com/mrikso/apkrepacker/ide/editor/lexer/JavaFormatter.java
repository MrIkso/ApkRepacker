package com.mrikso.apkrepacker.ide.editor.lexer;

import com.a4455jkjh.lexer.JavaParser;
import com.a4455jkjh.lexer.JavaParserBaseVisitor;
import com.mrikso.codeeditor.util.IndentStringBuilder;


import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

public class JavaFormatter extends JavaParserBaseVisitor<Object> {
    private static final ThreadLocal<JavaFormatter> formatters = new ThreadLocal<>();
    private CommonTokenStream tokens;
    private IndentStringBuilder sb;
    private int width;
    private JavaFormatter() {
    }

    private static JavaFormatter getInstance(CommonTokenStream t, IndentStringBuilder s, int w) {
        JavaFormatter f = formatters.get();
        if (f == null) {
            f = new JavaFormatter();
            formatters.set(f);
        }
        f.tokens = t;
        f.sb = s;
        f.width = w;
        return f;
    }

    public static void format(CommonTokenStream tokens, IndentStringBuilder sb, int width, JavaParser.CompilationUnitContext unit) {
        unit.accept(getInstance(tokens, sb, width));
    }

    private void writeComment(Token tk) {
        List<Token> left = tokens.getHiddenTokensToLeft(tk.getTokenIndex());
        if (left != null) {
            for (Token t : left) {
                int type = t.getType();
                if (type == JavaParser.LINE_COMMENT) {
                    sb.append(t.getText());
                    sb.append('\n');
                } else if (type == JavaParser.COMMENT) {
                    sb.append(t.getText());
                    sb.append('\n');
                }
            }
        }
    }

    @Override
    public Object visitCompilationUnit(JavaParser.CompilationUnitContext ctx) {

        return super.visitCompilationUnit(ctx);
    }

    @Override
    public Object visitTerminal(TerminalNode node) {

        return super.visitTerminal(node);
    }

}
