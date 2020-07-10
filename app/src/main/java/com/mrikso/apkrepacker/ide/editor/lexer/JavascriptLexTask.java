package com.mrikso.apkrepacker.ide.editor.lexer;

import com.a4455jkjh.lexer.JavaScriptLexer;
import com.a4455jkjh.lexer.JavaScriptParser;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.Pair;
import com.mrikso.codeeditor.view.ColorScheme;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

import static com.a4455jkjh.lexer.JavaScriptLexer.Class;
import static com.a4455jkjh.lexer.JavaScriptLexer.Package;
import static com.a4455jkjh.lexer.JavaScriptLexer.Void;

public class JavascriptLexTask extends Antlr4LexTask<JavaScriptLexer> {
    private final JavaScriptParser parser;
    private JavaScriptParser.ProgramContext program;

    public JavascriptLexTask() {
        super(JavaScriptLexer.VOCABULARY);
        parser = new JavaScriptParser(null);
    }
    @Override
    public String getLanguageType(){
        return "Js";
    }
    @Override
    protected JavaScriptLexer generateLexer() {
        return new JavaScriptLexer(null);
    }

    @Override
    protected void tokenize(List<Pair> _tokens, JavaScriptLexer lexer) {
        program = null;
        while (!abort) {
            Token token = lexer.nextToken();
            int tokenType = token.getType();
            if (tokenType == -1)
                break;
            ColorScheme.Colorable type;
            switch (tokenType) {
                case JavaScriptLexer.MultiLineComment:
                case JavaScriptLexer.SingleLineComment:
                case JavaScriptLexer.HtmlComment:
                case JavaScriptLexer.CDataComment:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.COMMENT));
                    break;
                case JavaScriptLexer.OpenBracket:
                case JavaScriptLexer.OpenParen:
                case JavaScriptLexer.OpenBrace:
                case JavaScriptLexer.CloseBracket:
                case JavaScriptLexer.CloseParen:
                case JavaScriptLexer.CloseBrace:
                case JavaScriptLexer.SemiColon:
                case JavaScriptLexer.Comma:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case JavaScriptLexer.Assign:
                case JavaScriptLexer.QuestionMark:
                case JavaScriptLexer.Colon:
                case JavaScriptLexer.Ellipsis:
                case JavaScriptLexer.Dot:
                case JavaScriptLexer.PlusPlus:
                case JavaScriptLexer.MinusMinus:
                case JavaScriptLexer.Plus:
                case JavaScriptLexer.Minus:
                case JavaScriptLexer.BitNot:
                case JavaScriptLexer.Not:
                case JavaScriptLexer.Multiply:
                case JavaScriptLexer.Divide:
                case JavaScriptLexer.Modulus:
                case JavaScriptLexer.RightShiftArithmetic:
                case JavaScriptLexer.LeftShiftArithmetic:
                case JavaScriptLexer.RightShiftLogical:
                case JavaScriptLexer.LessThan:
                case JavaScriptLexer.MoreThan:
                case JavaScriptLexer.LessThanEquals:
                case JavaScriptLexer.GreaterThanEquals:
                case JavaScriptLexer.Equals_:
                case JavaScriptLexer.NotEquals:
                case JavaScriptLexer.IdentityEquals:
                case JavaScriptLexer.IdentityNotEquals:
                case JavaScriptLexer.BitAnd:
                case JavaScriptLexer.BitXOr:
                case JavaScriptLexer.BitOr:
                case JavaScriptLexer.And:
                case JavaScriptLexer.Or:
                case JavaScriptLexer.MultiplyAssign:
                case JavaScriptLexer.DivideAssign:
                case JavaScriptLexer.ModulusAssign:
                case JavaScriptLexer.PlusAssign:
                case JavaScriptLexer.MinusAssign:
                case JavaScriptLexer.LeftShiftArithmeticAssign:
                case JavaScriptLexer.RightShiftArithmeticAssign:
                case JavaScriptLexer.RightShiftLogicalAssign:
                case JavaScriptLexer.BitAndAssign:
                case JavaScriptLexer.BitXorAssign:
                case JavaScriptLexer.BitOrAssign:
                case JavaScriptLexer.ARROW:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.OPERATOR));
                    break;
                case JavaScriptLexer.NullLiteral:
                case JavaScriptLexer.BooleanLiteral:
                case JavaScriptLexer.DecimalLiteral:
                case JavaScriptLexer.HexIntegerLiteral:
                case JavaScriptLexer.OctalIntegerLiteral:
                case JavaScriptLexer.OctalIntegerLiteral2:
                case JavaScriptLexer.BinaryIntegerLiteral:
                case JavaScriptLexer.StringLiteral:
                case JavaScriptLexer.TemplateStringLiteral:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.LITERAL));
                    break;
                case JavaScriptLexer.Break:
                case JavaScriptLexer.Do:
                case JavaScriptLexer.Instanceof:
                case JavaScriptLexer.Typeof:
                case JavaScriptLexer.Case:
                case JavaScriptLexer.Else:
                case JavaScriptLexer.New:
                case JavaScriptLexer.Var:
                case JavaScriptLexer.Catch:
                case JavaScriptLexer.Finally:
                case JavaScriptLexer.Return:
                case Void:
                case JavaScriptLexer.Continue:
                case JavaScriptLexer.For:
                case JavaScriptLexer.Switch:
                case JavaScriptLexer.While:
                case JavaScriptLexer.Debugger:
                case JavaScriptLexer.Function:
                case JavaScriptLexer.This:
                case JavaScriptLexer.With:
                case JavaScriptLexer.Default:
                case JavaScriptLexer.If:
                case JavaScriptLexer.Throw:
                case JavaScriptLexer.Delete:
                case JavaScriptLexer.In:
                case JavaScriptLexer.Try:
                case Class:
                case JavaScriptLexer.Enum:
                case JavaScriptLexer.Extends:
                case JavaScriptLexer.Super:
                case JavaScriptLexer.Const:
                case JavaScriptLexer.Export:
                case JavaScriptLexer.Import:
                case JavaScriptLexer.Implements:
                case JavaScriptLexer.Let:
                case JavaScriptLexer.Private:
                case JavaScriptLexer.Public:
                case JavaScriptLexer.Interface:
                case Package:
                case JavaScriptLexer.Protected:
                case JavaScriptLexer.Static:
                case JavaScriptLexer.Yield:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.KEYWORD));
                    break;
                case JavaScriptLexer.UnexpectedCharacter:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.ERROR));
                    break;
                default:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.NAME));
                    break;
            }
           // Pair pair = new Pair(token.getStopIndex(), ColorScheme.getColor(type));
           // _tokens.add(pair);
        }
    }

    @Override
    protected void parse(JavaScriptLexer lexer) {
        CommonTokenStream tks = new CommonTokenStream(lexer);
        parser.setTokenStream(tks);
        program = parser.program();
    }


    @Override
    protected ParseTree getTree() {
        return program;
    }

}
