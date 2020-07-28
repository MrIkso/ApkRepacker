package com.mrikso.apkrepacker.ide.editor.lexer;

import com.a4455jkjh.lexer.css3Lexer;
import com.a4455jkjh.lexer.css3Parser;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.Pair;
import com.mrikso.codeeditor.view.ColorScheme;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

import static com.a4455jkjh.lexer.css3Lexer.And;
import static com.a4455jkjh.lexer.css3Lexer.BitOr;
import static com.a4455jkjh.lexer.css3Lexer.Calc;
import static com.a4455jkjh.lexer.css3Lexer.Cdc;
import static com.a4455jkjh.lexer.css3Lexer.Cdo;
import static com.a4455jkjh.lexer.css3Lexer.Charset;
import static com.a4455jkjh.lexer.css3Lexer.Colon;
import static com.a4455jkjh.lexer.css3Lexer.Comma;
import static com.a4455jkjh.lexer.css3Lexer.Comment;
import static com.a4455jkjh.lexer.css3Lexer.CounterStyle;
import static com.a4455jkjh.lexer.css3Lexer.DashMatch;
import static com.a4455jkjh.lexer.css3Lexer.Dimension;
import static com.a4455jkjh.lexer.css3Lexer.Dot;
import static com.a4455jkjh.lexer.css3Lexer.DxImageTransform;
import static com.a4455jkjh.lexer.css3Lexer.FontFace;
import static com.a4455jkjh.lexer.css3Lexer.FontFeatureValues;
import static com.a4455jkjh.lexer.css3Lexer.From;
import static com.a4455jkjh.lexer.css3Lexer.Function;
import static com.a4455jkjh.lexer.css3Lexer.Greater;
import static com.a4455jkjh.lexer.css3Lexer.Hash;
import static com.a4455jkjh.lexer.css3Lexer.Import;
import static com.a4455jkjh.lexer.css3Lexer.Includes;
import static com.a4455jkjh.lexer.css3Lexer.Keyframes;
import static com.a4455jkjh.lexer.css3Lexer.LBrace;
import static com.a4455jkjh.lexer.css3Lexer.LBrack;
import static com.a4455jkjh.lexer.css3Lexer.LParen;
import static com.a4455jkjh.lexer.css3Lexer.Media;
import static com.a4455jkjh.lexer.css3Lexer.MediaOnly;
import static com.a4455jkjh.lexer.css3Lexer.Minus;
import static com.a4455jkjh.lexer.css3Lexer.Namespace;
import static com.a4455jkjh.lexer.css3Lexer.Not;
import static com.a4455jkjh.lexer.css3Lexer.Number;
import static com.a4455jkjh.lexer.css3Lexer.Or;
import static com.a4455jkjh.lexer.css3Lexer.Page;
import static com.a4455jkjh.lexer.css3Lexer.Percentage;
import static com.a4455jkjh.lexer.css3Lexer.Plus;
import static com.a4455jkjh.lexer.css3Lexer.PrefixMatch;
import static com.a4455jkjh.lexer.css3Lexer.PseudoNot;
import static com.a4455jkjh.lexer.css3Lexer.RBrace;
import static com.a4455jkjh.lexer.css3Lexer.RParen;
import static com.a4455jkjh.lexer.css3Lexer.Semi;
import static com.a4455jkjh.lexer.css3Lexer.Star;
import static com.a4455jkjh.lexer.css3Lexer.String;
import static com.a4455jkjh.lexer.css3Lexer.SubstringMatch;
import static com.a4455jkjh.lexer.css3Lexer.SuffixMatch;
import static com.a4455jkjh.lexer.css3Lexer.Supports;
import static com.a4455jkjh.lexer.css3Lexer.Tilde;
import static com.a4455jkjh.lexer.css3Lexer.To;
import static com.a4455jkjh.lexer.css3Lexer.UnderScroll;
import static com.a4455jkjh.lexer.css3Lexer.Uri;
import static com.a4455jkjh.lexer.css3Lexer.Var;
import static com.a4455jkjh.lexer.css3Lexer.Viewport;

public class CssLexTask extends Antlr4LexTask<css3Lexer> {
    private final css3Parser parser;
    private css3Parser.StylesheetContext unit;

    public CssLexTask() {
        super(css3Lexer.VOCABULARY);
        parser = new css3Parser(null);
    }
    @Override
    public String getLanguageType(){
        return "Css";
    }
    @Override
    protected void tokenize(List<Pair> _tokens, css3Lexer lexer) {
        unit = null;
        while (!abort) {
            Token token = lexer.nextToken();
            int tokenType = token.getType();
            if (tokenType == -1)
                break;
            ColorScheme.Colorable type;
            switch (tokenType) {
                case Comment:
                   // type = ColorScheme.Colorable.COMMENT;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.COMMENT));
                    break;
                case LParen:
                case LBrace:
                case LBrack:
                case RParen:
                case RBrace:
                case Colon:
                case Comma:
                case Semi:
                case Dot:
                case UnderScroll:
                   // type = ColorScheme.Colorable.SEPARATOR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case Number:
                case String:
                case Uri:
                case Dimension:
                case Percentage:
                case Hash:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.LITERAL));
                    break;
                case MediaOnly:
                case Not:
                case And:
                case PseudoNot:
                case Or:
                case FontFace:
                case Supports:
                case Keyframes:
                case From:
                case To:
                case Viewport:
                case CounterStyle:
                case FontFeatureValues:
                case Media:
                case Import:
                case Page:
                case Namespace:
                case Charset:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.KEYWORD));
                    break;
                case Calc:
                case DxImageTransform:
                case Var:
                case Function:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.PACKAGE));
                    break;
                case Plus:
                case Minus:
                case Greater:
                case Tilde:
                case PrefixMatch:
                case SuffixMatch:
                case SubstringMatch:
                case Star:
                case BitOr:
                case Cdo:
                case Cdc:
                case Includes:
                case DashMatch:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.OPERATOR));
                    break;
                default:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.NORMAL));
                    break;
            }
           // Pair pair = new Pair(token.getStopIndex(), ColorScheme.getColor(type));
           // _tokens.add(pair);
        }
    }

    @Override
    protected void parse(css3Lexer lexer) {
        CommonTokenStream tks = new CommonTokenStream(lexer);
        parser.setTokenStream(tks);
        unit = parser.stylesheet();
    }

    @Override
    protected ParseTree getTree() {
        return unit;
    }

    @Override
    protected css3Lexer generateLexer() {
        return new css3Lexer(null);
    }


}
