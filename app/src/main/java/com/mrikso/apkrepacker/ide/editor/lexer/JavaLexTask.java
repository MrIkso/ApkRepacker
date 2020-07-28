package com.mrikso.apkrepacker.ide.editor.lexer;

import com.a4455jkjh.lexer.JavaLexer;
import com.a4455jkjh.lexer.JavaParser;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.Pair;
import com.mrikso.codeeditor.view.ColorScheme;


import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public class JavaLexTask extends Antlr4LexTask<JavaLexer> {
    private final JavaParser parser;
    private JavaParser.CompilationUnitContext unit;

    public JavaLexTask() {
        super(JavaLexer.VOCABULARY);
        unit = null;
        parser = new JavaParser(null);
    }

    @Override
    protected JavaLexer generateLexer() {
        return new JavaLexer(null);
    }

    @Override
    protected void tokenize(List<Pair> _tokens, JavaLexer lexer) {
        boolean imp = false;
        unit = null;
        while (!abort) {
            Token token = lexer.nextToken();
            int tokenType = token.getType();
            if (tokenType == -1)
                break;
            ColorScheme.Colorable type;
            switch (tokenType) {
                case JavaLexer.PUBLIC:
                case JavaLexer.ABSTRACT:
                case JavaLexer.ASSERT:
                case JavaLexer.BREAK:
                case JavaLexer.CASE:
                case JavaLexer.CATCH:
                case JavaLexer.CONTINUE:
                case JavaLexer.CLASS:
                case JavaLexer.CONST:
                case JavaLexer.DEFAULT:
                case JavaLexer.DO:
                case JavaLexer.ELSE:
                case JavaLexer.ENUM:
                case JavaLexer.EXTENDS:
                case JavaLexer.FINAL:
                case JavaLexer.FINALLY:
                case JavaLexer.FOR:
                case JavaLexer.GOTO:
                case JavaLexer.IF:
                case JavaLexer.IMPLEMENTS:
                case JavaLexer.INSTANCEOF:
                case JavaLexer.INTERFACE:
                case JavaLexer.NATIVE:
                case JavaLexer.NEW:
                case JavaLexer.PRIVATE:
                case JavaLexer.PROTECTED:
                case JavaLexer.RETURN:
                case JavaLexer.STATIC:
                case JavaLexer.STRICTFP:
                case JavaLexer.SUPER:
                case JavaLexer.SWITCH:
                case JavaLexer.SYNCHRONIZED:
                case JavaLexer.THIS:
                case JavaLexer.THROW:
                case JavaLexer.THROWS:
                case JavaLexer.TRANSIENT:
                case JavaLexer.TRY:
                case JavaLexer.VOLATILE:
                case JavaLexer.WHILE:
                   // type = ColorScheme.Colorable.KEYWORD;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.TYPE));
                    break;
                case JavaLexer.IMPORT:
                case JavaLexer.PACKAGE:
                    //type = ColorScheme.Colorable.KEYWORD;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.KEYWORD));
                    imp = true;
                    break;
                case JavaLexer.BINARY_LITERAL:
                case JavaLexer.DECIMAL_LITERAL:
                case JavaLexer.HEX_FLOAT_LITERAL:
                case JavaLexer.HEX_LITERAL:
                case JavaLexer.FLOAT_LITERAL:
                case JavaLexer.OCT_LITERAL:
                case JavaLexer.STRING_LITERAL:
                case JavaLexer.CHAR_LITERAL:
                case JavaLexer.BOOL_LITERAL:
                case JavaLexer.NULL_LITERAL:
                  //  type = ColorScheme.Colorable.LITERAL;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.LITERAL));
                    break;
                case JavaLexer.BOOLEAN:
                case JavaLexer.BYTE:
                case JavaLexer.CHAR:
                case JavaLexer.FLOAT:
                case JavaLexer.DOUBLE:
                case JavaLexer.INT:
                case JavaLexer.LONG:
                case JavaLexer.SHORT:
                case JavaLexer.VOID:
                    //type = ColorScheme.Colorable.TYPE;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.TYPE));
                    break;
                case JavaLexer.SEMI:
                   // type = ColorScheme.Colorable.SEPARATOR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    imp = false;
                    break;
                case JavaLexer.ARROW:
                case JavaLexer.COLONCOLON:
                case JavaLexer.ELLIPSIS:
                case JavaLexer.ASSIGN:
                case JavaLexer.GT:
                case JavaLexer.LT:
                case JavaLexer.BANG:
                case JavaLexer.TILDE:
                case JavaLexer.QUESTION:
                case JavaLexer.COLON:
                case JavaLexer.EQUAL:
                case JavaLexer.LE:
                case JavaLexer.GE:
                case JavaLexer.NOTEQUAL:
                case JavaLexer.AND:
                case JavaLexer.OR:
                case JavaLexer.INC:
                case JavaLexer.DEC:
                case JavaLexer.ADD:
                case JavaLexer.SUB:
                case JavaLexer.DIV:
                case JavaLexer.BITOR:
                case JavaLexer.BITAND:
                case JavaLexer.CARET:
                case JavaLexer.MOD:
                case JavaLexer.ADD_ASSIGN:
                case JavaLexer.SUB_ASSIGN:
                case JavaLexer.MUL_ASSIGN:
                case JavaLexer.DIV_ASSIGN:
                case JavaLexer.AND_ASSIGN:
                case JavaLexer.OR_ASSIGN:
                case JavaLexer.XOR_ASSIGN:
                case JavaLexer.MOD_ASSIGN:
                case JavaLexer.LSHIFT_ASSIGN:
                case JavaLexer.RSHIFT_ASSIGN:
                case JavaLexer.URSHIFT_ASSIGN:
                case JavaLexer.AT:
                    //type = ColorScheme.Colorable.OPERATOR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.OPERATOR));
                    break;
                case JavaLexer.LPAREN:
                case JavaLexer.RPAREN:
                case JavaLexer.LBRACE:
                case JavaLexer.RBRACE:
                case JavaLexer.LBRACK:
                case JavaLexer.RBRACK:
                case JavaLexer.COMMA:
                case JavaLexer.DOT:
                   // type = ColorScheme.Colorable.SEPARATOR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case JavaLexer.MUL:
                    if (imp)
                        //type = ColorScheme.Colorable.PACKAGE;
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.PACKAGE));
                    else
                        //type = ColorScheme.Colorable.OPERATOR;
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.OPERATOR));
                    break;
                case JavaLexer.IDENTIFIER:
                    if (imp)
                       // type = ColorScheme.Colorable.PACKAGE;
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.PACKAGE));
                    else
                        //type = ColorScheme.Colorable.NAME;
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.NAME));
                    break;
                case JavaLexer.LINE_COMMENT:
                case JavaLexer.COMMENT:
                    //type = ColorScheme.Colorable.COMMENT;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.COMMENT));
                    break;
                case 0:
                    //type = ColorScheme.Colorable.ERROR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.ERROR));
                    break;
                default:
                    //type = ColorScheme.Colorable.NAME;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.NORMAL));
                    break;
            }
          //  Pair pair = new Pair(token.getStopIndex(), ColorScheme.getColor(type));
           // _tokens.add(pair);
        }
    }

    @Override
    protected void parse(JavaLexer lexer) {
        CommonTokenStream tks = new CommonTokenStream(lexer);
        parser.setTokenStream(tks);
        unit = parser.compilationUnit();
    }
    @Override
    public String getLanguageType(){
        return "Java";
    }
    @Override
    protected ParseTree getTree() {
        return unit;
    }
}
