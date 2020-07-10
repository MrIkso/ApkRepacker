package com.mrikso.apkrepacker.ide.editor.lexer;

import com.a4455jkjh.lexer.JSONLexer;
import com.a4455jkjh.lexer.JSONParser;
import com.mrikso.codeeditor.util.IndentStringBuilder;
import com.mrikso.codeeditor.util.Lexer;
import com.mrikso.codeeditor.util.Pair;
import com.mrikso.codeeditor.view.ColorScheme;


import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

import static com.a4455jkjh.lexer.JSONLexer.COLON;
import static com.a4455jkjh.lexer.JSONLexer.COMMA;
import static com.a4455jkjh.lexer.JSONLexer.FALSE;
import static com.a4455jkjh.lexer.JSONLexer.LBRACE;
import static com.a4455jkjh.lexer.JSONLexer.LBRACK;
import static com.a4455jkjh.lexer.JSONLexer.NULL;
import static com.a4455jkjh.lexer.JSONLexer.NUMBER;
import static com.a4455jkjh.lexer.JSONLexer.RBRACE;
import static com.a4455jkjh.lexer.JSONLexer.RBRACK;
import static com.a4455jkjh.lexer.JSONLexer.STRING;
import static com.a4455jkjh.lexer.JSONLexer.TRUE;
import static com.a4455jkjh.lexer.JSONLexer.WS;

public class JsonLexTask extends Antlr4LexTask<JSONLexer> {
    private final JSONParser parser;
    private JSONParser.JsonContext json;

    public JsonLexTask() {
        super(new NormalLanguage());
        parser = new JSONParser(null);
        json = null;
    }
    @Override
    public String getLanguageType(){
        return "Json";
    }

    @Override
    protected JSONLexer generateLexer() {
        return new JSONLexer(null);
    }

    @Override
    protected void tokenize(List<Pair> _tokens, JSONLexer lexer) {
        boolean key = false;
        boolean isArray = false;
        json = null;
        while (!abort) {
            Token token = lexer.nextToken();
            int tokenType = token.getType();
            if (tokenType == -1)
                break;
            ColorScheme.Colorable type;
            switch (tokenType) {
                case LBRACE:
                case COMMA:
                    key = true;
                   // type = ColorScheme.Colorable.SEPARATOR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case LBRACK:
                    isArray = true;
                    //type = ColorScheme.Colorable.SEPARATOR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case RBRACE:
                case RBRACK:
                case COLON:
                    isArray = false;
                    key = false;
                   // type = ColorScheme.Colorable.SEPARATOR;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.SEPARATOR));
                    break;
                case TRUE:
                case FALSE:
                case NULL:
                case NUMBER:
                    //type = ColorScheme.Colorable.LITERAL;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.LITERAL));
                    break;
                case STRING:
                    if (key && !isArray)
                      //  type = ColorScheme.Colorable.KEYWORD;
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.KEYWORD));
                    else
                      //  type = ColorScheme.Colorable.LITERAL;
                        _tokens.add(new Pair(token.getStopIndex(), Lexer.LITERAL));
                    break;
                default:
                   // type = ColorScheme.Colorable.NAME;
                    _tokens.add(new Pair(token.getStopIndex(), Lexer.NAME));
                    break;
            }
            //int end = token.getStopIndex();
           // Pair pair = new Pair(end, ColorScheme.getColor(type));
          //  _tokens.add(pair);
        }
    }

    @Override
    protected void parse(JSONLexer lexer) {
        CommonTokenStream tks = new CommonTokenStream(lexer);
        parser.setTokenStream(tks);
        json = parser.json();
    }


    @Override
    public boolean canFormat() {
        return true;
    }

    @Override
    protected int format(IndentStringBuilder sb, JSONLexer lexer, int width, int curPos) {
        int newPos = -1;
        int start = -1;
        while (true) {
            CommonToken token = (CommonToken) lexer.nextToken();
            int tokenType = token.getType();
            if (tokenType == -1)
                break;
            switch (tokenType) {
                case LBRACE:
                    sb.indent(width);
                    sb.append("{\n");
                    break;
                case COMMA:
                    sb.append(",\n");
                    break;
                case LBRACK:
                    sb.indent(width);
                    sb.append("[\n");
                    break;
                case RBRACE:
                    sb.deindent(width);
                    sb.append("\n}");
                    break;
                case RBRACK:
                    sb.deindent(width);
                    sb.append("]");
                    break;
                case COLON:
                    sb.append(" : ");
                    break;
                case WS:
                    break;
                default:
                    sb.append(token.getText());
                    break;
            }
            int end = token.getStopIndex();
            newPos = Antlr4LexTask.compute(sb.length(), start, end, curPos, newPos);
            start = end;
        }

        return newPos;
    }

    @Override
    protected ParseTree getTree() {
        return json;
    }

}
