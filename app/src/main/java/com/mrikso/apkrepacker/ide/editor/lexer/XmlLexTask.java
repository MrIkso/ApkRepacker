package com.mrikso.apkrepacker.ide.editor.lexer;

import android.util.Log;

import com.a4455jkjh.lexer.XMLLexer;
import com.a4455jkjh.lexer.XMLParser;
import com.a4455jkjh.lexer.XMLParser.AttributeContext;
import com.a4455jkjh.lexer.XMLParserBaseVisitor;
import com.mrikso.codeeditor.lang.Language;
import com.mrikso.codeeditor.util.IndentStringBuilder;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.Pair;
import com.mrikso.codeeditor.view.ColorScheme;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.File;
import java.util.List;

import static com.a4455jkjh.lexer.XMLLexer.CLOSE;
import static com.a4455jkjh.lexer.XMLLexer.COMMENT;
import static com.a4455jkjh.lexer.XMLLexer.DTD;
import static com.a4455jkjh.lexer.XMLLexer.EOF;
import static com.a4455jkjh.lexer.XMLLexer.EQUALS;
import static com.a4455jkjh.lexer.XMLLexer.Name;
import static com.a4455jkjh.lexer.XMLLexer.OPEN;
import static com.a4455jkjh.lexer.XMLLexer.SEA_WS;
import static com.a4455jkjh.lexer.XMLLexer.SLASH;
import static com.a4455jkjh.lexer.XMLLexer.SLASH_CLOSE;
import static com.a4455jkjh.lexer.XMLLexer.SPECIAL_CLOSE;
import static com.a4455jkjh.lexer.XMLLexer.STRING;
import static com.a4455jkjh.lexer.XMLLexer.XMLDeclOpen;

public class XmlLexTask extends Antlr4LexTask<XMLLexer> {
    protected final XMLParser parser;
    private final XMLLexer lexer;
    protected XMLParser.DocumentContext document;

    private File item;

    public XmlLexTask() {
        super(XMLLexer.VOCABULARY);
       // this.item = item;
        parser = new XMLParser(null);
        lexer = new XMLLexer(null);
        if (item != null) {
           // parser.removeErrorListeners();
           // parser.addErrorListener(item);
        }
        document = null;
    }
    @Override
    public String getLanguageType(){
        return "Xml";
    }
    @Override
    protected ParseTree getTree() {
        return document;
    }

    @Override
    protected XMLLexer generateLexer() {
        XMLLexer xMLLexer = new XMLLexer(null);
        if (item != null) {
          //  xMLLexer.removeErrorListeners();
         //   xMLLexer.addErrorListener(item);
        }
        return xMLLexer;
    }

    @Override
    protected void tokenize(List<Pair> _tokens, XMLLexer lexer) {
        int lastType = 0;
        document = null;
        while (!abort) {
            Token token = lexer.nextToken();
            int tokenType = token.getType();
            if (tokenType == EOF)
                break;
            ColorScheme.Colorable type;
            switch (tokenType) {
                case XMLDeclOpen:
                    parse(lexer, SPECIAL_CLOSE);
                case DTD:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.PACKAGE));
                    break;
                case COMMENT:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.COMMENT));
                    break;
                case OPEN:
                case CLOSE:
                case SLASH_CLOSE:
                case SLASH:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case Name:
                    if (lastType == OPEN || lastType == SLASH)
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.KEYWORD));
                    else
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.TYPE));
                    break;
                case STRING:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.LITERAL));
                    break;
                case EQUALS:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.OPERATOR));
                    break;
                default:
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.NAME));
                    break;
            }
            lastType = tokenType;
          //  Pair pair = new Pair(token.getStopIndex(), ColorScheme.getColor(type));
         //   _tokens.add(pair);
        }
    }

    @Override
    protected void parse(XMLLexer lexer) {
        Log.i("APKTOOL PARSE", "parse xml");
        //item.reset();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser.setTokenStream(tokens);
        document = parser.document();
    }

    @Override
    protected boolean canAnalysis() {
        return item != null;
    }


    @Override
    public boolean canFormat() {
        return true;
    }

    @Override
    protected void parse(ANTLRInputStream i) {
        lexer.setInputStream(i);
        parse(lexer);
    }

    protected void parse(XMLLexer lexer, int endType) {
        while (true) {
            if (lexer.nextToken().getType() == endType)
                break;
        }
    }

    @Override
    protected int format(IndentStringBuilder sb, XMLLexer lexer, int width, int curPos) {
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        parser.setTokenStream(tokens);
        XMLFormatter f = new XMLFormatter(sb, width, curPos);
        parser.document().accept(f);
        return f.newPos;
    }

    private static class LanguageXml extends Language {
        LanguageXml() {
            setKeywords(new String[0]);
        }
    }

    private class XMLFormatter extends XMLParserBaseVisitor<Void> {
        int newPos;
        private IndentStringBuilder sb;
        private int width;
        private int curPos;
        private boolean shouldNewLine = false;

        public XMLFormatter(IndentStringBuilder sb, int width, int curPos) {
            this.sb = sb;
            this.width = width;
            this.curPos = curPos;
            newPos = curPos;
        }

        @Override
        public Void visitElement(XMLParser.ElementContext ctx) {
            sb.append('<');
            sb.indent(width);
            ctx.Name(0).accept(this);
            List<AttributeContext> attrs = ctx.attribute();
            shouldNewLine = attrs.size() > 1;
            for (AttributeContext attr : attrs)
                attr.accept(this);
            XMLParser.ContentContext content = ctx.content();
            if (content == null) {
                sb.append("/>");
                sb.deindent(width);
            } else {
                sb.append('>');
                content.accept(this);
                sb.deindent(width);
                sb.append("</");
                visitTerminal(ctx.Name(1));
                //ctx.Name(1).accept(this);
                sb.append('>');
            }
            return null;
        }

        @Override
        public Void visitAttribute(XMLParser.AttributeContext ctx) {
            sb.append(shouldNewLine ? '\n' : ' ');
            ctx.Name().accept(this);
            sb.append('=');
            ctx.STRING().accept(this);
            return null;
        }

        @Override
        public Void visitTerminal(TerminalNode node) {
            if (node == null)
                return null;
            Token token = node.getSymbol();
            String text = token.getText();
            int type = token.getType();
            switch (type) {
                case SEA_WS:
                    SmaliFormater.processWhiteSpace(sb, text);
                    break;
                case SPECIAL_CLOSE:
                    sb.append(' ');
                    sb.append(text);
                    break;
                default:
                    sb.append(text);
                    break;
            }
            int start = token.getStartIndex();
            int end = token.getStopIndex();
            newPos = compute(sb.length(), start, end, curPos, newPos);
            return null;
        }
    }

}
