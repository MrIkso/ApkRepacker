package com.mrikso.apkrepacker.ide.editor.lexer;

import com.a4455jkjh.lexer.XMLLexer;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.Pair;
import com.mrikso.codeeditor.view.ColorScheme;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.util.List;

import static com.a4455jkjh.lexer.XMLLexer.COMMENT;
import static com.a4455jkjh.lexer.XMLLexer.DTD;
import static com.a4455jkjh.lexer.XMLLexer.EOF;
import static com.a4455jkjh.lexer.XMLLexer.EQUALS;
import static com.a4455jkjh.lexer.XMLLexer.Name;
import static com.a4455jkjh.lexer.XMLLexer.OPEN;
import static com.a4455jkjh.lexer.XMLLexer.SLASH;
import static com.a4455jkjh.lexer.XMLLexer.SLASH_CLOSE;
import static com.a4455jkjh.lexer.XMLLexer.SPECIAL_CLOSE;
import static com.a4455jkjh.lexer.XMLLexer.STRING;
import static com.a4455jkjh.lexer.XMLLexer.TEXT;

public class HtmlLexTask extends XmlLexTask {
    public HtmlLexTask() {
       // super(item);
    }
    @Override
    public String getLanguageType(){
        return "Html";
    }

    @Override
    protected void tokenize(List<Pair> _tokens, XMLLexer lexer) {
        int lastType = 0;
        String lastTag = null;
        while (!abort) {
            int start = lexer.getCharIndex();
            Token token = lexer.nextToken();
            int tokenType = token.getType();
            if (tokenType == EOF)
                break;
            ColorScheme.Colorable type;
            switch (tokenType) {
                case XMLLexer.XMLDeclOpen:
                    parse(lexer, SPECIAL_CLOSE);
                case DTD:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.PACKAGE));
                    break;
                case COMMENT:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.COMMENT));
                    break;
                case OPEN:
                case XMLLexer.CLOSE:
                case SLASH_CLOSE:
                case SLASH:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case Name:
                    if (lastType == OPEN) {
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.KEYWORD));
                        lastTag = token.getText();
                    } else if (lastType == SLASH) {
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.KEYWORD));
                        lastTag = null;
                    } else
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.TYPE));
                    break;
                case STRING:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.LITERAL));
                    break;
                case EQUALS:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.OPERATOR));
                    break;
                case TEXT:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.NORMAL));
                    break;
                default:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.NORMAL));
                    break;
            }
            lastType = tokenType;
            //Pair pair = new Pair(lexer.getCharIndex() - start, ColorScheme.getColor(type));
            // _tokens.add(pair);
        }
        lexer.reset();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser.setTokenStream(tokens);
        document = parser.document();
    }
}
